package com.mycookcode.bigData.ignite.servicegrid.microservices.pojo;

import java.util.Date;

/**
 *
 *
 * Created by zhaolu on 2018/2/12.
 */
public class Vehicle {

    private String name;

    private Date year;

    private Double price;

    public Vehicle(String name, Date year, Double price) {
        this.name = name;
        this.year = year;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getYear() {
        return year;
    }

    public void setYear(Date year) {
        this.year = year;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

}
