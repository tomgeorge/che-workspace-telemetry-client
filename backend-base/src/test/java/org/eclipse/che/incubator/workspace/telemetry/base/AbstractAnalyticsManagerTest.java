package org.eclipse.che.incubator.workspace.telemetry.base;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class AbstractAnalyticsManagerTest {

    @Inject
    AbstractAnalyticsManager analyticsManager;

    @BeforeAll
    public static void setUp() {
        System.setProperty("che.api", "https://fake-che.com/api");
        System.setProperty("che.workspace.id", "fake-workspace");
        System.setProperty("che.machine.token", "");
    }

    @AfterAll
    public static void tearDown() {
        System.clearProperty("che.api");
    }


    @Test
    public void testInstantiation() {
        assertNotNull(analyticsManager);
    }

    @Test
    public void testMockResponseProperties() {
        assertEquals(analyticsManager.workspaceId, "fake-workspace");
        assertEquals(analyticsManager.workspaceName, "wksp-lqq9");
        assertEquals(analyticsManager.createdOn, "1575567196811");
        assertEquals(analyticsManager.updatedOn, "1575575662792");
        assertEquals(analyticsManager.stackId, "Go");
        assertEquals(analyticsManager.firstStart, true);
    }
}
