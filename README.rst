============
Introduction
============


===============
Sink Connectors
===============

==============
Memcached Sink
==============

The Memcached Sink provides a :term:`Sink Connector` that can write data in real time to a memcached environment.


.. NOTE::
    This connector expects that the key will be a string and the value will be a byte representation of the message. Your data might not be formatted like this. Take a look at transformations to apply the convert the data to the proper format.



-------------
Configuration
-------------

----------
Connection
----------


^^^^^^^^^^^^^^^
memcached.hosts
^^^^^^^^^^^^^^^

Memcached hosts to connnect to.

**Importance:** High

**Type:** List

**Default Value:** [localhost:11211]

**Validator:** com.github.jcustenborder.kafka.connect.utils.config.ValidHostnameAndPort@5c13534a



^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
memcached.default.expiration.secs
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The default expiration in seconds.

**Importance:** Low

**Type:** Int

**Default Value:** 0

**Validator:** [0,...]



^^^^^^^^^^^^^^^^^^^^^^
memcached.failure.mode
^^^^^^^^^^^^^^^^^^^^^^

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

**Importance:** Low

**Type:** String

**Default Value:** Retry

**Validator:** ``Redistribute``, ``Retry``, ``Cancel``



^^^^^^^^^^^^^^^^^
memcached.locator
^^^^^^^^^^^^^^^^^

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

**Importance:** Low

**Type:** String

**Default Value:** ARRAY_MOD

**Validator:** ``ARRAY_MOD``, ``CONSISTENT``, ``VBUCKET``



^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
memcached.nagle.algorithm.enabled
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Enables the Nagle algorithm.

**Importance:** Low

**Type:** Boolean

**Default Value:** false



^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
memcached.op.time.timeout.msecs
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The default operation timeout in milliseconds.

**Importance:** Low

**Type:** Long

**Default Value:** 30000

**Validator:** [-1,...]



^^^^^^^^^^^^^^^^^^^^^^^^^
memcached.optimize.enable
^^^^^^^^^^^^^^^^^^^^^^^^^

Set to false if the default operation optimization is not desirable.

**Importance:** Low

**Type:** Boolean

**Default Value:** true



^^^^^^^^^^^^^^^^^^
memcached.protocol
^^^^^^^^^^^^^^^^^^

Specify the protocol to use.+==========+================================+
| Protocol | Description                    |
+==========+================================+
| BINARY   | Use the binary protocol.       |
+----------+--------------------------------+
| TEXT     | Use the text (ascii) protocol. |
+----------+--------------------------------+

**Importance:** Low

**Type:** String

**Default Value:** BINARY

**Validator:** ``TEXT``, ``BINARY``



^^^^^^^^^^^^^^^^^^^^^^^^^^^
memcached.read.buffer.bytes
^^^^^^^^^^^^^^^^^^^^^^^^^^^

memcached.read.buffer.bytes

**Importance:** Low

**Type:** Int

**Default Value:** -1

**Validator:** [-1,...]



^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
memcached.reconnect.delay.max.secs
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The maximum reconnect delay.

**Importance:** Low

**Type:** Long

**Default Value:** 30

**Validator:** [1,...]






--------
Examples
--------

^^^^^^^^^^^^^^^^^^^^^^
Property based example
^^^^^^^^^^^^^^^^^^^^^^


This configuration is used typically along with `standalone mode
<http://docs.confluent.io/current/connect/concepts.html#standalone-workers>`_.

.. code-block:: properties
    :name: connector.properties
    :emphasize-lines: 4

    name=MemcachedSinkConnector1
    connector.class=com.github.jcustenborder.kafka.connect.memcached.MemcachedSinkConnector
    tasks.max=1
    topics=< Required Configuration >




^^^^^^^^^^^^^^^^^^
Rest based example
^^^^^^^^^^^^^^^^^^


This configuration is used typically along with `distributed mode
<http://docs.confluent.io/current/connect/concepts.html#distributed-workers>`_.
Write the following json to `connector.json`, configure all of the required values, and use the command below to
post the configuration to one the distributed connect worker(s). Check here for more information about the
`Kafka Connect REST Interface. <https://docs.confluent.io/current/connect/restapi.html>`_

.. code-block:: json
    :caption: Connect Distributed REST example
    :name: connector.json
    :emphasize-lines: 6

    {
      "config" : {
        "name" : "MemcachedSinkConnector1",
        "connector.class" : "com.github.jcustenborder.kafka.connect.memcached.MemcachedSinkConnector",
        "tasks.max" : "1",
        "topics" : "< Required Configuration >"
      }
    }



Use curl to post the configuration to one of the Kafka Connect Workers. Change `http://localhost:8083/` the the endpoint of
one of your Kafka Connect worker(s).

.. code-block:: bash
    :caption: Create a new connector

    curl -s -X POST -H 'Content-Type: application/json' --data @connector.json http://localhost:8083/connectors


.. code-block:: bash
    :caption: Update an existing connector

    curl -s -X PUT -H 'Content-Type: application/json' --data @connector.json http://localhost:8083/connectors/MemcachedSinkConnector1/config




