package com.neusoft.babymonitor.backend.webcam.service;

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

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neusoft.babymonitor.backend.webcam.ClientTemplate;
import com.neusoft.babymonitor.backend.webcam.exception.HttpConnectionException;
import com.neusoft.babymonitor.backend.webcam.exception.InvalidTokenException;
import com.neusoft.babymonitor.backend.webcam.exception.ServerException;
import com.neusoft.babymonitor.backend.webcam.model.CaretakerInfoMessage;
import com.neusoft.babymonitor.backend.webcam.model.Command;
import com.neusoft.babymonitor.backend.webcam.model.CommandMessage;
import com.neusoft.babymonitor.backend.webcam.model.response.WebcamCodeResponse;
import com.neusoft.babymonitor.backend.webcam.stream.StreamingServer;

public class WebcamService extends SwingWorker<String, Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebcamService.class);

    private static String HOST = "host";
    private static int PORT = 443;
    private static int TIMEOUT = 3000;

    private long webcamCode;
    private ClientTemplate rest;
    private String feedBackMessage = "not connected yet";
    private StreamingServer streamingServer;

    public void startStreaming() {
        streamingServer = StreamingServer.getInstance();
    }

    public WebcamService() {
        rest = new ClientTemplate(HOST, PORT);
    }

    public WebcamCodeResponse checkWebcamCode(long webcamCode) throws HttpConnectionException, InvalidTokenException,
            ServerException {
        return rest.checkWebcamCode(webcamCode);
    }

    private void proccessCommands() throws InterruptedException {
        LOGGER.info("connect with babymonitor entered {}", webcamCode);
        while (true) {
            CommandMessage commandMessage;
            try {
                commandMessage = rest.getCommand(webcamCode);
                LOGGER.debug("Command message is {}", commandMessage);
                Command command = commandMessage.getCommand();
                LOGGER.debug("Processing command {}", command);
                CaretakerInfoMessage webcamInfoMessage = (CaretakerInfoMessage) commandMessage.getMessage();
                LOGGER.debug("Proccessing webcam info message info message: {}", webcamInfoMessage);
                // TODO restart the streaming when cartaker has no rights to view the stream.
                switch (command) {
                case HTTP_MP4:
                case HTTP_MP3:
                    rest.sendVideoStreamingURL(webcamCode, streamingServer.getHttpUrl(webcamInfoMessage.getRemoteIp()));
                    break;
                case RTSP_MP4:
                case RTSP_MP3:
                    rest.sendVideoStreamingURL(webcamCode, streamingServer.getRtspUrl(webcamInfoMessage.getRemoteIp()));
                    break;
                default:
                    LOGGER.info("Unkown command received: {}", command);
                    break;
                }
            } catch (Exception e) {
                LOGGER.info("Could not connect to server, waiting {} ms, exception is {} ", TIMEOUT, e);
                Thread.sleep(TIMEOUT);
            }
        }
    }

    public long getWebcamCode() {
        return webcamCode;
    }

    public void setWebcamCode(long webcamCode) {
        this.webcamCode = webcamCode;
    }

    @Override
    protected String doInBackground() throws Exception {
        LOGGER.debug("Entered doInBackground()");
        proccessCommands();
        return feedBackMessage;
    }

    public String getFeedBackMessage() {
        return feedBackMessage;
    }

    public void stopFfmpegProcess() {
        if (streamingServer != null) {
            streamingServer.stopFfmpeg();
        }
    }
}
