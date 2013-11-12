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

/**
 * Add all the commands, until the server sends only the right commands(for e.g the state command is received, and it
 * shouldn't)
 */
public enum Command {
    LIGHT_ON,
    LIGHT_OFF,
    STATE,
    PLAY,
    STOP,
    PAUSE,
    REPEAT,
    PLAYER_STATE,
    DOWNLOAD_SONG,
    DELETE_SONG,
    LIST_SONGS,
    ALERT,
    HTTP_MP4,
    RTSP_MP4,
    HTTP_MP3,
    RTSP_MP3;
}
