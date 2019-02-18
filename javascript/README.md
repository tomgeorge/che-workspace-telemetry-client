# Telemetry client

This is a client for telemetry REST API.

## Examples

### REST API client

```typescript
import TelemetryClient from '@dfatwork-pkgs/workspace-telemetry-client';

const telemetryClient = new TelemetryClient();
// notify activity
const promise = telemetryClient.activity({userId: 'anExampleOfUserId'});
promise.then(() => {
    // activity has been notified
});
```

## License

EPL-2
