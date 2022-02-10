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
import io.swagger.client.model.LanguageCode;
import io.swagger.client.model.Offense;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.math.BigDecimal;
/**
 * SearchReportMessageResultData
 */

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-02-04T14:33:33.766771+05:00[Asia/Karachi]")
public class SearchReportMessageResultData {
  @SerializedName("id")
  private String id = null;

  @SerializedName("channel_id")
  private String channelId = null;

  @SerializedName("message")
  private String message = null;

  @SerializedName("message_id")
  private String messageId = null;

  @SerializedName("language")
  private LanguageCode language = null;

  @SerializedName("user_id")
  private String userId = null;

  @SerializedName("reporter_id")
  private String reporterId = null;

  @SerializedName("offense")
  private Offense offense = null;

  @SerializedName("description")
  private String description = null;

  @SerializedName("time")
  private BigDecimal time = null;

  public SearchReportMessageResultData id(String id) {
    this.id = id;
    return this;
  }

   /**
   * Unique identifier of reported message.
   * @return id
  **/
  @Schema(required = true, description = "Unique identifier of reported message.")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public SearchReportMessageResultData channelId(String channelId) {
    this.channelId = channelId;
    return this;
  }

   /**
   * The channel&#x27;s Id (name).
   * @return channelId
  **/
  @Schema(required = true, description = "The channel's Id (name).")
  public String getChannelId() {
    return channelId;
  }

  public void setChannelId(String channelId) {
    this.channelId = channelId;
  }

  public SearchReportMessageResultData message(String message) {
    this.message = message;
    return this;
  }

   /**
   * The body of reported message.
   * @return message
  **/
  @Schema(required = true, description = "The body of reported message.")
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public SearchReportMessageResultData messageId(String messageId) {
    this.messageId = messageId;
    return this;
  }

   /**
   * The id of the message.
   * @return messageId
  **/
  @Schema(required = true, description = "The id of the message.")
  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public SearchReportMessageResultData language(LanguageCode language) {
    this.language = language;
    return this;
  }

   /**
   * Get language
   * @return language
  **/
  @Schema(required = true, description = "")
  public LanguageCode getLanguage() {
    return language;
  }

  public void setLanguage(LanguageCode language) {
    this.language = language;
  }

  public SearchReportMessageResultData userId(String userId) {
    this.userId = userId;
    return this;
  }

   /**
   * Get userId
   * @return userId
  **/
  @Schema(required = true, description = "")
  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public SearchReportMessageResultData reporterId(String reporterId) {
    this.reporterId = reporterId;
    return this;
  }

   /**
   * Get reporterId
   * @return reporterId
  **/
  @Schema(required = true, description = "")
  public String getReporterId() {
    return reporterId;
  }

  public void setReporterId(String reporterId) {
    this.reporterId = reporterId;
  }

  public SearchReportMessageResultData offense(Offense offense) {
    this.offense = offense;
    return this;
  }

   /**
   * Get offense
   * @return offense
  **/
  @Schema(required = true, description = "")
  public Offense getOffense() {
    return offense;
  }

  public void setOffense(Offense offense) {
    this.offense = offense;
  }

  public SearchReportMessageResultData description(String description) {
    this.description = description;
    return this;
  }

   /**
   * Any user provided information about the reported message.
   * @return description
  **/
  @Schema(required = true, description = "Any user provided information about the reported message.")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public SearchReportMessageResultData time(BigDecimal time) {
    this.time = time;
    return this;
  }

   /**
   * Unix timestamp (in milliseconds) of the message&#x27;s creation time.
   * @return time
  **/
  @Schema(required = true, description = "Unix timestamp (in milliseconds) of the message's creation time.")
  public BigDecimal getTime() {
    return time;
  }

  public void setTime(BigDecimal time) {
    this.time = time;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SearchReportMessageResultData searchReportMessageResultData = (SearchReportMessageResultData) o;
    return Objects.equals(this.id, searchReportMessageResultData.id) &&
        Objects.equals(this.channelId, searchReportMessageResultData.channelId) &&
        Objects.equals(this.message, searchReportMessageResultData.message) &&
        Objects.equals(this.messageId, searchReportMessageResultData.messageId) &&
        Objects.equals(this.language, searchReportMessageResultData.language) &&
        Objects.equals(this.userId, searchReportMessageResultData.userId) &&
        Objects.equals(this.reporterId, searchReportMessageResultData.reporterId) &&
        Objects.equals(this.offense, searchReportMessageResultData.offense) &&
        Objects.equals(this.description, searchReportMessageResultData.description) &&
        Objects.equals(this.time, searchReportMessageResultData.time);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, channelId, message, messageId, language, userId, reporterId, offense, description, time);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SearchReportMessageResultData {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    channelId: ").append(toIndentedString(channelId)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    messageId: ").append(toIndentedString(messageId)).append("\n");
    sb.append("    language: ").append(toIndentedString(language)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    reporterId: ").append(toIndentedString(reporterId)).append("\n");
    sb.append("    offense: ").append(toIndentedString(offense)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    time: ").append(toIndentedString(time)).append("\n");
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