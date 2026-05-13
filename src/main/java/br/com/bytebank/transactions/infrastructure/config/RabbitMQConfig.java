package br.com.bytebank.transactions.infrastructure.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String TRANSACTION_COMPLETED = "transaction.completed";
    public static final String TRANSACTION_FAILED = "transaction.failed";

    public static final String MESSAGE_DELIVERY_COMPLETED = "message.delivered";
    public static final String MESSAGE_DELIVERY_FAILED = "message.failed";


}
