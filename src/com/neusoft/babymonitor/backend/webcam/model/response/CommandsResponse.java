package com.neusoft.babymonitor.backend.webcam.model.response;

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

import java.util.List;

import com.neusoft.babymonitor.backend.webcam.model.CommandMessage;

public class CommandsResponse {
    private List<CommandMessage> commands;

    public CommandsResponse(List<CommandMessage> commands) {
        super();
        this.commands = commands;
    }

    public List<CommandMessage> getCommands() {
        return commands;
    }

    public void setCommands(List<CommandMessage> commands) {
        this.commands = commands;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((commands == null) ? 0 : commands.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CommandsResponse other = (CommandsResponse) obj;
        if (commands == null) {
            if (other.commands != null)
                return false;
        } else if (!commands.equals(other.commands))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CommandsResponse [commands=" + commands + "]";
    }

}