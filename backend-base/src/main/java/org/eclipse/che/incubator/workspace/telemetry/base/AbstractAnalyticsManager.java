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
import static org.eclipse.che.incubator.workspace.telemetry.base.AnalyticsEvent.WORKSPACE_OPENED;
import static org.eclipse.che.incubator.workspace.telemetry.base.AnalyticsEvent.WORKSPACE_STARTED;
import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.USER_ID_CLAIM;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.core.model.workspace.devfile.Devfile;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.slf4j.Logger;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

public abstract class AbstractAnalyticsManager {
  private static final Logger LOG = getLogger(AbstractAnalyticsManager.class);

  protected final String workspaceId;
  protected String userId = "";

  @VisibleForTesting
  protected long noActivityTimeout = 60000 * 3;

  @VisibleForTesting
  final protected String workspaceName;
  @VisibleForTesting
  final protected String factoryId;
  @VisibleForTesting
  final protected String stackId;
  @VisibleForTesting
  protected String factoryName;
  @VisibleForTesting
  protected String factoryOwner;
  @VisibleForTesting
  protected String factoryUrl;
  @VisibleForTesting
  final protected String createdOn;
  @VisibleForTesting
  final protected String updatedOn;
  @VisibleForTesting
  final protected String stoppedOn;
  @VisibleForTesting
  final protected String stoppedAbnormally;
  @VisibleForTesting
  final protected String lastErrorMessage;
  @VisibleForTesting
  final protected String sourceTypes;
  @VisibleForTesting
  final protected String startNumber;
  @VisibleForTesting
  final protected List<String> pluginNames;

  @VisibleForTesting
  final protected Long age;
  @VisibleForTesting
  final protected Long returnDelay;
  @VisibleForTesting
  final protected Boolean firstStart;

  @VisibleForTesting
  protected String workspaceStartingUserId = null;

  @VisibleForTesting
  protected Map<String, Object> commonProperties;

  @VisibleForTesting
  protected static long pingTimeoutSeconds = 30;

  @VisibleForTesting
  protected static long pingTimeout = pingTimeoutSeconds * 1000;

  protected AnalyticsEvent lastEvent = null;

  @VisibleForTesting
  Map<String, Object> lastEventProperties = null;

  protected long lastActivityTime;

  protected long lastEventTime;

  protected String lastIp = null;

  protected String lastUserAgent = null;

  protected String lastResolution = null;

  protected long debounceTimeMillis = 1500;

  private HttpJsonRequestFactory requestFactory;

  public abstract boolean isEnabled();

  public abstract void onActivity();

  public abstract void onEvent(AnalyticsEvent event, String ownerId, String ip, String userAgent, String resolution,
      Map<String, Object> properties);

  public abstract void increaseDuration(AnalyticsEvent event, Map<String, Object> properties);

  public abstract void destroy();

  public AbstractAnalyticsManager(String apiEndpoint, String workspaceId, String machineToken,
      HttpJsonRequestFactory requestFactory) {
    this.workspaceId = workspaceId;
    this.requestFactory = requestFactory;

    String endpoint = apiEndpoint + "/workspace/" + workspaceId;

    Workspace workspace = getWorkspace(endpoint);

    createdOn = workspace.getAttributes().get(Constants.CREATED_ATTRIBUTE_NAME);
    updatedOn = workspace.getAttributes().get(Constants.UPDATED_ATTRIBUTE_NAME);
    stoppedOn = workspace.getAttributes().get(Constants.STOPPED_ATTRIBUTE_NAME);
    stoppedAbnormally = workspace.getAttributes().get(Constants.STOPPED_ABNORMALLY_ATTRIBUTE_NAME);
    lastErrorMessage = workspace.getAttributes().get(Constants.ERROR_MESSAGE_ATTRIBUTE_NAME);
    sourceTypes = workspace.getAttributes().get("sourceTypes");
    startNumber = workspace.getAttributes().get("startNumber");

    pluginNames = getPluginNamesFromWorkspace(workspace);

    Long createDate = getDateFromString("created date", createdOn);
    Long updateDate = getDateFromString("updated date", updatedOn);
    Long stopDate = getDateFromString("stop date", stoppedOn);


    age = getSecondsBetween(updateDate, createDate);
    returnDelay = getSecondsBetween(updateDate, stopDate);

    if (updateDate != null) {
      firstStart = stopDate == null;
    } else {
      firstStart = null;
    }

    WorkspaceConfig workspaceConfig = workspace.getConfig();
    Devfile devfile = workspace.getDevfile();

    stackId = workspace.getAttributes().get("stackName");
    factoryId = workspace.getAttributes().get("factoryId");
    setFactoryVariables(endpoint, workspaceConfig);

    workspaceName = getWorkspaceName(workspaceConfig, devfile);
    userId = getUserIdFromMachineToken(machineToken);
    commonProperties = makeCommonProperties();
  }

