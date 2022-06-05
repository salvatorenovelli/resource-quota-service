package com.myseotoolbox.resourcequota;


import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.TimeMeter;

import java.time.Clock;
import java.time.Duration;

/**
 * Allow users to interact with the crawler within a certain quota
 */
public class TimedResourceQuota implements ResourceQuota {

    private final Bucket bucket;

    public TimedResourceQuota(int limit, Duration duration, Clock clock) {
        Bandwidth bw = Bandwidth.simple(limit, duration);
        TimeMeter customTimeMeter = getClockBasedTimeMeter(clock);
        bucket = Bucket.builder()
                .addLimit(bw)
                .withCustomTimePrecision(customTimeMeter)
                .build();
    }

    @Override
    public boolean tryConsume(int quantity) {
        return bucket.tryConsume(quantity);
    }

    @Override
    public long getRemaining() {
        return bucket.getAvailableTokens();
    }

    private TimeMeter getClockBasedTimeMeter(Clock clock) {
        return new TimeMeter() {
            @Override
            public long currentTimeNanos() {
                return clock.millis() * 1000000;
            }

            @Override
            public boolean isWallClockBased() {
                return false;
            }
        };
    }
}
