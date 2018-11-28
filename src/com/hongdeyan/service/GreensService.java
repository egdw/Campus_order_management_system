package com.hongdeyan.service;

import com.hongdeyan.daoImpl.DutyImpl;
import com.hongdeyan.daoImpl.GreensImpl;
import com.hongdeyan.model.Greens;

import java.util.List;

public class GreensService implements AllService<Greens> {


    private static GreensImpl greensImpl = new GreensImpl();
    private static GreensService greensService;

    private GreensService() {

    }

    public synchronized static GreensService getInstance() {
        if (greensService == null) {
            greensService = new GreensService();
        }
        return greensService;
    }


    @Override
    public Greens add(Greens greens) {
        return greensImpl.add(greens);
    }

    @Override
    public int remove(String id) {
        return greensImpl.remove(id);
    }

    @Override
    public int update(Greens greens) {
        return greensImpl.update(greens);
    }

    @Override
    public List<Greens> findAll() {
        return greensImpl.findAll();
    }

    @Override
    public Greens get(String id) {
        return greensImpl.get(id);
    }
}
