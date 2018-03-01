package com.mycookcode.bigData.ignite.persistentstore;

import org.apache.ignite.Ignition;

/**
 * Created by zhaolu on 2018/2/13.
 */
public class PersistentStoreExampleNodeStartup {


    public static void main(String[] args) throws Exception {
        Ignition.start("example-persistent-store.xml");
    }
}
