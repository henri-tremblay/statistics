/*
 * All content copyright Terracotta, Inc., unless otherwise indicated.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terracotta.statistics;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.terracotta.context.annotations.ContextAttribute;

@ContextAttribute(value="this")
class PassThroughStatistic<T extends Number> implements ValueStatistic<T> {

  @ContextAttribute("name") public final String name;
  @ContextAttribute("tags") public final Set<String> tags;
  @ContextAttribute("properties") public final Map<String, Object> properties;
  private final Callable<T> source;
  
  public PassThroughStatistic(Object context, String name, Set<String> tags, Map<String, ? extends Object> properties, Callable<T> source) {
    this.name = name;
    this.tags = Collections.unmodifiableSet(new HashSet<String>(tags));
    this.properties = Collections.unmodifiableMap(new HashMap<String, Object>(properties));
    this.source = source;
  }

  @Override
  public T value() {
    try {
      return source.call();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
