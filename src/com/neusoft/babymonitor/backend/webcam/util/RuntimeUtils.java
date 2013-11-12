package com.neusoft.babymonitor.backend.webcam.util;

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
import static com.neusoft.babymonitor.backend.webcam.Constants.WINDOWS;
import static com.neusoft.babymonitor.backend.webcam.Constants.LINUX;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuntimeUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeUtils.class);

    /**
     * Destroys the process that listens on that port.
     * 
     * @param port the port that is killed
     */
    public static void destroyProcess(int port) {
        try {
            if (System.getProperty("os.name").startsWith(WINDOWS)) {
                // if the operating system is windows use taskkill command to destroy the process that uses the port
                Runtime.getRuntime().exec("taskkill /F /PID " + getPidOnWindows(port));
            } else if (System.getProperty("os.name").startsWith(LINUX)) {
                // if the operating system is Linux use kill command to destroy the process that uses the port
                Runtime.getRuntime().exec("kill -9 " + getPidOnLinux(port));
            }

        } catch (IOException e) {
            LOGGER.error("Error when trying to kill the process listening on this port {} , exception {}", port, e);
        }
    }

    /**
     * Gets process name that listens on given port
     * 
     * @param port the port that process listens
     * @return the process name/null if no process was found.
     */
    public static String getProcesName(int port) {
        String PID = null;
        if (System.getProperty("os.name").startsWith(WINDOWS)) {
            PID = getPidOnWindows(port);
        } else if (System.getProperty("os.name").startsWith(LINUX)) {
            PID = getPidOnLinux(port);
        }
        if (PID == null) {
            return null;
        }
        String command[] = { "tasklist" };
        try {
            Process nestat = Runtime.getRuntime().exec(command);
            BufferedReader processReader = new BufferedReader(new InputStreamReader(nestat.getInputStream()));
            String sdpInfoLine = processReader.readLine();

            while (sdpInfoLine != null) {
                if (sdpInfoLine.contains(PID)) {
                    String[] output = sdpInfoLine.split(" ");
                    return output[0];
                }
                sdpInfoLine = processReader.readLine();

            }
        } catch (IOException e) {
            LOGGER.error("Error when trying to create the process that outputs the running tasks with exception {} ", e);
        }
        return null;

    }

    /**
     * Gets the PID that listens on that port for Windows.
     * 
     * @param port the PID (process id) that listens on that port
     * @return the PID/null if no PID was found
     */
    private static String getPidOnWindows(int port) {
        String command[] = { "netstat", "-n", "-a", "-o" };
        try {
            Process netstat = Runtime.getRuntime().exec(command);
            BufferedReader processReader = new BufferedReader(new InputStreamReader(netstat.getInputStream()));
            String sdpInfoLine = processReader.readLine();
            while (sdpInfoLine != null) {
                if (sdpInfoLine.contains(":" + port)) {
                    String[] output = sdpInfoLine.split(" ");
                    return output[output.length - 1];
                }
                sdpInfoLine = processReader.readLine();
            }
        } catch (IOException e) {
            LOGGER.error("Exception occured when trying to get PID with exception {}", e);
        }

        return null;
    }

    /**
     * Gets the PID that listens on that port for Linux.
     * 
     * @param port the PID (process id) that listens on that port
     * @return the PID/null if no PID was found
     */
    private static String getPidOnLinux(int port) {
        String command[] = { "netstat", "-plnat" };
        try {
            Process netstat = Runtime.getRuntime().exec(command);
            BufferedReader processReader = new BufferedReader(new InputStreamReader(netstat.getInputStream()));
            String sdpInfoLine = processReader.readLine();
            while (sdpInfoLine != null) {
                if (sdpInfoLine.contains(":" + port)) {
                    String[] output = sdpInfoLine.split(" ");
                    // get the word containing the id and name of the process
                    String pidWord = output[output.length - 1];
                    // pidWord has the following format: pid/name or -
                    int delimiterPosition = pidWord.indexOf("/");
                    if (delimiterPosition != -1) {
                        // return the process id
                        return pidWord.substring(0, delimiterPosition);
                    }
                }
                sdpInfoLine = processReader.readLine();
            }
        } catch (IOException e) {
            LOGGER.error("Exception occured when trying to get PID with exception {}", e);
        }
        return null;
    }
}
