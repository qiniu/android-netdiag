package com.qiniu.android.netdiag;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by bailong on 16/2/24.
 */
public final class IP {
    public static String external() throws IOException{
        return Util.httpGetString("http://whatismyip.akamai.com");
    }

    public static String local(){
        DatagramSocket socket;
        try {
            socket = new DatagramSocket();
            InetAddress addr = InetAddress.getByName("114.114.114.114");
            socket.connect(addr, 53);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        InetAddress local = socket.getLocalAddress();
        socket.close();
        return local.getHostAddress();
    }
}
