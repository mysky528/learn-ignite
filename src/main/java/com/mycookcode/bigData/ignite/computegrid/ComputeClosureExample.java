package com.mycookcode.bigData.ignite.computegrid;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;

import java.util.Arrays;
import java.util.Collection;

/**
 * 演示闭包运算
 *
 * 闭包是一个代码块，他是把代码体和任何外部变量包装起来然后以一个函数对象的形式在内部使用他们，
 * 然后可以在任何传入一个变量的地方传递这样一个函数对象，然后执行。所有的apply方法都可以在集群内执行闭包
 *
 * Created by zhaolu on 2018/2/10.
 */
public class ComputeClosureExample {

    public static void main(String[] args) throws IgniteException
    {
        try (Ignite ignite = Ignition.start("example-ignite.xml")){

            System.out.println();
            System.out.println(">>> Compute closure example started.");

            //在所有集群的节点上执行闭包运算
            Collection<Integer> res = ignite.compute().apply((String word) -> {
                System.out.println();
                System.out.println(">>> Printing '" + word + "' on this node from ignite job.");
                //返回单词的长度
                return word.length();
            },
                    //任务参数。Ignite会创建许多任务
                    Arrays.asList("Count characters using closure".split(" ")));

            int sum = res.stream().mapToInt(i -> i).sum();

            System.out.println();
            System.out.println(">>> Total number of characters in the phrase is '" + sum + "'.");
            System.out.println(">>> Check all nodes for output (this node is also part of the cluster).");
        }
    }
}
