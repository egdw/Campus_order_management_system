package com.hongdeyan.daoImpl;

import com.hongdeyan.dao.DutyDao;
import com.hongdeyan.model.Duty;
import com.hongdeyan.model.User;
import com.hongdeyan.orm.Orm;
import com.mongodb.BasicDBObject;
import com.sun.tools.corba.se.idl.constExpr.Or;

import java.util.List;

public class DutyImpl implements DutyDao {
    @Override
    public Duty add(Duty duty) {
        return Orm.save(duty);
    }

    @Override
    public int remove(String id) {
        Duty duty = new Duty();
        duty.setId(id);
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

    @Override
    public List findAll() {
        return Orm.selectAll(Duty.class);
    }


    public Duty findDutyByName(String name){
        BasicDBObject object = new BasicDBObject();
        object.put("dutyName", name);
        List list = Orm.select(Duty.class, object);
        if (list == null || list.size() == 0) {
            return null;
        } else {
            return (Duty) list.get(0);
        }
    }
}
