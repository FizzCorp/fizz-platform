package io.fizz.gateway.http.models;

public class PostEvent {
    private String user_id;
    private String type;
    private Integer ver;
    private String session_id;
    private Long time;
    private String platform;
    private String build;
    private String custom_01;
    private String custom_02;
    private String custom_03;
    private int duration;
    private String content;
    private String channel_id;
    private String nick;
    private Integer amount;
    private String currency;
    private String product_id;
    private String receipt;

    public String getUser_id() {
        return user_id;
    }

    public PostEvent setUser_id(String user_id) {
        this.user_id = user_id;
        return this;
    }

    public String getType() {
        return type;
    }

    public PostEvent setType(String type) {
        this.type = type;
        return this;
    }

    public Integer getVer() {
        return ver;
    }

    public PostEvent setVer(Integer ver) {
        this.ver = ver;
        return this;
    }

    public String getSession_id() {
        return session_id;
    }

    public PostEvent setSession_id(String session_id) {
        this.session_id = session_id;
        return this;
    }

    public Long getTime() {
        return time;
    }

    public PostEvent setTime(Long time) {
        this.time = time;
        return this;
    }

    public String getPlatform() {
        return platform;
    }

    public PostEvent setPlatform(String platform) {
        this.platform = platform;
        return this;
    }

    public String getBuild() {
        return build;
    }

    public PostEvent setBuild(String build) {
        this.build = build;
        return this;
    }

    public String getCustom_01() {
        return custom_01;
    }

    public PostEvent setCustom_01(String custom_01) {
        this.custom_01 = custom_01;
        return this;
    }

    public String getCustom_02() {
        return custom_02;
    }

    public PostEvent setCustom_02(String custom_02) {
        this.custom_02 = custom_02;
        return this;
    }

    public String getCustom_03() {
        return custom_03;
    }

    public PostEvent setCustom_03(String custom_03) {
        this.custom_03 = custom_03;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public PostEvent setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public String getContent() {
        return content;
    }

    public PostEvent setContent(String content) {
        this.content = content;
        return this;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public PostEvent setChannel_id(String channel_id) {
        this.channel_id = channel_id;
        return this;
    }

    public String getNick() {
        return nick;
    }

    public PostEvent setNick(String nick) {
        this.nick = nick;
        return this;
    }

    public Integer getAmount() {
        return amount;
    }

    public PostEvent setAmount(Integer amount) {
        this.amount = amount;
        return this;
    }

    public String getCurrency() {
        return currency;
    }

    public PostEvent setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public String getProduct_id() {
        return product_id;
    }

    public PostEvent setProduct_id(String product_id) {
        this.product_id = product_id;
        return this;
    }

    public PostEvent setReceipt(String receipt) {
        this.receipt = receipt;
        return this;
    }
}
