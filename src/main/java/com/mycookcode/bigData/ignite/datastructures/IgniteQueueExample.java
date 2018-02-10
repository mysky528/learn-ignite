package com.mycookcode.bigData.ignite.datastructures;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.IgniteQueue;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CollectionConfiguration;
import org.apache.ignite.lang.IgniteRunnable;

import java.util.UUID;

import static org.apache.ignite.cache.CacheMode.PARTITIONED;

/**
 * 分布式队列演示
 *
 * Created by zhaolu on 2018/2/10.
 */
public class IgniteQueueExample {

    /*重试次数*/
    private static final int RETRIES = 20;

    private static IgniteQueue<String> queue;


    public static void main(String[] args) throws Exception
    {
        try (Ignite ignite = Ignition.start("example-ignite.xml")){
            System.out.println();
            System.out.println(">>> Ignite queue example started.");

            String queueName = UUID.randomUUID().toString();
            queue = initializeQueue(ignite, queueName);

            readFromQueue(ignite);

            writeToQueue(ignite);

            clearAndRemoveQueue();
        }
    }

    /**
     * 从队列中读取数据
     * @param ignite
     * @throws IgniteException
     */
    private static void readFromQueue(Ignite ignite) throws IgniteException
    {
        final String queueName = queue.name();
        ignite.compute().broadcast(new QueueClosure(queueName, false));
        System.out.println("Queue size after reading [expected=0, actual=" + queue.size() + ']');
    }

    /**
     * 初始化队列
     */
    private static IgniteQueue<String> initializeQueue(Ignite ignite, String queueName) throws IgniteException
    {
        CollectionConfiguration colCfg = new CollectionConfiguration();
        colCfg.setCacheMode(PARTITIONED);

        //初始化FIFO队列
        IgniteQueue<String> queue = ignite.queue(queueName, 0, colCfg);
        //使用阻塞操作，必须指定队列的大小
        for (int i = 0; i < ignite.cluster().nodes().size() * RETRIES * 2; i++){
            queue.put(Integer.toString(i));
        }
        System.out.println("Queue size after initializing: " + queue.size());
        return queue;
    }


    /**
     * 写数据到各个节点的队列中
     * @param ignite
     */
    private static void writeToQueue(Ignite ignite){
        final String queueName = queue.name();
        ignite.compute().broadcast(new QueueClosure(queueName,true));

        System.out.println("Queue size after writing [expected=" + ignite.cluster().nodes().size() * RETRIES +
                ", actual=" + queue.size() + ']');

        System.out.println("Iterate over queue.");

        for (String item : queue)
            System.out.println("Queue item: " + item);
    }

    /**
     * 从队列中清除数据
     *
     * @throws IgniteException If execution failed.
     */
    private static void clearAndRemoveQueue() throws IgniteException {
        System.out.println("Queue size before clearing: " + queue.size());


        queue.clear();

        System.out.println("Queue size after clearing: " + queue.size());


        queue.close();


        try {
            queue.poll();
        }
        catch (IllegalStateException expected) {
            System.out.println("Expected exception - " + expected.getMessage());
        }
    }

    private static class QueueClosure implements IgniteRunnable
    {
        private final String queueName;

        private final boolean put;

        QueueClosure(String queueName, boolean put) {
            this.queueName = queueName;
            this.put = put;
        }

        @Override public void run()
        {
            IgniteQueue<String> queue = Ignition.ignite().queue(queueName, 0, null);
            if(put)
            {
                UUID locId = Ignition.ignite().cluster().localNode().id();
                for (int i = 0; i < RETRIES; i++) {
                    String item = locId + "_" + Integer.toString(i);

                    queue.put(item);

                    System.out.println("Queue item has been added: " + item);
                }
            }else {
                //从队列头部读取数据
                for (int i = 0; i < RETRIES; i++)
                    System.out.println("Queue item has been read from queue head: " + queue.take());

                //使用poll方式从队列再次取出
                for (int i = 0; i < RETRIES; i++)
                    System.out.println("Queue item has been read from queue head: " + queue.poll());
            }
        }
    }
}
