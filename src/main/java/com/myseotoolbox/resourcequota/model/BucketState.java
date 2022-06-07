package com.myseotoolbox.resourcequota.model;

import java.time.Instant;

public record BucketState(Limit limit, long remainingTokens, Instant lastRefill) {
}
