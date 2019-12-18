package org.eclipse.che.incubator.workspace.telemetry.base;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;

import org.eclipse.che.api.core.rest.DefaultHttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.arc.DefaultBean;

@Dependent
@Default
public class BaseConfiguration {
    @ConfigProperty(name = "che.api") 
    protected String apiEndpoint;
    
    @ConfigProperty(name = "che.workspace.id")
    protected String workspaceId;
    
    @ConfigProperty(name = "che.machine.token", defaultValue = "")
    protected String machineToken;

    private HttpJsonRequestFactory requestFactory() {
        return new DefaultHttpJsonRequestFactory() {

            @Override
            public HttpJsonRequest fromUrl(String url) {
              return super.fromUrl(url).setAuthorizationHeader(machineToken);
            }
          
            @Override
            public HttpJsonRequest fromLink(Link link) {
              return super.fromLink(link).setAuthorizationHeader(machineToken);
            }
        };
    }

    @Produces
    @DefaultBean
    protected AbstractAnalyticsManager analyticsManager() {
      return new DefaultAnalyticsManager(apiEndpoint, workspaceId, machineToken, requestFactory());
    }
}
