package com.mycookcode.bigData.ignite.servicegrid.microservices.pojo;

import java.io.Serializable;
import java.util.Date;

/**
 * Maintenance的pojo
 *
 * Created by zhaolu on 2018/2/11.
 */
public class Maintenance implements Serializable{

    private int vehicleId;

    private Date date;

    public Maintenance(int vehicleId, Date date) {
        this.vehicleId = vehicleId;
        this.date = date;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override public String toString() {
        return "Maintenance{" +
                "vehicleId=" + vehicleId +
                ", date=" + date +
                '}';
    }
}
