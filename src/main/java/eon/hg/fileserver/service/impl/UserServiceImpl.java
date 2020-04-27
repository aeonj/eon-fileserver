package eon.hg.fileserver.service.impl;

import eon.hg.fileserver.mapper.UserMapper;
import eon.hg.fileserver.model.User;
import eon.hg.fileserver.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;


    @Override
    public User getUserByAccount(String username) {
        return userMapper.getByName(username);
    }

    @Override
    public User getUser(Long currentUserId) {
        return userMapper.getOne(currentUserId);
    }
}
