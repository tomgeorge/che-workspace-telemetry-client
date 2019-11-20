/*
 * Che Workspace Telemetry API
 * This is the API of the Che workspace telemetry manager
 *
 */

package org.eclipse.che.incubator.workspace.telemetry.model;

import java.util.Objects;

/**
 * EventProperties
 */

public class EventProperties {

  public String id;
  public String value;

  /**
  * Get id
  * @return id
  **/
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
  * Get value
  * @return value
  **/
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
    EventProperties eventProperties = (EventProperties) o;
    return Objects.equals(this.id, eventProperties.id) &&
        Objects.equals(this.value, eventProperties.value);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(id, value);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EventProperties {\n");
    
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
