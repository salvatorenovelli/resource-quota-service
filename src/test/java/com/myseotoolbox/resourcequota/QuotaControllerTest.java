package com.myseotoolbox.resourcequota;


import com.myseotoolbox.quota4j.QuotaService;
import com.myseotoolbox.quota4j.model.Quota;
import com.myseotoolbox.quota4j.model.QuotaState;
import com.myseotoolbox.quota4j.model.QuotaStateId;
import com.myseotoolbox.quota4j.quotamanager.quantityovertime.QuantityOverTimeLimit;
import com.myseotoolbox.quota4j.quotamanager.quantityovertime.QuantityOverTimeQuotaManager;
import com.myseotoolbox.quota4j.quotamanager.quantityovertime.QuantityOverTimeState;
import com.myseotoolbox.resourcequota.persistence.QuotaRepository;
import com.myseotoolbox.resourcequota.persistence.QuotaStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
class QuotaControllerTest {

    private static final String NON_EXISTING_RESOURCE_ID = "crawler.non_existing_resource";
    public static final String OWNER_ID = "123456";
    public static final String RESOURCE_ID = "crawler.crawls_per_day";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    QuotaStateRepository quotaStateRepository;
    @Autowired
    QuotaRepository quotaRepository;

    @BeforeEach
    void setUp() {
        quotaStateRepository.deleteAll();
        quotaRepository.deleteAll();
    }

    @Autowired
    QuotaService quotaService;

    TestClock testClock = new TestClock();

    @Test
    void shouldAllowAcquireIfQuotaIsAvailable() throws Exception {
        givenQuota(RESOURCE_ID)
                .withRemainingTokens(10)
                .build();

        mockMvc.perform(post("/resource/" + RESOURCE_ID + "/owner/" + OWNER_ID + "/acquire"))
                .andExpect(status().is(OK.value()))
                .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    void shouldDeclineIfQuotaIsExhausted() throws Exception {
        givenQuota(RESOURCE_ID)
                .withRemainingTokens(0)
                .build();

        mockMvc.perform(post("/resource/" + RESOURCE_ID + "/owner/" + OWNER_ID + "/acquire"))
                .andExpect(status().is(OK.value()))
                .andExpect(jsonPath("$.result").value(false));
    }

    @Test
    void shouldReportAvailableRemainingWithEveryRequest() throws Exception {
        givenQuota(RESOURCE_ID)
                .withRemainingTokens(10)
                .build();

        mockMvc.perform(post("/resource/" + RESOURCE_ID + "/owner/" + OWNER_ID + "/acquire"))
                .andExpect(status().is(OK.value()))
                .andDo(print())
                .andExpect(jsonPath("$.state.available").value(9));
    }

    @Test
    void shouldBeAbleToAcquireMoreThan1() throws Exception {
        givenQuota(RESOURCE_ID)
                .withRemainingTokens(10)
                .build();

        mockMvc.perform(post("/resource/" + RESOURCE_ID + "/owner/" + OWNER_ID + "/acquire?quantity=5"))
                .andExpect(status().is(OK.value()))
                .andDo(print())
                .andExpect(jsonPath("$.state.available").value(5));
    }

    @Test
    void shouldHandleQuotaNotFound() throws Exception {
        mockMvc.perform(post("/resource/" + NON_EXISTING_RESOURCE_ID + "/owner/" + OWNER_ID + "/acquire"))
                .andExpect(status().is(NOT_FOUND.value()))
                .andDo(print())
                .andExpect(content().string(containsString(NON_EXISTING_RESOURCE_ID)));
    }

    @Test
    void shouldHandleQuotaManagerNotFound() throws Exception {
        givenQuota(RESOURCE_ID)
                .withoutRegisteredQuotaManager()
                .build();

        mockMvc.perform(post("/resource/" + RESOURCE_ID + "/owner/" + OWNER_ID + "/acquire"))
                .andExpect(status().is(NOT_IMPLEMENTED.value()))
                .andExpect(content().string(containsString("Quota manager not registered")));
    }

    @Test
    void shouldReturnState() throws Exception {
        givenQuota(RESOURCE_ID)
                .withRemainingTokens(6)
                .build();

        mockMvc.perform(get("/resource/" + RESOURCE_ID + "/owner/" + OWNER_ID))
                .andExpect(status().is(OK.value()))
                .andDo(print())
                .andExpect(jsonPath("$.available").value(6))
                .andExpect(jsonPath("$.lastRefill").value("" + Instant.EPOCH))
                .andExpect(jsonPath("$.limit.quantity").value(10))
                .andExpect(jsonPath("$.limit.duration").value("PT24H"));
    }

    private QuotaBuilder givenQuota(String resourceId) {
        return new QuotaBuilder(resourceId);
    }

    private class QuotaBuilder {
        private final String resourceId;
        private int remainingTokens;
        private boolean registerQuotaManager = true;

        public QuotaBuilder(String resourceId) {
            this.resourceId = resourceId;
        }

        public QuotaBuilder withRemainingTokens(int remainingTokens) {
            this.remainingTokens = remainingTokens;
            return this;
        }

        public void build() {
            if (registerQuotaManager) {
                quotaService.registerQuotaManagerFactory(QuantityOverTimeQuotaManager.class.getName(),
                        () -> new QuantityOverTimeQuotaManager(testClock));
            }
            QuotaState quota = initializeTestQuotaState(OWNER_ID, resourceId, remainingTokens);
            quotaStateRepository.save(quota);
        }

        private Quota initializeTestQuota() {
            QuantityOverTimeState defaultState = new QuantityOverTimeState(QuantityOverTimeLimit.limitOf(10, Duration.ofDays(1)), 10, Instant.EPOCH);
            return new Quota(RESOURCE_ID, QuantityOverTimeQuotaManager.class.getName(), defaultState);
        }

        private QuotaState initializeTestQuotaState(String ownerId, String resourceId, int remainingTokens) {
            QuantityOverTimeState initialState = new QuantityOverTimeState(QuantityOverTimeLimit.limitOf(10, Duration.ofDays(1)), remainingTokens, Instant.EPOCH);
            return new QuotaState(QuotaStateId.create(ownerId, resourceId), QuantityOverTimeQuotaManager.class.getName(), initialState);
        }

        public QuotaBuilder withoutRegisteredQuotaManager() {
            this.registerQuotaManager = false;
            return this;
        }
    }
}