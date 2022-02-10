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
import io.swagger.client.model.GroupMemberState;
import io.swagger.client.model.GroupRoleName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.math.BigDecimal;
/**
 * UserGroup
 */

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-02-04T14:33:33.766771+05:00[Asia/Karachi]")
public class UserGroup {
  @SerializedName("group_id")
  private String groupId = null;

  @SerializedName("role")
  private GroupRoleName role = null;

  @SerializedName("state")
  private GroupMemberState state = null;

  @SerializedName("last_read_message_id")
  private BigDecimal lastReadMessageId = null;

  @SerializedName("created")
  private BigDecimal created = null;

  public UserGroup groupId(String groupId) {
    this.groupId = groupId;
    return this;
  }

   /**
   * Uniquely identifies the group.
   * @return groupId
  **/
  @Schema(description = "Uniquely identifies the group.")
  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public UserGroup role(GroupRoleName role) {
    this.role = role;
    return this;
  }

   /**
   * Get role
   * @return role
  **/
  @Schema(description = "")
  public GroupRoleName getRole() {
    return role;
  }

  public void setRole(GroupRoleName role) {
    this.role = role;
  }

  public UserGroup state(GroupMemberState state) {
    this.state = state;
    return this;
  }

   /**
   * Get state
   * @return state
  **/
  @Schema(description = "")
  public GroupMemberState getState() {
    return state;
  }

  public void setState(GroupMemberState state) {
    this.state = state;
  }

  public UserGroup lastReadMessageId(BigDecimal lastReadMessageId) {
    this.lastReadMessageId = lastReadMessageId;
    return this;
  }

   /**
   * Get lastReadMessageId
   * minimum: 0
   * @return lastReadMessageId
  **/
  @Schema(description = "")
  public BigDecimal getLastReadMessageId() {
    return lastReadMessageId;
  }

  public void setLastReadMessageId(BigDecimal lastReadMessageId) {
    this.lastReadMessageId = lastReadMessageId;
  }

  public UserGroup created(BigDecimal created) {
    this.created = created;
    return this;
  }

   /**
   * Unix timestamp (in milliseconds) of the message&#x27;s creation time.
   * @return created
  **/
  @Schema(description = "Unix timestamp (in milliseconds) of the message's creation time.")
  public BigDecimal getCreated() {
    return created;
  }

  public void setCreated(BigDecimal created) {
    this.created = created;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserGroup userGroup = (UserGroup) o;
    return Objects.equals(this.groupId, userGroup.groupId) &&
        Objects.equals(this.role, userGroup.role) &&
        Objects.equals(this.state, userGroup.state) &&
        Objects.equals(this.lastReadMessageId, userGroup.lastReadMessageId) &&
        Objects.equals(this.created, userGroup.created);
  }

  @Override
  public int hashCode() {
    return Objects.hash(groupId, role, state, lastReadMessageId, created);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UserGroup {\n");
    
    sb.append("    groupId: ").append(toIndentedString(groupId)).append("\n");
    sb.append("    role: ").append(toIndentedString(role)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    lastReadMessageId: ").append(toIndentedString(lastReadMessageId)).append("\n");
    sb.append("    created: ").append(toIndentedString(created)).append("\n");
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