package br.com.bytebank.transactions.infrastructure.api.openapi.controller;

import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import br.com.bytebank.transactions.infrastructure.api.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.infrastructure.api.dtos.requests.TransferenceRequestDTO;
import br.com.bytebank.transactions.infrastructure.api.dtos.requests.UpdateTransactionStatusRequestDTO;
import br.com.bytebank.transactions.infrastructure.api.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.infrastructure.api.dtos.responses.BankStatementResponseDTO;
import br.com.bytebank.transactions.infrastructure.api.dtos.responses.DepositResponseDTO;
import br.com.bytebank.transactions.infrastructure.api.dtos.responses.TransactionResponseDTO;
import br.com.bytebank.transactions.infrastructure.api.dtos.responses.WithdrawResponseDTO;
import br.com.bytebank.transactions.infrastructure.exception.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@Tag(name = "MS - Transactions")
public interface TransactionControllerOpenApi {

    @Operation(summary = "Deposit",description = "Method responsible for deposit operation ")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Deposited successfully",
                    content = @Content(schema = @Schema(implementation = DepositResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Invalid amount",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )

    })
    ResponseEntity<DepositResponseDTO> deposit(
            @Parameter(
                    description = "Unique key to ensure idempotency of the request",
                    required = true
            )  UUID idempotencyKey ,
            @RequestBody(description = "DTO to perform deposit", required = true) DepositRequestDTO depositRequestDTO);

    @Operation(summary = "Withdraw",description = "Method responsible for withdraw operation ")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Withdraw successfully",
                    content = @Content(schema = @Schema(implementation = WithdrawResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Invalid amount",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )

    })
    ResponseEntity<WithdrawResponseDTO> withdraw(
            @Parameter(
                    description = "Unique key to ensure idempotency of the request",
                    required = true
            )  UUID idempotencyKey,
            @RequestBody(description = "DTo to perform withdraw", required = true) WithdrawRequestDTO withdrawRequestDTO);

    @Operation(summary = "Transference",description = "Method responsible for Transference operation ")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transference done successfully",
                    content = @Content(schema = @Schema(implementation = TransactionResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict between accounts",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Account service unavailable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Service unavailable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )

    })
    ResponseEntity<TransactionResponseDTO> transference(
            @Parameter(
                    description = "Unique key to ensure idempotency of the request",
                    required = true
            )  UUID idempotencyKey,
            @RequestBody(description = "DTo to perform transference", required = true) TransferenceRequestDTO transferenceRequestDTO) ;

    @Operation(summary = "Show Statement",description = "Method responsible for return all the transactions of an account ")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Statement generated",
                    content = @Content(schema = @Schema(implementation = BankStatementResponseDTO.class))
            )
    })
    ResponseEntity<List<BankStatementResponseDTO>> getStatement(@Parameter(description = "Id to find account to generate statement", required = true) UUID id);

    @Operation(summary = "Find Transaction",description = "Method responsible for find specific transaction ")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transaction found",
                    content = @Content(schema = @Schema(implementation = TransactionResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),

    })
    ResponseEntity<TransactionResponseDTO> getTransaction(@Parameter(description = "Id to find a transaction", required = true) UUID id);

    @Operation(summary = "Change transaction status")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Transaction status changed"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),

    })
    ResponseEntity<Void> updateTransactionStatus(@Parameter(description = "Id to find a transaction", required = true) UUID id,
                                                 @Parameter(description = "DTO receiving status") UpdateTransactionStatusRequestDTO status);
}
