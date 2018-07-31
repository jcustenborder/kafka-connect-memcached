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

import com.github.jcustenborder.docker.junit5.Compose;
import com.github.jcustenborder.docker.junit5.Port;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTaskContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.github.jcustenborder.kafka.connect.utils.SinkRecordHelper.write;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Compose(dockerComposePath = "src/test/resources/docker-compose.yml")
public class MemcachedSinkTaskTestIT {
  private static final Logger log = LoggerFactory.getLogger(MemcachedSinkTaskTestIT.class);
  private static final String CONTAINER = "memcached";
  private static final int PORT = 11211;

  @BeforeAll
  public static void beforeAll() {
    System.setProperty("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SLF4JLogger");
  }

  MemcachedClient client(InetSocketAddress address) throws IOException {
    ConnectionFactory connectionFactory = new ConnectionFactoryBuilder()
        .build();
    return new MemcachedClient(connectionFactory, Collections.singletonList(address));
  }

  SinkTaskContext context;
  static final TopicPartition TOPIC_PARTITION = new TopicPartition("foo", 1);

  @BeforeEach
  public void before() {
    this.context = mock(SinkTaskContext.class);
    when(this.context.assignment()).thenReturn(ImmutableSet.of(TOPIC_PARTITION));
  }

  @Test
  public void existingOffsets(@Port(container = CONTAINER, internalPort = PORT) InetSocketAddress address) throws IOException, ExecutionException, InterruptedException {
    MemcachedClient client = client(address);
    final String key = MemcachedSinkTask.memcacheOffsetKey(TOPIC_PARTITION);
    client.set(key, 0, OffsetState.of(TOPIC_PARTITION, 12345L), OffsetStateTranscoder.INSTANCE).get();
    MemcachedSinkTask task = new MemcachedSinkTask();
    task.initialize(this.context);
    Map<String, String> settings = settings(address);
    task.start(settings);

  }


  @Test
  public void putWrites(@Port(container = CONTAINER, internalPort = PORT) InetSocketAddress address) throws IOException {
    MemcachedSinkTask task = new MemcachedSinkTask();
    task.initialize(this.context);
    Map<String, String> settings = settings(address);
    task.start(settings);

    Map<String, String> messages = new LinkedHashMap<>(100);

    for (int i = 0; i < 100; i++) {
      String key = String.format("key%s", i);
      messages.put(key, "This is a test");
    }
    List<SinkRecord> records = new ArrayList<>(messages.size());
    for (Map.Entry<String, String> e : messages.entrySet()) {
      records.add(
          write("foo",
              new SchemaAndValue(Schema.STRING_SCHEMA, e.getKey()),
              new SchemaAndValue(Schema.BYTES_SCHEMA, e.getValue().getBytes(Charsets.UTF_8))
          )
      );
    }
    task.put(records);
    MemcachedClient client = client(address);

    for (Map.Entry<String, String> e : messages.entrySet()) {
      final String actual = client.get(e.getKey(), StringTranscoder.INSTANCE);
      assertEquals(e.getValue(), actual);
    }
  }

  private Map<String, String> settings(InetSocketAddress address) {
    return ImmutableMap.of(
        MemcachedSinkConnectorConfig.HOSTS_CONF, String.format("%s:%s", address.getHostString(), address.getPort())
    );
  }

}
