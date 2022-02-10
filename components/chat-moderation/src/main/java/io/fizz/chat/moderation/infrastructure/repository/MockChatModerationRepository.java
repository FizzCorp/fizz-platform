package io.fizz.chat.moderation.infrastructure.repository;

import io.fizz.chat.moderation.domain.ReportedChannel;
import io.fizz.chat.moderation.domain.ReportedUser;
import io.fizz.chat.moderation.infrastructure.model.ReportedMessageES;
import io.fizz.chat.moderation.infrastructure.model.ReportedMessageESFilter;
import io.fizz.chat.moderation.infrastructure.model.ReportedMessageESSearchResult;
import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.QueryRange;
import io.fizz.common.domain.UserId;

import java.util.*;

public class MockChatModerationRepository extends ESChatModerationRepository {
    private List<ReportedMessageES> mockData = new ArrayList<>();
    public MockChatModerationRepository() {
        initMockData();
    }

    @Override
    String save(final ReportedMessageES aReportedMessageES) {
        mockData.removeIf(messageES -> messageES.getId().equals(aReportedMessageES.getId()));
        mockData.add(aReportedMessageES);
        return aReportedMessageES.getId();
    }

    @Override
    ReportedMessageESSearchResult getReportedMessages(final ApplicationId aAppId,
                                                      final UserId aUserId,
                                                      final ChannelId aChannelId,
                                                      final LanguageCode aLang,
                                                      final QueryRange aRange,
                                                      final Integer aCursor,
                                                      final Integer aPageSize,
                                                      final String aSort) {

        List<ReportedMessageES> mockData = new ArrayList<>(this.mockData);

        mockData.sort((object1, object2) -> {
            if (Objects.isNull(aSort)) {
                return 0;
            }
            Long t1 = object1.getTimestamp();
            Long t2 = object2.getTimestamp();

            if (aSort.toUpperCase().equals("DESC")) {
                return t1.compareTo(t2);
            }

            return t2.compareTo(t1);
        });

        List<ReportedMessageES> results = new ArrayList<>();
        int index = Objects.isNull(aCursor) ? 0 : aCursor;
        int resultSize = 0;
        while (index < mockData.size()) {
            ReportedMessageES messageES = mockData.get(index);
            ReportedMessageES filteredMessageES = new ReportedMessageESFilter(messageES)
                    .filter(aAppId)
                    .filter(aUserId)
                    .filter(aChannelId)
                    .filter(aLang)
                    .filter(aRange)
                    .get();
            if (Objects.nonNull(filteredMessageES)) {
                resultSize++;
                if (Objects.isNull(aPageSize) || results.size() < aPageSize) {
                    results.add(filteredMessageES);
                }
            }
            index++;
        }

        return new ReportedMessageESSearchResult(results, resultSize);
    }

    @Override
    List<ReportedUser> getReportedUserAggregation(final ApplicationId aAppId,
                                                  final ChannelId aChannelId,
                                                  final LanguageCode aLang,
                                                  final Integer aResultLimit,
                                                  final QueryRange aRange) {
        Map<String, Integer> userCountMap = new HashMap<>();
        for (int i = 0; i < mockData.size() && (Objects.isNull(aResultLimit) || i < aResultLimit); i++) {
        ReportedMessageES messageES = mockData.get(i);
            ReportedMessageES filteredMessageES = new ReportedMessageESFilter(messageES)
                    .filter(aAppId)
                    .filter(aChannelId)
                    .filter(aLang)
                    .filter(aRange)
                    .get();
            if (Objects.nonNull(filteredMessageES)) {
                String userId = filteredMessageES.getReportedUserId();
                Integer count = userCountMap.computeIfAbsent(userId, s -> 0);
                userCountMap.put(userId, ++count);
            }
        }

        List<ReportedUser> users = new ArrayList<>();
        for (Map.Entry<String, Integer> entity: userCountMap.entrySet()) {
            users.add(new ReportedUser(new UserId(entity.getKey()), entity.getValue()));
        }

        return users;
    }

    @Override
    List<ReportedChannel> getChannelAggregation(final ApplicationId aAppId,
                                                final LanguageCode aLang,
                                                final Integer aResultLimit,
                                                final QueryRange aRange) {
        Map<String, Integer> channelCountMap = new HashMap<>();
        for (int i = 0; i < mockData.size() && (Objects.isNull(aResultLimit) || i < aResultLimit); i++) {
            ReportedMessageES messageES = mockData.get(i);
            ReportedMessageES filteredMessageES = new ReportedMessageESFilter(messageES)
                    .filter(aAppId)
                    .filter(aLang)
                    .filter(aRange)
                    .get();
            if (Objects.nonNull(filteredMessageES)) {
                String channelId = filteredMessageES.getChannelId();
                Integer count = channelCountMap.computeIfAbsent(channelId, s -> 0);
                channelCountMap.put(channelId, ++count);
            }
        }

        List<ReportedChannel> users = new ArrayList<>();
        for (Map.Entry<String, Integer> entity: channelCountMap.entrySet()) {
            users.add(new ReportedChannel(new ChannelId(aAppId, entity.getKey()), entity.getValue()));
        }

        return users;
    }

    private void initMockData() {
        mockData.add(new ReportedMessageES(
                "id_1",
                "appA",
                "user_1",
                "user_2",
                "test_message_1",
                "message_id_1",
                "channel_a",
                "en",
                "Toxic",
                "this is mocked message 1",
                1553510320));
        mockData.add(new ReportedMessageES(
                "id_2",
                "appA",
                "user_1",
                "user_2",
                "test_message_2",
                "message_id_2",
                "channel_b",
                "en",
                "Spam",
                "this is mocked message 2",
                1553510350));
        mockData.add(new ReportedMessageES(
                "id_3",
                "appA",
                "user_3",
                "user_4",
                "test_message_3",
                "message_id_3",
                "channel_a",
                "fr",
                "Other",
                "this is mocked message 3",
                1553510380));
    }
}
