package com.mycookcode.bigData.ignite.servicegrid.microservices.filters;

import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.lang.IgnitePredicate;

/**
 * Created by zhaolu on 2018/2/12.
 */
public class MaintenanceServiceFilter implements IgnitePredicate<ClusterNode> {

    public boolean apply(ClusterNode node) {
        Boolean dataNode = node.attribute("maintenance.service.node");
        return dataNode != null && dataNode;
    }
}