  public void doSendEvent(AnalyticsEvent event, String ownerId, String ip, String userAgent, String resolution,
      Map<String, Object> properties) {
    if (shouldSendEvent(event, properties)) {
      onEvent(event, ownerId, ip, userAgent, resolution, getCurrentEventProperties(properties));
      lastEvent = event;
      lastEventTime = System.currentTimeMillis();
      lastIp = ip;
      lastUserAgent = userAgent;
      lastResolution = resolution;
      lastEventProperties = properties;
    } else {
      increaseDuration(event, properties);
      return;
    }
  }

  public final String getWorkspaceId() {
    return workspaceId;
  }

  public final String getUserId() {

    return userId;
  }

  public final List<String> getPluginNames() {
    return pluginNames;
  }

  /**
   * transformEvent performs preliminary modification to the event passed to
   * onEvent. If the event is an instance of WORKSPACED_OPEN, and the starting
   * user ID is null, it sets the starting user ID and returns a WORKSPACE_STARTED
   * event.
   *
   * @param event  the incoming event
   * @param userId the incoming user ID
   * @return the correct AnalyticsEvent, WORKSPACE_STARTED if the conditions are
   *         met, the same event otherwise.
   */
  public AnalyticsEvent transformEvent(AnalyticsEvent event, String userId) {
    if (event == WORKSPACE_OPENED && workspaceStartingUserId == null) {
      event = AnalyticsEvent.WORKSPACE_STARTED;
    }
    if (event == WORKSPACE_STARTED) {
      workspaceStartingUserId = userId;

    }
    return event;
  }

  public void setCommonProperties(Map<String, Object> commonProperties) {
    this.commonProperties = commonProperties;
  }

  public Map<String, Object> getCommonProperties() {
    return commonProperties;
  }

  private Workspace getWorkspace(String endpoint) {
    try {
      return this.requestFactory.fromUrl(endpoint).request().asDto(WorkspaceDto.class);
    } catch (IOException | ServerException | UnauthorizedException | ForbiddenException | NotFoundException
        | ConflictException | BadRequestException e) {
      throw new RuntimeException("Can't get workspace information for Che analytics", e);
    }

  }

  private Long getDateFromString(String fieldName, String date) {
    Long parsedDate = null;
    try {
      parsedDate = parseLong(date);
    } catch (NumberFormatException nfe) {
      LOG.warn("the timestamp for " + fieldName + " is null or invalid: [" + date + "]");
    }
    return parsedDate;
  }

  private Long getSecondsBetween(Long end, Long start) {
    Long timeBetween = null;
    if (end != null && start != null) {
      timeBetween = (end - start) / 1000;
    }
    return timeBetween;
  }

  private String getWorkspaceName(WorkspaceConfig config, Devfile devfile) {
    String workspaceName;
    if (config != null) {
      workspaceName = config.getName();
    } else if (devfile != null) {
      workspaceName = devfile.getMetadata().getName();
    } else {
      workspaceName = null;
    }
    return workspaceName;
  }

  private List<String> getPluginNamesFromWorkspace(Workspace ws) {
    List<? extends Component> components = ws.getDevfile().getComponents();
    return components.stream().filter((e -> e.getType().equals("chePlugin"))).map((e -> e.getId()))
        .collect(Collectors.toList());
  }

