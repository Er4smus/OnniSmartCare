package com.neusoft.babymonitor.backend.webcam.ui.observer;

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

public class StreamingStatus {

    private static StreamingStatus instance = new StreamingStatus();
    private String state = "";
    private String webcam = "";
    private boolean upnpWorked;

    public static StreamingStatus getInstance() {
        return instance;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getWebcam() {
        return webcam;
    }

    public void setWebcam(String webcam) {
        this.webcam = webcam;
    }

    public boolean isUpnpWorked() {
        return upnpWorked;
    }

    public void setUpnpWorked(boolean upnpWorked) {
        this.upnpWorked = upnpWorked;
    }

}