package com.myseotoolbox.resourcequota.model;

import java.time.LocalDateTime;

public record UserQuota(String username, String resourceId, Limit limit, long remainingTokens, LocalDateTime lastRefill) {
}
