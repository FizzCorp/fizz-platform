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
import io.swagger.client.model.SortOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.math.BigDecimal;
/**
 * SearchReportMessage
 */

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-02-04T14:33:33.766771+05:00[Asia/Karachi]")
public class SearchReportMessage {
  @SerializedName("channel_id")
  private String channelId = null;

  @SerializedName("language")
  private LanguageCode language = null;

  @SerializedName("user_id")
  private String userId = null;

  @SerializedName("sort")
  private SortOrder sort = null;

  @SerializedName("cursor")
  private BigDecimal cursor = null;

  @SerializedName("page_size")
  private BigDecimal pageSize = null;

  @SerializedName("start")
  private BigDecimal start = null;

  @SerializedName("end")
  private BigDecimal end = null;

  public SearchReportMessage channelId(String channelId) {
    this.channelId = channelId;
    return this;
  }

   /**
   * The channel&#x27;s Id (name).
   * @return channelId
  **/
  @Schema(description = "The channel's Id (name).")
  public String getChannelId() {
    return channelId;
  }

  public void setChannelId(String channelId) {
    this.channelId = channelId;
  }

  public SearchReportMessage language(LanguageCode language) {
    this.language = language;
    return this;
  }

   /**
   * Get language
   * @return language
  **/
  @Schema(description = "")
  public LanguageCode getLanguage() {
    return language;
  }

  public void setLanguage(LanguageCode language) {
    this.language = language;
  }

  public SearchReportMessage userId(String userId) {
    this.userId = userId;
    return this;
  }

   /**
   * Get userId
   * @return userId
  **/
  @Schema(description = "")
  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public SearchReportMessage sort(SortOrder sort) {
    this.sort = sort;
    return this;
  }

   /**
   * Get sort
   * @return sort
  **/
  @Schema(description = "")
  public SortOrder getSort() {
    return sort;
  }

  public void setSort(SortOrder sort) {
    this.sort = sort;
  }

  public SearchReportMessage cursor(BigDecimal cursor) {
    this.cursor = cursor;
    return this;
  }

   /**
   * Start index of query result.
   * @return cursor
  **/
  @Schema(description = "Start index of query result.")
  public BigDecimal getCursor() {
    return cursor;
  }

  public void setCursor(BigDecimal cursor) {
    this.cursor = cursor;
  }

  public SearchReportMessage pageSize(BigDecimal pageSize) {
    this.pageSize = pageSize;
    return this;
  }

   /**
   * Page size of result.
   * @return pageSize
  **/
  @Schema(example = "10", description = "Page size of result.")
  public BigDecimal getPageSize() {
    return pageSize;
  }

  public void setPageSize(BigDecimal pageSize) {
    this.pageSize = pageSize;
  }

  public SearchReportMessage start(BigDecimal start) {
    this.start = start;
    return this;
  }

   /**
   * Start Unix timestamp (in milliseconds) of the message&#x27;s creation time.
   * @return start
  **/
  @Schema(required = true, description = "Start Unix timestamp (in milliseconds) of the message's creation time.")
  public BigDecimal getStart() {
    return start;
  }

  public void setStart(BigDecimal start) {
    this.start = start;
  }

  public SearchReportMessage end(BigDecimal end) {
    this.end = end;
    return this;
  }

   /**
   * End Unix timestamp (in milliseconds) of the message&#x27;s creation time.
   * @return end
  **/
  @Schema(required = true, description = "End Unix timestamp (in milliseconds) of the message's creation time.")
  public BigDecimal getEnd() {
    return end;
  }

  public void setEnd(BigDecimal end) {
    this.end = end;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SearchReportMessage searchReportMessage = (SearchReportMessage) o;
    return Objects.equals(this.channelId, searchReportMessage.channelId) &&
        Objects.equals(this.language, searchReportMessage.language) &&
        Objects.equals(this.userId, searchReportMessage.userId) &&
        Objects.equals(this.sort, searchReportMessage.sort) &&
        Objects.equals(this.cursor, searchReportMessage.cursor) &&
        Objects.equals(this.pageSize, searchReportMessage.pageSize) &&
        Objects.equals(this.start, searchReportMessage.start) &&
        Objects.equals(this.end, searchReportMessage.end);
  }

  @Override
  public int hashCode() {
    return Objects.hash(channelId, language, userId, sort, cursor, pageSize, start, end);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SearchReportMessage {\n");
    
    sb.append("    channelId: ").append(toIndentedString(channelId)).append("\n");
    sb.append("    language: ").append(toIndentedString(language)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    sort: ").append(toIndentedString(sort)).append("\n");
    sb.append("    cursor: ").append(toIndentedString(cursor)).append("\n");
    sb.append("    pageSize: ").append(toIndentedString(pageSize)).append("\n");
    sb.append("    start: ").append(toIndentedString(start)).append("\n");
    sb.append("    end: ").append(toIndentedString(end)).append("\n");
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
