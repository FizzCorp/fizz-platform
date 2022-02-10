package io.fizz.common.infastructure.model;

import java.util.List;

public class TextMessageES {
    private String id;
    private String appId;
    private String countryCode;
    private String actorId;
    private String nick;
    private String content;
    private String body;
    private List<String> keywords;
    private String channel;
    private String platform;
    private String build;
    private String custom01;
    private String custom02;
    private String custom03;
    private String age;
    private String spender;
    private long timestamp;
    private double sentimentScore;

    public TextMessageES(String id,
                         String appId,
                         String countryCode,
                         String actorId,
                         String nick,
                         String content,
                         String channel,
                         String platform,
                         String build,
                         String custom01,
                         String custom02,
                         String custom03,
                         long timestamp,
                         double sentimentScore,
                         String age,
                         String spender,
                         List<String> keywords) {
        this.id = id;
        this.appId = appId;
        this.countryCode = countryCode;
        this.actorId = actorId;
        this.nick = nick;
        this.content = this.body = content;
        this.channel = channel;
        this.platform = platform;
        this.build = build;
        this.custom01 = custom01;
        this.custom02 = custom02;
        this.custom03 = custom03;
        this.timestamp = timestamp;
        this.sentimentScore = sentimentScore;
        this.age = age;
        this.spender = spender;
        this.keywords = keywords;
    }

    public TextMessageES() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = this.body = content;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getCustom01() {
        return custom01;
    }

    public void setCustom01(String custom01) {
        this.custom01 = custom01;
    }

    public String getCustom02() {
        return custom02;
    }

    public void setCustom02(String custom02) {
        this.custom02 = custom02;
    }

    public String getCustom03() {
        return custom03;
    }

    public void setCustom03(String custom03) {
        this.custom03 = custom03;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getSentimentScore() {
        return sentimentScore;
    }

    public void setSentimentScore(double sentimentScore) {
        this.sentimentScore = sentimentScore;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getSpender() {
        return spender;
    }

    public void setSpender(String spender) {
        this.spender = spender;
    }
}
