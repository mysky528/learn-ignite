package com.mycookcode.bigData.ignite.servicegrid.microservices.services;

import com.mycookcode.bigData.ignite.servicegrid.microservices.TestAppStartup;
import com.mycookcode.bigData.ignite.servicegrid.microservices.pojo.Maintenance;

import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.List;

/**
 *
 *
 * Created by zhaolu on 2018/2/12.
 */
public class ExternalTestApp {

    public static void main(String[] args)
    {
        try{
            for (int vehicleId = 0; vehicleId < TestAppStartup.TOTAL_VEHICLES_NUMBER; vehicleId++)
            {
                System.out.println(" >>> Getting maintenance schedule for vehicle:" + vehicleId);

                Socket socket = new Socket("127.0.0.1", 50000);

                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                dos.writeInt(vehicleId);

                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                List<Maintenance> result = (List<Maintenance>)ois.readObject();

                for (Maintenance maintenance : result)
                    System.out.println("    >>>   " + maintenance);

                dos.close();
                ois.close();
                socket.close();

            }

            System.out.println(" >>> Shutting down the application.");
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
