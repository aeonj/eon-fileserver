package eon.hg.fileserver.exception;

import eon.hg.fileserver.util.body.ResultBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 说明：Controller切面，捕获全局异常并返回统一错误码，
 * 这里的错误码因为前段人员处理不了，只能返回200， 然后讲权限码放入 code来处理
 */
@ControllerAdvice
@ResponseBody
public class ExceptionAdvice {
    public static Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    /**
     * 说明：400 - Bad Request
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResultBody handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        logger.error("json 参数解析失败", e);
        return  ResultBody.failed("请求参数解析失败!");
    }


    /**
     * 说明：401 身份验证错误
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(AuthorizationException.class)
    public ResultBody authorizationException(Exception e){
        logger.error("身份验证异常", "user authority error");
        return  ResultBody.failed(HttpStatus.UNAUTHORIZED.value(),"身份验证失败!");
    }

    /**
     * 405 - Method Not Allowed
     */
//    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResultBody handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        logger.error("不支持当前请求方法", e);
        return  ResultBody.failed(HttpStatus.METHOD_NOT_ALLOWED.value(),"请求方法错误!");
    }

    /**
     * 415 - Unsupported Media Type
     */
//    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResultBody handleHttpMediaTypeNotSupportedException(Exception e) {
        logger.error("不支持当前媒体类型", e);
        return  ResultBody.failed(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),"不支持当前媒体类型!");
    }

    /**
     * 500 - Internal Server Error
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(Exception.class)
    public ResultBody handleException(Exception e) {
        logger.error("服务运行异常", e);
        return ResultBody.failed(HttpStatus.INTERNAL_SERVER_ERROR.value(),"服务运行异常!");
    }


}