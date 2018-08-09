package resources;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pooltable.exceptions.DetectorException;

@ControllerAdvice("resources")
public class RestExceptionsHandler {

    @ExceptionHandler(DetectorException.class)
    public ResponseEntity handleException(Exception e){
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
    }

}
