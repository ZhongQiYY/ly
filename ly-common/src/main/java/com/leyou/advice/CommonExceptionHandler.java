package com.leyou.advice;

import com.leyou.enums.ExceptionEnum;
import com.leyou.exception.LyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(LyException.class)
    public ResponseEntity<String> handlerException(LyException lye){
        ExceptionEnum em = lye.getExceptionEnum();
        return ResponseEntity.status(em.getCode()).body(em.getMsg());
    }
}
