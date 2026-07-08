package br.com.bytebank.transactions.infrastructure.controller;

import br.com.bytebank.transactions.application.usecase.UserConfirmationUseCase;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/user-confirmation")
public class UserConfirmationController {

    private final UserConfirmationUseCase userConfirmationUseCase;

    @PostMapping
    public ResponseEntity<Void> receiveUserConfirmation(@NotBlank @RequestHeader("X-User-Id") String customerId,
                                                        @NotBlank @RequestBody String answer){

        log.info("Receiving Customer answer from Notification-Service. Customer ID={}, Answer={}", customerId, answer);
        userConfirmationUseCase.execute(UUID.fromString(customerId), answer);
        return ResponseEntity.noContent().build();
    }

}
