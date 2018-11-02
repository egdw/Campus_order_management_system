package com.hongdeyan.service;

import com.hongdeyan.daoImpl.UserImpl;
import com.hongdeyan.model.User;

import java.util.List;

public class UserService implements AllService<User> {
    private static UserImpl userImpl = new UserImpl();
    private static UserService userService;

    private UserService() {

    }

    public synchronized static UserService getInstance() {
        if (userService == null) {
            userService = new UserService();
        }
        return userService;
    }


    @Override
    public User add(User user) {
        User add = userImpl.add(user);
        return add;
    }

    public User findByUserNameAndPassword(String username, String password) {
        User user = userImpl.findByUserNameAndPassword(username, password);
        return user;
    }

    public User findByUserName(String username) {
        User user = userImpl.findByUserName(username);
        return user;
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
