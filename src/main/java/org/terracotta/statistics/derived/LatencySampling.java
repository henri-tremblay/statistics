/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics.derived;

import org.terracotta.statistics.AbstractSourceStatistic;
import org.terracotta.statistics.jsr166e.ThreadLocalRandom;
import org.terracotta.statistics.observer.EventObserver;
import org.terracotta.statistics.observer.OperationObserver;

import static org.terracotta.statistics.Time.time;

/**
 *
 * @author cdennis
 */
public class LatencySampling<T extends Enum<T>> extends AbstractSourceStatistic<EventObserver> implements OperationObserver<T> {

  private final ThreadLocal<Long> operationStartTime = new ThreadLocal<Long>();
  private final T targetOperation;
  private final int ceiling;
  
  public LatencySampling(T target, double sampling) {
    if (sampling > 1.0 || sampling < 0.0) {
      throw new IllegalArgumentException();
    }
    this.ceiling = (int) (Integer.MAX_VALUE * sampling);
    this.targetOperation = target;
  }

  @Override
  public void begin() {
    if (sample()) {
      operationStartTime.set(time());
    }
  }

  @Override
  public void end(T result) {
    if (targetOperation.equals(result)) {
      Long start  = operationStartTime.get();
      if (start != null) {
        long latency = time() - start.longValue();
        for (EventObserver observer : derived()) {
          observer.event(latency);
        }
      }
    }
    operationStartTime.remove();
  }

  @Override
  public void end(T result, long ... parameters) {
    end(result);
  }
  
  private boolean sample() {
    return ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE) < ceiling;
  }
}
