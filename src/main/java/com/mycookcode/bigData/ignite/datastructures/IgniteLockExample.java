package com.mycookcode.bigData.ignite.datastructures;

import org.apache.ignite.*;
import org.apache.ignite.lang.IgniteRunnable;

/**
 * 演示基于可重入锁的缓存
 *
 * Created by zhaolu on 2018/2/10.
 */
public class IgniteLockExample {

    private static final int OPS_COUNT = 100;

    private static final int NUM_PRODUCERS = 5;

    private static final int NUM_CONSUMERS = 5;

    private static final String CACHE_NAME = "cache";

    private static final String QUEUE_ID = "queue";

    private static final String SYNC_NAME = "done";

    private static final String NOT_FULL = "notFull";

    private static final String NOT_EMPTY = "notEmpty";

    private abstract static class ReentrantLockExampleClosure implements IgniteRunnable
    {
        protected final String reentrantLockName;

        ReentrantLockExampleClosure(String reentrantLockName) {
            this.reentrantLockName = reentrantLockName;
        }

    }

    /**
     * 模拟集群的生产者
     */
    private static class Producer extends ReentrantLockExampleClosure
    {
        public Producer(String reentrantLockName) {
            super(reentrantLockName);
        }

        @Override public void run()
        {
            System.out.println("Producer started. ");
            IgniteLock lock = Ignition.ignite().reentrantLock(reentrantLockName, true, false, true);

            //当队列为满的时候等待
            IgniteCondition notFull = lock.getOrCreateCondition(NOT_FULL);

            //当队列为空的时候等待
            IgniteCondition notEmpty = lock.getOrCreateCondition(NOT_EMPTY);

            //当job执行完毕的时候等待
            IgniteCondition done = lock.getOrCreateCondition(SYNC_NAME);

            IgniteCache<String, Integer> cache = Ignition.ignite().cache(CACHE_NAME);

            for (int i = 0; i < OPS_COUNT; i++)
            {
                try {
                    lock.lock();
                    int val = cache.get(QUEUE_ID);
                    while(val >= 100){
                        System.out.println("Queue is full. Producer [nodeId=" + Ignition.ignite().cluster().localNode().id() +
                                " paused.");

                        notFull.await();

                        val = cache.get(QUEUE_ID);
                    }

                    val++;
                    System.out.println("Producer [nodeId=" + Ignition.ignite().cluster().localNode().id() +
                            ", available=" + val + ']');

                    cache.put(QUEUE_ID, val);

                    notEmpty.signalAll();
                }finally {
                    lock.unlock();
                }

                System.out.println("Producer finished [nodeId=" + Ignition.ignite().cluster().localNode().id() + ']');
                try {
                    lock.lock();

                    int count = cache.get(SYNC_NAME);

                    count--;

                    cache.put(SYNC_NAME, count);

                    // Signals the master thread.
                    done.signal();
                }
                finally {
                    lock.unlock();
                }
            }
        }
    }

    /**
     * 模拟消费者
     */
    private static class Consumer extends ReentrantLockExampleClosure
    {
        public Consumer(String reentrantLockName) {
            super(reentrantLockName);
        }

        @Override public void run()
        {
            System.out.println("Consumer started. ");

            Ignite g = Ignition.ignite();
            IgniteLock lock = g.reentrantLock(reentrantLockName, true, false, true);

            IgniteCondition notFull = lock.getOrCreateCondition(NOT_FULL);
            IgniteCondition notEmpty = lock.getOrCreateCondition(NOT_EMPTY);
            IgniteCondition done = lock.getOrCreateCondition(SYNC_NAME);
            IgniteCache<String, Integer> cache = g.cache(CACHE_NAME);
            for (int i = 0; i < OPS_COUNT; i++)
            {
                try {
                    lock.lock();
                    int val = cache.get(QUEUE_ID);
                    while (val <= 0) {
                        System.out.println("Queue is empty. Consumer [nodeId=" +
                                Ignition.ignite().cluster().localNode().id() + " paused.");

                        notEmpty.await();

                        val = cache.get(QUEUE_ID);
                    }

                    val--;

                    System.out.println("Consumer [nodeId=" + Ignition.ignite().cluster().localNode().id() +
                            ", available=" + val + ']');

                    cache.put(QUEUE_ID, val);

                    notFull.signalAll();
                }finally {
                    lock.unlock();
                }

            }

            System.out.println("Consumer finished [nodeId=" + Ignition.ignite().cluster().localNode().id() + ']');

            try {
                lock.lock();

                int count = cache.get(SYNC_NAME);

                count--;

                cache.put(SYNC_NAME, count);

                // Signals the master thread.
                done.signal();
            }
            finally {
                lock.unlock();
            }
        }

    }
}
