package com.neusoft.babymonitor.backend.webcam.upnp;

import static com.neusoft.babymonitor.backend.webcam.Constants.*;

/*
 This file is part of “Onni smart care desktop application” software
 Copyright (C) <2013>  Erasmus van Niekerk <erasmus.van.nieker@gmail.com>

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

import com.neusoft.babymonitor.backend.webcam.Constants;
import com.neusoft.babymonitor.backend.webcam.exception.UPnPNotWorkingException;
import com.neusoft.babymonitor.backend.webcam.ui.observer.StreamingStatus;
import com.neusoft.babymonitor.backend.webcam.util.RuntimeUtils;
import com.neusoft.babymonitor.backend.webcam.util.network.NetworkUtil;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Class that provides UPnP features. It's based on Weupnp library.
 */
public class UPnPHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(UPnPHelper.class);
    /** This port is as alternative for 554 **/
    private static final int RTSP_ALT = 8554;

    private int httpPort;
    private int rtspPort;
    private String externalIPAddress = "";
    private GatewayDevice activeGatewayDevice;

    public UPnPHelper(int httpPort, int rtspPort) {
        this.httpPort = httpPort;
        this.rtspPort = rtspPort;
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                stopWeupnp();
            }
        }));
    }

    public void addPortMapping() throws Exception {
        LOGGER.info("Entered add port mapping {} with rtspPort {}", httpPort, rtspPort);
        // kill the process
        RuntimeUtils.destroyProcess(rtspPort);
        GatewayDiscover gatewayDiscover = new GatewayDiscover();

        Map<InetAddress, GatewayDevice> gateways = gatewayDiscover.discover();

        if (gateways.isEmpty()) {
            LOGGER.info("No gateways found, the UPnP will not work........");
            throw new UPnPNotWorkingException("No gateway was found");
        }

        // choose the first active gateway for the tests
        // this is the most important thing for upnp
        // if IGD was found then there are huge chances that portmapping to work
        activeGatewayDevice = gatewayDiscover.getValidGateway();

        if (null != activeGatewayDevice) {
            LOGGER.info("Using gateway {} ", activeGatewayDevice.getFriendlyName());
            StreamingStatus.getInstance().setUpnpWorked(true);
        } else {
            LOGGER.info("No gateways found, the UPnP will not work........");
            throw new UPnPNotWorkingException("No active gateway was found....");
        }

        // create a portMapping
        PortMappingEntry portMapping = new PortMappingEntry();

        RuntimeUtils.destroyProcess(rtspPort);
        // if '554' is still occupied, then this port is still used and it can't be killed programmatically
        // this means that Java does not has full rights, use another port as start
        // In Linux, and other UNIX-like systems, you have to be root (have superuser privileges) in order to use ports
        // below 1024. 554 port cannot be used, use another port as start.
        if (!NetworkUtil.isAvailable(rtspPort) || System.getProperty("os.name").startsWith(LINUX)) {
            rtspPort = RTSP_ALT;
        }
        httpPort = openPortOnRouter(portMapping, httpPort, 10000);
        rtspPort = openPortOnRouter(portMapping, rtspPort, 50000);
        setExternalIPAddress(activeGatewayDevice.getExternalIPAddress());
    }

    /**
     * Opens an available port. Available means that is available also on router and also that it's not used by some
     * service.
     * 
     * @param portMapping the portmapping that is created
     * @param port the port that is
     * @param endRange
     * @return
     * @throws IOException
     * @throws SAXException
     */
    private int openPortOnRouter(PortMappingEntry portMapping, int port, int endRange) throws IOException, SAXException {
        boolean isPortAdded = false;
        do {
            if (activeGatewayDevice.getSpecificPortMappingEntry(port, "TCP", portMapping)) {
                LOGGER.debug(
                        "Port {} is already mapped. Trying to open another port.But first try if that port is not used on the system",
                        port);
                port++;
                port = NetworkUtil.getPort(port, endRange, 1);
            } else {
                LOGGER.info("Mapping free. Sending port mapping request for port {}", port);
                if (activeGatewayDevice.addPortMapping(port, port, Constants.INTERNAL_IP, "TCP", "Onni SmartCare ")) {
                    LOGGER.info("Port added successfully {}", port);
                    isPortAdded = true;
                }
            }
        } while (!isPortAdded);

        return port;
    }

    private boolean stopWeupnp() {
        try {
            if (activeGatewayDevice != null) {
                boolean wasHttpDeleted = activeGatewayDevice.deletePortMapping(httpPort, "TCP");
                boolean wasRtspPortDeleted = activeGatewayDevice.deletePortMapping(rtspPort, "TCP");
                return wasHttpDeleted && wasRtspPortDeleted;
            }
        } catch (IOException e) {
            LOGGER.info("IOexception when trying to delete port {}", e);
        } catch (SAXException e) {
            LOGGER.info("SAXException when trying to delete  port {}", e);
        }
        return false;
    }

    public void setRtspPort(int rtspPort) {
        this.rtspPort = rtspPort;
    }

    public String getExternalIPAddress() {
        return externalIPAddress.equals("") ? Constants.INTERNAL_IP : externalIPAddress;
    }

    public void setExternalIPAddress(String externalIPAddress) {
        this.externalIPAddress = externalIPAddress;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public int getRtspPort() {
        return rtspPort;
    }

}
