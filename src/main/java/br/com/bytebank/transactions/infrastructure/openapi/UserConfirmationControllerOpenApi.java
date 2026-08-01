package br.com.bytebank.transactions.infrastructure.openapi;

import br.com.bytebank.transactions.infrastructure.dtos.responses.DepositResponseDTO;
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

@Tag(name = "MS - Transactions")
public interface UserConfirmationControllerOpenApi {

    @Operation(summary = "Receive user confirmation for suspicious transaction")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Confirmation processed successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction Not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )

    })
    ResponseEntity<Void> receiveUserConfirmation(@Parameter(description = "Customer's id received in header after gateway interception",
                                                         required = true) String customerId,

                                               @RequestBody(description = "The user's answer",
                                                       required = true) String answer);
}
