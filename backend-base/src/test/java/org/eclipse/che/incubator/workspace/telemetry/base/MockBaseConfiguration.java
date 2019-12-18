package org.eclipse.che.incubator.workspace.telemetry.base;

import io.quarkus.test.Mock;
import org.eclipse.che.api.core.*;
import org.eclipse.che.api.core.rest.*;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.incubator.workspace.telemetry.MockHttpJsonResponse;

import javax.enterprise.inject.Produces;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.List;
import java.util.Map;

@Mock
public class MockBaseConfiguration extends BaseConfiguration {
    private class MockHttpRequest implements HttpJsonRequest {

        private String mockResponse;

        public MockHttpRequest() {
            mockResponse = this.getMockResponse();
        }

        @Override
        public HttpJsonRequest setMethod(@NotNull String method) {
            return null;
        }

        private String getMockResponse() {
            String workspaceResponse = "";
            try {
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("mock-response.json");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String l;
                while ((l = reader.readLine()) != null) {
                    workspaceResponse = workspaceResponse + l;
                }
            } catch(IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            return workspaceResponse;
        }


        @Override
        public HttpJsonRequest setBody(@NotNull Object body) {
            return null;
        }

        @Override
        public HttpJsonRequest setBody(@NotNull Map<String, String> map) {
            return null;
        }

        @Override
        public HttpJsonRequest setBody(@NotNull List<?> list) {
            return null;
        }

        @Override
        public HttpJsonRequest addQueryParam(@NotNull String name, @NotNull Object value) {
            return null;
        }

        @Override
        public HttpJsonRequest addHeader(@NotNull String name, @NotNull String value) {
            return null;
        }

        @Override
        public HttpJsonRequest setAuthorizationHeader(@NotNull String value) {
            return null;
        }

        @Override
        public HttpJsonRequest setTimeout(int timeoutMs) {
            return null;
        }

        @Override
        public String getUrl() {
            return null;
        }

        @Override
        public HttpJsonResponse request() throws IOException, ServerException, UnauthorizedException, ForbiddenException, NotFoundException, ConflictException, BadRequestException {
            return new MockHttpJsonResponse(mockResponse, 200);
        }
    }

   private HttpJsonRequestFactory requestFactory() {
        return new DefaultHttpJsonRequestFactory() {

            @Override
            public HttpJsonRequest fromUrl(String url) {
                return new MockHttpRequest();
            }

            @Override
            public HttpJsonRequest fromLink(Link link) {
                return new MockHttpRequest();
            }
        };
    }

    @Produces
    protected AbstractAnalyticsManager analyticsManager() {
        return new DefaultAnalyticsManager(apiEndpoint, workspaceId, machineToken, requestFactory());
    }

}
