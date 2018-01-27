package com.mycookcode.bigData.ignite.lifecycle;

import org.apache.ignite.lifecycle.LifecycleBean;
import org.apache.ignite.lifecycle.LifecycleEventType;

/**
 * Created by zhaolu on 2017/12/13.
 */
public class MyLifecycleBean implements LifecycleBean {

    @Override public void onLifecycleEvent(LifecycleEventType evt) {
        if (evt == LifecycleEventType.BEFORE_NODE_START) {
            System.out.println("-----ignite-----" + "before start");
        }
        if (evt == LifecycleEventType.AFTER_NODE_START) {
            System.out.println("-----ignite-----" + "after start");
        }
        if (evt == LifecycleEventType.BEFORE_NODE_STOP) {
            System.out.println("-----ignite-----" + "before stop");
        }
        if (evt == LifecycleEventType.AFTER_NODE_STOP) {
            System.out.println("-----ignite-----" + "after stop");
        }
    }
}
