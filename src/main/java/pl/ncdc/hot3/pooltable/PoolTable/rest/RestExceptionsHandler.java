package pl.ncdc.hot3.pooltable.PoolTable.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.DetectorException;

@ControllerAdvice("pl/ncdc/hot3/pooltable/PoolTable/rest")
public class RestExceptionsHandler {

    @ExceptionHandler({DetectorException.class})
    public ResponseEntity handleException(Exception e){
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
    }

}
