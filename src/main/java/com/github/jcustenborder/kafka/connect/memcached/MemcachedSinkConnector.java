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
import com.github.jcustenborder.kafka.connect.utils.config.Description;
import com.github.jcustenborder.kafka.connect.utils.config.DocumentationNote;
import com.github.jcustenborder.kafka.connect.utils.config.TaskConfigs;
import com.github.jcustenborder.kafka.connect.utils.config.Title;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;

import java.util.List;
import java.util.Map;

@Title("Memcached Sink")
@Description("The Memcached Sink provides a :term:`Sink Connector` that can write data in real time to a memcached " +
    "environment.")
@DocumentationNote("This connector expects that the key will be a string and the value will be a byte representation of " +
    "the message. Your data might not be formatted like this. Take a look at transformations to apply the convert the " +
    "data to the proper format.")
public class MemcachedSinkConnector extends SinkConnector {
  @Override
  public String version() {
    return VersionUtil.version(this.getClass());
  }

  MemcachedSinkConnectorConfig config;
  Map<String, String> settings;

  @Override
  public void start(Map<String, String> settings) {
    this.config = new MemcachedSinkConnectorConfig(settings);
    this.settings = settings;
  }

  @Override
  public Class<? extends Task> taskClass() {
    return MemcachedSinkTask.class;
  }

  @Override
  public List<Map<String, String>> taskConfigs(int taskCount) {
    return TaskConfigs.multiple(this.settings, taskCount);
  }

  @Override
  public void stop() {

  }

  @Override
  public ConfigDef config() {
    return MemcachedSinkConnectorConfig.config();
  }
}
