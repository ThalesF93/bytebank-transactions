package br.com.bytebank.transactions.infrastructure.controller;


import br.com.bytebank.transactions.application.usecase.FraudCallBackUseCase;
import br.com.bytebank.transactions.infrastructure.dtos.requests.FraudServiceRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fraud-callback")
@Slf4j
@RequiredArgsConstructor
public class FraudCallBackController {

    private final FraudCallBackUseCase fraudCallBackUseCase;

    @PostMapping()
    public ResponseEntity<Void> fraudResponse(@Valid @RequestBody FraudServiceRequestDTO dto){
    log.info("Receiving response from Fraud Service. Score received is={}", dto.score());
        fraudCallBackUseCase.execute(dto);
        return ResponseEntity.noContent().build();
    }

}
