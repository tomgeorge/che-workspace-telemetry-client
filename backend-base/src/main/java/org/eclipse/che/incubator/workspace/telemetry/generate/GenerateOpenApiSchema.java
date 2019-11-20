package org.eclipse.che.incubator.workspace.telemetry.generate;

import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;

import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;
import io.smallrye.openapi.runtime.io.OpenApiSerializer.Format;
import io.smallrye.openapi.runtime.scanner.OpenApiAnnotationScanner;

public class GenerateOpenApiSchema {
  public static void main(String[] args) throws IOException {
    IndexReader indexReader = new IndexReader(GenerateOpenApiSchema.class.getClassLoader().getResourceAsStream("META-INF/jandex.idx"));
    Index index = indexReader.read();
    OpenAPI openAPI = new OpenApiAnnotationScanner(new OpenApiConfigImpl(ConfigProvider.getConfig()), index).scan();
    String yaml = OpenApiSerializer.serialize(openAPI, Format.YAML);
    FileWriter fileWriter = new FileWriter(args[0]);
    fileWriter.write(yaml);
    fileWriter.close();
  }
}