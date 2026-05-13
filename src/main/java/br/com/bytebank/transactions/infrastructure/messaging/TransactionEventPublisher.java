package br.com.bytebank.transactions.infrastructure.messaging;

import br.com.bytebank.transactions.api.dtos.client.responses.CustomerClientResponseDTO;
import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.exception.CustomerNotFoundException;
import br.com.bytebank.transactions.infrastructure.config.RabbitMQConfig;
import br.com.bytebank.transactions.infrastructure.feignclient.AccountClient;
import br.com.bytebank.transactions.infrastructure.messaging.event.TransactionCompletedEvent;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final AccountClient accountClient;

    public void publishTransferenceCompleted(Transaction transaction){
        CustomerClientResponseDTO customer;
        try {
            customer = accountClient.findCustomerByAccountId(transaction.getId());
        } catch (FeignException.NotFound e) {
            throw new CustomerNotFoundException("Customer Not Found. See StackTrace from MS-Accounts to find out");
        }
        var event = new TransactionCompletedEvent(
                transaction.getId(),
                transaction.getOriginAccountId(),
                customer.email(),
                customer.name(),
                transaction.getAmount(),
                transaction.getType().toString(),
                transaction.getDateTime());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_TRANSACTION,
                RabbitMQConfig.ROUTING_KEY_TRANSACTION_COMPLETED,
                event);

        log.info("Event published: TransferenceCompleteEvent transactionId={}", transaction.getId());
    }
}
