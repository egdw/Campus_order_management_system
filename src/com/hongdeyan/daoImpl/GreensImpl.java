package com.hongdeyan.daoImpl;

import com.hongdeyan.dao.DutyDao;
import com.hongdeyan.dao.GreensDao;
import com.hongdeyan.model.Duty;
import com.hongdeyan.model.Greens;
import com.hongdeyan.orm.Orm;

import java.util.List;

public class GreensImpl implements GreensDao {
    @Override
    public Greens add(Greens duty) {
        return Orm.save(duty);
    }

    @Override
    public int remove(String id) {
        Greens duty = new Greens(id, null, null, null, 0, 0);
        return Orm.remove(duty);
    }

    @Override
    public Greens get(String id) {
        return Orm.get(id, Greens.class);
    }

    @Override
    public int update(Greens duty) {
        return Orm.update(duty);
    }

    @Override
    public List findAll() {
        return null;
    }
}
