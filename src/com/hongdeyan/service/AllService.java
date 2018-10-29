package com.hongdeyan.service;

import java.util.List;

public interface AllService<E> {
    public E add(E e);

    public int remove(String id);

    public int update(E e);

    public List<E> findAll();

    public E get(String id);
}
