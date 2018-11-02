package com.hongdeyan.service;

import com.hongdeyan.daoImpl.DutyImpl;
import com.hongdeyan.daoImpl.UserImpl;
import com.hongdeyan.model.Duty;

import java.util.List;

public class DutyService implements AllService<Duty> {
    private static DutyImpl dutyImpl = new DutyImpl();
    private static DutyService dutyService;

    private DutyService() {

    }

    public synchronized static DutyService getInstance() {
        if (dutyService == null) {
            dutyService = new DutyService();
        }
        return dutyService;
    }


    @Override
    public Duty add(Duty duty) {
        return dutyImpl.add(duty);
    }

    @Override
    public int remove(String id) {
        return dutyImpl.remove(id);
    }

    @Override
    public int update(Duty duty) {
        return dutyImpl.update(duty);
    }

    @Override
    public List<Duty> findAll() {
        return dutyImpl.findAll();
    }

    @Override
    public Duty get(String id) {
        return dutyImpl.get(id);
    }

    public Duty getDutyByName(String dutyName) {
        return dutyImpl.findDutyByName(dutyName);
    }
}
