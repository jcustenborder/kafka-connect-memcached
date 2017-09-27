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

import com.github.jcustenborder.kafka.connect.utils.VersionUtil;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.MemcachedClientIF;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.errors.RetriableException;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class MemcachedSinkConnectorTask extends SinkTask {
  private static final Logger log = LoggerFactory.getLogger(MemcachedSinkConnectorTask.class);
  MemcachedClientIF client;

  @Override
  public String version() {
    return VersionUtil.version(this.getClass());
  }

  MemcachedSinkConnectorConfig config;

  @Override
  public void start(Map<String, String> map) {
    this.config = new MemcachedSinkConnectorConfig(map);
    ConnectionFactory connectionFactory = new ConnectionFactoryBuilder()
        .setProtocol(this.config.protocol)
        .setFailureMode(this.config.failureMode)
        .setUseNagleAlgorithm(this.config.nagleAlgorithmEnabled)
        .setShouldOptimize(this.config.optimizeEnabled)
        .setMaxReconnectDelay(this.config.reconnectDelayMax)
        .setLocatorType(this.config.locator)
        .setReadBufferSize(this.config.readBufferSize)
        .setTranscoder(new ByteArrayTranscoder())
        .build();
    try {
      this.client = new MemcachedClient(connectionFactory, this.config.hosts);
    } catch (IOException e) {
      throw new ConnectException(
          "Exception thrown while creating client",
          e
      );
    }
  }

  @Override
  public void put(Collection<SinkRecord> records) {

    List<Future<Boolean>> operations = new ArrayList<>(records.size());

    for (SinkRecord record : records) {
      Preconditions.checkState(
          record.key() instanceof String,
          "record.key() must be a String. Use a transform to convert other data types."
      );

      if (null != record.value()) {
        Preconditions.checkState(
            record.value() instanceof byte[],
            "record.value() must be a byte[]. Use a transform to convert other data types."
        );
      }

      final String key = (String) record.key();
      final byte[] value = (byte[]) record.value();
      final Future<Boolean> operation;

      if (null != value) {
        log.trace("put() - Processing set for key '{}'", key);
        operation = this.client.set(key, this.config.defaultExpirationSecs, value);
      } else {
        log.trace("put() - Processing delete for key '{}'", key);
        operation = this.client.delete(key);
      }
      operations.add(operation);
    }

    try {
      for (Future<Boolean> operation : operations) {
        final boolean status = Futures.get(operation, this.config.opTimeout * 2, TimeUnit.MILLISECONDS, Exception.class);
      }
    } catch (Exception ex) {
      throw new RetriableException(ex);
    }
  }

  @Override
  public void stop() {
    this.client.shutdown();
  }
}
