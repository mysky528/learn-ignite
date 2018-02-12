package com.mycookcode.bigData.ignite.servicegrid.microservices.services.vehicles;

import com.mycookcode.bigData.ignite.servicegrid.microservices.pojo.Vehicle;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.services.ServiceContext;
import sun.reflect.annotation.ExceptionProxy;

/**
 * Created by zhaolu on 2018/2/12.
 */
public class VehicleServiceImpl implements VehicleService {

    @IgniteInstanceResource
    private Ignite ignite;

    /*将车辆对象映射到缓存中*/
    private IgniteCache<Integer, Vehicle> vehiclesCache;


    public void init(ServiceContext ctx) throws Exception
    {
        System.out.println("Initializing Vehicle Service on node:" + ignite.cluster().localNode());
        vehiclesCache = ignite.cache("vehicles");
    }


    public void execute(ServiceContext ctx) throws Exception
    {
        System.out.println("Executing Vehicle Service on node:" + ignite.cluster().localNode());
    }

    public void cancel(ServiceContext ctx) {
        System.out.println("Stopping Vehicle Service on node:" + ignite.cluster().localNode());

    }


    public void addVehicle(int vehicleId, Vehicle vehicle) {
        vehiclesCache.put(vehicleId, vehicle);
    }

    public Vehicle getVehicle(int vehicleId) {
        return vehiclesCache.get(vehicleId);
    }


    public void removeVehicle(int vehicleId) {
        vehiclesCache.remove(vehicleId);
    }


}
