package br.com.bytebank.transactions.application.usecase;

import java.util.UUID;

public interface UserConfirmationUseCase {

    void execute(UUID uuid, String answer);
}
