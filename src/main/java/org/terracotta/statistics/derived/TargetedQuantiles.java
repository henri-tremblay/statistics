/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics.derived;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Executor;

import org.terracotta.statistics.ValueStatistic;
import org.terracotta.statistics.observer.EventObserver;
import org.terracotta.statistics.util.InThreadExecutor;

/**
 *
 * @author cdennis
 */
public class TargetedQuantiles implements EventObserver {

  private final Executor executor;
  private final Quantiles quantiles;
  
  public TargetedQuantiles(Quantile ... quantiles) {
    this(InThreadExecutor.INSTANCE, quantiles);
  }
  
  public TargetedQuantiles(Executor executor, Quantile ... quantiles) {
    this.executor = executor;
    this.quantiles = new Quantiles(quantiles);
  }

  @Override
  public void event(long parameter) {
    quantiles.insert(new long[] {parameter});
  }

  public long quantile(double quantile) {
    return quantiles.query(quantile);
  }
  
  public ValueStatistic<Long> quantileStatistic(final double quantile) {
    return new ValueStatistic<Long>() {

      @Override
      public Long value() {
        return quantile(quantile);
      }
    };
  }
  
  @Override
  public String toString() {
    return quantiles.toString();
  }
  
  static class Quantiles extends CompressedQuantiles {
    private final Collection<Quantile> quantiles;

    public Quantiles(Quantile ... quantiles) {
      this.quantiles = Arrays.asList(quantiles);
    }

    @Override
    protected long allowableError(long r, long n) {
      long allowable = Long.MAX_VALUE;
      for (Quantile q : quantiles) {
        allowable = Math.min(allowable, (long) Math.floor(q.allowableError(r, n)));
      }
      
      return Math.max(allowable, 1L);
    }
  }
  
  static Quantile quantile(double phi, double epsilon) {
    return new Quantile(phi, epsilon);
  }
  
  public static class Quantile {
    
    private final double phi;
    private final double epsilon;

    private Quantile(double phi, double epsilon) {
      if (phi < 0.0 || phi > 1.0) {
        throw new IllegalArgumentException("Quantile must in the range [0, 1]");
      }
      if (epsilon < 0.0 || epsilon > 1.0) {
        throw new IllegalArgumentException("Fractional error must in the range [0, 1]");
      }
      this.phi = phi;
      this.epsilon = epsilon;
    }
    
    public double quantile() {
      return phi;
    }
    
    public double error() {
      return epsilon;
    }
    
    private double allowableError(long r, long n) {
      if (r <= phi * n) {
        return epsilon * 2.0 * (n - r) / (1 - phi);
      } else {
        return epsilon * 2.0 * r / phi;
      }
    }
  }
}