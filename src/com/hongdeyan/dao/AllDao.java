package com.hongdeyan.dao;

public interface AllDao<E> {
    //增加
    public void add(E e);
    //移除
    public void remove(String id);
    //获取
    public void get(String id);
    //修改
    public void update(E e);
}
