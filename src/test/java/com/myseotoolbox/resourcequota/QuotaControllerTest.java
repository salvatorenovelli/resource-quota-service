package com.myseotoolbox.resourcequota;


import io.github.quota4j.QuotaManagerNotRegisteredException;
import io.github.quota4j.ResourceQuotaNotFoundException;
import io.github.quota4j.QuotaService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
class QuotaControllerTest extends DataRedisContainerTest {

    private static final String NON_EXISTING_RESOURCE_ID = "crawler.non_existing_resource";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    QuotaService quotaService;


    @Test
    void shouldReturnOkIfQuotaIsAvailable() throws Exception {
        givenResourceQuota("crawler.crawls_per_day")
                .withRemainingTokens(10)
                .build();

        mockMvc.perform(get("/resources/crawler.crawls_per_day/workspaces/123456"))
                .andExpect(status().is(OK.value()));
    }

    @Test
    void shouldReturn() throws Exception {
        givenResourceQuota("crawler.crawls_per_day")
                .withRemainingTokens(0)
                .build();

        mockMvc.perform(get("/resources/crawler.crawls_per_day/workspaces/123456"))
                .andExpect(status().is(TOO_MANY_REQUESTS.value()));
    }

    @Test
    void shouldHandleResourceNotFound() throws Exception {
        Mockito.when(quotaService.tryAcquire(anyString(), anyString(), anyInt())).thenThrow(new ResourceQuotaNotFoundException(NON_EXISTING_RESOURCE_ID));

        mockMvc.perform(get("/resources/" + NON_EXISTING_RESOURCE_ID + "/workspaces/123456"))
                .andExpect(status().is(NOT_FOUND.value()))
                .andExpect(content().string(containsString(NON_EXISTING_RESOURCE_ID)));
    }

    @Test
    void shouldHandleQuotaManagerNotFound() throws Exception {
        Mockito.when(quotaService.tryAcquire(anyString(), anyString(), anyInt())).thenThrow(new QuotaManagerNotRegisteredException("NON_REGISTERED_QUOTA_MANAGER"));

        mockMvc.perform(get("/resources/" + NON_EXISTING_RESOURCE_ID + "/workspaces/123456"))
                .andExpect(status().is(NOT_IMPLEMENTED.value()))
                .andExpect(content().string(containsString("NON_REGISTERED_QUOTA_MANAGER")));
    }

    private QuotaBuilder givenResourceQuota(String resourceId) {
        return new QuotaBuilder(resourceId);
    }

    private class QuotaBuilder {
        private final String resourceId;
        private int remainingTokens;

        public QuotaBuilder(String resourceId) {
            this.resourceId = resourceId;
        }

        public QuotaBuilder withRemainingTokens(int remainingTokens) {
            this.remainingTokens = remainingTokens;
            return this;
        }

        public void build() {
            Mockito.when(quotaService.tryAcquire(anyString(), Mockito.eq(resourceId), anyInt())).thenAnswer(invocation -> {
                Integer quantity = invocation.getArgument(2, Integer.class);
                remainingTokens -= quantity;
                return remainingTokens > 0;
            });
        }
    }
}