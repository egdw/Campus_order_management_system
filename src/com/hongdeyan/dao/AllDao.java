package com.hongdeyan.dao;

import java.util.List;

public interface AllDao<E> {
    //增加
    public E add(E e);

    //移除
    public int remove(String id);

    //获取
    public E get(String id);

    //修改
    public int update(E e);

    //获取所有的
    public List findAll();
}
