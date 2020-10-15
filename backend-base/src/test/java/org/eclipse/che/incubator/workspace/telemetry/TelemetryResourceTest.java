package org.eclipse.che.incubator.workspace.telemetry;

import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.che.incubator.workspace.telemetry.model.Event;
import org.eclipse.che.incubator.workspace.telemetry.model.EventProperty;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class TelemetryResourceTest {

  @Inject
  TelemetryResource telemetryResource;

  @BeforeAll
  public static void setUp() {
    System.setProperty("che.api.internal", "http://fake-che.com/api");
    System.setProperty("che.workspace.id", "fake-workspace");
    System.setProperty("che.machine.token", "fake-token");
  }

  @AfterAll
  public static void tearDown() {
    System.clearProperty("che.api.internal");
    System.clearProperty("che.workspace.id");
    System.clearProperty("che.machine.token");
  }

  @Test
  public void testActivity() {
    String response = telemetryResource.activity();
    assertEquals("", response);
  }

  @Test
  public void testEvent() {
    ArrayList<EventProperty> properties = new ArrayList<>();
    Event e = new Event("WORKSPACE_STARTED", "1", "127.0.0.1", "curl", "", properties);
    String response = telemetryResource.event(e);
    assertEquals("", response);
  }
}
