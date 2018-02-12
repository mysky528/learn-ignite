package com.mycookcode.bigData.ignite.servicegrid.microservices.app;

import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;

/**
 * Created by zhaolu on 2018/2/12.
 */
public class MaintenanceServiceNodeStartup {

    public static void main(String[] args) throws IgniteException {
        Ignition.start("microservices/maintenance-service-node-config.xml");
    }
}
