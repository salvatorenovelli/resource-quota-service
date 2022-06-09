package com.myseotoolbox.tokenbucket;

import com.github.simpletokenbucket.Limit;

public record ResourceQuota(String resourceId, Limit defaultLimit) {
}
