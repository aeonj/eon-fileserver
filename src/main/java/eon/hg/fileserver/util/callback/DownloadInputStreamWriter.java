package eon.hg.fileserver.util.callback;

import com.github.tobato.fastdfs.proto.storage.DownloadCallback;

import java.io.InputStream;

/**
 * 文件下载回调方法
 * 
 * @author tobato
 *
 */
public class DownloadInputStreamWriter implements DownloadCallback<InputStream> {

    /**
     * 文件接收处理
     */
    @Override
    public InputStream recv(InputStream ins) {
        return ins;
    }

}
