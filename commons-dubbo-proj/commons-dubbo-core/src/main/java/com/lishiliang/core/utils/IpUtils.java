package com.lishiliang.core.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
* @author: lisl 获取服务端的ip
*/
public class IpUtils {

    private static volatile String cachedIpAddress;

    public static String getIp() {
        if (null != cachedIpAddress) {
            return cachedIpAddress;
        } else {
            Enumeration netInterfaces;
            try {
                netInterfaces = NetworkInterface.getNetworkInterfaces();
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }

            String localIpAddress = null;

            while(netInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface)netInterfaces.nextElement();
                Enumeration ipAddresses = netInterface.getInetAddresses();

                while(ipAddresses.hasMoreElements()) {
                    InetAddress ipAddress = (InetAddress)ipAddresses.nextElement();
                    if (isPublicIpAddress(ipAddress)) {
                        String publicIpAddress = ipAddress.getHostAddress();
                        cachedIpAddress = publicIpAddress;
                        return publicIpAddress;
                    }

                    if (isLocalIpAddress(ipAddress)) {
                        localIpAddress = ipAddress.getHostAddress();
                    }
                }
            }

            cachedIpAddress = localIpAddress;
            return localIpAddress;
        }
    }

    private static boolean isPublicIpAddress(InetAddress ipAddress) {
        return !ipAddress.isSiteLocalAddress() && !ipAddress.isLoopbackAddress() && !isV6IpAddress(ipAddress);
    }

    private static boolean isLocalIpAddress(InetAddress ipAddress) {
        return ipAddress.isSiteLocalAddress() && !ipAddress.isLoopbackAddress() && !isV6IpAddress(ipAddress);
    }

    private static boolean isV6IpAddress(InetAddress ipAddress) {
        return ipAddress.getHostAddress().contains(":");
    }


}