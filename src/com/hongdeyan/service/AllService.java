package com.hongdeyan.service;

public interface AllService<E> {
    public void add(E e);

    public void remove(String id);

    public void update(E e);
}
