package com.hongdeyan.service;

import com.hongdeyan.daoImpl.GreensImpl;
import com.hongdeyan.daoImpl.OrderImpl;
import com.hongdeyan.model.Order;

import java.util.List;

public class OrderService implements AllService<Order> {


    private static OrderImpl orderImpl = new OrderImpl();
    private static OrderService orderService;

    private OrderService() {

    }

    public synchronized static OrderService getInstance() {
        if (orderService == null) {
            orderService = new OrderService();
        }
        return orderService;
    }


    @Override
    public Order add(Order order) {
        return orderImpl.add(order);
    }

    @Override
    public int remove(String id) {
        return orderImpl.remove(id);
    }

    @Override
    public int update(Order order) {
        return orderImpl.update(order);
    }

    @Override
    public List<Order> findAll() {
        return orderImpl.findAll();
    }

    @Override
    public Order get(String id) {
        return orderImpl.get(id);
    }
}
