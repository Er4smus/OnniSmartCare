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

public class CommandMessage {
    private Command command;
    private HardwareMessage message;

    public CommandMessage() {
    }

    public CommandMessage(Command command, HardwareMessage message) {
        this.command = command;
        this.message = message;
    }

    public CommandMessage(Command command) {
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public HardwareMessage getMessage() {
        return message;
    }

    public void setMessage(HardwareMessage message) {
        this.message = message;
    }
}