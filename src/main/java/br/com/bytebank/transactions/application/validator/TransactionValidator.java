package br.com.bytebank.transactions.application.validator;

import br.com.bytebank.transactions.infrastructure.dtos.client.responses.AccountResponseDTO;
import br.com.bytebank.transactions.infrastructure.dtos.requests.TransferenceRequestDTO;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.AccountNotFoundException;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.AccountServiceUnavailableException;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.InvalidAmountException;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.SameAccountException;
import br.com.bytebank.transactions.infrastructure.feignclient.AccountClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionValidator {

    private final AccountClient accountClient;

    public void amountValidation(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0){
            log.warn("Invalid value entered. value={}", amount);
            throw new InvalidAmountException("Amount must be greater than zero");
        }
    }

    public void validatingTransference(TransferenceRequestDTO dto) {
        if (dto.originAccountId().equals(dto.destinationAccountId())){
            log.warn("User informed identical accounts. originAccountId={}, destinationAccountId={} ", dto.originAccountId(), dto.destinationAccountId());
            throw new SameAccountException("The accounts must be different");

        }
        amountValidation(dto.amount());
    }

    public AccountResponseDTO getAccountForTransaction(UUID uuid) {
        AccountResponseDTO account;
        try {
            account = accountClient.findAccount(uuid);
        } catch (FeignException.NotFound e) {
            throw new AccountNotFoundException(uuid);
        }catch (FeignException e){
            throw new AccountServiceUnavailableException();
        }
        return account;
    }
}
