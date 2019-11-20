/*
 * Che Workspace Telemetry API
 * This is the API of the Che workspace telemetry manager
 *
 * OpenAPI spec version: 1.0.0-oas3
 * Contact: dfestal@redhat.com
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package org.eclipse.che.incubator.workspace.telemetry.model;

import java.util.Objects;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * Event
 */

public class Event {

  public String id;
  public String userId;
  public String ownerId;
  public String ip;
  public String agent;
  public List<EventProperties> properties = new ArrayList<EventProperties>();
  
  public Event() {
  }

  @Schema(
    description = "Identifier of the event type",
    required = true,
    example = "WORKSPACE_USED"
  )
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  @Schema(
    description = "Identifier of the user that issued the event",
    required = true
  )
  public String getUserId() {
    return userId;
  }
  public void setUserId(String userId) {
    this.userId = userId;
  }

  @Schema(
    description = "Identifier of the component that issued the event",
    required = true
  )
  public String getOwnerId() {
    return ownerId;
  }
  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  @Schema(
    description = "IP address of the browser that issued the event",
    required = true,
    format = "ipv4",
    example = "127.0.0.1"
  )
  public String getIp() {
    return ip;
  }
  public void setIp(String ip) {
    this.ip = ip;
  }

  @Schema(
    description = "User agent of the browser that issued the event",
    required = true,
    example = "Mozilla/5.0 (X11; Linux x86_64…) Gecko/20100101 Firefox/60.0"
  )
  public String getAgent() {
    return agent;
  }
  public void setAgent(String agent) {
    this.agent = agent;
  }

  @Schema(
    description = "Properties of the event",
    required = true
  )
  public List<EventProperties> getProperties() {
    return properties;
  }
  public void setProperties(List<EventProperties> properties) {
    this.properties = properties;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Event event = (Event) o;
    return Objects.equals(this.id, event.id) &&
        Objects.equals(this.userId, event.userId) &&
        Objects.equals(this.ownerId, event.ownerId) &&
        Objects.equals(this.ip, event.ip) &&
        Objects.equals(this.agent, event.agent) &&
        Objects.equals(this.properties, event.properties);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(id, userId, ownerId, ip, agent, properties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Event {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    ownerId: ").append(toIndentedString(ownerId)).append("\n");
    sb.append("    ip: ").append(toIndentedString(ip)).append("\n");
    sb.append("    agent: ").append(toIndentedString(agent)).append("\n");
    sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
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
