package com.neusoft.babymonitor.backend.webcam.util.network;

/*
 This file is part of “Onni smart care desktop application” software
 Copyright (C) <2013>  Erasmus van Niekerk <erasmus.van.niekerk@sepsolutions.fi>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkUtil.class);

    public static String getIP() {
        LOGGER.debug("getIp{} enterered.....");
        String ip = "";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    String hostAddress = addr.getHostAddress();
                    if (hostAddress.startsWith("192") || hostAddress.startsWith("172")) {
                        ip = hostAddress;
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            // this should not happen also
            LOGGER.error("Error when trying to get the internalIp {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            LOGGER.error("Error when trying to get the internalIp {}", e.getMessage());
        }
        LOGGER.info("The ip found is {} ", ip);
        return ip;
    }

    /**
     * Retrieves the first available port between startRange and endRange.
     * 
     * @param startRange the start value of port
     * @param endRange the end value of port
     * @param incrementValue the value to increment
     * @return
     */
    public static int getPort(int startRange, int endRange, int incrementValue) {
        LOGGER.debug("getPort between startRange {} and  endRange {} , and the incrementValue is {}", startRange,
                endRange, incrementValue);
        for (int port = startRange; port <= endRange; port = port + incrementValue) {
            if (isAvailable(port)) {
                return port;
            }
        }
        LOGGER.error("No port was found between startRange {} and endRange {}", startRange, endRange);
        // TODO figure how to handle if no port is available
        throw new RuntimeException();
    }

    /**
     * Binds a socket on that port. If a ConnectException is caught than it means that the port is not used.
     * 
     * @param port the port that is tested
     * @return true/false if the port is available/unavailable
     */
    public static boolean isAvailable(int port) {
        LOGGER.debug("checkAvailability entered {}", port);

        String command[] = { "netstat", "-n", "-a" };
        try {
            Process nestat = Runtime.getRuntime().exec(command);
            BufferedReader processReader = new BufferedReader(new InputStreamReader(nestat.getInputStream()));
            String sdpInfoLine = processReader.readLine();
            while (sdpInfoLine != null) {
                if (sdpInfoLine.contains(":" + port)) {
                    return false;
                }
                sdpInfoLine = processReader.readLine();
            }
        } catch (IOException e) {
            LOGGER.error("Unable to run netstat command for port: {}", port);
        }
        return true;
    }
}
