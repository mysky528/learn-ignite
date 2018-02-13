package com.mycookcode.bigData.ignite.events;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.compute.ComputeTaskSession;
import org.apache.ignite.events.TaskEvent;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.lang.IgnitePredicate;
import org.apache.ignite.lang.IgniteRunnable;
import org.apache.ignite.resources.TaskSessionResource;

import java.util.UUID;

import static org.apache.ignite.events.EventType.EVTS_TASK_EXECUTION;

/**
 * Created by zhaolu on 2018/2/12.
 */
public class EventsExample {


    public static void main(String[] args) throws Exception
    {
        try (Ignite ignite = Ignition.start("example-ignite.xml")){
            System.out.println();
            System.out.println(">>> Events API example started.");

            localListen();

            remoteListener();

            Thread.sleep(1000);
        }
    }

    /**
     * 监听本地事件
     *
     * @throws Exception
     */
    private static void localListen() throws Exception{
        System.out.println();
        System.out.println(">>> Local event listener example.");

        Ignite ignite = Ignition.ignite();

        IgnitePredicate<TaskEvent> lsnr = evt -> {
            System.out.println("Received task event [evt=" + evt.name() + ", taskName=" + evt.taskName() + ']');
            return true; //返回true继续监听
        };

        //对本地所有的任务执行事件注册监听
        ignite.events().localListen(lsnr,EVTS_TASK_EXECUTION);

        //生成一个任务事件
        ignite.compute().withName("example-event-task").run(()->System.out.println("Executing sample job."));

        //取消监听
        ignite.events().stopLocalListen(lsnr);

    }

    /**
     * 远程事件的监听
     * @throws Exception
     */
    private static void remoteListener()throws Exception
    {
        System.out.println();
        System.out.println(">>> Remote event listener example.");

        //将监听远程的事件回调到本地
        IgniteBiPredicate<UUID,TaskEvent> locLsnr = (nodeId, evt) -> {
            assert evt.taskName().startsWith("good-task");
            System.out.println("Received task event [evt=" + evt.name() + ", taskName=" + evt.taskName());
            return true;
        };

        //远程任务过滤器：只接受"good-task"名称开头的任务事件
        IgnitePredicate<TaskEvent> rmtLsnr = evt -> evt.taskName().startsWith("good-task");

        Ignite ignite = Ignition.ignite();

        ignite.events().remoteListen(locLsnr,rmtLsnr,EVTS_TASK_EXECUTION);

        //生成任务事件
        for(int i = 0;i < 10;i++)
        {
            ignite.compute().withName(i < 5 ? "good-task-" + i:"bad-task" + i).run(new IgniteRunnable() {
                @TaskSessionResource
                private ComputeTaskSession ses;

                @Override
                public void run() {
                    System.out.println("Executing sample job for task: " + ses.getTaskName());
                }
            });
        }

    }
}
