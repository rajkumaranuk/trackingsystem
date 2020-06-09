package com.vodafone.tracking.api;

import com.vodafone.tracking.exception.DeviceNotLocatedException;
import com.vodafone.tracking.exception.FileNotFoundException;
import com.vodafone.tracking.exception.ProductNotFoundException;
import com.vodafone.tracking.model.StandardResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler
    public ResponseEntity clientError(final FileNotFoundException exception) {
        log.info("Unable to find the file", exception);
        return createResponseEntity(HttpStatus.NOT_FOUND, "ERROR: No data file found");
    }

    @ExceptionHandler
    public ResponseEntity clientError(final DeviceNotLocatedException exception) {
        log.info("Unable to locate the device even though the AirplaneMode is OFF", exception);
        return createResponseEntity(HttpStatus.BAD_REQUEST, "ERROR: Device could not be located");
    }

    @ExceptionHandler
    public ResponseEntity clientError(final ProductNotFoundException exception) {
        log.info("Unable to find the product", exception);
        return createResponseEntity(HttpStatus.NOT_FOUND, "ERROR: Id " + exception.getProductId() + " not found");
    }

    @ExceptionHandler
    public ResponseEntity clientError(final Exception exception) {
        log.info("Internal error", exception);
        return createResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR: A technical exception occurred");
    }

    private ResponseEntity createResponseEntity(final HttpStatus httpStatus, final String description) {
        return ResponseEntity.status(httpStatus).body(new StandardResponse(description));
    }
}
