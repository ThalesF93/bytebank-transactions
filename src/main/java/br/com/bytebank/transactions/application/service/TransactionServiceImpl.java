package br.com.bytebank.transactions.application.service;

import br.com.bytebank.transactions.domain.repository.TransactionRepositoryDomain;
import br.com.bytebank.transactions.infrastructure.dtos.responses.BankStatementResponseDTO;
import br.com.bytebank.transactions.infrastructure.dtos.responses.TransactionResponseDTO;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.TransactionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepositoryDomain transactionRepository;

    @Transactional(readOnly = true)
    @Override
    public List<BankStatementResponseDTO> generateBankStatement(UUID accountId)  {
        var transactions = transactionRepository.findByOriginAccountIdOrTargetAccountIdOrderByDateTimeDesc(accountId, accountId);
        return transactions
                .stream()
                .map(BankStatementResponseDTO::generateStatement)
                .toList();
    }

    @Override
    public TransactionResponseDTO getTransactionById(UUID id) {
        var transaction = transactionRepository.findById(id).orElseThrow(
                ()-> new TransactionException(id)
        );
        return new TransactionResponseDTO(id, transaction.getTargetAccountId(), transaction.getType(), transaction.getStatus(), transaction.getAmount(), null);
    }


}
