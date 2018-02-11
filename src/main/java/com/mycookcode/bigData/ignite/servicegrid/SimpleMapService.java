package com.mycookcode.bigData.ignite.servicegrid;

/**
 * Created by zhaolu on 2018/2/11.
 */
public interface SimpleMapService<K,V> {

    void put(K key, V val);

    V get(K key);

    void clear();

    int size();
}
