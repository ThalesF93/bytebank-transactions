package br.com.bytebank.transactions.domain.contract;

public interface IdempotencyContract {

    void toIdempotencyCache(String cacheKey, Object value);

    <T> T fromIdempotencyCache(Object value, Class<T> clazz);

    Object get(String cacheKey);
}
