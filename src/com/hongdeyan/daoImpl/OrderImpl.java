package com.hongdeyan.daoImpl;

import com.hongdeyan.dao.OrderDao;
import com.hongdeyan.model.Order;
import com.hongdeyan.orm.Orm;
import com.sun.tools.corba.se.idl.constExpr.Or;

import java.util.List;

public class OrderImpl implements OrderDao {

    @Override
    public Order add(Order order) {
        return Orm.save(order);
    }

    @Override
    public int remove(String id) {
        return Orm.remove(id);
    }

    @Override
    public Order get(String id) {
        return Orm.get(id, Order.class);
    }

    @Override
    public int update(Order order) {
        return Orm.update(order);
    }

    @Override
    public List findAll() {
        return Orm.selectAll(Order.class);
    }
}
