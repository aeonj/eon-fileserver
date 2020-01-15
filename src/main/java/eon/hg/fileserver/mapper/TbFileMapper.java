package eon.hg.fileserver.mapper;

import eon.hg.fileserver.model.TbFile;

import java.util.List;

public interface TbFileMapper {
    List<TbFile> getAll();

    TbFile getOne(Long id);

    List<TbFile> selectByMd5(String md5);

    List<TbFile> selectByAppNo(String app_no);

    void insert(TbFile file);

    void update(TbFile file);

    void delete(Long id);
}