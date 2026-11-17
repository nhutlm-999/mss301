package com.fucar.rentingservice.config;

import com.fucar.rentingservice.exception.CustomException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class CustomErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 404) {
            return new CustomException("Car not found", HttpStatus.NOT_FOUND);
        }
        return new CustomException("Error communicating with Car Service", HttpStatus.valueOf(response.status()));
    }
}
