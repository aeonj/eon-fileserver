package eon.hg.fileserver.util.callback;

import eon.hg.fileserver.model.FileInfo;

import java.io.InputStream;

/**
 * 用于返回处理后的文件信息，包括上传，下载（输入流）返回controller处理
 * @author eonook
 */
public interface FileCallback<T> {
    T recv(FileInfo tbFile, InputStream ins);
}