  private void setFactoryVariables(String apiEndpoint, WorkspaceConfig config) {
    if (factoryId != null && !"undefined".equals(factoryId)) {
      String endpoint = apiEndpoint + "/factory/" + factoryId;

      FactoryDto factory = null;
      try {
        factory = requestFactory.fromUrl(endpoint).request().asDto(FactoryDto.class);
      } catch (Exception e) {
        LOG.warn("Can't get workspace factory ('" + factoryId + "') information for Che analytics", e);
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
      if (config != null) {
        Map<String, String> configAttributes = config.getAttributes();
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
  }

  private String getUserIdFromMachineToken(String machineToken) {
    String userId = this.userId;
    if (machineToken != null && !machineToken.isEmpty()) {
      try {
        JwtParser jwtParser = Jwts.parser();
        String[] splitted = machineToken.split("\\.");
        if (splitted.length != 3) {
          LOG.warn("Cannot retrieve user Id from the machine token: invalid token");
        } else {
          Object userIdClaim = jwtParser.parseClaimsJwt(splitted[0] + "." + splitted[1] + ".").getBody()
              .get(USER_ID_CLAIM);
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
    return userId;
  }

  /**
   * create a map of the common and current event properties merged together
   *
   * @return a map of the current event and common workspace properties
   */
  public Map<String, Object> getCurrentEventProperties(Map<String, Object> eventProperties) {
    ImmutableMap.Builder<String, Object> currentEventPropertiesBuilder = ImmutableMap.builder();
    commonProperties.forEach((k, v) -> {
      currentEventPropertiesBuilder.put(k, v);
    });
    eventProperties.forEach((k, v) -> {
      currentEventPropertiesBuilder.put(k, v);
    });
    return currentEventPropertiesBuilder.build();
  }

  private Map<String, Object> makeCommonProperties() {
    ImmutableMap.Builder<String, Object> commonPropertiesBuilder = ImmutableMap.builder();

    Arrays.asList(new SimpleImmutableEntry<>(EventProperties.CREATED, createdOn),
        new SimpleImmutableEntry<>(EventProperties.WORKSPACE_ID, workspaceId),
        new SimpleImmutableEntry<>(EventProperties.WORKSPACE_NAME, workspaceName),
        new SimpleImmutableEntry<>(EventProperties.UPDATED, updatedOn),
        new SimpleImmutableEntry<>(EventProperties.STOPPED, stoppedOn),
        new SimpleImmutableEntry<>(EventProperties.AGE, age),
        new SimpleImmutableEntry<>(EventProperties.RETURN_DELAY, returnDelay),
        new SimpleImmutableEntry<>(EventProperties.FIRST_START, firstStart),
        new SimpleImmutableEntry<>(EventProperties.STACK_ID, stackId),
        new SimpleImmutableEntry<>(EventProperties.FACTORY_ID, factoryId),
        new SimpleImmutableEntry<>(EventProperties.FACTORY_NAME, factoryName),
        new SimpleImmutableEntry<>(EventProperties.FACTORY_URL, factoryUrl),
        new SimpleImmutableEntry<>(EventProperties.FACTORY_OWNER, factoryOwner),
        new SimpleImmutableEntry<>(EventProperties.LAST_WORKSPACE_FAILED, stoppedAbnormally),
        new SimpleImmutableEntry<>(EventProperties.LAST_WORKSPACE_FAILURE, lastErrorMessage),
        new SimpleImmutableEntry<>(EventProperties.SOURCE_TYPES, sourceTypes),
        new SimpleImmutableEntry<>(EventProperties.START_NUMBER, startNumber),
        new SimpleImmutableEntry<>(EventProperties.PLUGINS, makePluginString(pluginNames))).forEach((entry) -> {
          if (entry.getValue() != null) {
            commonPropertiesBuilder.put(entry.getKey(), entry.getValue());
          }

        });
    return commonPropertiesBuilder.build();
  }

  private String makePluginString(List<String> pluginNames) {
    return pluginNames.stream().collect(Collectors.joining(", "));
  }

  private boolean shouldSendEvent(AnalyticsEvent event, Map<String, Object> properties) {
    long eventTime = System.currentTimeMillis();
    if (lastEventTime != 0
        && (sameAsLastEvent(event, properties) && insideDebounceTime(eventTime, lastEventTime, debounceTimeMillis))) {
      return false;
    } else {
      return true;
    }
  }

  private boolean sameAsLastEvent(AnalyticsEvent event, Map<String, Object> properties) {
    if (lastEvent == null || lastEvent != event) {
      return false;
    }

    if (lastEventProperties == null) {
      return false;
    }

    for (String property: event.getPropertiesToCheck()) {
      Object lastValue = lastEventProperties.get(property);
      Object newValue = properties.get(property);
      if (lastValue != null && newValue != null && lastValue.equals(newValue)) {
        continue;
      }
      if (lastValue == null && newValue == null) {
        continue;
      }
      return false;
    }
    return true;
  }

  private boolean insideDebounceTime(long eventTime, long lastEventTime, long debounceTimeMillis) {
    return (eventTime - lastEventTime) < debounceTimeMillis;
  }

}
