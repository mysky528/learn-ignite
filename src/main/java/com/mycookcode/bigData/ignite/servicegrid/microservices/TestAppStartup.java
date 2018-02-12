package com.mycookcode.bigData.ignite.servicegrid.microservices;

import com.mycookcode.bigData.ignite.servicegrid.microservices.pojo.Vehicle;
import com.mycookcode.bigData.ignite.servicegrid.microservices.services.maintenance.MaintenanceService;
import com.mycookcode.bigData.ignite.servicegrid.microservices.services.vehicles.VehicleService;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * 使用Ignite客户端节点访问服务网格集群
 *
 * Created by zhaolu on 2018/2/12.
 */
public class TestAppStartup {

    /*车辆品牌*/
    private static final String[] VEHICLES_NAMES = new String [] {
            "TOYOTA", "BMW", "MERCEDES", "HYUNDAI", "FORD"};

    /*车辆数量*/
    public static int TOTAL_VEHICLES_NUMBER = 10;


    public static void main(String[] args) throws IgniteException {
        Ignite ignite = Ignition.start("microservices/client-node-config.xml");

        System.out.println("Client node has connected to the cluster");

        //访问车辆缓存数据
        IgniteCache<Integer, Vehicle> vehiclesCache = ignite.cache("vehicles");
        Random rand = new Random();
        Calendar calendar = Calendar.getInstance();

        //向车辆缓存中填充数据
        for(int i = 0;i < TOTAL_VEHICLES_NUMBER;i++)
        {
            calendar.set(Calendar.MONTH, rand.nextInt(12));
            calendar.set(Calendar.YEAR, 2000 + rand.nextInt(17));
            vehiclesCache.put(i, new Vehicle(
                    VEHICLES_NAMES[rand.nextInt(VEHICLES_NAMES.length)],
                    calendar.getTime(),
                    (double)(11000 + rand.nextInt(10000))
            ));
        }

        System.out.println("Filled in Vehicles cache. Entries number: " + vehiclesCache.size());

        //使用服务代理访问远程部署的车辆服务。代理必须在没有部署本地服务的节点上使用
        VehicleService vehicleService = ignite.services().serviceProxy(VehicleService.SERVICE_NAME,
                VehicleService.class, false);

        System.out.println("Getting info for a random vehicle using VehiclesService: " + vehicleService.getVehicle(
                rand.nextInt(TOTAL_VEHICLES_NUMBER)));


        MaintenanceService maintenanceService = ignite.services().serviceProxy(MaintenanceService.SERVICE_NAME,
                MaintenanceService.class, false);

        int vehicleId = rand.nextInt(TOTAL_VEHICLES_NUMBER);

        Date date = maintenanceService.scheduleVehicleMaintenance(vehicleId);

        System.out.println("Scheduled maintenance service [vehicleID=" + vehicleId + ", " + "date=" + date + ']');

    }
}
