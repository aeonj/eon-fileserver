package eon.hg.fileserver.service;

import eon.hg.fileserver.model.TbFile;
import eon.hg.fileserver.util.callback.FileCallback;
import eon.hg.fileserver.util.dto.FileDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;

/**
 * 文件集中处理接口
 * @author eonook
 */
public interface FileService {

    /**
     * 一般文件检查，是否已上传
     * @param appNo
     * @param appFileId
     * @return
     */
    Map<String,Object> checkNormalFile(String appNo, String appFileId);

    /**
     * 断点文件续传，是否已上传，获取未完成上传的文件分片号
     * @param appNo
     * @param appFileId
     * @return
     */
    Map<String,Object> checkChunkFile(String appNo, String appFileId);

    /**
     * 上传文件，不支持断点续传
     * @param fileDTO Request接口参数注入
     * @param multipartFile 文件流
     * @return
     */
    TbFile uploadNormalFile(FileDTO fileDTO, MultipartFile multipartFile);

    /**
     * 上传文件，不支持断点续传
     * @param fileDTO Request接口参数注入
     * @param multipartFile 文件流
     * @param callback Controller端自定义回调函数
     * @param <T>
     * @return
     */
    <T> T uploadNormalFile(FileDTO fileDTO, MultipartFile multipartFile, FileCallback<T> callback);

    /**
     * 上传文件，支持断点续传
     * @param fileDTO Request接口参数注入
     * @param multipartFile 文件流
     * @param callback Controller端自定义回调函数
     * @param <T>
     * @return
     */
    <T> T uploadChunkFile(FileDTO fileDTO, MultipartFile multipartFile, FileCallback<T> callback);
    /**
     * 下载文件
     * @param appNo
     * @param appFileId
     * @return
     */
    TbFile downloadNormalFile(String appNo, String appFileId);

    /**
     * 下载文件，待回调处理
     * @param appNo 系统编号
     * @param appFileId 系统文件id
     * @param callback
     * @param <T>
     * @return
     */
    <T> T downloadNormalFile(String appNo, String appFileId, FileCallback<T> callback);

    /**
     * 获取本地文件InputStream
     * @param url
     * @return
     */
    InputStream downloadLocalFile(String url);

    InputStream downloadFastFile(String fileUrl);

    InputStream downloadFastChunkFile(String fileUrl, long fileOffset, long fileSize);

    /**
     * 获取文件完整下载地址
     * @param appNo
     * @param url
     * @return
     */
    String getFileFullUrl(String appNo, String url);
    /**
     * 删除文件
     * @param appNo 系统编号
     * @param appFileId 系统文件id
     * @return
     */
    void deleteFile(String appNo, String appFileId, String fileMd5);

    /**
     * 获取文件记录
     * @param appNo
     * @param appFileId
     * @return
     */
    TbFile getFile(String appNo, String appFileId);
}
