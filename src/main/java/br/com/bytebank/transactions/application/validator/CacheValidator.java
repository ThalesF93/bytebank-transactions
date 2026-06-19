package br.com.bytebank.transactions.application.validator;

import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.IdempotencyCacheException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.IdempotencyCacheException.Operation.DESERIALIZE;
import static br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.IdempotencyCacheException.Operation.SERIALIZE;


@RequiredArgsConstructor
@Slf4j
@Component
public class CacheValidator {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void toIdempotencyCache(String cacheKey, Object value) {
        try {
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(value), Duration.ofHours(24));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize idempotency response. type={}", value.getClass().getSimpleName(), e);
            throw new IdempotencyCacheException(SERIALIZE);
        }
    }

    public  <T> T fromIdempotencyCache(Object value, Class<T> clazz) {
        try {
            return objectMapper.readValue(value.toString(), clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize idempotency response. type={}", clazz.getSimpleName(), e);
            throw new IdempotencyCacheException(DESERIALIZE);
        }
    }
}
