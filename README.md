# che-workspace-telemetry-client

[![Contribute](https://camo.githubusercontent.com/7ca4f6be43fb5eb61a73ba6d40b3481d93ef5813/68747470733a2f2f6368652e6f70656e73686966742e696f2f666163746f72792f7265736f75726365732f666163746f72792d636f6e747269627574652e737667)](https://che.openshift.io/f?url=https://github.com/che-incubator/che-workspace-telemetry-client)

This repository contains an abstract telemetry API and a Typescript implementation of the API.

## Releasing

Whenever a commit is pushed to `master`, the GitHub Action defined in [`publish.yaml`](./.github/workflows/publish.yaml) is run.  This installs `maven`, increments the versions in each POM, builds the projects, creates a release commit, and runs `mvn deploy`.  This job pushes a number of artifacts to the [GitHub package repository](https://github.com/che-incubator/che-workspace-telemetry-client/packages) for this repository.

| Project        | Artifact Name       |  Description | Used By |
|----------------|---------------------|--------------|---------|
| `parent`       | `parent-$VERSION.pom` | The POM file of the parent repository (this repo)| Not used |
| `backend-base` | `backend-base-$VERSION-resources.zip` | A zip file containing the telemetry OpenAPI spec in `yaml` format | `javascript` project (see `./javascript/pom.xml`)|
| `backend-base` | `backend-base-$VERSION.jar` | The compiled `backend-base` artifact | [`che-workspace-telemetry-woopra-plugin`](https://github.com/che-incubator/che-workspace-telemetry-woopra-plugin) |
| `backend-base` | `backend-base-$VERSION.pom` | The POM file of the `backend-base` project | [`che-workspace-telemetry-woopra-plugin`](https://github.com/che-incubator/che-workspace-telemetry-woopra-plugin) | 
| `javascript`   | `javascript-$VERSION.pom`   | The POM file of the `javascript` project | Not used | 

After the maven artifacts are published, the GitHub Action runs `npm publish` in the `javascript` project to push a new version of [`@eclipse-che/workspace-telemetry-client`](https://npmjs.com/package/@eclipse-che/workspace-telemetry-client)

To use the new version in the woopra plugin, replace the version coordinate in `pom.xml`:

```xml
<dependency>
  <groupId>org.eclipse.che.incubator.workspace-telemetry</groupId>
  <artifactId>backend-base</artifactId>
  <version>      </version>
</dependency>
```


## For information about the abstract Java API, see [this README](./backend-base/README.md)

