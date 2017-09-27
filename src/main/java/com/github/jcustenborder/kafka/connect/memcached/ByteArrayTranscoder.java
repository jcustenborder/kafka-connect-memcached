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

import net.spy.memcached.CachedData;
import net.spy.memcached.transcoders.Transcoder;

class ByteArrayTranscoder implements Transcoder<Object> {
  @Override
  public boolean asyncDecode(CachedData cachedData) {
    return true;
  }

  @Override
  public CachedData encode(Object input) {
    return new CachedData(0, (byte[]) input, CachedData.MAX_SIZE);
  }

  @Override
  public Object decode(CachedData cachedData) {
    return cachedData.getData();
  }


  @Override
  public int getMaxSize() {
    return CachedData.MAX_SIZE;
  }
}