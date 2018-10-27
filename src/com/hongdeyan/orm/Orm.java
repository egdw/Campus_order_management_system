package com.hongdeyan.orm;

import com.hongdeyan.annotation.DbRef;
import com.hongdeyan.annotation.Id;
import com.hongdeyan.annotation.Param;
import com.hongdeyan.static_class.MongoServer;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.lang.reflect.Field;
import java.util.*;

/**
 * final 禁止被继承
 * synchronize 防止脏数据
 *
 * @author 洪德衍
 * @version 2018-10-27
 */
@Slf4j
public final class Orm {

    private Orm() {
        //禁止实例化
    }


    public synchronized static <V> V get(String objId, Class<V> aClass) {
        MongoCollection<Document> collection = getCollection(aClass);
        BasicDBObject dbObject = new BasicDBObject();
        dbObject.put("_id", new ObjectId(objId));
        MongoCursor<Document> cursor = collection.find(dbObject).iterator();
        //获取所有的属性
        while (cursor.hasNext()) {
            Document document = cursor.next();
            return convertToClass(aClass, document);
        }
        return (V) null;
    }


    /**
     * 重要的函数,把一个Document当中的所有数据转换成为与类相关的数据
     *
     * @param aClass   需要转换的类对象
     * @param document 数据库查询出来的结果对象
     * @param <V>      返回输入的类对象
     * @return 输入的类对象
     */
    private synchronized static <V> V convertToClass(Class<V> aClass, Document document) {
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
        for (int i = 0; i < declaredFields.length; i++) {
            Field field = declaredFields[i];
            Class<?> type = field.getType();
            //获取当前类中的类型.档位数据库中数据默认的值
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
                    } else {
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
                                } else {
                                    //进行递归调用
                                    declaredField.set(newInstance, get(objectId.toString(), fieldType));
                                }
                            }
                        }
                    }
                }

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }


        return (V) newInstance;
    }

    /**
     * 保存类到数据库
     *
     * @param object 需要保存的类
     * @param <V>
     * @return
     */
    public synchronized static <V> V save(V object) {
        ObjectId id = getObjectId(object);
        if (id == null) {
            id = saveInner(object);
        } else {
            update(object);
        }
        if (id != null) {
            //说明插入成功
            Object obj = get(id.toString(), object.getClass());
            return (V) obj;
        }
        return null;
    }


    /**
     * save的内函数
     *
     * @param object 传入需要保存在collection中的object类
     * @return 返回存入的id
     */
    private synchronized static ObjectId saveInner(Object object) {
        if (object == null)
            return null;
        Class<?> aClass = object.getClass();
        MongoCollection<Document> collection = getCollection(aClass);
        Document document = new Document();
        //获取类的所有的fields属性.
        Field[] fields = aClass.getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            Param param = field.getAnnotation(Param.class);
            document = getParamDocument(object, document, field, param);
            DbRef dbRef = field.getAnnotation(DbRef.class);
            //判断是否存在外部引用
            if (dbRef != null) {
                String param_name = field.getName();
                if (!dbRef.param_name().equals("")) {
                    param_name = dbRef.param_name();
                }
                try {
                    //判断引用是否也需要update
                    Object dbref = field.get(object);
                    if (dbref != null) {
                        ObjectId objectId = getObjectId(dbref);
                        if (objectId != null) {
                            //如果传入的外键是已经存在的话.那么就不进行保存了进行修改操作.
                            update(dbref);
                        } else {
                            ObjectId inner = saveInner(dbref);
                            document.append(param_name, inner);
                        }
                    } else {
                        document.append(param_name, null);
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


    /**
     * 暂不支持并联删除
     *
     * @param object 需要删除的数据
     * @return
     */
    public synchronized static int remove(Object object) {
        Class<?> aClass = object.getClass();
        MongoCollection<Document> collection = getCollection(aClass);
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
                    if (obj == null) {
                        dbObject.put("_id", null);

                    } else {
                        dbObject.put("_id", new ObjectId(obj));
                    }
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

    /**
     * update方式支持以及存在数据库当中的数据进行修改的操作
     *
     * @param object
     * @return
     */
    public synchronized static int update(Object object) {
        Class<?> aClass = object.getClass();
        MongoCollection<Document> collection = getCollection(aClass);

        Document document = new Document();
        Field[] fields = aClass.getDeclaredFields();
        Bson query = null;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            Param param = field.getAnnotation(Param.class);
            DbRef dbRef = field.getAnnotation(DbRef.class);
            Id id = field.getAnnotation(Id.class);
            document = getParamDocument(object, document, field, param);
            if (id != null) {
                String param_name = field.getName();
                if (!id.param_name().equals("")) {
                    param_name = id.param_name();
                }
                if (param_name.equals("id")) {
                    param_name = "_id";
                }
                try {
                    if (field.get(object) == null) {
                        query = Filters.eq(param_name, null);
                    } else {
                        query = Filters.eq(param_name, new ObjectId((String) field.get(object)));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            if (dbRef != null) {
                String param_name = field.getName();
                if (!dbRef.param_name().equals("")) {
                    param_name = dbRef.param_name();
                }
                try {
                    //判断引用是否也需要update
                    Object dbref = field.get(object);
                    if (dbref != null) {
                        ObjectId inner = getObjectId(dbref);
                        //如果返回的是null的话说明没有初始化
                        if (inner == null) {
                            inner = saveInner(field.get(object));
                            document.append(param_name, inner);
                        } else {
                            int update = update(dbref);
                        }
                    } else {
                        document.append(param_name, null);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        UpdateResult updateResult = collection.updateOne(query, new Document("$set", document));
        log.info("数据修改:" + updateResult);
        return (int) updateResult.getModifiedCount();
    }

    private synchronized static Document getParamDocument(Object object, Document document, Field field, Param param) {
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
        return document;
    }


    /**
     * 通过传入的object类获取他的id
     *
     * @param obj
     * @return
     */
    private synchronized static ObjectId getObjectId(Object obj) {
        if (obj == null)
            return null;
        Class<?> aClass = obj.getClass();
        MongoCollection<Document> collection = getCollection(aClass);
        Field[] fields = aClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            Id id = fields[i].getAnnotation(Id.class);
            if (id != null) {
                try {
                    //从数据库中查找是否已经存在相应id
                    Object value = fields[i].get(obj);
                    if (value != null) {
                        Document document = new Document();
                        String param_name = fields[i].getName();
                        if (!id.param_name().equals("")) {
                            param_name = id.param_name();
                        }
                        if (param_name.equals("id")) {
                            param_name = "_id";
                        }
                        boolean hasNext = collection.find(document.append(param_name, new ObjectId((String) value))).iterator().hasNext();
                        if (hasNext) {
                            return new ObjectId((String) fields[i].get(obj));
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     * 传入相应的外部map参数,删除指定参数的数据
     *
     * @param filter 条件
     * @param aClass 需要删除数据的对象class
     * @param multi  是否删除多条记录
     * @return 删除的数量
     */
    public synchronized static int deleteOneOrMany(HashMap<String, Object> filter, Class<?> aClass, boolean multi) {
        //从collection当中查询相应的id.
        MongoCollection<Document> collection = getCollection(aClass);
        Document document = new Document();
        Iterator<Map.Entry<String, Object>> iterator = filter.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            document.append(entry.getKey(), entry.getValue());
        }
        DeleteResult deleteResult = collection.deleteMany(document);
        return (int) deleteResult.getDeletedCount();
    }


    /**
     * 通过传入外部的bson代码进行获取相应的数据
     *
     * @param aClass 需要的例行
     * @param bson   beso数据
     * @return List 返回查询到的类
     */
    public synchronized static List select(Class<?> aClass, Bson bson) {
        MongoCollection<Document> collection = getCollection(aClass);
        FindIterable<Document> documents = collection.find(bson);
        MongoCursor<Document> iterator = documents.iterator();
        List list = new ArrayList();
        while (iterator.hasNext()) {
            //遍历所有的属性
            Document document = iterator.next();
            Object convert = convertToClass(aClass, document);
            list.add(convert);
        }
        return list;
    }


    /**
     * 获取一个Collection当中的所有数据
     *
     * @param aClass 传入的类类型
     * @return 集合
     */
    public synchronized static List selectAll(Class<?> aClass) {
        MongoCollection<Document> collection = getCollection(aClass);
        FindIterable<Document> documents = collection.find();
        MongoCursor<Document> iterator = documents.iterator();
        List list = new ArrayList();
        while (iterator.hasNext()) {
            //遍历所有的属性
            Document document = iterator.next();
            Object convert = convertToClass(aClass, document);
            list.add(convert);
        }
        return list;
    }


    /**
     * 通过一个Class对象获取Collection对象
     *
     * @param aClass
     * @return 返回的Collection对象
     */
    private synchronized static MongoCollection<Document> getCollection(Class<?> aClass) {
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
        if (collection == null) {
            //如果collection对象为null的话说明数据库当中没有存在collection.
            //自动创建一个
            MongoServer.database.createCollection(collection_name);
            log.info("没有找到" + collection_name + "的collection自动创建完成.");
        }
        return collection;
    }


}
