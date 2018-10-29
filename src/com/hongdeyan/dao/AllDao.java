package com.hongdeyan.dao;

public interface AllDao<E> {
    //增加
    public E add(E e);
    //移除
    public int remove(String id);
    //获取
    public E get(String id);
    //修改
    public int update(E e);
}
