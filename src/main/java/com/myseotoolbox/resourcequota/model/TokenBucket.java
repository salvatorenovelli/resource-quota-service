package com.myseotoolbox.resourcequota.model;

public interface TokenBucket {
    boolean tryConsume(long quantity);

    long getRemaining();
}
