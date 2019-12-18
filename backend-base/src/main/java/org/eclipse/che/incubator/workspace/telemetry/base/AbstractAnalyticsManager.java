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

import static java.lang.Long.parseLong;
import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.USER_ID_CLAIM;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;

import com.google.common.annotations.VisibleForTesting;

import org.eclipse.che.api.core.*;
import org.eclipse.che.api.core.model.factory.Factory;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.devfile.Devfile;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import java.io.IOException;
import java.util.Map;

public abstract class AbstractAnalyticsManager {
  private static final Logger LOG = getLogger(AbstractAnalyticsManager.class);

  protected final String workspaceId;
  protected String userId = "";

  @VisibleForTesting final protected String workspaceName;
  @VisibleForTesting final protected String factoryId;
  @VisibleForTesting final protected String stackId;
  @VisibleForTesting final protected String factoryName;
  @VisibleForTesting final protected String factoryOwner;
  @VisibleForTesting final protected String factoryUrl;
  @VisibleForTesting final protected String createdOn;
  @VisibleForTesting final protected String updatedOn;
  @VisibleForTesting final protected String stoppedOn;
  @VisibleForTesting final protected String stoppedAbnormally;
  @VisibleForTesting final protected String lastErrorMessage;
  @VisibleForTesting final protected String osioSpaceId;
  @VisibleForTesting final protected String sourceTypes;
  @VisibleForTesting final protected String startNumber;

  @VisibleForTesting final protected Long age;
  @VisibleForTesting final protected Long returnDelay;
  @VisibleForTesting final protected Boolean firstStart;

  private HttpJsonRequestFactory requestFactory;

  public AbstractAnalyticsManager(String apiEndpoint, String workspaceId, String machineToken, HttpJsonRequestFactory requestFactory) {
    this.workspaceId = workspaceId;
    this.requestFactory = requestFactory;

    String endpoint = apiEndpoint + "/workspace/" + workspaceId;

    Workspace workspace = getWorkspace(endpoint);

    createdOn = workspace.getAttributes().get(Constants.CREATED_ATTRIBUTE_NAME);
    updatedOn = workspace.getAttributes().get(Constants.UPDATED_ATTRIBUTE_NAME);
    stoppedOn = workspace.getAttributes().get(Constants.STOPPED_ATTRIBUTE_NAME);
    stoppedAbnormally =
        workspace.getAttributes().get(Constants.STOPPED_ABNORMALLY_ATTRIBUTE_NAME);
    lastErrorMessage = workspace.getAttributes().get(Constants.ERROR_MESSAGE_ATTRIBUTE_NAME);
    sourceTypes = workspace.getAttributes().get("sourceTypes");
    startNumber = workspace.getAttributes().get("startNumber");

    Long createDate = null;
    Long updateDate = null;
    Long stopDate = null;
    try {
      createDate = parseLong(createdOn);
    } catch (NumberFormatException nfe) {
      LOG.warn("the create timestamp ( " + createdOn + " ) has invalid format", nfe);
    }
    try {
      updateDate = parseLong(updatedOn);
    } catch (NumberFormatException nfe) {
      LOG.warn("the update timestamp ( " + updatedOn + " ) has invalid format", nfe);
    }
    if (stoppedOn != null) {
      try {
        stopDate = parseLong(stoppedOn);
      } catch (NumberFormatException nfe) {
        LOG.warn("the stop timestamp ( " + stoppedOn + " ) has invalid format", nfe);
      }
    }

    if (updateDate != null && createDate != null) {
      age = (updateDate - createDate) / 1000;
    } else {
      age = null;
    }
    if (updateDate != null && stopDate != null) {
      returnDelay = (updateDate - stopDate) / 1000;
    } else {
      returnDelay = null;
    }
    if (updateDate != null) {
      firstStart = stopDate == null;
    } else {
      firstStart = null;
    }

    WorkspaceConfig workspaceConfig = workspace.getConfig();
    Devfile devfile = workspace.getDevfile();

    stackId = workspace.getAttributes().get("stackName");
    factoryId = workspace.getAttributes().get("factoryId");
    if (factoryId != null && !"undefined".equals(factoryId)) {
      endpoint = apiEndpoint + "/factory/" + factoryId;

      FactoryDto factory = null;
      try {
        factory = requestFactory.fromUrl(endpoint).request().asDto(FactoryDto.class);
      } catch (Exception e) {
        LOG.warn(
            "Can't get workspace factory ('" + factoryId + "') information for Che analytics",
            e);
      }
      if (factory != null) {
        factoryName = factory.getName();
        factoryOwner = factory.getCreator().getName();
      } else {
        factoryName = null;
        factoryOwner = null;
      }
      factoryUrl = null;
    } else {
      String parametersPrefix = "factory.parameter.";
      if (workspaceConfig != null) {
        Map<String, String> configAttributes = workspaceConfig.getAttributes();
        if (configAttributes.containsKey(parametersPrefix + "name")) {
          factoryName = configAttributes.get(parametersPrefix + "name");
        } else {
          factoryName = null;
        }
        if (configAttributes.containsKey(parametersPrefix + "user")) {
          factoryOwner = configAttributes.get(parametersPrefix + "user");
        } else {
          factoryOwner = null;
        }
        if (configAttributes.containsKey(parametersPrefix + "url")) {
          factoryUrl = configAttributes.get(parametersPrefix + "url");
        } else {
          factoryUrl = null;
        }
      } else {
        factoryName = null;
        factoryOwner = null;
        factoryUrl = null;
      }
    }
    osioSpaceId = workspace.getAttributes().get("osio_spaceId");

    if (workspaceConfig != null) {
      workspaceName = workspaceConfig.getName();
    } else if (devfile != null) {
      workspaceName = devfile.getMetadata().getName();
    } else {
      workspaceName = null;
    }

    if (machineToken != null && ! machineToken.isEmpty()) {
      try {
      JwtParser jwtParser = Jwts.parser();
        String[] splitted = machineToken.split("\\.");
        if (splitted.length != 3) {
          LOG.warn("Cannot retrieve user Id from the machine token: invalid token");
        } else {
          Object userIdClaim = jwtParser.parseClaimsJwt(splitted[0]+ "." + splitted[1] + ".").getBody().get(USER_ID_CLAIM);
          if (userIdClaim == null) {
            LOG.warn("Cannot retrieve user Id from the machine token: No '{}' claim", USER_ID_CLAIM);
          } else {
            userId = userIdClaim.toString();
          }
        }
      } catch (Exception e) {
        LOG.warn("Cannot retrieve user Id from the machine token", e);
      }
    }
  }

  private Workspace getWorkspace(String endpoint) {
    try {
      return this.requestFactory.fromUrl(endpoint).request().asDto(WorkspaceDto.class);
    } catch (IOException |
        ServerException |
        UnauthorizedException |
        ForbiddenException |
        NotFoundException |
        ConflictException |
        BadRequestException e) {
      throw new RuntimeException("Can't get workspace information for Che analytics", e);
    }
  }

  public final String getWorkspaceId() {
    return workspaceId;
  }

  public final String getUserId() {
    return userId;
  }

  public abstract boolean isEnabled();

  public abstract void onActivity();

  public abstract void onEvent(
      AnalyticsEvent event,
      String ownerId,
      String ip,
      String userAgent,
      String resolution,
      Map<String, Object> properties);

  public abstract void destroy();
}
