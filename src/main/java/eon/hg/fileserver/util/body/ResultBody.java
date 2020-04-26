package eon.hg.fileserver.util.body;

import lombok.*;

/**
 * 统一前端返回对象
 * @author eonook
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@ToString(exclude = {"success","data"})
public class ResultBody {
    //设置统一的返回码
    @NonNull private Integer code;
    private String msg;
    private Object data;

    public ResultBody(ResultCode resultCode) {
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
    }

    /**
     * 业务处理成功，专用于数据增删改等数据处理
     * @return
     */
    public static ResultBody success() {
        ResultBody resultBody = new ResultBody(ResultCode.SUCCESS);
        return resultBody;
    }

    public static ResultBody success(String message) {
        ResultBody resultBody = new ResultBody(ResultCode.SUCCESS.getCode());
        resultBody.setMsg(message);
        return resultBody;
    }

    /**
     * 业务处理成功，并返回内容
     * @param data
     * @return
     */
    public static ResultBody success(Object data) {
        ResultBody resultBody = new ResultBody(ResultCode.SUCCESS.getCode());
        return resultBody.addObject(data);
    }

    /**
     * 业务数据失败
     * @return
     */
    public static ResultBody failed() {
        return failed(ResultCode.FAILED);
    }

    /**
     * 业务数据失败，返回预定义的错误类型
     * @param resultCode
     * @return
     */
    public static ResultBody failed(ResultCode resultCode) {
        ResultBody resultBody = new ResultBody(resultCode);
        return resultBody;
    }

    /**
     * 业务数据失败，返回自定义的错误类型，错误提示自行提供
     * @param reason
     * @return
     */
    public static ResultBody failed(String reason) {
        ResultBody resultBody = new ResultBody(ResultCode.CUSTOMIZE_FAIL.getCode());
        resultBody.setMsg(reason);
        return resultBody;
    }

    /**
     * 业务数据失败，返回自定义的错误类型，错误提示自行提供
     * @param reason
     * @return
     */
    public static ResultBody failed(Integer code, String reason) {
        ResultBody resultBody = new ResultBody(code);
        resultBody.setMsg(reason);
        return resultBody;
    }

    public ResultBody addObject(Object o) {
        setData(o);
        return this;
    }

    public ResultBody setMsg(String msg) {
        this.msg = msg;
        return this;
    }
}
