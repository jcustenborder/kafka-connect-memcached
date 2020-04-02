# Introduction
[Documentation](https://jcustenborder.github.io/kafka-connect-documentation/projects/kafka-connect-memcached) | [Confluent Hub](https://www.confluent.io/hub/jcustenborder/kafka-connect-memcached)

The memcached plugin is a mechanism for writing data to a memcached cluster.

# Installation

## Confluent Hub

The following command can be used to install the plugin directly from the Confluent Hub using the
[Confluent Hub Client](https://docs.confluent.io/current/connect/managing/confluent-hub/client.html).

```bash
confluent-hub install jcustenborder/kafka-connect-memcached:latest
```

## Manually

The zip file that is deployed to the [Confluent Hub](https://www.confluent.io/hub/jcustenborder/kafka-connect-memcached) is available under
`target/components/packages/`. You can manually extract this zip file which includes all dependencies. All the dependencies
that are required to deploy the plugin are under `target/kafka-connect-target` as well. Make sure that you include all the dependencies that are required
to run the plugin.

1. Create a directory under the `plugin.path` on your Connect worker.
2. Copy all of the dependencies under the newly created subdirectory.
3. Restart the Connect worker.



# Sink Connectors
## [Memcached Sink](https://jcustenborder.github.io/kafka-connect-documentation/projects/kafka-connect-memcached/sinks/MemcachedSinkConnector.html)

```
com.github.jcustenborder.kafka.connect.memcached.MemcachedSinkConnector
```

The Memcached Sink provides a :term:`Sink Connector` that can write data in real time to a memcached environment.
### Note

This connector expects that the key will be a string and the value will be a byte representation of the message. Your data might not be formatted like this. Take a look at transformations to apply the convert the data to the proper format.
### Configuration

#### Connection


##### `memcached.hosts`

Memcached hosts to connect to.

*Importance:* HIGH

*Type:* LIST

*Default Value:* [localhost:11211]

*Validator:* com.github.jcustenborder.kafka.connect.utils.config.ValidHostnameAndPort@427c8f6



##### `memcached.default.expiration.secs`

The default expiration in seconds.

*Importance:* LOW

*Type:* INT

*Default Value:* 0

*Validator:* [0,...]



##### `memcached.failure.mode`

Set the failure mode. `Cancel` - Automatically cancel all operations heading towards a downed node., `Redistribute` - Move on to functional nodes when nodes fail., `Retry` - Continue to retry a failing node until it comes back up.

*Importance:* LOW

*Type:* STRING

*Default Value:* Retry

*Validator:* Matches: ``Redistribute``, ``Retry``, ``Cancel``



##### `memcached.locator`

The locator type. `ARRAY_MOD` - The classic node location algorithm., `CONSISTENT` - Consistent hash algorithm., `VBUCKET` - VBucket support.

*Importance:* LOW

*Type:* STRING

*Default Value:* ARRAY_MOD

*Validator:* Matches: ``ARRAY_MOD``, ``CONSISTENT``, ``VBUCKET``



##### `memcached.nagle.algorithm.enabled`

Enables the Nagle algorithm.

*Importance:* LOW

*Type:* BOOLEAN



##### `memcached.op.time.timeout.msecs`

The default operation timeout in milliseconds.

*Importance:* LOW

*Type:* LONG

*Default Value:* 30000

*Validator:* [-1,...]



##### `memcached.optimize.enable`

Set to false if the default operation optimization is not desirable.

*Importance:* LOW

*Type:* BOOLEAN

*Default Value:* true



##### `memcached.protocol`

Specify the protocol to use. `BINARY` - Use the binary protocol., `TEXT` - Use the text (ascii) protocol.

*Importance:* LOW

*Type:* STRING

*Default Value:* BINARY

*Validator:* Matches: ``TEXT``, ``BINARY``



##### `memcached.read.buffer.bytes`

memcached.read.buffer.bytes

*Importance:* LOW

*Type:* INT

*Default Value:* -1

*Validator:* [-1,...]



##### `memcached.reconnect.delay.max.secs`

The maximum reconnect delay.

*Importance:* LOW

*Type:* LONG

*Default Value:* 30

*Validator:* [1,...]





# Development

## Building the source

```bash
mvn clean package
```

## Contributions

Contributions are always welcomed! Before you start any development please create an issue and
start a discussion. Create a pull request against your newly created issue and we're happy to see
if we can merge your pull request. First and foremost any time you're adding code to the code base
you need to include test coverage. Make sure that you run `mvn clean package` before submitting your
pull to ensure that all of the tests, checkstyle rules, and the package can be successfully built.