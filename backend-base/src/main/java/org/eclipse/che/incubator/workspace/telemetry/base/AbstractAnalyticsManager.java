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
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.che.incubator.workspace.telemetry.base.AnalyticsEvent.WORKSPACE_OPENED;
import static org.eclipse.che.incubator.workspace.telemetry.base.AnalyticsEvent.WORKSPACE_STARTED;
import static org.eclipse.che.incubator.workspace.telemetry.base.AnalyticsEvent.WORKSPACE_INACTIVE;
import static org.eclipse.che.incubator.workspace.telemetry.base.AnalyticsEvent.WORKSPACE_USED;
import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.USER_ID_CLAIM;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

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
  final protected String osioSpaceId;
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

  private HttpJsonRequestFactory requestFactory;

  @VisibleForTesting
  LoadingCache<String, EventDispatcher> dispatchers;

  protected ScheduledExecutorService checkActivityExecutor = Executors
      .newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("Analytics Activity Checker").build());

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
    LOG.info(workspace.toString());
    LOG.info(workspace.getDevfile().toString());

    pluginNames = getPluginNamesFromWorkspace(workspace);

    LOG.info("createdOn is " + createdOn);
    Long createDate = getDateFromString(createdOn);
    Long updateDate = getDateFromString(updatedOn);
    Long stopDate = getDateFromString(stoppedOn);

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
    osioSpaceId = workspace.getAttributes().get("osio_spaceId");

    workspaceName = getWorkspaceName(workspaceConfig, devfile);
    userId = getUserIdFromMachineToken(machineToken);

    long checkActivityPeriod = pingTimeoutSeconds / 2;

    checkActivityExecutor.scheduleAtFixedRate(this::checkActivity, checkActivityPeriod, checkActivityPeriod, SECONDS);

    dispatchers = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).maximumSize(10)
        .removalListener((RemovalNotification<String, EventDispatcher> n) -> {
          EventDispatcher dispatcher = n.getValue();
          if (dispatcher != null) {
            dispatcher.close();
          }
        }).build(CacheLoader.<String, EventDispatcher>from(userId -> newEventDispatcher(userId)));

  }

  private void checkActivity() {
    LOG.debug("In checkActivity");
    long inactiveLimit = System.currentTimeMillis() - noActivityTimeout;
    dispatchers.asMap().values().forEach(dispatcher -> {
      LOG.debug("Checking activity of dispatcher for user: {}", dispatcher.getUserId());
      if (dispatcher.getLastActivityTime() < inactiveLimit) {
        LOG.debug("Sending 'WORKSPACE_INACTIVE' event for user: {}", dispatcher.getUserId());
        if (dispatcher.sendTrackEvent(WORKSPACE_INACTIVE, getCommonProperties(), dispatcher.getLastIp(),
            dispatcher.getLastUserAgent(), dispatcher.getLastResolution()) != null) {
          LOG.debug("Sent 'WORKSPACE_INACTIVE' event for user: {}", dispatcher.getUserId());
          return;
        }
        LOG.debug(
            "Skipped sending 'WORKSPACE_INACTIVE' event for user: {} since it is the same event as the previous one",
            dispatcher.getUserId());
        return;
      }
      synchronized (dispatcher) {
        AnalyticsEvent lastEvent = dispatcher.getLastEvent();
        if (lastEvent == null) {
          return;
        }

        long expectedDuration = lastEvent.getExpectedDurationSeconds() * 1000;
        if (lastEvent == WORKSPACE_INACTIVE || (expectedDuration >= 0
            && System.currentTimeMillis() > expectedDuration + dispatcher.getLastEventTime())) {
          dispatcher.sendTrackEvent(WORKSPACE_USED, getCommonProperties(), dispatcher.getLastIp(),
              dispatcher.getLastUserAgent(), dispatcher.getLastResolution());
        }
      }
    });
  }

  EventDispatcher newEventDispatcher(String userId) {
    return new EventDispatcher(userId, this);
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

  private Long getDateFromString(String date) {
    Long parsedDate = null;
    try {
      parsedDate = parseLong(date);
    } catch (NumberFormatException nfe) {
      LOG.warn("the timestamp ( " + date + " ) has invalid format", nfe);
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
    return components.stream()
      .filter((e -> e.getType().equals("chePlugin")))
      .map((e -> e.getId()))
      .collect(Collectors.toList());
  }

 private void setFactoryVariables(String apiEndpoint, WorkspaceConfig config) {
   if (factoryId != null && !"undefined".equals(factoryId)) {
     String endpoint = apiEndpoint + "/factory/" + factoryId;

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
          Object userIdClaim = jwtParser.parseClaimsJwt(splitted[0] + "." + splitted[1] + ".").getBody().get(USER_ID_CLAIM);
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

  public final String getWorkspaceId() {
    return workspaceId;
  }

  public final String getUserId() {

    return userId;
  }

  public final List<String> getPluginNames() {
    return pluginNames;
  }

  private String makePluginString(List<String> pluginNames) {
    return pluginNames.stream().collect(Collectors.joining(", "));
  }

  public abstract boolean isEnabled();

  public abstract void onActivity();

  public abstract void onEvent(AnalyticsEvent event, String ownerId, String ip, String userAgent, String resolution,
      Map<String, Object> properties);

  public abstract void destroy();

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
    LOG.info("transformEvent userId=[" + userId +"]");
    if (event == WORKSPACE_OPENED && workspaceStartingUserId == null) {
      event = AnalyticsEvent.WORKSPACE_STARTED;
    }
    if (event == WORKSPACE_STARTED) {
      workspaceStartingUserId = userId;

    }
    return event;
  }

  /**
   * Adds common event properties to the properties object
   *
   * @param properties
   * @return
   */
  public Map<String, Object> transformProperties(Map<String, Object> properties) {
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
        new SimpleImmutableEntry<>(EventProperties.OSIO_SPACE_ID, osioSpaceId),
        new SimpleImmutableEntry<>(EventProperties.SOURCE_TYPES, sourceTypes),
        new SimpleImmutableEntry<>(EventProperties.START_NUMBER, startNumber),
        new SimpleImmutableEntry<>(EventProperties.PLUGINS, makePluginString(pluginNames))).forEach((entry) -> {
          if (entry.getValue() != null) {
            commonPropertiesBuilder.put(entry.getKey(), entry.getValue());
          }

        });
    properties.forEach((k, v)-> {
      commonPropertiesBuilder.put(k, v);
    });
    return commonPropertiesBuilder.build();
  }

  public void setCommonProperties(Map<String, Object> commonProperties) {
    this.commonProperties = commonProperties;
  }

  public Map<String, Object> getCommonProperties() {
    return commonProperties;
  }

  @VisibleForTesting
  class EventDispatcher {

    @VisibleForTesting
    String userId;
    @VisibleForTesting
    String cookie;

    private AnalyticsEvent lastEvent = null;
    @VisibleForTesting
    Map<String, Object> lastEventProperties = null;
    private long lastActivityTime;
    private long lastEventTime;
    private String lastIp = null;
    private String lastUserAgent = null;
    private String lastResolution = null;
    private ScheduledFuture<?> pinger = null;

    EventDispatcher(String userId, AbstractAnalyticsManager manager) {
      this.userId = userId;
      cookie = Hashing.sha256().hashString(workspaceId + userId + System.currentTimeMillis(), StandardCharsets.UTF_8)
          .toString();
      LOG.info("Analytics Woopra Cookie for user {} and workspace {} : {}", userId, workspaceId, cookie);
    }

    void onActivity() {
      lastActivityTime = System.currentTimeMillis();
    }

    private boolean areEventsEqual(AnalyticsEvent event, Map<String, Object> properties) {
      if (lastEvent == null || lastEvent != event) {
        return false;
      }

      if (lastEventProperties == null) {
        return false;
      }

      for (String propToCheck : event.getPropertiesToCheck()) {
        Object lastValue = lastEventProperties.get(propToCheck);
        Object newValue = properties.get(propToCheck);
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

    String sendTrackEvent(AnalyticsEvent event, final Map<String, Object> properties, String ip, String userAgent,
        String resolution) {
      return sendTrackEvent(event, properties, ip, userAgent, resolution, false);
    }

    String sendTrackEvent(AnalyticsEvent event, final Map<String, Object> properties, String ip, String userAgent,
        String resolution, boolean force) {
      String eventId;
      lastIp = ip;
      lastUserAgent = userAgent;
      lastResolution = resolution;
      final String theIp = ip != null ? ip : "0.0.0.0";
      synchronized (this) {
        lastEventTime = System.currentTimeMillis();
        if (!force && areEventsEqual(event, properties)) {
          LOG.debug("Skipping event " + event.toString() + " since it is the same as the last one");
          return null;
        }

        eventId = UUID.randomUUID().toString();
        TrackMessage.Builder messageBuilder = TrackMessage.builder(event.toString()).userId(userId).messageId(eventId);

        ImmutableMap.Builder<String, Object> integrationBuilder = ImmutableMap.<String, Object>builder()
            .put("cookie", cookie).put("timeout", pingTimeout);
        messageBuilder.integrationOptions("Woopra", integrationBuilder.build());

        ImmutableMap.Builder<String, Object> propertiesBuilder = ImmutableMap.<String, Object>builder()
            .putAll(properties);
        messageBuilder.properties(propertiesBuilder.build());

        ImmutableMap.Builder<String, Object> contextBuilder = ImmutableMap.<String, Object>builder().put("ip", ip);
        if (userAgent != null) {
          contextBuilder.put("userAgent", userAgent);
        }
        if (event.getExpectedDurationSeconds() == 0) {
          contextBuilder.put("duration", 0);
        }
        messageBuilder.context(contextBuilder.build());

        LOG.debug("sending " + event.toString() + " (ip=" + theIp + " - userAgent=" + userAgent + ") with properties: "
            + properties);
        analytics.enqueue(messageBuilder);

        lastEvent = event;
        lastEventProperties = properties;

        long pingPeriod = pingTimeoutSeconds / 3 + 2;

        if (pinger != null) {
          pinger.cancel(true);
        }

        LOG.debug("scheduling ping request with the following delay: " + pingPeriod);
        pinger = networkExecutor.scheduleAtFixedRate(() -> sendPingRequest(false), pingPeriod, pingPeriod,
            TimeUnit.SECONDS);
      }
      return eventId;
    }

    long getLastActivityTime() {
      return lastActivityTime;
    }

    String getLastIp() {
      return lastIp;
    }

    String getLastUserAgent() {
      return lastUserAgent;
    }

    String getLastResolution() {
      return lastResolution;
    }

    String getUserId() {
      return userId;
    }

    AnalyticsEvent getLastEvent() {
      return lastEvent;
    }

    long getLastEventTime() {
      return lastEventTime;
    }

    void close() {
      if (pinger != null) {
        pinger.cancel(true);
      }
      pinger = null;
    }
  }
}
