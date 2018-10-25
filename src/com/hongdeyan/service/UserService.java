package com.hongdeyan.service;

import com.hongdeyan.daoImpl.UserImpl;
import com.hongdeyan.model.User;

public class UserService implements AllService<User> {
    private UserImpl userImpl = new UserImpl();

    @Override
    public void add(User user) {

    }

    @Override
    public void remove(String id) {

    }

    @Override
    public void update(User user) {

    }
}
