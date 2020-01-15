package eon.hg.fileserver.mapper;

import eon.hg.fileserver.model.TbApp;

import java.util.List;

public interface TbAppMapper {
    List<TbApp> getAll();

    TbApp getOne(Long id);

    TbApp getByAppNo(String app_no);

    void insert(TbApp obj);

    void update(TbApp obj);

    void delete(Long id);

}
