# backend-base

This library is meant to be extended later by other libraries.  It implements a very small telemetry API that does not do much, require extension to add more capability.

## Building

Be sure to set `che.api` and `che.workspace.id` in `src/main/resources/application.properties` or set them on the command line during the maven run

`mvn pckage [-Dche.api=http://... -Dche.workspace.id....]`

For a native Quarkus image:

`mvn pckage -Pnative [-Dche.api=http://... -Dche.workspace.id....]`

## Testing

### Unit testing

`mvn test`

###  Integration Testing

#### Prerequesites

+ A Running Che Cluster in CRC, Minikube, Kind, etc.
+ A workspace ID
+ The `CHE_MACHINE_TOKEN` from the workspace

##### Standard Integration Test (Not native-mode)

```shell script
export CHE_MACHINE_TOKEN=<token from workspace>
mvn [integration-test | verify] -Dche.api<the URL of the che api> -Dche.workspace.id=<che workspace id>
```

##### Native-mode testing

Compile the native binary with your che.api and che.workspace.id values either in application.properties or on the command line, as above

```shell script
mvn [integration-test | verify] -Dche.api=<URL of Che API> -Dche.workspace.id=<che workspace ID> -Dnative.image.path=target/backend-base-0.0.1-SNAPSHOT-runner
```
