package com.hongdeyan.static_class;

import com.hongdeyan.exception.NoDatabaseException;
import com.mongodb.MongoClient;
import com.mongodb.client.ListCollectionsIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

/**
 * 用于连接mongodb的数据库静态类
 */
public class MongoServer {

    //创建MongoDB的客户端
    public static MongoClient mongoClient;
    //对应名称的数据库
    public static MongoDatabase database;

    //初始化数据
    static {
        mongoClient = new MongoClient("localhost", 27017);
        database = mongoClient.getDatabase("campus_order_xx");
        if (database == null) {
            try {
                throw new NoDatabaseException();
            } catch (NoDatabaseException e) {
                e.printStackTrace();
            }
        } else {
            isCreated();
        }
    }

    /**
     * 判断数据库当中是否已经创建了相关的数据库和关系
     * 如果没有相应的collection的话就创建数据
     */
    public static void isCreated() {
        
    }
}
