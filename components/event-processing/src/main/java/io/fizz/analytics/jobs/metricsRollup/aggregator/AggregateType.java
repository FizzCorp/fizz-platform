package io.fizz.analytics.jobs.metricsRollup.aggregator;

public class AggregateType {
    public static final int COUNT = (1<<0);
    public static final int SUM = (1<<1);
    public static final int MEAN =(1<<2);
    public static final int MIN = (1<<3);
    public static final int MAX = (1<<4);
    public static final int TYPE_COUNT = 5;
    public static final int ALL = COUNT | SUM | MEAN | MIN | MAX;
}
