package eon.hg.fileserver.exception;

import eon.hg.fileserver.util.body.ResultBody;
import eon.hg.fileserver.util.body.ResultCode;

/**
 * 前端同一异常处理
 * @author eonook
 */
public class ResultException extends RuntimeException {

    private Integer code;
    private String msg;
    private ResultBody body;

    public ResultException(String msg) {
        super(msg);
        this.code = ResultCode.FAILED.getCode();
        this.msg = msg;
    }

    public ResultException(Throwable cause) {
        super(cause);
        this.code = ResultCode.FAILED.getCode();
        this.msg = cause.getMessage();
    }

    public ResultException(String msg, Throwable cause) {
        super(msg,cause);
        this.code = ResultCode.FAILED.getCode();
        this.msg = msg;
    }

    public ResultException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public ResultException(Integer code, String msg, Throwable cause) {
        super(msg,cause);
        this.code = code;
        this.msg = msg;
    }

    public ResultException(ResultBody body) {
        super(body.getMsg());
        this.code = body.getCode();
        this.msg = body.getMsg();
        this.body = body;
    }

    public ResultException(ResultBody body, Throwable cause) {
        super(body.getMsg(),cause);
        this.code = body.getCode();
        this.msg = body.getMsg();
        this.body = body;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public ResultBody getBody() {
        return body;
    }

    public void setBody(ResultBody body) {
        this.body = body;
    }
}
