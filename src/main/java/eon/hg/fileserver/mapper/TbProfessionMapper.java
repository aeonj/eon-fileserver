package eon.hg.fileserver.mapper;

import eon.hg.fileserver.model.TbProfession;

import java.util.List;
import java.util.Map;

public interface TbProfessionMapper {
    List<TbProfession> getAll();

    TbProfession getOne(Long id);

    List<TbProfession> selectByMap(Map<String,Object> param);

    void insert(TbProfession obj);

    void update(TbProfession obj);

    void delete(Long id);

}
