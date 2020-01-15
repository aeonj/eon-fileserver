package eon.hg.fileserver.controller;

import eon.hg.fileserver.util.body.ResultBody;
import eon.hg.fileserver.exception.ResultException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalAdvice {

    @ResponseBody
    @ExceptionHandler(ResultException.class)
    public ResponseEntity<ResultBody> ErrorHandler(ResultException exception) {

        return new ResponseEntity(exception.getBody(), HttpStatus.EXPECTATION_FAILED);
    }
}
