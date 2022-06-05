package com.myseotoolbox.resourcequota;

public interface ResourceQuota {
    boolean tryConsume(int quantity);

    long getRemaining();
}
