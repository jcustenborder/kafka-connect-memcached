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

import com.github.jcustenborder.kafka.connect.utils.config.ConfigKeyBuilder;
import com.github.jcustenborder.kafka.connect.utils.config.ConfigUtils;
import com.github.jcustenborder.kafka.connect.utils.config.ValidEnum;
import com.github.jcustenborder.kafka.connect.utils.config.ValidHostnameAndPort;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.FailureMode;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

class MemcachedSinkConnectorConfig extends AbstractConfig {

  public static final String PROTOCOL_CONF = "memcached.protocol";
  static final String PROTOCOL_DOC = "Specify the protocol to use. " +
      ConfigUtils.enumDescription(
          ImmutableMap.of(
              ConnectionFactoryBuilder.Protocol.BINARY, "Use the binary protocol.",
              ConnectionFactoryBuilder.Protocol.TEXT, "Use the text (ascii) protocol."
          )
      );
  static final String PROTOCOL_DEFAULT = ConnectionFactoryBuilder.Protocol.BINARY.name();

  public static final String HASH_ALGORITHM_CONF = "memcached.hash.algorithm";
  static final String HASH_ALGORITHM_DOC = "Set the hash algorithm.";
//  static final String HASH_ALGORITHM_DEFAULT = HashAlgorithm;

  public static final String FAILURE_MODE_CONF = "memcached.failure.mode";
  static final String FAILURE_MODE_DEFAULT = FailureMode.Retry.toString();
  static final String FAILURE_MODE_DOC = "Set the failure mode. " +
      ConfigUtils.enumDescription(
          ImmutableMap.of(
              FailureMode.Cancel, "Automatically cancel all operations heading towards a downed node.",
              FailureMode.Redistribute, "Move on to functional nodes when nodes fail.",
              FailureMode.Retry, "Continue to retry a failing node until it comes back up."
          )
      );

  public static final String NAGLE_ALGORITHM_ENABLED_CONF = "memcached.nagle.algorithm.enabled";
  static final String NAGLE_ALGORITHM_ENABLED_DOC = "Enables the Nagle algorithm.";
  static final boolean NAGLE_ALGORITHM_ENABLED_DEFAULT = false;

  public static final String RECONNECT_DELAY_MAX_CONF = "memcached.reconnect.delay.max.secs";
  static final String RECONNECT_DELAY_MAX_DOC = "The maximum reconnect delay.";
  static final Long RECONNECT_DELAY_MAX_DEFAULT = 30L;

  public static final String OP_TIME_TIMEOUT_CONF = "memcached.op.time.timeout.msecs";
  static final String OP_TIME_TIMEOUT_DOC = "The default operation timeout in milliseconds.";
  static final Long OP_TIME_TIMEOUT_DEFAULT = TimeUnit.SECONDS.toMillis(30);

  public static final String OPTIMIZE_ENABLED_CONF = "memcached.optimize.enable";
  static final String OPTIMIZE_ENABLED_DOC = "Set to false if the default operation optimization is not desirable.";
  static final boolean OPTIMIZE_ENABLED_DEFAULT = true;

  public static final String READ_BUFFER_SIZE_BYTES_CONF = "memcached.read.buffer.bytes";
  public static final String READ_BUFFER_SIZE_BYTES_DOC = "memcached.read.buffer.bytes";
  public static final int READ_BUFFER_SIZE_BYTES_DEFAULT = -1;

  public static final String LOCATOR_TYPE_CONF = "memcached.locator";
  static final String LOCATOR_TYPE_DOC = "The locator type. " +
      ConfigUtils.enumDescription(
          ImmutableMap.of(
              ConnectionFactoryBuilder.Locator.ARRAY_MOD, "The classic node location algorithm.",
              ConnectionFactoryBuilder.Locator.CONSISTENT, "Consistent hash algorithm.",
              ConnectionFactoryBuilder.Locator.VBUCKET, "VBucket support."

          )
      );
  static final String LOCATOR_TYPE_DEFAULT = ConnectionFactoryBuilder.Locator.ARRAY_MOD.name();

  public static final String DEFAULT_EXPIRATION_SECS_CONF = "memcached.default.expiration.secs";
  static final String DEFAULT_EXPIRATION_SECS_DOC = "The default expiration in seconds.";
  static final int DEFAULT_EXPIRATION_SECS_DEFAULT = 0;

  public static final String HOSTS_CONF = "memcached.hosts";
  static final String HOSTS_DOC = "Memcached hosts to connect to.";
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

  static class EnumRecommender implements ConfigDef.Recommender {
    final Set<String> validEnums;
    final Class<?> enumClass;

    public static EnumRecommender of(Class<?> enumClass, String... excludes) {
      return new EnumRecommender(enumClass, excludes);
    }

    private EnumRecommender(Class<?> enumClass, String... excludes) {
      Preconditions.checkNotNull(enumClass, "enumClass cannot be null");
      Preconditions.checkState(enumClass.isEnum(), "enumClass must be an enum.");
      Set<String> validEnums = new LinkedHashSet();
      Object[] var4 = enumClass.getEnumConstants();
      int var5 = var4.length;

      for (int var6 = 0; var6 < var5; ++var6) {
        Object o = var4[var6];
        String key = o.toString();
        validEnums.add(key);
      }

      validEnums.removeAll(Arrays.asList(excludes));
      this.validEnums = validEnums;
      this.enumClass = enumClass;
    }

