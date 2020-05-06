package eon.hg.fileserver.util.body;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@ToString(exclude = {"success","data"})
public class PageBody {

    @NonNull private Integer code;
    private Boolean success;
    private String msg;
    private int page;
    private int total;
    private Object data;

    public PageBody(ResultCode resultCode) {
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
    }

    /**
     * 业务处理成功
     * @return
     */
    public static PageBody success() {
        PageBody pageBody = new PageBody(ResultCode.SUCCESS.getCode());
        pageBody.setSuccess(true);
        return pageBody;
    }

    /**
     * 业务处理成功，并返回对象
     * @param o
     * @return
     */
    public static PageBody success(Object o) {
        PageBody pageBody = new PageBody(ResultCode.SUCCESS.getCode());
        pageBody.setSuccess(true);
        return pageBody.addObject(o);
    }

    /**
     * 业务处理失败，返回自定义的错误提示
     * @param reason
     * @return
     */
    public static PageBody failed(String reason) {
        PageBody pageBody = new PageBody(ResultCode.CUSTOMIZE_FAIL.getCode());
        pageBody.setSuccess(false);
        return pageBody;
    }

    public static PageBody failed(ResultCode resultCode) {
        PageBody pageBody = new PageBody(resultCode);
        pageBody.setSuccess(false);
        return pageBody;
    }

    public PageBody addPageInfo(List resultList, int page, int total) {
        this.page = page;
        this.total = total;
        this.data = resultList;
        return this;
    }

    public PageBody addObject(Object o) {
        setData(o);
        return this;
    }
}
