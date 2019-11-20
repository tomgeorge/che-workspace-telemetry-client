/*
 * Copyright (c) 2016-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.incubator.workspace.telemetry.base;

import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;

public class DefaultAnalyticsManager extends AbstractAnalyticsManager {
  private static final Logger LOG = getLogger(DefaultAnalyticsManager.class);

  public DefaultAnalyticsManager(
    String apiEndpoint,
    String workspaceId,
    HttpJsonRequestFactory requestFactory) {
      super(apiEndpoint, workspaceId, requestFactory);
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void onActivity(String userId) {
    LOG.info("Activity from user {}", userId);
  }

  @Override
  public void onEvent(String userId, AnalyticsEvent event, Map<String, Object> properties, String ip,
      String userAgent) {
    LOG.info("Event triggered by user {} from ip {} on agent {} :\n{}\nwith properties:\n{}", userId, ip, userAgent, event, properties);
  }

  @Override
  public void destroy() {
    LOG.info("Destroying the Analytics manager");
  }
}
