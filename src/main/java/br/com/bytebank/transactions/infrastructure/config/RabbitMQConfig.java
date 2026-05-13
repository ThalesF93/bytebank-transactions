package br.com.bytebank.transactions.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String TRANSACTION_COMPLETED = "transaction.completed";
    public static final String TRANSACTION_FAILED = "transaction.failed";

    public static final String ROUTING_KEY_TRANSACTION_COMPLETED = "transaction.completed";
    public static final String EXCHANGE_TRANSACTION = "transaction.exchange";

    @Bean
    public Queue transactionCompletedQueue(){
        return QueueBuilder.durable(TRANSACTION_COMPLETED).build();
    }

    @Bean
    public Queue transactionFailedQueue(){
        return QueueBuilder.durable(TRANSACTION_FAILED).build();
    }

    @Bean
    public DirectExchange transactionExchange(){
        return new DirectExchange(EXCHANGE_TRANSACTION);
    }

    @Bean
    public Binding transactionCreatedBinding(Queue transactionCompletedQueue, DirectExchange transactionExchange){
        return BindingBuilder.bind(transactionCompletedQueue).to(transactionExchange).with(ROUTING_KEY_TRANSACTION_COMPLETED);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
