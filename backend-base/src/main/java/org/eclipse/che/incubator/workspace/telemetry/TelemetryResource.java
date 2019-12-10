package org.eclipse.che.incubator.workspace.telemetry;

import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.che.incubator.workspace.telemetry.base.AbstractAnalyticsManager;
import org.eclipse.che.incubator.workspace.telemetry.base.AnalyticsEvent;
import org.eclipse.che.incubator.workspace.telemetry.model.Activity;
import org.eclipse.che.incubator.workspace.telemetry.model.Event;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

@Path("/")
@ApplicationScoped
public class TelemetryResource {

  @Inject
  AbstractAnalyticsManager analyticsManager;

  @POST
  @Path("/event")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(summary = "Posts a telemetry event",
      description = "Submit telemetry events to the workspace telemetry manager.\nThe event Id should be the Id of a built-in event or of an alread-registered custom event",
      operationId = "event")
  @APIResponse(responseCode = "200", description = "Event was successfully submitted")
  @APIResponse(responseCode = "400", description = "Error during event submission")
  public String event(
      @RequestBody(
          description = "Event to send",
          required = true)
          Event event) {
    Map<String, Object> params =
        event.getProperties().stream()
            .collect(Collectors.toMap(e -> e.getId(), e -> e.getValue()));

    analyticsManager.onActivity(event.getUserId());
    analyticsManager.onEvent(event.getUserId(), AnalyticsEvent.valueOf(event.getId()), params, event.getIp(), event.getAgent());
    return "";
  }

  @POST
  @Path("/activity")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(summary = "Notifies that some activity is still occuring from a given user",
      description = "Notifies that some activity is still occuring for a given user. This will allow maintaining the current session alive for telemetry backends that manage user sessions.",
      operationId = "activity")
  @APIResponse(responseCode = "200", description = "Notification was successfully submitted")
  public String activity(
      @RequestBody(
          description = "Activity to notify",
          required = true
      )
          Activity activity
  ) {
    analyticsManager.onActivity(activity.getUserId());
    return "";
  }

  void onStart(@Observes StartupEvent ev) {
    // Just to trigger the injection of the
    // analytics manager at start, so that initialization
    // errors can be thrown at start.
  }

  void onStop(@Observes ShutdownEvent ev) {
    analyticsManager.destroy();
  }
}