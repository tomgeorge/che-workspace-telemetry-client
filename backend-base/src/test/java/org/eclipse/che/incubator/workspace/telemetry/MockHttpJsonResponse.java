package org.eclipse.che.incubator.workspace.telemetry;

import com.google.gson.reflect.TypeToken;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.dto.server.DtoFactory;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class MockHttpJsonResponse implements HttpJsonResponse {

  private static final Type STRING_MAP_TYPE = new TypeToken<Map<String, String>>() {
  }.getType();

  private int responseCode;
  private String responseBody;
  private Map<String, List<String>> headers;

  public MockHttpJsonResponse(String response, int responseCode) {
    this.responseBody = response;
    this.responseCode = responseCode;
    this.headers = Collections.emptyMap();
  }


  @Override
  public int getResponseCode() {
    return responseCode;
  }

  @Override
  public String asString() {
    return responseBody;
  }

  @Override
  public <T> T asDto(@NotNull Class<T> dtoInterface) {
    requireNonNull(dtoInterface, "Required non-null dto interface");
    return DtoFactory.getInstance().createDtoFromJson(responseBody, dtoInterface);
  }

  @Override
  public <T> List<T> asList(@NotNull Class<T> dtoInterface) {
    return DtoFactory.getInstance().createListDtoFromJson(responseBody, dtoInterface);
  }

  @Override
  public Map<String, String> asProperties() throws IOException {
    return as(Map.class, STRING_MAP_TYPE);
  }

  @Override
  public <T> T as(@NotNull Class<T> clazz, Type genericType) throws IOException {
    return null;
  }

  @Override
  public Map<String, List<String>> getHeaders() {
    return headers;
  }
}
