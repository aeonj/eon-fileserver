package eon.hg.fileserver.util.dto;

import eon.hg.fileserver.exception.ResultException;
import eon.hg.fileserver.util.body.ResultBody;
import lombok.Data;

@Data
public class FileDTO {
    private String action;
    private String appNo;
    private String fileId;
    private String filePath;
    private String fileName;
    private String fileMd5;
    private Long fileSize;//总大小
    private Integer chunks;//总片数
    private Integer chunk;//当前是第几片
    private String date; //文件第一个分片上传的日期(如:20170122)
    private String pipe;
    private String fastGroup;

    public void valid() {
        if (appNo==null)
            throw new ResultException(ResultBody.failed("参数【appNo】不可为空"));
        if (fileId==null)
            throw new ResultException(ResultBody.failed("参数【fileId】不可为空"));
        if (fileName==null)
            throw new ResultException(ResultBody.failed("参数【fileName】不可为空"));
    }

}
