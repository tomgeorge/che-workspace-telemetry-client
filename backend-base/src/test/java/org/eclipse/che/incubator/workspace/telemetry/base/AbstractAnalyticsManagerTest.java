package org.eclipse.che.incubator.workspace.telemetry.base;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class AbstractAnalyticsManagerTest {

    @Inject
    AbstractAnalyticsManager analyticsManager;

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