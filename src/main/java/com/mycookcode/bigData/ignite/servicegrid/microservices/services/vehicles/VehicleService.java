package com.mycookcode.bigData.ignite.servicegrid.microservices.services.vehicles;

import com.mycookcode.bigData.ignite.servicegrid.microservices.pojo.Vehicle;
import org.apache.ignite.services.Service;

/**
 * Created by zhaolu on 2018/2/12.
 */
public interface VehicleService extends Service {

    public static final String SERVICE_NAME = "VehicleService";

    /**
     * 待用服务添加新的车辆
     *
     * @param vehicleId
     * @param vehicle
     */
    public void addVehicle(int vehicleId, Vehicle vehicle);

    /**
     * 根据ID获得车险详细信息
     *
     * @param vehicleId
     * @return
     */
    public Vehicle getVehicle(int vehicleId);


    /**
     * 调用服务删除车辆信息
     *
     * @param vehicleId
     */
    public void removeVehicle(int vehicleId);
}
