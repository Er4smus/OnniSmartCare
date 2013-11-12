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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neusoft.babymonitor.backend.webcam.Constants;

public class PlatformUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformUtil.class);

    /** Enum type for JVM types - 32 or 64bit */
    private enum JVM_TYPE {
        X64,
        X86
    };

    /**
     * Reads the system properties to determine what kind of JVM the system uses.
     * 
     * @return {@link JVM_TYPE} the type of JVM
     */
    private static final JVM_TYPE getJVMType() {
        String arch = System.getProperty("os.arch");
        // read system properties to determine JVM type - search for both SUN and IBM implementations
        String dataModel = System.getProperty("sun.arch.data.model", System.getProperty("com.ibm.vm.bitmode"));
        if ("32".equals(dataModel)) {
            LOGGER.info("found 32bit JVM");
            return JVM_TYPE.X86;
        } else {
            LOGGER.info("found 64bit JVM");
            if ("64".equals(dataModel)) {
                return JVM_TYPE.X64;
            } else {
                LOGGER.info("user has a {} os type, guessing JVM based on it", arch);
                // if neither 32 or 64 is retrieved, we take a wild guess using the computers processor type
                return (arch.contains("64") || arch.equalsIgnoreCase("sparcv9")) ? JVM_TYPE.X64 : JVM_TYPE.X86;
            }
        }
    }

    public static final String getVLCNativeLibsPath() {
        return getJVMType().equals(JVM_TYPE.X86) ? Constants.VLC_32BIT : Constants.VLC_64BIT;
    }
}
