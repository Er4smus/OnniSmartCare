package com.neusoft.babymonitor.backend.webcam.model;

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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum HardwareMessageType {
    /** Song file name. */
    SONG_FILE_NAME(0),
    /** Playlist information. */
    PLAYLIST_INFO(1),
    /** Caretaker information */
    CARETAKER_INFO(2);

    private static final Map<Integer, HardwareMessageType> lookup = new HashMap<Integer, HardwareMessageType>();

    private final int code;

    static {
        for (HardwareMessageType messageType : EnumSet.allOf(HardwareMessageType.class)) {
            lookup.put(messageType.getCode(), messageType);
        }
    }

    private HardwareMessageType(int code) {
        this.code = code;
    }

    public static HardwareMessageType get(int code) {
        return lookup.get(code);
    }

    public int getCode() {
        return code;
    }
}