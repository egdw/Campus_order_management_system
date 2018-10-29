package com.hongdeyan.daoImpl;

import com.hongdeyan.dao.UserDao;
import com.hongdeyan.model.User;
import com.hongdeyan.orm.Orm;
import com.sun.tools.corba.se.idl.constExpr.Or;

import java.util.List;

public class UserImpl implements UserDao {

    @Override
    public User add(User user) {
        User save = Orm.save(user);
        return save;
    }

    @Override
    public int remove(String id) {
        User user = new User();
        user.setId(id);
        return Orm.remove(user);
    }

    @Override
    public User get(String id) {
        return Orm.get(id, User.class);
    }

    @Override
    public int update(User user) {
        return Orm.update(user);
    }


    public List<User> findAll() {
        List list = Orm.selectAll(User.class);
        return list;
    }
}
