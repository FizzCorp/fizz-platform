/*
 * Fizz
 * API reference for the Fizz platform.  # Introduction The Fizz platform comprises of the following services:   - **Analytics**: Run analysis on ingested events to provide insights into user behaviour and sentiment.   - **Chat**: A multilingual chat solution for integrating global audiences.   - **Cloud Messaging**: Create topics for communicating between users, systems etc reliably. Communicate using in-app messages or push notifications.   - **Translate**: Translate text in 36 languages in real-time with slang support.  # Authentication Some Fizz APIs use a signature based authentication. Each application is provided with an Id and a secret. The application Id is specified as part of the API route. However the secret is used to compute an HMAC-SHA256 digest of the raw body of the request. In case of get call body will be replaced with json containing nonce as property. It is expected that the digest is supplied in the Authorization header with the keyword HMAC e.g<br/><br/> digest = hmac_sha256(body, app_secret)<br/> Authorization Header => HMAC-SHA256 digest  # Glossary ## Users  All operations in the Fizz services are performed by users. A user is anything that uses the Fizz system. Users can include users, bots, systems etc Please note that Fizz does not ensure that users are uniquely identified and is the responsibility of the application system.  ## Subscriber An entities that subscribes to messages published on a topic (or channel).  ## Topics Implements a topic-based publish-subscribe system that allows entities (publishers) to send messages to subscribers in a reliable way.  ## Channels Establishes a channel of communication (or \"chat-room\") between different entities. Entities can publish messages to a channel which are then delivered to all subscribing entities. Channels persist all messages to a message journal which allows offline entities to read through the message history. A channel can also comprise of multiple topics for sharding large channels.  ## Event A thing of interest that takes place in a system. Fizz allows events to be ingested into the system. The analytics service runs various kinds of analyses on the ingested data to extract valuable insights. 
 *
 * OpenAPI spec version: 1.1.0
 * Contact: support@fizz.io
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package io.swagger.client.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
/**
 * ChannelMessageModel
 */

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-02-09T18:25:36.265+05:00[Asia/Karachi]")
public class ChannelMessageModel {
  @SerializedName("nick")
  private String nick = null;

  @SerializedName("body")
  private String body = null;

  @SerializedName("data")
  private String data = null;

  @SerializedName("translate")
  private Boolean translate = false;

  @SerializedName("filter")
  private Boolean filter = true;

  @SerializedName("persist")
  private Boolean persist = true;

  @SerializedName("locale")
  private Object locale = null;

  public ChannelMessageModel nick(String nick) {
    this.nick = nick;
    return this;
  }

   /**
   * Human friendly name of the sender.
   * @return nick
  **/
  @Schema(description = "Human friendly name of the sender.")
  public String getNick() {
    return nick;
  }

  public void setNick(String nick) {
    this.nick = nick;
  }

  public ChannelMessageModel body(String body) {
    this.body = body;
    return this;
  }

   /**
   * The message body.
   * @return body
  **/
  @Schema(description = "The message body.")
  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public ChannelMessageModel data(String data) {
    this.data = data;
    return this;
  }

   /**
   * Any user specified data that can be attached with the message.
   * @return data
  **/
  @Schema(description = "Any user specified data that can be attached with the message.")
  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public ChannelMessageModel translate(Boolean translate) {
    this.translate = translate;
    return this;
  }

   /**
   * The message body will be translated if set to true.
   * @return translate
  **/
  @Schema(description = "The message body will be translated if set to true.")
  public Boolean isTranslate() {
    return translate;
  }

  public void setTranslate(Boolean translate) {
    this.translate = translate;
  }

  public ChannelMessageModel filter(Boolean filter) {
    this.filter = filter;
    return this;
  }

   /**
   * The message body will be moderated if set to true.
   * @return filter
  **/
  @Schema(description = "The message body will be moderated if set to true.")
  public Boolean isFilter() {
    return filter;
  }

  public void setFilter(Boolean filter) {
    this.filter = filter;
  }

  public ChannelMessageModel persist(Boolean persist) {
    this.persist = persist;
    return this;
  }

   /**
   * Persist the message in the channel&#x27;s message journal. All messages are persisted by default. Set to false to prevent the system from persisting the message.
   * @return persist
  **/
  @Schema(description = "Persist the message in the channel's message journal. All messages are persisted by default. Set to false to prevent the system from persisting the message.")
  public Boolean isPersist() {
    return persist;
  }

  public void setPersist(Boolean persist) {
    this.persist = persist;
  }

  public ChannelMessageModel locale(Object locale) {
    this.locale = locale;
    return this;
  }

   /**
   * It is the locale of field body. If this key is not provided, then system wil auto dedect the locale.
   * @return locale
  **/
  @Schema(description = "It is the locale of field body. If this key is not provided, then system wil auto dedect the locale.")
  public Object getLocale() {
    return locale;
  }

  public void setLocale(Object locale) {
    this.locale = locale;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ChannelMessageModel channelMessageModel = (ChannelMessageModel) o;
    return Objects.equals(this.nick, channelMessageModel.nick) &&
        Objects.equals(this.body, channelMessageModel.body) &&
        Objects.equals(this.data, channelMessageModel.data) &&
        Objects.equals(this.translate, channelMessageModel.translate) &&
        Objects.equals(this.filter, channelMessageModel.filter) &&
        Objects.equals(this.persist, channelMessageModel.persist) &&
        Objects.equals(this.locale, channelMessageModel.locale);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nick, body, data, translate, filter, persist, locale);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ChannelMessageModel {\n");
    
    sb.append("    nick: ").append(toIndentedString(nick)).append("\n");
    sb.append("    body: ").append(toIndentedString(body)).append("\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
    sb.append("    translate: ").append(toIndentedString(translate)).append("\n");
    sb.append("    filter: ").append(toIndentedString(filter)).append("\n");
    sb.append("    persist: ").append(toIndentedString(persist)).append("\n");
    sb.append("    locale: ").append(toIndentedString(locale)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}