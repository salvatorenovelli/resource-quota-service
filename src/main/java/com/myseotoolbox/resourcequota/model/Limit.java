package com.myseotoolbox.resourcequota.model;

import java.time.Duration;

public record Limit(long quantity, Duration duration) { }