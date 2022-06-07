package com.myseotoolbox.resourcequota;


import com.myseotoolbox.resourcequota.model.BucketPersistence;
import com.myseotoolbox.resourcequota.model.BucketState;
import com.myseotoolbox.resourcequota.model.Limit;
import com.myseotoolbox.resourcequota.model.TokenBucket;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;


public class PersistentTokenBucket implements TokenBucket {
    private final Clock clock;
    private final BucketPersistence bucketPersistence;
    private BucketState currentState;

    public PersistentTokenBucket(BucketPersistence bucketPersistence, String bucketKey, long defaultQuantity, Duration defaultDuration, Clock clock) {
        this.bucketPersistence = bucketPersistence;
        this.clock = clock;
        BucketState state = retrieveBucketState(bucketKey)
                .orElse(new BucketState(new Limit(defaultQuantity, defaultDuration), defaultQuantity, clock.instant()));
        this.currentState = state;
    }

    @Override
    public boolean tryConsume(long quantity) {
        Instant requestInstant = clock.instant();
        refill(requestInstant);
        if (currentState.remainingTokens() >= quantity) {
            currentState = recreate(currentState.remainingTokens() - quantity, requestInstant);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public long getRemaining() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    private void refill(Instant requestInstant) {
        Duration durationSinceLastRefill = Duration.between(currentState.lastRefill(), requestInstant);
        if (durationSinceLastRefill.compareTo(currentState.limit().duration()) >= 0) {
            currentState = recreate(currentState.limit().quantity(), requestInstant);
        }
    }

    private BucketState recreate(long quantity, Instant lastRefill) {
        return new BucketState(currentState.limit(), quantity, lastRefill);
    }

    private Optional<BucketState> retrieveBucketState(String bucketKey) {
        if (bucketExist(bucketKey)) {
            return Optional.of(restoreBucket(bucketKey));
        }
        return Optional.empty();
    }

    private BucketState restoreBucket(String bucketKey) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    private boolean bucketExist(String bucketKey) {
        return false;
    }

}
