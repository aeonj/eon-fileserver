package eon.hg.fileserver.mapper;

import eon.hg.fileserver.model.Profession;

import java.util.List;
import java.util.Map;

public interface ProfessionMapper {
    List<Profession> getAll();

    Profession getOne(Long id);

    List<Profession> selectByMap(Map<String,Object> param);

    void insert(Profession obj);

    void update(Profession obj);

    void delete(Long id);

}
