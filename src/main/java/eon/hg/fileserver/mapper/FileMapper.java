package eon.hg.fileserver.mapper;

import eon.hg.fileserver.model.FileInfo;

import java.util.List;

public interface FileMapper {
    List<FileInfo> getAll();

    FileInfo getOne(Long id);

    List<FileInfo> selectByMd5(String md5);

    List<FileInfo> selectByAppNo(String app_no);

    void insert(FileInfo file);

    void update(FileInfo file);

    void delete(Long id);
}