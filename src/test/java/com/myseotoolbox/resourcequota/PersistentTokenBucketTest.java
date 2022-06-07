package com.myseotoolbox.resourcequota;


import com.myseotoolbox.resourcequota.model.BucketPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PersistentTokenBucketTest {

    PersistentTokenBucket sut;
    Instant testTime = Instant.ofEpochMilli(0);
    private TestClock testClock = new TestClock(testTime);
    @Mock private BucketPersistence bucketPersistence;


    @BeforeEach
    public void setUp() {
        sut = givenResourceQuota().withDailyLimit(10).build();
    }


    @Test
    void shouldAllowToConsumeTokens() {
        assertTrue(sut.tryConsume(1));
    }

    @Test
    public void shouldNotAllowToExceedQuota() {
        assertFalse(sut.tryConsume(11));
    }
    @Test
    public void shouldNotAllowToExceedQuotaWithSubsequentRequests() {
        assertTrue(sut.tryConsume(10));
        assertFalse(sut.tryConsume(1));
    }

    @Test
    public void shouldReplenishQuotaWhenTimeExpires() {
        sut.tryConsume(10);
        setCurrentTimeTo(testTime.plus(1, ChronoUnit.DAYS));
        assertTrue(sut.tryConsume(10));
    }

    @Test
    void shouldNotReplenishEarly() {
        sut.tryConsume(10);
        Instant enoughTime = testTime.plus(1, ChronoUnit.DAYS);
        Instant notEnoughTime = enoughTime.minusMillis(1);
        setCurrentTimeTo(notEnoughTime);
        assertFalse(sut.tryConsume(1));
    }

    @Test
    public void shouldProvideRemainingQuantity() {
        sut.tryConsume(5);
        assertThat(sut.getRemaining(), is(5L));
    }

    @Test
    public void shouldReplenishAvailableWithoutWrite() {
        sut.tryConsume(5);
        setCurrentTimeTo(testTime.plus(1, ChronoUnit.DAYS));
        assertThat(sut.getRemaining(), is(10L));
    }

    @Test
    public void quotaIsNotCumulative() {
        assertThat(sut.getRemaining(), is(10L));
        setCurrentTimeTo(testTime.plus(10, ChronoUnit.DAYS));
        assertThat(sut.getRemaining(), is(10L));
    }

    @Test
    void shouldNotOverrideExistingStateOnPersistentLayer() {
        fail();
    }

    @Test
    void shouldRecoverExistingStateFromPersistentLayer() {

    }

    @Test
    void shouldCreateNewPersistentRecordIfNoStateIsFound() {

    }

    private TimedResourceQuotaBuilder givenResourceQuota() {
        return new TimedResourceQuotaBuilder();
    }

    private class TimedResourceQuotaBuilder {
        private int limit = 0;
        private Duration duration;

        public TimedResourceQuotaBuilder withDailyLimit(int limit) {
            this.duration = Duration.ofDays(1);
            this.limit = limit;
            return this;
        }

        public PersistentTokenBucket build() {
            return new PersistentTokenBucket(bucketPersistence, "test bucket", limit, duration, testClock);
        }
    }

    private void setCurrentTimeTo(Instant instant) {
        System.out.println("Setting local time to: " + instant);
        this.testClock.setTime(instant);
    }

    private static class TestClock extends Clock {

        private Instant currentTime;

        public TestClock(Instant testTime) {
            this.currentTime = testTime;
        }

        public void setTime(Instant newTime) {
            this.currentTime = newTime;
        }

        @Override
        public ZoneId getZone() {
            throw new UnsupportedOperationException("Not implemented yet!");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            throw new UnsupportedOperationException("Not implemented yet!");
        }

        @Override
        public Instant instant() {
            return this.currentTime;
        }
    }
}