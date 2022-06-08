package com.myseotoolbox.resourcequota;


import com.myseotoolbox.resourcequota.model.BucketPersistence;
import com.myseotoolbox.resourcequota.model.BucketState;
import com.myseotoolbox.resourcequota.model.Limit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PersistentTokenBucketTest {

    private static final Limit DEFAULT_QUOTA_LIMIT = new Limit(10, Duration.ofDays(1));
    public static final String TEST_BUCKET_KEY = "crawler.maxCrawlCount";
    Instant testTime = Instant.ofEpochMilli(0);
    private TestClock testClock = new TestClock(testTime);

    @Mock
    private BucketPersistence bucketPersistence;

    PersistentTokenBucket sut;


    @BeforeEach
    public void setUp() {
        sut = givenTokenBucket().withLimit(DEFAULT_QUOTA_LIMIT).build();
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
    void shouldRecoverExistingRemainingTokens() {
        giveExistingBucketStateForKey(TEST_BUCKET_KEY)
                .withLimit(DEFAULT_QUOTA_LIMIT)
                .withRemainingTokens(2)
                .lastRefill(testTime)
                .build();

        sut = givenTokenBucket().withLimit(DEFAULT_QUOTA_LIMIT).build();
        assertThat(sut.getRemaining(), is(2L));
    }

    @Test
    void shouldRecoverExistingLastRefill() {
        giveExistingBucketStateForKey(TEST_BUCKET_KEY)
                .withLimit(DEFAULT_QUOTA_LIMIT)
                .withRemainingTokens(2)
                .lastRefill(testTime)
                .build();

        sut = givenTokenBucket().withLimit(DEFAULT_QUOTA_LIMIT).build();

        assertThat(sut.getRemaining(), is(2L));
        setCurrentTimeTo(testTime.plus(1, ChronoUnit.DAYS));
        assertThat(sut.getRemaining(), is(10L));
    }

    @Test
    @DisplayName("Existing state on loading should remain the same, in the persistent layer")
    void shouldNotOverrideExistingStateOnPersistentLayerOnLoading() {

    }

    @Test
    void shouldNotPersistIfNoStateChange() {
        giveExistingBucketStateForKey(TEST_BUCKET_KEY)
                .build();

        sut = givenTokenBucket().withLimit(DEFAULT_QUOTA_LIMIT).build();

        sut.getRemaining();
        verify(bucketPersistence, never()).save(any());
    }

    @Test
    void shouldCreateNewPersistentRecordIfNoStateIsFound() {
        //given no existing bucket in persistent layer

        //token is created in @BeforeAll

        verify(bucketPersistence, times(1)).save(new BucketState(DEFAULT_QUOTA_LIMIT, DEFAULT_QUOTA_LIMIT.quantity(), testTime));
    }

    @Test
    void shouldUpdateWhenStateChanges() {

        sut = givenTokenBucket().withLimit(DEFAULT_QUOTA_LIMIT).build();

        setCurrentTimeTo(testTime.plus(30, ChronoUnit.SECONDS));
        sut.tryConsume(1);

        verify(bucketPersistence, times(1)).save(
                new BucketState(DEFAULT_QUOTA_LIMIT, DEFAULT_QUOTA_LIMIT.quantity() - 1, testTime.plusSeconds(30))
        );
    }

    private BucketTestStateBuilder giveExistingBucketStateForKey(String bucketKey) {
        return new BucketTestStateBuilder(bucketKey);
    }

    private TimedResourceQuotaBuilder givenTokenBucket() {
        return new TimedResourceQuotaBuilder();
    }

    private class TimedResourceQuotaBuilder {
        private Limit limit;

        public TimedResourceQuotaBuilder withLimit(Limit limit) {
            this.limit = limit;
            return this;
        }

        public PersistentTokenBucket build() {
            return new PersistentTokenBucket(bucketPersistence, TEST_BUCKET_KEY, limit.quantity(), limit.duration(), testClock);
        }
    }

    private void setCurrentTimeTo(Instant instant) {
        System.out.println("Setting local time to: " + instant);
        this.testClock.setTime(instant);
    }

    private class BucketTestStateBuilder {
        private final String bucketKey;
        private int remainingTokens = 2;
        private Instant lastRefill = testTime;
        private Limit limit = DEFAULT_QUOTA_LIMIT;

        private BucketTestStateBuilder(String bucketKey) {
            this.bucketKey = bucketKey;
        }

        public BucketTestStateBuilder withRemainingTokens(int remainingTokens) {
            this.remainingTokens = remainingTokens;
            return this;
        }

        public BucketTestStateBuilder lastRefill(Instant lastRefill) {
            this.lastRefill = lastRefill;
            return this;
        }

        public BucketTestStateBuilder withLimit(Limit limit) {
            this.limit = limit;
            return this;
        }

        public void build() {
            Mockito.when(bucketPersistence.getBucketState(bucketKey))
                    .thenReturn(
                            Optional.of(new BucketState(limit, remainingTokens, lastRefill))
                    );
        }
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