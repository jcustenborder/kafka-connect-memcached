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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import org.apache.kafka.common.TopicPartition;


public class OffsetState {
  @JsonProperty("topic")
  private String topic;
  @JsonProperty("partition")
  private Integer partition;
  @JsonProperty("offset")
  private Long offset;

  public OffsetState() {
  }

  public String topic() {
    return this.topic;
  }

  public Integer partition() {
    return this.partition;
  }

  public Long offset() {
    return this.offset;
  }

  public OffsetState(String topic, Integer partition, Long offset) {
    this.topic = topic;
    this.partition = partition;
    this.offset = offset;
  }

  public TopicPartition topicPartition() {
    return new TopicPartition(this.topic, this.partition);
  }

  public static OffsetState of(TopicPartition topicPartition, long offset) {
    return new OffsetState(topicPartition.topic(), topicPartition.partition(), offset);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("topic", this.topic)
        .add("partition", this.partition)
        .add("offset", this.offset)
        .toString();
  }
}
