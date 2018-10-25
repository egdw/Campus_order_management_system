package com.hongdeyan.orm;

import com.hongdeyan.model.User;
import com.hongdeyan.static_class.MongoServer;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 通过从数据库当中查询出相应的结果转换成相应的对象属性
 */
public class OrmObject implements OrmInterface {

    @Override
    public Object getObj(MongoCursor<Document> cursor, Class<?> aClass) {
        //获取传入类的名称.查找相关的数据
//        MongoCollection<Document> collection = MongoServer.database.getCollection(aClass.getSimpleName());
        Object newInstance = null;
        try {
            //通过反射创建一个新的对象
            newInstance = aClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        Field[] declaredFields = aClass.getDeclaredFields();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            for (int i = 0; i < declaredFields.length; i++) {
                Field field = declaredFields[i];
                String type = field.getType().toString();
                try {
                    Field declaredField = newInstance.getClass().getDeclaredField(field.getName());
                    declaredField.setAccessible(true);
                    if (type.equals(String.class.toString())) {
                        //说明是字符串
                        declaredField.set(newInstance, document.getString(field.getName()));
                    } else if (type.equals(Integer.class.toString())) {
                        declaredField.set(newInstance, document.getInteger(field.getName()));
                    } else if (type.equals(Double.class.toString())) {
                        declaredField.set(newInstance, document.getDouble(field.getName()));
                    } else if (type.equals(Float.class.toString())) {
                        declaredField.set(newInstance, document.getDouble(field.getName()));
                    } else if (type.equals(Boolean.class.toString())) {
                        declaredField.set(newInstance, document.getBoolean(field.getName()));
                    } else {
                        //说明是其他的object类
                        //需要再次进行查询操作.
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return newInstance;
    }

    @Override
    public List<Object> getObjArr(MongoCursor<Document> cursor, Class<?> aClass) {
        return null;
    }

    @Override
    public int add(Object object) {
        return 0;
    }

    @Override
    public int remove(String id) {
        return 0;
    }

    @Override
    public int remove(Object object) {
        return 0;
    }


}
