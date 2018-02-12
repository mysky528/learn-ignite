package com.mycookcode.bigData.ignite.servicegrid.microservices.services.maintenance;

import com.mycookcode.bigData.ignite.servicegrid.microservices.pojo.Maintenance;
import org.apache.ignite.services.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by zhaolu on 2018/2/12.
 */
public interface MaintenanceService extends Service {

    public static final String SERVICE_NAME = "MaintenanceService";

    /**
     * 调度车辆
     * @param vehicleId
     * @return
     */
    public Date scheduleVehicleMaintenance(int vehicleId);



    /*获得车辆所有维护记录*/
    public List<Maintenance> getMaintenanceRecords(int vehicleId);




}
