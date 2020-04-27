package eon.hg.fileserver.mapper;

import eon.hg.fileserver.model.App;

import java.util.List;

public interface AppMapper {
    List<App> getAll();

    App getOne(Long id);

    App getByAppNo(String app_no);

    void insert(App obj);

    void update(App obj);

    void delete(Long id);

}
