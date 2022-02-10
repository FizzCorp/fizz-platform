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
import io.swagger.client.model.ComparisonOp;
import io.swagger.client.model.SentimentValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
/**
 * MessagesQuerySentimentScore
 */

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-02-04T14:33:33.766771+05:00[Asia/Karachi]")
public class MessagesQuerySentimentScore {
  @SerializedName("op")
  private ComparisonOp op = null;

  @SerializedName("score")
  private SentimentValue score = null;

  public MessagesQuerySentimentScore op(ComparisonOp op) {
    this.op = op;
    return this;
  }

   /**
   * Get op
   * @return op
  **/
  @Schema(required = true, description = "")
  public ComparisonOp getOp() {
    return op;
  }

  public void setOp(ComparisonOp op) {
    this.op = op;
  }

  public MessagesQuerySentimentScore score(SentimentValue score) {
    this.score = score;
    return this;
  }

   /**
   * Get score
   * @return score
  **/
  @Schema(required = true, description = "")
  public SentimentValue getScore() {
    return score;
  }

  public void setScore(SentimentValue score) {
    this.score = score;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MessagesQuerySentimentScore messagesQuerySentimentScore = (MessagesQuerySentimentScore) o;
    return Objects.equals(this.op, messagesQuerySentimentScore.op) &&
        Objects.equals(this.score, messagesQuerySentimentScore.score);
  }

  @Override
  public int hashCode() {
    return Objects.hash(op, score);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MessagesQuerySentimentScore {\n");
    
    sb.append("    op: ").append(toIndentedString(op)).append("\n");
    sb.append("    score: ").append(toIndentedString(score)).append("\n");
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