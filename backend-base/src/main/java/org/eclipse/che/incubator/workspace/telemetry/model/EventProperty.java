/*
 * Che Workspace Telemetry API
 * This is the API of the Che workspace telemetry manager
 *
 */

package org.eclipse.che.incubator.workspace.telemetry.model;

import java.util.Objects;

import org.eclipse.che.incubator.workspace.telemetry.base.AnalyticsEvent;
import org.eclipse.che.incubator.workspace.telemetry.base.EventProperties;
import org.eclipse.microprofile.openapi.annotations.ExternalDocumentation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
  title = "Event property definition",
  description = "Definition of a additional property of an event."
)
public class EventProperty {

  public String id;
  public String value;

  public EventProperty() {
  }

  public EventProperty(String id, String value) {
    this.id = id;
    this.value = value;
  }

  @Schema(
    description = "Id of the event property",
    required = true,
    example = EventProperties.PROGRAMMING_LANGUAGE
  )
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  @Schema(
    description = "Value of the event property",
    required = true,
    example = "java"
  )
  public String getValue() {
    return value;
  }
  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EventProperty eventProperty = (EventProperty) o;
    return Objects.equals(this.id, eventProperty.id) &&
        Objects.equals(this.value, eventProperty.value);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(id, value);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EventProperty {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
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
