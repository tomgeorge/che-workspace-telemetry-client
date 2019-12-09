package org.eclipse.che.incubator.workspace.telemetry;

import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.che.incubator.workspace.telemetry.model.Activity;
import org.eclipse.che.incubator.workspace.telemetry.model.Event;
import org.eclipse.che.incubator.workspace.telemetry.model.EventProperty;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class TelemetryResourceTest {

    @Inject
    TelemetryResource telemetryResource;

    @Test
    public void testActivity() {
        Activity activity = new Activity("user1");
        String response = telemetryResource.activity(activity);
        assertEquals("", response);
    }

    @Test
    public void testEvent() {
        ArrayList<EventProperty> properties = new ArrayList<EventProperty>();
        Event e = new Event("WORKSPACE_STARTED", "user1", "1", "127.0.0.1", "curl", "", properties);
        String response = telemetryResource.event(e);
        assertEquals("", response);
    }
}