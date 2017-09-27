/**
 * Copyright © 2017 Jeremy Custenborder (jcustenborder@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jcustenborder.kafka.connect.memcached;

import com.github.jcustenborder.kafka.connect.utils.config.ConfigUtils;
import com.github.jcustenborder.kafka.connect.utils.config.ValidEnum;
import com.github.jcustenborder.kafka.connect.utils.config.ValidHostnameAndPort;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.FailureMode;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

class MemcachedSinkConnectorConfig extends AbstractConfig {

  public static final String PROTOCOL_CONF = "memcached.protocol";
  static final String PROTOCOL_DOC = "Specify the protocol to use." +
      "+==========+================================+\n" +
      "| Protocol | Description                    |\n" +
      "+==========+================================+\n" +
      "| BINARY   | Use the binary protocol.       |\n" +
      "+----------+--------------------------------+\n" +
      "| TEXT     | Use the text (ascii) protocol. |\n" +
      "+----------+--------------------------------+";
  static final String PROTOCOL_DEFAULT = ConnectionFactoryBuilder.Protocol.BINARY.name();

  public static final String HASH_ALGORITHM_CONF = "memcached.hash.algorithm";
  static final String HASH_ALGORITHM_DOC = "Set the hash algorithm.";
//  static final String HASH_ALGORITHM_DEFAULT = HashAlgorithm;

  public static final String FAILURE_MODE_CONF = "memcached.failure.mode";
  static final String FAILURE_MODE_DEFAULT = FailureMode.Retry.toString();
  static final String FAILURE_MODE_DOC = "Set the failure mode.\n" +
      "+==============+====================================================================+\n" +
      "| Mode         | Description                                                        |\n" +
      "+==============+====================================================================+\n" +
      "| Cancel       | Automatically cancel all operations heading towards a downed node. |\n" +
      "+--------------+--------------------------------------------------------------------+\n" +
      "| Redistribute | Move on to functional nodes when nodes fail.                       |\n" +
      "+--------------+--------------------------------------------------------------------+\n" +
      "| Retry        | Continue to retry a failing node until it comes back up.           |\n" +
      "+--------------+--------------------------------------------------------------------+";

  public static final String NAGLE_ALGORITHM_ENABLED_CONF = "memcached.nagle.algorithm.enabled";
  static final String NAGLE_ALGORITHM_ENABLED_DOC = "Enables the Nagle algorithm.";
  static final boolean NAGLE_ALGORITHM_ENABLED_DEFAULT = false;

  public static final String RECONNECT_DELAY_MAX_CONF = "memcached.reconnect.delay.max.secs";
  static final String RECONNECT_DELAY_MAX_DOC = "The maximum reconnect delay.";
  static final Long RECONNECT_DELAY_MAX_DEFAULT = 30L;

  public static final String OP_TIME_TIMEOUT_CONF = "memcached.op.time.timeout.msecs";
  static final String OP_TIME_TIMEOUT_DOC = "The default operation timeout in milliseconds.";
  static final Long OP_TIME_TIMEOUT_DEFAULT = -1L;

  public static final String OPTIMIZE_ENABLED_CONF = "memcached.optimize.enable";
  static final String OPTIMIZE_ENABLED_DOC = "Set to false if the default operation optimization is not desirable.";
  static final boolean OPTIMIZE_ENABLED_DEFAULT = true;

  public static final String READ_BUFFER_SIZE_BYTES_CONF = "memcached.read.buffer.bytes";
  public static final String READ_BUFFER_SIZE_BYTES_DOC = "memcached.read.buffer.bytes";
  public static final int READ_BUFFER_SIZE_BYTES_DEFAULT = -1;

  public static final String LOCATOR_TYPE_CONF = "memcached.locator";
  static final String LOCATOR_TYPE_DOC = "The locator type.\n" +
      "+============+======================================================+\n" +
      "| Locator    | Description                                          |\n" +
      "+============+======================================================+\n" +
      "| ARRAY_MOD  | Array modulus - the classic node location algorithm. |\n" +
      "+------------+------------------------------------------------------+\n" +
      "| CONSISTENT | Consistent hash algorithm.                           |\n" +
      "+------------+------------------------------------------------------+\n" +
      "| VBUCKET    | VBucket support.                                     |\n" +
      "+------------+------------------------------------------------------+";
  static final String LOCATOR_TYPE_DEFAULT = ConnectionFactoryBuilder.Locator.ARRAY_MOD.name();

  public static final String DEFAULT_EXPIRATION_SECS_CONF = "memcached.default.expiration.secs";
  static final String DEFAULT_EXPIRATION_SECS_DOC = "The default expiration in seconds.";
  static final int DEFAULT_EXPIRATION_SECS_DEFAULT = 0;

  public static final String HOSTS_CONF = "memcached.hosts";
  static final String HOSTS_DOC = "Memcached hosts to connnect to.";
  static final List<String> HOSTS_DEFAULT = Arrays.asList("localhost:11211");


  public final ConnectionFactoryBuilder.Protocol protocol;
  public final FailureMode failureMode;
  public final boolean nagleAlgorithmEnabled;
  public final boolean optimizeEnabled;
  public final long reconnectDelayMax;
  public final ConnectionFactoryBuilder.Locator locator;
  public final int readBufferSize;
  public final int defaultExpirationSecs;
  public final List<InetSocketAddress> hosts;
  public final long opTimeout;


  public MemcachedSinkConnectorConfig(Map<String, String> settings) {
    super(config(), settings);

    this.protocol = ConfigUtils.getEnum(ConnectionFactoryBuilder.Protocol.class, this, PROTOCOL_CONF);
    this.failureMode = ConfigUtils.getEnum(FailureMode.class, this, FAILURE_MODE_CONF);
    this.nagleAlgorithmEnabled = getBoolean(NAGLE_ALGORITHM_ENABLED_CONF);
    this.reconnectDelayMax = getLong(RECONNECT_DELAY_MAX_CONF);
    this.optimizeEnabled = getBoolean(OPTIMIZE_ENABLED_CONF);
    this.locator = ConfigUtils.getEnum(ConnectionFactoryBuilder.Locator.class, this, LOCATOR_TYPE_CONF);
    this.readBufferSize = getInt(READ_BUFFER_SIZE_BYTES_CONF);
    this.defaultExpirationSecs = getInt(DEFAULT_EXPIRATION_SECS_CONF);
    this.hosts = ConfigUtils.inetSocketAddresses(this, HOSTS_CONF);
    this.opTimeout = getLong(OP_TIME_TIMEOUT_CONF);
  }


  static ConfigDef config() {
    return new ConfigDef()
        .define(HOSTS_CONF, ConfigDef.Type.LIST, HOSTS_DEFAULT, ValidHostnameAndPort.of(), ConfigDef.Importance.HIGH, HOSTS_DOC)
        .define(PROTOCOL_CONF, ConfigDef.Type.STRING, PROTOCOL_DEFAULT, ValidEnum.of(ConnectionFactoryBuilder.Protocol.class), ConfigDef.Importance.LOW, PROTOCOL_DOC)
        .define(FAILURE_MODE_CONF, ConfigDef.Type.STRING, FAILURE_MODE_DEFAULT, ValidEnum.of(FailureMode.class), ConfigDef.Importance.LOW, FAILURE_MODE_DOC)
        .define(NAGLE_ALGORITHM_ENABLED_CONF, ConfigDef.Type.BOOLEAN, NAGLE_ALGORITHM_ENABLED_DEFAULT, ConfigDef.Importance.LOW, NAGLE_ALGORITHM_ENABLED_DOC)
        .define(OPTIMIZE_ENABLED_CONF, ConfigDef.Type.BOOLEAN, OPTIMIZE_ENABLED_DEFAULT, ConfigDef.Importance.LOW, OPTIMIZE_ENABLED_DOC)
        .define(RECONNECT_DELAY_MAX_CONF, ConfigDef.Type.LONG, RECONNECT_DELAY_MAX_DEFAULT, ConfigDef.Range.atLeast(1), ConfigDef.Importance.LOW, RECONNECT_DELAY_MAX_DOC)
        .define(OP_TIME_TIMEOUT_CONF, ConfigDef.Type.LONG, OP_TIME_TIMEOUT_DEFAULT, ConfigDef.Range.atLeast(-1), ConfigDef.Importance.LOW, OP_TIME_TIMEOUT_DOC)
        .define(LOCATOR_TYPE_CONF, ConfigDef.Type.STRING, LOCATOR_TYPE_DEFAULT, ValidEnum.of(ConnectionFactoryBuilder.Locator.class), ConfigDef.Importance.LOW, LOCATOR_TYPE_DOC)
        .define(READ_BUFFER_SIZE_BYTES_CONF, ConfigDef.Type.INT, READ_BUFFER_SIZE_BYTES_DEFAULT, ConfigDef.Range.atLeast(-1), ConfigDef.Importance.LOW, READ_BUFFER_SIZE_BYTES_DOC)
        .define(DEFAULT_EXPIRATION_SECS_CONF, ConfigDef.Type.INT, DEFAULT_EXPIRATION_SECS_DEFAULT, ConfigDef.Range.atLeast(0), ConfigDef.Importance.LOW, DEFAULT_EXPIRATION_SECS_DOC);
  }
}
