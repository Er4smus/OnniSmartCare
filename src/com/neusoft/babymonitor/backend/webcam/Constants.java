package com.neusoft.babymonitor.backend.webcam;

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

import com.neusoft.babymonitor.backend.webcam.util.network.NetworkUtil;
import java.io.File;

public class Constants {
    public static final String PROTOCOL = "https://";
    public static final String ROOT = "/api";
    public static final String LOGIN_URL = "/oauth/token";

    /** Webcam url. */
    public static final String WEBCAM = "/webcam";
    /** Commands url. */
    public static final String COMMANDS = "/commands";
    /** Get the streaming url. */
    public static final String STREAMING = "/streaming";
    /** Get the video streaming url. */
    public static final String VIDEO = "/video";
    /** Get the audio streaming url. */
    public static final String AUDIO = "/audio";
    /** Hardware url. */
    public static final String HARDWARE = "/hardware";
    /** Webcam code verification url . */
    public static final String WEBCAM_CODE = "/webcamCode";

    public final static String LIVE_HTTP_URL = "/livehttp";
    public final static String LIVE_HTTP_FOLDER_NAME = "livehttp";
    public final static String LIVE_HTTP_FILES_PATH = LIVE_HTTP_FOLDER_NAME + File.separator;
    public final static String LIVE_HTTP_PLAYLIST_NAME = "babystream.m3u8";

    private static final String VLC_ROOT = "vlc" + File.separator;
    public static final String VLC_32BIT = VLC_ROOT + "vlc32B";
    public static final String VLC_64BIT = VLC_ROOT + "vlc64B";

    public static final String FFMPEG_PATH = "ffmpeg" + File.separator;
    public static final String SDP_FILE_PATH = "sdp" + File.separator + "sdp.info";
    public static final String INTERNAL_IP = NetworkUtil.getIP();
    public static final String FFMPEG_PROCESS_NAME = "ffmpeg.exe";
    /**
     * {0} the ip of the streaming, {1} the port of the streaming;
     */
    public static final String RTSP_URL = "rtsp://{0}:{1,number,#}/test.sdp";
    /**
     * {0} the ip of the streaming, {1} the port of the streaming;
     */
    public static final String HTTP_URL = "http://{0}:{1,number,#}" + LIVE_HTTP_URL + "/" + LIVE_HTTP_PLAYLIST_NAME;

    /** Operating systems */
    public static final String WINDOWS = "Windows";
    public static final String LINUX = "Linux";

}