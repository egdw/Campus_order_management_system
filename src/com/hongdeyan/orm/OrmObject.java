package com.hongdeyan.orm;

import com.hongdeyan.annotation.DbRef;
import com.hongdeyan.annotation.Id;
import com.hongdeyan.annotation.Param;
import com.hongdeyan.model.User;
import com.hongdeyan.static_class.MongoServer;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import lombok.extern.slf4j.Slf4j;
import org.bson.BasicBSONObject;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.print.attribute.standard.DocumentName;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

/**
 * 通过从数据库当中查询出相应的结果转换成相应的对象属性
 *
 * @author hdy
 */
@Slf4j
public class OrmObject implements OrmInterface {

    @Override
    public Object getObj(String objId, Class<?> aClass) {
        com.hongdeyan.annotation.Document annotation = aClass.getAnnotation(com.hongdeyan.annotation.Document.class);
        if (annotation == null) {
            throw new UnsupportedOperationException("当前的类没有@Document的注解!");
        }
        String collection_name = aClass.getSimpleName();
        if (!annotation.doucument_name().equals("")) {
            collection_name = annotation.doucument_name();
        }

        //从collection当中查询相应的id.
        MongoCollection<Document> collection = MongoServer.database.getCollection(collection_name);
        BasicDBObject dbObject = new BasicDBObject();
        dbObject.put("_id", new ObjectId(objId));
        FindIterable<Document> documents = collection.find(dbObject);
        MongoCursor<Document> cursor = documents.iterator();

        Object newInstance = null;
        try {
            //通过反射创建一个新的对象
            newInstance = aClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        //获取所有的属性
        Field[] declaredFields = aClass.getDeclaredFields();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            for (int i = 0; i < declaredFields.length; i++) {
                Field field = declaredFields[i];
                //获取当前类中的类型.档位数据库中数据默认的值
                Class<?> type = field.getType();
                try {
                    Field declaredField = newInstance.getClass().getDeclaredField(field.getName());
                    declaredField.setAccessible(true);
                    //获取当前属性的注解
                    Id id = declaredField.getAnnotation(Id.class);
                    Param param = declaredField.getAnnotation(Param.class);
                    DbRef dbRef = declaredField.getAnnotation(DbRef.class);
                    if (id != null || param != null || dbRef != null) {
                        //说明是已经标记注解的属性.可以进行数据自动的导入.
                        String paramName = null;
                        if (id != null && !id.param_name().equals("")) {
                            //如果存在特定的标记的话就是用特定的标记
                            paramName = id.param_name();
                        } else if (param != null && !param.param_name().equals("")) {
                            paramName = param.param_name();
                        } else {
                            //如果没有设置的话默认是用属性的名称.
                            paramName = field.getName();
                        }
                        if (paramName.equals("id") && id != null) {
                            //判断是否是id.如果是.转换成为_id;
                            paramName = "_id";
                        }
                        if (id != null) {
                            //说明是主键
                            declaredField.set(newInstance, document.getObjectId(paramName).toString());
                            //下面的代码不执行
                            continue;
                        }
                        if (type == String.class) {
                            //说明是字符串
                            declaredField.set(newInstance, document.getString(paramName));
                        } else if (type == Integer.class || type == int.class) {
                            //说明是int
                            declaredField.set(newInstance, document.getInteger(paramName));
                        } else if (type == Double.class || type == double.class || type == float.class || type == Float.class) {
                            //说明是double Float
                            declaredField.set(newInstance, document.getDouble(paramName));
                        } else if (type == Boolean.class || type == boolean.class) {
                            declaredField.set(newInstance, document.getBoolean(paramName));
                        } else if (type == Date.class) {
                            declaredField.set(newInstance, document.getDate(paramName));
                        } else if (type == Long.class || type == long.class) {
                            declaredField.set(newInstance, document.getLong(paramName));
                        } else {
                            //说明是其他的object类
                            //需要再次进行查询操作.
                            Class<?> fieldType = declaredField.getType();
                            //判断当前的object是否是其他的关系
                            com.hongdeyan.annotation.Document dbRef_document = fieldType.getAnnotation(com.hongdeyan.annotation.Document.class);
                            if (dbRef != null && dbRef_document != null) {
                                //如果是相关的依赖.则继续进行查询
                                String dbRef_param_name = dbRef.param_name();
                                if (dbRef_param_name.equals("")) {
                                    paramName = dbRef_param_name;
                                }
                                //判断存放的objectId是否存在.
                                ObjectId objectId = document.getObjectId(paramName);
                                if (objectId == null) {
                                    //说明不存在,为空
                                    declaredField.set(newInstance, null);
                                    continue;
                                }
                                //进行递归调用
                                Object obj = getObj(objectId.toString(), fieldType);
                                declaredField.set(newInstance, obj);
                            }
                        }
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
    public Object save(Object object) {
        ObjectId objectId = saveInner(object);
        if (objectId != null) {
            //说明插入成功
            return getObj(objectId.toString(), object.getClass());
        }
        return null;
    }


    /**
     * save的内函数
     *
     * @param object 传入需要保存在collection中的object类
     * @return 返回存入的id
     */
    private ObjectId saveInner(Object object) {
        Class<?> aClass = object.getClass();
        com.hongdeyan.annotation.Document annotation = aClass.getAnnotation(com.hongdeyan.annotation.Document.class);
        if (annotation == null) {
            throw new UnsupportedOperationException("当前的类没有@Document的注解!");
        }
        String doucumentName = annotation.doucument_name();
        if ("".equals(doucumentName)) {
            //如果没有输入的话默认为类的名称
            doucumentName = aClass.getSimpleName();
        }
        //获取到一个collection对象
        MongoCollection<Document> collection = MongoServer.database.getCollection(doucumentName);
        if (collection == null) {
            //如果collection对象为null的话说明数据库当中没有存在collection.
            //自动创建一个
            MongoServer.database.createCollection(doucumentName);
            log.info("没有找到" + doucumentName + "的collection自动创建完成.");
        }
        Document document = new Document();
        //获取类的所有的fields属性.
        Field[] fields = aClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            Param param = field.getAnnotation(Param.class);
            Id id = field.getAnnotation(Id.class);
            if (param != null) {
                //说明添加了注解的
                String param_name = field.getName();
                if (!param.param_name().equals("")) {
                    param_name = param.param_name();
                }
                try {
                    document.append(param_name, field.get(object));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            if (id != null) {
                String param_name = field.getName();
                if (!id.param_name().equals("")) {
                    param_name = id.param_name();
                }
                if (param_name.equals("id")) {
                    param_name = "_id";
                }
                try {
                    //这里的更新保存还存在问题..还需要后续修复...
                    document.append(param_name, field.get(object));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            DbRef dbRef = field.getAnnotation(DbRef.class);
            //判断是否存在外部引用
            if (dbRef != null) {
                String param_name = field.getName();
                if (!dbRef.param_name().equals("")) {
                    param_name = dbRef.param_name();
                }
                try {
                    ObjectId inner = saveInner(field.get(object));
                    if (inner != null) {
                        document.append(param_name, inner);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }
        //插入到collection当中去.
        collection.insertOne(document);
        ObjectId objectId = document.getObjectId("_id");
        return objectId;

    }


    @Override
    public int remove(Object object) {
        Class<?> aClass = object.getClass();
        com.hongdeyan.annotation.Document annotation = aClass.getAnnotation(com.hongdeyan.annotation.Document.class);
        if (annotation == null) {
            throw new UnsupportedOperationException("当前的类没有@Document的注解!");
        }
        String collection_name = aClass.getSimpleName();
        if (!annotation.doucument_name().equals("")) {
            collection_name = annotation.doucument_name();
        }
        //从collection当中查询相应的id.
        MongoCollection<Document> collection = MongoServer.database.getCollection(collection_name);
        //从class当中获取id
        Field[] declaredFields = aClass.getDeclaredFields();
        for (int i = 0; i < declaredFields.length; i++) {
            Field field = declaredFields[i];
            field.setAccessible(true);
            Id id = field.getAnnotation(Id.class);
            if (id != null) {
                try {
                    String obj = (String) field.get(object);
                    BasicDBObject dbObject = new BasicDBObject();
                    dbObject.put("_id", new ObjectId(obj));
                    DeleteResult deleteResult = collection.deleteOne(dbObject);
                    long deletedCount = deleteResult.getDeletedCount();
                    return (int) deletedCount;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    @Override
    public int update(Object object) {
        return 0;
    }


}
