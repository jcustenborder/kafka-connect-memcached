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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.spy.memcached.CachedData;
import net.spy.memcached.transcoders.Transcoder;

import java.io.IOException;

class OffsetStateTranscoder implements Transcoder<OffsetState> {
  private static final ObjectMapper MAPPER;
  public static final Transcoder<OffsetState> INSTANCE = new OffsetStateTranscoder();

  private OffsetStateTranscoder() {
  }

  static {
    MAPPER = new ObjectMapper();
  }

  @Override
  public boolean asyncDecode(CachedData cachedData) {
    return false;
  }

  @Override
  public CachedData encode(OffsetState offsetState) {
    try {
      byte[] buffer = MAPPER.writeValueAsBytes(offsetState);
      return new CachedData(0, buffer, CachedData.MAX_SIZE);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Exception thrown while serializing", e);
    }
  }

  @Override
  public OffsetState decode(CachedData cachedData) {
    final byte[] buffer = cachedData.getData();
    try {
      return MAPPER.readValue(buffer, OffsetState.class);
    } catch (IOException e) {
      throw new IllegalStateException("Exception thrown while deserializing", e);
    }
  }

  @Override
  public int getMaxSize() {
    return CachedData.MAX_SIZE;
  }
}
