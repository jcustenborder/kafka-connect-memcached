============
Introduction
============

=================
Source Connectors
=================

==============
Memcached Sink
==============

The Memcached Sink provides a :term:`Sink Connector` that can write data in real time to a memcached environment.


.. NOTE::
    This connector expects that the key will be a string and the value will be a byte representation of the message. Your data might not be formatted like this. Take a look at transformations to apply the convert the data to the proper format.



-------------
Configuration
-------------

.. csv-table:: Configuration
    :header: "Name", "Type", "Importance", "Default Value", "Validator", "Documentation"
    :widths: auto

    "memcached.hosts","List","High","[localhost:11211]","com.github.jcustenborder.kafka.connect.utils.config.ValidHostnameAndPort@444fe2bd","Memcached hosts to connnect to."
    "memcached.default.expiration.secs","Int","Low","0","[0,...]","The default expiration in seconds."
    "memcached.failure.mode","String","Low","Retry","ValidEnum{enum=FailureMode, allowed=[Redistribute, Retry, Cancel]}","Set the failure mode.
+==============+====================================================================+
| Mode         | Description                                                        |
+==============+====================================================================+
| Cancel       | Automatically cancel all operations heading towards a downed node. |
+--------------+--------------------------------------------------------------------+
| Redistribute | Move on to functional nodes when nodes fail.                       |
+--------------+--------------------------------------------------------------------+
| Retry        | Continue to retry a failing node until it comes back up.           |
+--------------+--------------------------------------------------------------------+"
    "memcached.locator","String","Low","ARRAY_MOD","ValidEnum{enum=Locator, allowed=[ARRAY_MOD, CONSISTENT, VBUCKET]}","The locator type.
+============+======================================================+
| Locator    | Description                                          |
+============+======================================================+
| ARRAY_MOD  | Array modulus - the classic node location algorithm. |
+------------+------------------------------------------------------+
| CONSISTENT | Consistent hash algorithm.                           |
+------------+------------------------------------------------------+
| VBUCKET    | VBucket support.                                     |
+------------+------------------------------------------------------+"
    "memcached.nagle.algorithm.enabled","Boolean","Low","false","","Enables the Nagle algorithm."
    "memcached.op.time.timeout.msecs","Long","Low","-1","[-1,...]","The default operation timeout in milliseconds."
    "memcached.optimize.enable","Boolean","Low","true","","Set to false if the default operation optimization is not desirable."
    "memcached.protocol","String","Low","BINARY","ValidEnum{enum=Protocol, allowed=[TEXT, BINARY]}","Specify the protocol to use.+==========+================================+
| Protocol | Description                    |
+==========+================================+
| BINARY   | Use the binary protocol.       |
+----------+--------------------------------+
| TEXT     | Use the text (ascii) protocol. |
+----------+--------------------------------+"
    "memcached.read.buffer.bytes","Int","Low","-1","[-1,...]","memcached.read.buffer.bytes"
    "memcached.reconnect.delay.max.secs","Long","Low","30","[1,...]","The maximum reconnect delay."


^^^^^^^^^^^^^^^^^^^^^^
Property based example
^^^^^^^^^^^^^^^^^^^^^^


This configuration is used typically along with `standalone mode
<http://docs.confluent.io/current/connect/concepts.html#standalone-workers>`_.

.. code-block:: properties

    name=connector1
    tasks.max=1
    connector.class=com.github.jcustenborder.kafka.connect.memcached.MemcachedSinkConnector
    # The following values must be configured.



^^^^^^^^^^^^^^^^^^
Rest based example
^^^^^^^^^^^^^^^^^^


This configuration is used typically along with `distributed mode
<http://docs.confluent.io/current/connect/concepts.html#distributed-workers>`_.
Write the following json to `connector.json`, configure all of the required values, and use the command below to
post the configuration to one the distributed connect worker(s).

.. code-block:: json

    {
        "name": "connector1",
        "config": {
            "connector.class": "com.github.jcustenborder.kafka.connect.memcached.MemcachedSinkConnector",
        }
    }

Use curl to post the configuration to one of the Kafka Connect Workers. Change `http://localhost:8083/` the the endpoint of
one of your Kafka Connect worker(s).

.. code-block:: bash

    curl -s -X POST -H 'Content-Type: application/json' --data @connector.json http://localhost:8083/connectors





