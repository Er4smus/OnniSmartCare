package com.neusoft.babymonitor.backend.webcam.ui.controller;

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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neusoft.babymonitor.backend.webcam.exception.HttpConnectionException;
import com.neusoft.babymonitor.backend.webcam.exception.InvalidTokenException;
import com.neusoft.babymonitor.backend.webcam.exception.ServerException;
import com.neusoft.babymonitor.backend.webcam.model.response.WebcamCodeResponse;
import com.neusoft.babymonitor.backend.webcam.service.WebcamService;
import com.neusoft.babymonitor.backend.webcam.ui.observer.StreamingStatus;
import com.neusoft.babymonitor.backend.webcam.ui.view.UIView;
import com.neusoft.babymonitor.backend.webcam.util.RuntimeUtils;
import com.neusoft.babymonitor.backend.webcam.util.WebcamUtil;

public class UIController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UIController.class);

    private UIView view = new UIView();
    private WebcamService webcamService = new WebcamService();
    private StreamingStatus streamingStatus = StreamingStatus.getInstance();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final long INITIAL_DELAY = 10;
    private static final long PERIOD = 10;
    private static final TimeUnit TIME_UNITS = TimeUnit.SECONDS;

    // UI messages
    private static final String CONNECTION_FEEDBACK_MESSAGE = "<html>The connection with the server was established.<br/>Please wait while the streaming starts...</html>";
    private static final String WEBCAM_CODE_INVALID = "The webcam code you entered is invalid";
    private static final String WEBCAM_INPUT_CODE = "You need to input a webcam code";
    private static final String WEBCAM_NUMBER_FORMAT = "You need to input a valid webcam code";
    private static final String SERVER_CONNECTION_ERROR = "Error connecting to the server";
    private static final String SERVER_ERRORS = "Some errors occurred on the server";
    private static final String STREAMING_STARTED_WITHOUT_UPNP = "<html>Streaming started,<br/>but it is available only in your network.</html>";
    private static final String STREAMING_STARTED = "Streaming started";

    public UIController() {
        view.setVisible(true);
        view.addWindowListener(new WindowCloseListener());

        if (view.isOSSupported()) {
            RuntimeUtils.destroyProcess(554);
            view.babyIdTextFieldListener(new BabyIdInputListener());
            view.startStreamingMouseListener(new StartStreamingMouseListener());
            view.babyIdTextFieldActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // start streaming
                    startStreaming();
                }
            });

            view.getWebcamDropdown().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    WebcamUtil.getInstance().setWebcam(view.getSelectedWebcam());
                }
            });
            view.getMicrophoneDropdown().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    WebcamUtil.getInstance().setMicrophone(view.getSelectedMicrophone());
                }
            });
        }
    }

    private class StartStreamingMouseListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            startStreaming();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

    }
    
    private void startStreaming() {
        if (view.getBabyIdTextField().getText().trim().equals("")) {
            view.getErrorMessage().setText(WEBCAM_INPUT_CODE);
        } else {
            try {
                webcamService.startStreaming();
                long webcamCode = Long.parseLong(view.getBabyIdTextField().getText());
                webcamService.setWebcamCode(webcamCode);
                // check the webcam code
                WebcamCodeResponse codeResponse = webcamService.checkWebcamCode(webcamCode);
                switch (codeResponse.getErrorCode()) {
                case 0:
                    view.setLoadingText(CONNECTION_FEEDBACK_MESSAGE);
                    view.getStartStreamingButton().setEnabled(false);
                    view.getBabyIdTextField().setEditable(false);
                    view.getWebcamDropdown().setEditable(false);
                    view.getMicrophoneDropdown().setEditable(false);
                    view.getSeparatorLine().setVisible(true);
                    webcamService.execute();
                    scheduleStreaminingStatus();
                    break;
                case 401:
                    view.getErrorMessage().setText(WEBCAM_CODE_INVALID);
                    break;
                default:
                    view.getErrorMessage().setText(SERVER_ERRORS);
                    break;
                }
            } catch (HttpConnectionException e) {
                view.getErrorMessage().setText(SERVER_CONNECTION_ERROR);
            } catch (InvalidTokenException e) {
                view.getErrorMessage().setText(SERVER_CONNECTION_ERROR);
            } catch (ServerException e) {
                view.getErrorMessage().setText(SERVER_CONNECTION_ERROR);
            } catch (NumberFormatException e) {
                view.getErrorMessage().setText(WEBCAM_NUMBER_FORMAT);
            }
        }
    }

    private class BabyIdInputListener implements KeyListener {

        @Override
        public void keyPressed(KeyEvent e) {
            babyIdKeyPressed(e);
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        private void babyIdKeyPressed(java.awt.event.KeyEvent evt) {
            view.getErrorMessage().setText("");
        }
    }

    private class WindowCloseListener extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent arg0) {
            LOGGER.info("Stopping ffmpeg process");
            if (webcamService != null) {
                webcamService.stopFfmpegProcess();
            }
        }
    }

    /**
     * Checks the status of streaming. Checks if the webcam is connected, if the upnp has successfully worked, and if
     * stream started with success . This method starts after 10 seconds and is executed continuosly at 10 seconds .
     */
    private void scheduleStreaminingStatus() {
        final Runnable status = new Runnable() {
            public void run() {
                LOGGER.debug("Entered scheduler method with webcam {}", streamingStatus.getWebcam() + " , upnpWorked:"
                        + streamingStatus.isUpnpWorked() + ", state of streaming: " + streamingStatus.getState());
                if (!streamingStatus.isUpnpWorked()) {
                    view.getErrorMessage().setText("");
                    if (streamingStatus.getState().equals("START")) {
                        view.setLoadingText(STREAMING_STARTED_WITHOUT_UPNP);
                    }
                } else {
                    if (streamingStatus.getState().equals("START")) {
                        LOGGER.info("Streaming started......");
                        view.setLoadingText(STREAMING_STARTED);
                    }
                    view.getErrorMessage().setText("");
                }
            }
        };

        // start the scheduler after 10 seconds, and run it with 10 seconds delay
        final ScheduledFuture<?> streamingStatusHandler = scheduler.scheduleAtFixedRate(status, INITIAL_DELAY, PERIOD,
                TIME_UNITS);
        scheduler.schedule(new Runnable() {
            public void run() {
                streamingStatusHandler.cancel(true);
            }
        }, 60 * 60, TIME_UNITS);
    }
}
