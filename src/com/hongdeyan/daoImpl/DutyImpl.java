package com.hongdeyan.daoImpl;

import com.hongdeyan.dao.DutyDao;
import com.hongdeyan.model.Duty;
import com.hongdeyan.orm.Orm;
import com.sun.tools.corba.se.idl.constExpr.Or;

public class DutyImpl implements DutyDao {
    @Override
    public Duty add(Duty duty) {
        return Orm.save(duty);
    }

    @Override
    public int remove(String id) {
        Duty duty = new Duty(id, "");
        return Orm.remove(duty);
    }

    @Override
    public Duty get(String id) {
        return Orm.get(id, Duty.class);
    }

    @Override
    public int update(Duty duty) {
        return Orm.update(duty);
    }
}
