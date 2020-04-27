package eon.hg.fileserver.service;

import eon.hg.fileserver.model.User;

public interface UserService {
    User getUserByAccount(String username);

    User getUser(Long currentUserId);
}
