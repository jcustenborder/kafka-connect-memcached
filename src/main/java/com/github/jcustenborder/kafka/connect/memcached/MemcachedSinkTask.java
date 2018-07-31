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
import com.github.jcustenborder.kafka.connect.utils.data.TopicPartitionCounter;
import com.google.common.base.Preconditions;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.MemcachedClientIF;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.errors.RetriableException;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class MemcachedSinkTask extends SinkTask {
  static {
    System.setProperty("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SLF4JLogger");
  }

  private static final Logger log = LoggerFactory.getLogger(MemcachedSinkTask.class);
  MemcachedClientIF client;

  @Override
  public String version() {
    return VersionUtil.version(this.getClass());
  }

  MemcachedSinkConnectorConfig config;

  @Override
  public void start(Map<String, String> map) {
    this.config = new MemcachedSinkConnectorConfig(map);
    log.info("Creating Memcached Client");
    final ConnectionFactory connectionFactory = new ConnectionFactoryBuilder()
        .setProtocol(this.config.protocol)
        .setFailureMode(this.config.failureMode)
        .setUseNagleAlgorithm(this.config.nagleAlgorithmEnabled)
        .setShouldOptimize(this.config.optimizeEnabled)
        .setMaxReconnectDelay(this.config.reconnectDelayMax)
        .setLocatorType(this.config.locator)
        .setReadBufferSize(this.config.readBufferSize)
        .build();
    try {
      this.client = new MemcachedClient(connectionFactory, this.config.hosts);
    } catch (IOException e) {
      throw new ConnectException(
          "Exception thrown while creating client",
          e
      );
    }

    log.info("Checking memcached for existing offsets.");
    Set<String> partitionKeys = this.context.assignment().stream()
        .map(MemcachedSinkTask::memcacheOffsetKey)
        .collect(Collectors.toSet());
    Map<String, OffsetState> lastOffsets = this.client.getBulk(partitionKeys, OffsetStateTranscoder.INSTANCE);
    if (!lastOffsets.isEmpty()) {
      Map<TopicPartition, Long> offsets = lastOffsets.values().stream()
          .collect(Collectors.toMap(OffsetState::topicPartition, OffsetState::offset));
      for (Map.Entry<TopicPartition, Long> e : offsets.entrySet()) {
        log.info("Requesting offset {} for {}:{}", e.getValue(), e.getKey().topic(), e.getKey().partition());
      }
      this.context.offset(offsets);
    } else {
      Map<TopicPartition, Long> offsets = this.context.assignment().stream()
          .map(e -> new AbstractMap.SimpleEntry<>(new TopicPartition(e.topic(), e.partition()), 0L))
          .collect(
              Collectors.toMap(
                  Map.Entry::getKey,
                  Map.Entry::getValue
              )
          );
      for (Map.Entry<TopicPartition, Long> e : offsets.entrySet()) {
        log.info("Requesting offset {} for {}:{}", e.getValue(), e.getKey().topic(), e.getKey().partition());
      }
      this.context.offset(offsets);
    }
  }


  @Override
  public void put(Collection<SinkRecord> records) {

    TopicPartitionCounter counter = new TopicPartitionCounter();
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
        operation = this.client.set(key, this.config.defaultExpirationSecs, value, ByteArrayTranscoder.INSTANCE);
      } else {
        log.trace("put() - Processing delete for key '{}'", key);
        operation = this.client.delete(key);
      }

      handleOperation(operation);
      counter.increment(record.topic(), record.kafkaPartition(), record.kafkaOffset());
    }

    final Map<TopicPartition, Long> data = counter.data();

    for (Map.Entry<TopicPartition, Long> e : data.entrySet()) {
      final TopicPartition topicPartition = e.getKey();
      final Long offset = e.getValue();
      final String key = memcacheOffsetKey(topicPartition);
      final OffsetState state = OffsetState.of(topicPartition, offset);
      log.trace("put() - Setting {} to {}", key, offset);
      log.debug("put() - Setting offset for topic partition {} to {}", topicPartition, offset);
      Future<Boolean> operation = this.client.set(key, 0, state, OffsetStateTranscoder.INSTANCE);
      handleOperation(operation);
    }

    this.context.requestCommit();
  }

  static String memcacheOffsetKey(TopicPartition topicPartition) {
    return String.format("__kafka.offset.%s.%s", topicPartition.topic(), topicPartition.partition());
  }

  private void handleOperation(Future<Boolean> operation) {
    try {
      operation.get(this.config.opTimeout, TimeUnit.MILLISECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      log.error("Exception thrown while writing to memcached.", e);
      operation.cancel(true);
      throw new RetriableException(e);
    }
  }

  @Override
  public void stop() {
    this.client.shutdown();
  }
}
