package com.hongdeyan.orm;

import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.util.List;

public interface OrmInterface {

    public Object getObj(String objId, Class<?> aClass);

    public List getObjArr(MongoCursor<Document> cursor, Class<?> aClass);

    public Object save(Object object);

    public int remove(Object object);

    public int update(Object object);
}