    @Override
    public List<Object> validValues(String s, Map<String, Object> map) {
      return this.validEnums.stream().collect(Collectors.toList());
    }

    @Override
    public boolean visible(String s, Map<String, Object> map) {
      return true;
    }
  }

  static final String GROUP_CONNNECTION = "Connection";

  static ConfigDef config() {
    return new ConfigDef()
        .define(
            ConfigKeyBuilder.of(HOSTS_CONF, ConfigDef.Type.LIST)
                .group(GROUP_CONNNECTION)
                .displayName("Hosts")
                .defaultValue(HOSTS_DEFAULT)
                .validator(ValidHostnameAndPort.of())
                .importance(ConfigDef.Importance.HIGH)
                .documentation(HOSTS_DOC)
                .build()
        )
        .define(
            ConfigKeyBuilder.of(PROTOCOL_CONF, ConfigDef.Type.STRING)
                .group(GROUP_CONNNECTION)
                .displayName("Protocol")
                .defaultValue(PROTOCOL_DEFAULT)
                .validator(ValidEnum.of(ConnectionFactoryBuilder.Protocol.class))
                .importance(ConfigDef.Importance.LOW)
                .documentation(PROTOCOL_DOC)
                .recommender(EnumRecommender.of(ConnectionFactoryBuilder.Protocol.class))
                .build()
        )
        .define(
            ConfigKeyBuilder.of(FAILURE_MODE_CONF, ConfigDef.Type.STRING)
                .group(GROUP_CONNNECTION)
                .displayName("Failure mode")
                .defaultValue(FAILURE_MODE_DEFAULT)
                .validator(ValidEnum.of(FailureMode.class))
                .importance(ConfigDef.Importance.LOW)
                .documentation(FAILURE_MODE_DOC)
                .recommender(EnumRecommender.of(FailureMode.class))
                .build()
        )
        .define(
            ConfigKeyBuilder.of(NAGLE_ALGORITHM_ENABLED_CONF, ConfigDef.Type.BOOLEAN)
                .group(GROUP_CONNNECTION)
                .displayName("Nagle algorithm")
                .defaultValue(NAGLE_ALGORITHM_ENABLED_DEFAULT)
                .importance(ConfigDef.Importance.LOW)
                .documentation(NAGLE_ALGORITHM_ENABLED_DOC)
                .build()
        )
        .define(
            ConfigKeyBuilder.of(OPTIMIZE_ENABLED_CONF, ConfigDef.Type.BOOLEAN)
                .group(GROUP_CONNNECTION)
                .displayName("Optimize")
                .defaultValue(OPTIMIZE_ENABLED_DEFAULT)
                .importance(ConfigDef.Importance.LOW)
                .documentation(OPTIMIZE_ENABLED_DOC)
                .build()
        )
        .define(
            ConfigKeyBuilder.of(RECONNECT_DELAY_MAX_CONF, ConfigDef.Type.LONG)
                .group(GROUP_CONNNECTION)
                .displayName("Reconnect delay")
                .defaultValue(RECONNECT_DELAY_MAX_DEFAULT)
                .importance(ConfigDef.Importance.LOW)
                .documentation(RECONNECT_DELAY_MAX_DOC)
                .validator(ConfigDef.Range.atLeast(1))
                .build()
        )
        .define(
            ConfigKeyBuilder.of(OP_TIME_TIMEOUT_CONF, ConfigDef.Type.LONG)
                .group(GROUP_CONNNECTION)
                .displayName("Operation timeout")
                .defaultValue(OP_TIME_TIMEOUT_DEFAULT)
                .importance(ConfigDef.Importance.LOW)
                .documentation(OP_TIME_TIMEOUT_DOC)
                .validator(ConfigDef.Range.atLeast(-1))
                .build()
        )
        .define(
            ConfigKeyBuilder.of(LOCATOR_TYPE_CONF, ConfigDef.Type.STRING)
                .group(GROUP_CONNNECTION)
                .displayName("Locator type")
                .defaultValue(LOCATOR_TYPE_DEFAULT)
                .validator(ValidEnum.of(ConnectionFactoryBuilder.Locator.class))
                .importance(ConfigDef.Importance.LOW)
                .documentation(LOCATOR_TYPE_DOC)
                .recommender(EnumRecommender.of(ConnectionFactoryBuilder.Locator.class))
                .build()
        )
        .define(
            ConfigKeyBuilder.of(READ_BUFFER_SIZE_BYTES_CONF, ConfigDef.Type.INT)
                .group(GROUP_CONNNECTION)
                .displayName("Read buffer size")
                .defaultValue(READ_BUFFER_SIZE_BYTES_DEFAULT)
                .validator(ConfigDef.Range.atLeast(-1))
                .importance(ConfigDef.Importance.LOW)
                .documentation(READ_BUFFER_SIZE_BYTES_DOC)
                .build()
        )
        .define(
            ConfigKeyBuilder.of(DEFAULT_EXPIRATION_SECS_CONF, ConfigDef.Type.INT)
                .group(GROUP_CONNNECTION)
                .displayName("Default expiration")
                .defaultValue(DEFAULT_EXPIRATION_SECS_DEFAULT)
                .validator(ConfigDef.Range.atLeast(0))
                .importance(ConfigDef.Importance.LOW)
                .documentation(DEFAULT_EXPIRATION_SECS_DOC)
                .build()
        );
  }
}
