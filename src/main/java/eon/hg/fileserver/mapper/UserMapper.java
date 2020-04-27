package eon.hg.fileserver.mapper;

import eon.hg.fileserver.model.User;

import java.util.List;

public interface UserMapper {
    List<User> getAll();

    User getOne(Long id);

    User getByName(String name);

    void insert(User obj);

    void update(User obj);
    void updatePsw(User obj);

    void delete(Long id);
}
