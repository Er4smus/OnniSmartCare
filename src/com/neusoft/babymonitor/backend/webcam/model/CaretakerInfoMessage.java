package com.neusoft.babymonitor.backend.webcam.model;

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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CaretakerInfoMessage implements HardwareMessage, Serializable {
    private static final long serialVersionUID = -6003002555858614391L;
    private boolean hasRights;
    private int daysAllowed;
    private long startHour;
    private long endHour;
    private String timeZoneId;
    private int type;
    private String remoteIp;

    public CaretakerInfoMessage(boolean hasRights, int daysAllowed, long startHour, long endHour, String timeZoneId) {
        this(hasRights, daysAllowed, startHour, endHour, timeZoneId, "");
    }

    public CaretakerInfoMessage(boolean hasRights, int daysAllowed, long startHour, long endHour, String timeZoneId,
            String remoteIp) {
        super();
        this.hasRights = hasRights;
        this.daysAllowed = daysAllowed;
        this.startHour = startHour;
        this.endHour = endHour;
        this.timeZoneId = timeZoneId;
        type = HardwareMessageType.CARETAKER_INFO.getCode();
        this.remoteIp = remoteIp;
    }

    public CaretakerInfoMessage() {
        type = HardwareMessageType.CARETAKER_INFO.getCode();
    }

    public boolean isHasRights() {
        return hasRights;
    }

    public void setHasRights(boolean hasRights) {
        this.hasRights = hasRights;
    }

    public int getDaysAllowed() {
        return daysAllowed;
    }

    public void setDaysAllowed(int daysAllowed) {
        this.daysAllowed = daysAllowed;
    }

    public long getStartHour() {
        return startHour;
    }

    public void setStartHour(long startHour) {
        this.startHour = startHour;
    }

    public long getEndHour() {
        return endHour;
    }

    public void setEndHour(long endHour) {
        this.endHour = endHour;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
