package com.neusoft.babymonitor.backend.webcam.exception;

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

public class UPnPNotWorkingException extends Exception {
    public UPnPNotWorkingException() {
        super();
    }

    public UPnPNotWorkingException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public UPnPNotWorkingException(String detailMessage) {
        super(detailMessage);
    }

    public UPnPNotWorkingException(Throwable throwable) {
        super(throwable);
    }
}
