package com.mycookcode.bigData.ignite.servicegrid.microservices.services.maintenance;

import com.mycookcode.bigData.ignite.servicegrid.microservices.pojo.Maintenance;
import com.mycookcode.bigData.ignite.servicegrid.microservices.services.vehicles.VehicleService;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicSequence;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.SqlQuery;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.services.ServiceContext;

import javax.cache.Cache;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zhaolu on 2018/2/12.
 */
public class MaintenanceServiceImpl implements MaintenanceService {

    @IgniteInstanceResource
    private Ignite ignite;


    private IgniteCache<Integer, Maintenance> maintCache;


    /*生成ID*/
    private IgniteAtomicSequence sequence;


    private ExternalCallsProcessor externalCallsProcessor;


    public void init(ServiceContext ctx) throws Exception
    {
        System.out.println("Initializing Maintenance Service on node:" + ignite.cluster().localNode());

        maintCache = ignite.cache("maintenance");

        externalCallsProcessor = new ExternalCallsProcessor();

        externalCallsProcessor.start();
    }

    public void execute(ServiceContext ctx) throws Exception
    {
        System.out.println("Executing Maintenance Service on node:" + ignite.cluster().localNode());
        sequence = ignite.atomicSequence("MaintenanceIds", 1, true);
    }

    public void cancel(ServiceContext ctx)
    {
        System.out.println("Stopping Maintenance Service on node:" + ignite.cluster().localNode());
        externalCallsProcessor.interrupt();
    }

    public Date scheduleVehicleMaintenance(int vehicleId)
    {
        Date date = new Date();
        VehicleService vehicleService = ignite.services().serviceProxy(VehicleService.SERVICE_NAME,
                VehicleService.class, false);
        //调用远程服务
        if (vehicleService.getVehicle(vehicleId) == null)
            throw new RuntimeException("Vehicle with provided ID doesn't exist:" + vehicleId);

        maintCache.put((int)sequence.getAndIncrement(), new Maintenance(vehicleId, date));
        return date;

    }

    public List<Maintenance> getMaintenanceRecords(int vehicleId)
    {
        SqlQuery<Long, Maintenance> query = new SqlQuery<Long, Maintenance>(Maintenance.class,
                "WHERE vehicleId = ?").setArgs(vehicleId);

        List<Cache.Entry<Long, Maintenance>> res = maintCache.query(query).getAll();

        List<Maintenance> res2 = new ArrayList<Maintenance>(res.size());

        for (Cache.Entry<Long, Maintenance> entry : res)
            res2.add(entry.getValue());

        return res2;
    }



    /**
     * 不使用Apache ignite API接受外部程序的调用
     *
     */
    private class ExternalCallsProcessor extends Thread
    {
        /** 外部连接的socket服务. */
        private ServerSocket externalConnect;

        @Override
        public void run()
        {
            try {
                externalConnect = new ServerSocket(50000);
            while (!isInterrupted())
            {
                Socket socket = externalConnect.accept();
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                int vehicleId = dis.readInt();
                List<Maintenance> result = getMaintenanceRecords(vehicleId);
                ObjectOutputStream dos = new ObjectOutputStream(socket.getOutputStream());
                dos.writeObject(result);

                dis.close();
                dos.close();
                socket.close();
            }

            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        @Override public void interrupt() {
            super.interrupt();

            try {
                externalConnect.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
