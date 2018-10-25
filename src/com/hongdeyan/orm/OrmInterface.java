package com.hongdeyan.orm;

import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.util.List;

public interface OrmInterface {

    public Object getObj(MongoCursor<Document> cursor, Class<?> aClass);

    public List getObjArr(MongoCursor<Document> cursor, Class<?> aClass);

    public int add(Object object);

    public int remove(String id);

    public int remove(Object object);
}
