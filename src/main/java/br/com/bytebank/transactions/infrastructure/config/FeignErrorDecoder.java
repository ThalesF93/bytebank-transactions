package br.com.bytebank.transactions.infrastructure.config;


import br.com.bytebank.transactions.domain.exceptions.ResourceNotFoundException;
import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.apache.coyote.BadRequestException;

import javax.naming.ServiceUnavailableException;

public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()){
            case 400 -> new BadRequestException("Invalid Request");
            case 404 -> new ResourceNotFoundException("Resource not found");
            case 500 -> new ServiceUnavailableException("Service Unavailable");
            default -> new FeignException.FeignClientException(
                    response.status(), response.reason(), response.request(), null, null
            );
        };
    }
}
