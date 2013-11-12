package com.neusoft.babymonitor.backend.webcam.util;

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

import static com.neusoft.babymonitor.backend.webcam.Constants.WINDOWS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neusoft.babymonitor.backend.webcam.Constants;
import com.neusoft.babymonitor.backend.webcam.stream.WebcamThread;

public class WebcamUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebcamThread.class);

    private static WebcamUtil instance = new WebcamUtil();

    // selected inputs
    private String webcam;
    private String microphone;

    public static WebcamUtil getInstance() {
        return instance;
    }

    /**
     * Gets the audio video command that is needed by ffmpeg to start streaming.
     * 
     * @param webcamName name of the webcam
     * @param microphoneName name of the microphone
     * @return name of audio video command
     */
    public static String getAudioVideoCommand(String webcamName, String microphone) {
        LOGGER.debug("entered getAudioVideoCommand with webcamName {} and with microphone {}", webcamName, microphone);

        return "video=" + webcamName + ":audio=" + microphone;
    }

    private static List<String> getDshowLines() {
        LOGGER.debug("entered getDshowLines ");
        String getDevicesStr[] = { Constants.FFMPEG_PATH + "ffmpeg", "-list_devices", "true", "-f", "dshow", "-i",
                "dummy" };
        List<String> dshowLines = new ArrayList<String>();
        try {
            Process listDevicesProc = Runtime.getRuntime().exec(getDevicesStr);
            BufferedReader processOutput = new BufferedReader(new InputStreamReader(listDevicesProc.getErrorStream()));
            String outputLine = processOutput.readLine();
            while (outputLine != null) {
                // add only the dshow output from process
                if (outputLine.contains("dshow")) {
                    dshowLines.add(outputLine);
                }
                outputLine = processOutput.readLine();
            }
            listDevicesProc.destroy();
        } catch (IOException e) {
            LOGGER.error("error finding the ffmpeg folder at this path, check if the ffmpeg folder is not missing {}",
                    e);
        }

        return dshowLines;
    }

    private static List<String> getWindowsDeviceNames(int startPosition, int endPosition, List<String> dshowLines) {
        List<String> names = new ArrayList<String>();
        for (int i = ++startPosition; i < endPosition; i++) {
            String deviceName = dshowLines.get(i);
            int devicePosition = deviceName.indexOf("\"");
            names.add(deviceName.substring(devicePosition + 1, deviceName.length() - 1));
        }
        return names;
    }

    /**
     ** Get a list with the connected devices for Windows
     * 
     * @param deviceType the type of the device
     **/
    public static List<String> getWindowsDeviceNames(DeviceType deviceType) {
        LOGGER.debug("entered getWindowsDeviceNames with deviceType {}", deviceType);

        List<String> dshowLines = getDshowLines();
        switch (deviceType) {
        case WEBCAM:
            int startPosition = getPosition(dshowLines, "DirectShow video");
            int endPosition = getPosition(dshowLines, "DirectShow audio");
            return getWindowsDeviceNames(startPosition, endPosition, dshowLines);
        case MICROPHONE:
            startPosition = getPosition(dshowLines, "DirectShow audio");
            endPosition = dshowLines.size();
            return getWindowsDeviceNames(startPosition, endPosition, dshowLines);
        default:
            throw new IllegalArgumentException();
        }

    }

    /**
     ** Get a list with the connected devices for Linux
     **/
    public static List<String> getLinuxDeviceNames() {
        LOGGER.debug("entered getLinuxDeviceNames ");

        List<String> devices = new ArrayList<String>();
        // get connected webcams
        String getDevicesStr[] = { "./listLinuxVideo.sh" };
        try {
            Process listDevicesProc = Runtime.getRuntime().exec(getDevicesStr);
            BufferedReader processOutput = new BufferedReader(new InputStreamReader(listDevicesProc.getInputStream()));
            String outputLine = processOutput.readLine();
            while (outputLine != null) {
                devices.add(outputLine);
                outputLine = processOutput.readLine();
            }
            listDevicesProc.destroy();
        } catch (IOException e) {
            LOGGER.error("error while listing devices", e);
        }
        return devices;
    }

    private static int getPosition(List<String> dshowLines, String dshowLineName) {
        for (int i = 0; i < dshowLines.size(); i++) {
            // the first line after "DirectShow video devices" should be the name of the video device
            if (dshowLines.get(i).contains(dshowLineName)) {
                return i;
            }
        }

        return 0;
    }

    /**
     * Check if microphone and webcam windows inputs are connected.
     **/
    public boolean areWindowsInputsConnected() {
        if (getWindowsDeviceNames(DeviceType.WEBCAM).get(0).contains("Could not enumerate video devices")) {
            return false;
        }
        if (getWindowsDeviceNames(DeviceType.MICROPHONE).get(0).contains("Could not enumerate video devices")) {
            return false;
        }

        return true;
    }

    /**
     * Check if webcam linux inputs are connected.
     **/
    public boolean areLinuxInputsConnected() {
        if (getLinuxDeviceNames().size() == 0) {
            return false;
        }
        return true;
    }

    /**
     * Check if inputs are connected.
     **/
    public boolean areInputsConnected() {
        if (System.getProperty("os.name").startsWith(WINDOWS)) {
            // verify if webcam and microphone inputs are connected for windows
            return WebcamUtil.getInstance().areWindowsInputsConnected();
        } else {
            // verify if webcam are connected for linux
            return WebcamUtil.getInstance().areLinuxInputsConnected();
        }
    }

    public String getWebcam() {
        return webcam;
    }

    public void setWebcam(String webcam) {
        this.webcam = webcam;
    }

    public String getMicrophone() {
        return microphone;
    }

    public void setMicrophone(String microphone) {
        this.microphone = microphone;
    }

}
