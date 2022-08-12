package com.myseotoolbox.resourcequota;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.quota4j.UserQuotaService;
import io.github.quota4j.model.ResourceQuota;
import io.github.quota4j.model.UserQuotaId;
import io.github.quota4j.model.UserQuotaState;
import io.github.quota4j.persistence.ResourceQuotaPersistence;
import io.github.quota4j.persistence.UserQuotaPersistence;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.redis.core.convert.RedisCustomConversions;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Configuration
@EnableRedisRepositories
public class RedisConfig {
    @Bean
    public UserQuotaService getUserQuotaService(ResourceQuotaPersistence resourceQuotaPersistence, UserQuotaPersistence userQuotaPersistence) {
        return new UserQuotaService(resourceQuotaPersistence, userQuotaPersistence);
    }

    @Bean
    public RedisCustomConversions redisCustomConversions(UserQuotaIdWriter userQuotaIdWriter, UserQuotaIdReader userQuotaIdReader, UserQuotaIdToStringConverter userQuotaIdToStringConverter, ResourceQuotaWriter ResourceQuotaWriter, ResourceQuotaReader ResourceQuotaReader, UserQuotaStateWriter UserQuotaStateWriter, UserQuotaStateReader UserQuotaStateReader) {
        return new RedisCustomConversions(Arrays.asList(userQuotaIdWriter, userQuotaIdReader, userQuotaIdToStringConverter, ResourceQuotaWriter, ResourceQuotaReader, UserQuotaStateWriter, UserQuotaStateReader));
    }


    @Bean
    public com.fasterxml.jackson.databind.Module dateTimeModule() {
        return new JavaTimeModule();
    }

    @Component
    static class UserQuotaIdToStringConverter implements Converter<UserQuotaId, String> {
        @Override
        public String convert(UserQuotaId source) {
            return source.username() + "." + source.resourceId();
        }
    }


    @Component
    @WritingConverter
    @RequiredArgsConstructor
    static class UserQuotaStateWriter implements Converter<UserQuotaState, byte[]> {

        private final ObjectMapper mapper;

        @SneakyThrows
        @Override
        public byte[] convert(UserQuotaState source) {
            return mapper.writeValueAsBytes(source);
        }

    }

    @Component
    @ReadingConverter
    @RequiredArgsConstructor
    static class UserQuotaStateReader implements Converter<byte[], UserQuotaState> {
        private final ObjectMapper mapper;

        @SneakyThrows
        @Override
        public UserQuotaState convert(byte[] source) {
            return mapper.readValue(source, UserQuotaState.class);
        }
    }


    @Component
    @WritingConverter
    @RequiredArgsConstructor
    static class ResourceQuotaWriter implements Converter<ResourceQuota, byte[]> {

        private final ObjectMapper mapper;

        @SneakyThrows
        @Override
        public byte[] convert(ResourceQuota source) {
            return mapper.writeValueAsBytes(source);
        }
    }

    @Component
    @ReadingConverter
    @RequiredArgsConstructor
    static class ResourceQuotaReader implements Converter<byte[], ResourceQuota> {
        private final ObjectMapper mapper;

        @SneakyThrows
        @Override
        public ResourceQuota convert(byte[] source) {
            return mapper.readValue(source, ResourceQuota.class);
        }
    }

    @Component
    @WritingConverter
    @RequiredArgsConstructor
    static class UserQuotaIdWriter implements Converter<UserQuotaId, byte[]> {
        private final ObjectMapper mapper;

        @SneakyThrows
        @Override
        public byte[] convert(UserQuotaId source) {
            return mapper.writeValueAsBytes(source);
        }
    }

    @Component
    @ReadingConverter
    @RequiredArgsConstructor
    static class UserQuotaIdReader implements Converter<String, UserQuotaId> {
        private final ObjectMapper mapper;

        @SneakyThrows
        @Override
        public UserQuotaId convert(String source) {
            return mapper.readValue(source, UserQuotaId.class);
        }
    }
}

