-- UTILITIES
local function buildSubscriberTopicKey(aNamespace, aQualifiedChannelId, aSubscriberId)
  return aNamespace .. ":{" .. aQualifiedChannelId .. "}" .. aSubscriberId
end

local function buildQualifiedChannelId(appId, aChannelId)
  return appId .. aChannelId
end

local function buildChannelTopicCountKey(aNamespace, aChannelId)
  return aNamespace .. ":{" .. aChannelId .. "}:topicCount"
end

local function buildTopicId(aChannelId, aTopicSeq)
  if aTopicSeq == 0 then
    return aChannelId
  else
    return aChannelId .. "$" .. aTopicSeq
  end
end

local function fetchTopic(aNamespace, aQualifiedChannelId, aSubscriberId)
    local key = buildSubscriberTopicKey(aNamespace, aQualifiedChannelId, aSubscriberId)

    if redis.call("EXISTS", key) == 0 then
      return nil
    end

    return tonumber(redis.call("GET", key))
end

local function buildChannelSubscribersKey(aNamespace, aQualifiedChannelId)
  return aNamespace .. ":{" .. aQualifiedChannelId .. "}:subs"
end

local function buildChannelLocalesKey(aNamespace, aQualifiedChannelId)
  return aNamespace .. ":{" .. aQualifiedChannelId .. "}:locales"
end

-- COMMANDS
local function updateChannelTopicCount(aNamespace, aQualifiedChannelId, aTopicCount, aSubscriberTTL)
  local key = buildChannelTopicCountKey(aNamespace, aQualifiedChannelId)
  redis.call("SETEX", key, aSubscriberTTL, aTopicCount)
end

local function getChannelTopicCount(aNamespace, aAppId, aChannelId)
  local qualifiedChannelId = buildQualifiedChannelId(aAppId, aChannelId)
  local key = buildChannelTopicCountKey(aNamespace, qualifiedChannelId)
  if redis.call("EXISTS", key) == 0 then
    return 0
  end

  return tonumber(redis.call("GET", key))
end

local function calcTopicSeq(aNamespace, aQualifiedChannelId, aSubscriberId, aSubscriberTTL, aTopicSize)
  local now = redis.call("TIME")[1]
  local timeout = now - aSubscriberTTL
  local channelSubKey = buildChannelSubscribersKey(aNamespace, aQualifiedChannelId)

  redis.call("ZADD", channelSubKey, now, aSubscriberId)
  redis.call("ZREMRANGEBYSCORE", channelSubKey, "-inf", timeout)
  redis.call("EXPIRE", channelSubKey, aSubscriberTTL)

  local ccus = redis.call("ZCARD", channelSubKey)
  local topicCount = math.ceil(ccus / aTopicSize)

  updateChannelTopicCount(aNamespace, aQualifiedChannelId, topicCount, aSubscriberTTL)

  return math.fmod(ccus, topicCount)
end

local function assignTopic(aNamespace, aQualifiedChannelId, aSubscriberId, aLocale, aSubscriberTTL, aTopicSize)
  local topicSeq = calcTopicSeq(aNamespace, aQualifiedChannelId, aSubscriberId, aSubscriberTTL, aTopicSize)

  local subTopicKey = buildSubscriberTopicKey(aNamespace, aQualifiedChannelId, aSubscriberId)
  local channelLocaleKey = buildChannelLocalesKey(aNamespace, aQualifiedChannelId)

  redis.call("SETEX", subTopicKey, aSubscriberTTL, topicSeq)
  redis.call("SADD", channelLocaleKey, aLocale)
  redis.call("EXPIRE", channelLocaleKey, 48*60*60)

  return topicSeq
end

local function assignOrFetchTopic(aNamespace, aAppId, aChannelId, aSubscriberId, aLocale, aSubscriberTTL, aTopicSize)
  local qualifiedChannelId = buildQualifiedChannelId(aAppId, aChannelId)
  local topic = fetchTopic(aNamespace, qualifiedChannelId, aSubscriberId)

  if topic == nil then
    topic = assignTopic(aNamespace, qualifiedChannelId, aSubscriberId, aLocale, aSubscriberTTL, aTopicSize)
  end

  return buildTopicId(aChannelId, topic)
end

local function fetchLocales(aNamespace, aAppId, aChannelId)
  local qualifiedChannelId = buildQualifiedChannelId(aAppId, aChannelId)
  local channelLocaleKey = buildChannelLocalesKey(aNamespace, qualifiedChannelId)

  return redis.call("SMEMBERS", channelLocaleKey)
end

local function main()
  local namespace = KEYS[1]
  local appId = KEYS[2]
  local channelId = KEYS[3]

  local command = ARGV[1]

  if command == "ASSIGN" then
    local subscriberId = ARGV[2]
    local locale = ARGV[3]
    local subscriberTTL = tonumber(ARGV[4])
    local topicSize = tonumber(ARGV[5])

    return assignOrFetchTopic(namespace, appId, channelId, subscriberId, locale, subscriberTTL, topicSize)
  elseif command == "LOCALES" then
    return fetchLocales(namespace, appId, channelId)
  elseif command == "TOPIC_COUNT" then
    return getChannelTopicCount(namespace, appId, channelId)
  end
end

return main()