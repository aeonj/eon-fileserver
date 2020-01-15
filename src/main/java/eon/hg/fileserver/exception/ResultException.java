package eon.hg.fileserver.exception;

import eon.hg.fileserver.util.body.ResultBody;

/**
 * 前端同一异常处理
 * @author eonook
 */
public class ResultException extends RuntimeException {

    private Integer code;
    private String msg;
    private ResultBody body;

    public ResultException(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResultException(ResultBody body) {
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
