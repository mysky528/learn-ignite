package com.mycookcode.bigData.ignite.streaming.wordcount.socket;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;


/**
 * 演示在组件外部将数据以流的方式加载到ignite cache中(客户端读取数据传输到服务端)
 *
 * Created by zhaolu on 2018/2/9.
 */
public class WordsSocketStreamerClient {


    private static  final int PORT = 5555;

    private static final byte[] DELIM = new byte[]{0};


    public static void main(String[] args)throws Exception
    {
        InetAddress addr = InetAddress.getLocalHost();

        try( Socket sock = new Socket(addr,PORT);
             OutputStream oos = new BufferedOutputStream(sock.getOutputStream());) {
            System.out.println("Words streaming started.");

            while (true)
            {
                try(InputStream in = WordsSocketStreamerClient.class.getResourceAsStream("alice-in-wonderland.txt");
                    LineNumberReader rdr = new LineNumberReader(new InputStreamReader(in))){
                    for(String line = rdr.readLine();line != null;line = rdr.readLine())
                    {
                        for(String word:line.split(" "))
                        {
                            if(!word.isEmpty())
                            {
                                byte[] arr = word.getBytes("ASCII");
                                oos.write(arr);
                                oos.write(DELIM);
                            }
                        }
                    }
                }
            }
        }
    }

}
