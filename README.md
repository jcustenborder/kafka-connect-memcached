
# Introduction


# Sink Connectors


## Memcached Sink

The Memcached Sink provides a :term:`Sink Connector` that can write data in real time to a memcached environment.




### Note

This connector expects that the key will be a string and the value will be a byte representation of the message. Your data might not be formatted like this. Take a look at transformations to apply the convert the data to the proper format.


### Configuration

##### `memcached.hosts`
*Importance:* High

*Type:* List

*Default Value:* [localhost:11211]

*Validator:* com.github.jcustenborder.kafka.connect.utils.config.ValidHostnameAndPort@3cee181


Memcached hosts to connnect to.
##### `memcached.default.expiration.secs`
*Importance:* Low

*Type:* Int

*Default Value:* 0

*Validator:* [-1,...]


The default expiration in seconds.
##### `memcached.failure.mode`
*Importance:* Low

*Type:* String

*Default Value:* Retry

*Validator:* ValidEnum{enum=FailureMode, allowed=[Redistribute, Retry, Cancel]}


Set the failure mode.
+==============+====================================================================+
| Mode         | Description                                                        |
+==============+====================================================================+
| Cancel       | Automatically cancel all operations heading towards a downed node. |
+--------------+--------------------------------------------------------------------+
| Redistribute | Move on to functional nodes when nodes fail.                       |
+--------------+--------------------------------------------------------------------+
| Retry        | Continue to retry a failing node until it comes back up.           |
+--------------+--------------------------------------------------------------------+
##### `memcached.locator`
*Importance:* Low

*Type:* String

*Default Value:* ARRAY_MOD

*Validator:* ValidEnum{enum=Locator, allowed=[ARRAY_MOD, CONSISTENT, VBUCKET]}


The locator type.
+============+======================================================+
| Locator    | Description                                          |
+============+======================================================+
| ARRAY_MOD  | Array modulus - the classic node location algorithm. |
+------------+------------------------------------------------------+
| CONSISTENT | Consistent hash algorithm.                           |
+------------+------------------------------------------------------+
| VBUCKET    | VBucket support.                                     |
+------------+------------------------------------------------------+
##### `memcached.nagle.algorithm.enabled`
*Importance:* Low

*Type:* Boolean

*Default Value:* false


Enables the Nagle algorithm.
##### `memcached.op.time.timeout.msecs`
*Importance:* Low

*Type:* Long

*Default Value:* -1

*Validator:* [-1,...]


The default operation timeout in milliseconds.
##### `memcached.optimize.enable`
*Importance:* Low

*Type:* Boolean

*Default Value:* true


Set to false if the default operation optimization is not desirable.
##### `memcached.protocol`
*Importance:* Low

*Type:* String

*Default Value:* BINARY

*Validator:* ValidEnum{enum=Protocol, allowed=[TEXT, BINARY]}


Specify the protocol to use.+==========+================================+
| Protocol | Description                    |
+==========+================================+
| BINARY   | Use the binary protocol.       |
+----------+--------------------------------+
| TEXT     | Use the text (ascii) protocol. |
+----------+--------------------------------+
##### `memcached.read.buffer.bytes`
*Importance:* Low

*Type:* Int

*Default Value:* -1

*Validator:* [-1,...]


memcached.read.buffer.bytes
##### `memcached.reconnect.delay.max.secs`
*Importance:* Low

*Type:* Long

*Default Value:* 30

*Validator:* [1,...]


The maximum reconnect delay.

#### Examples

##### Standalone Example

This configuration is used typically along with [standalone mode](http://docs.confluent.io/current/connect/concepts.html#standalone-workers).

```properties
name=MemcachedSinkConnector1
connector.class=com.github.jcustenborder.kafka.connect.memcached.MemcachedSinkConnector
tasks.max=1
topics=< Required Configuration >
```

##### Distributed Example

This configuration is used typically along with [distributed mode](http://docs.confluent.io/current/connect/concepts.html#distributed-workers).
Write the following json to `connector.json`, configure all of the required values, and use the command below to
post the configuration to one the distributed connect worker(s).

```json
{
  "config" : {
    "name" : "MemcachedSinkConnector1",
    "connector.class" : "com.github.jcustenborder.kafka.connect.memcached.MemcachedSinkConnector",
    "tasks.max" : "1",
    "topics" : "< Required Configuration >"
  }
}
```

Use curl to post the configuration to one of the Kafka Connect Workers. Change `http://localhost:8083/` the the endpoint of
one of your Kafka Connect worker(s).

Create a new instance.
```bash
curl -s -X POST -H 'Content-Type: application/json' --data @connector.json http://localhost:8083/connectors
```

Update an existing instance.
```bash
curl -s -X PUT -H 'Content-Type: application/json' --data @connector.json http://localhost:8083/connectors/TestSinkConnector1/config
```



