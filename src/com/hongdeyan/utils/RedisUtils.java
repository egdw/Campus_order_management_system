package com.hongdeyan.utils;

import redis.clients.jedis.Jedis;

/**
 * Created by hdy on 18-10-25.
 * Redis的简易工具类
 */
public class RedisUtils {

    private Jedis jedis;
    private String key;

    public RedisUtils(Jedis jedis, String key) {
        this.jedis = jedis;
        this.key = key;
    }

    public Jedis getJedis() {
        return jedis;
    }

    public void setJedis(Jedis jedis) {
        this.jedis = jedis;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * 判断字段是否存在
     *
     * @return
     */
    public boolean exist() {
        String s = jedis.get(key);
        if (s == null) {
            return false;
        }
        return true;
    }

    /**
     * 添加数据
     *
     * @param param 参数
     * @param close 是否关闭
     */
    public void set(String param, boolean close) {
        String s = jedis.set(key, param);
        if (close) {
            close();
        }
    }


    /**
     * 添加数据并且添加过期时间
     *
     * @param param 参数
     * @param times 时间
     * @param close 是否关闭
     */
    public void setAndExpire(String param, int times, boolean close) {
        String s = jedis.set(key, param);
        jedis.expire(key, times);
        if (close) {
            close();
        }
    }


    /**
     * 获取数据
     *
     * @return 获取的参数
     */
    public String get() {
        String s = jedis.get(key);
        return s;
    }

    public String get(boolean close) {
        String s = jedis.get(key);
        if (close) {
            jedis.close();
        }
        return s;
    }

    public boolean remove() {
        Long del = jedis.del(key);
        return del > 0 ? true : false;
    }

    public void close() {
        jedis.close();
    }

}