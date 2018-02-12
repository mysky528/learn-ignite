package com.mycookcode.bigData.ignite.servicegrid.microservices.filters;

import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.lang.IgnitePredicate;

/**
 * 为每个数据缓存配置节点过滤策略
 *
 * Created by zhaolu on 2018/2/11.
 */
public class DataNodeFilter implements IgnitePredicate<ClusterNode> {

    public boolean apply(ClusterNode node) {
        Boolean dataNode = node.attribute("data.node");

        return dataNode != null && dataNode;
    }
}
