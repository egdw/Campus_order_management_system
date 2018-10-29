package com.hongdeyan.service;

import com.hongdeyan.daoImpl.UserImpl;
import com.hongdeyan.model.User;

import java.util.List;

public class UserService implements AllService<User> {
    private static UserImpl userImpl = new UserImpl();

    @Override
    public User add(User user) {
        User add = userImpl.add(user);
        return add;
    }

    @Override
    public int remove(String id) {
        return userImpl.remove(id);
    }

    @Override
    public int update(User user) {
        return userImpl.update(user);
    }

    @Override
    public List<User> findAll() {
        return userImpl.findAll();
    }

    @Override
    public User get(String id) {
        return userImpl.get(id);
    }
}
