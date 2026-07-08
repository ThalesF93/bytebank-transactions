package br.com.bytebank.transactions.application.usecase;

import br.com.bytebank.transactions.infrastructure.messaging.kafka.event.FraudScoreEvent;

public interface FraudCallBackUseCase {

    void execute(FraudScoreEvent dto);
}
