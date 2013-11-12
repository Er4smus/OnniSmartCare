package com.neusoft.babymonitor.backend.webcam.stream;

/*
 This file is part of “Onni smart care desktop application” software”.

 Copyright (C) <2013>  Erasmus van Niekerk <erasmus.van.niekerk@gmail.com>

 This program is free software: you may copy, redistribute
 and/or modify it under the terms of the GNU General Public License as
 published by the Free Software Foundation, either version 2 of the
 License, or (at your option) any later version.

 This file is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 This file incorporates work covered by the following copyright and
 permission notice:  
 Copyright (C) 2011 Varga Bence

 Permission to use, copy, modify, and/or distribute this software  
 for any purpose with or without fee is hereby granted, provided  
 that the above copyright notice and this permission notice appear  
 in all copies.  

 THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL  
 WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED  
 WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE  
 AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR  
 CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS  
 OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,  
 NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN  
 CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.  
 */

import com.neusoft.babymonitor.backend.webcam.Constants;
import com.neusoft.babymonitor.backend.webcam.minihttp.HTTPException;
import com.neusoft.babymonitor.backend.webcam.minihttp.HTTPRequest;
import com.neusoft.babymonitor.backend.webcam.minihttp.HTTPResource;
import com.neusoft.babymonitor.backend.webcam.minihttp.HTTPResponse;
import com.neusoft.babymonitor.backend.webcam.minihttp.MiniHTTP;
import com.neusoft.babymonitor.backend.webcam.threadedevent.EventDispatcher;
import com.neusoft.babymonitor.backend.webcam.ui.observer.StreamingStatus;
import com.neusoft.babymonitor.backend.webcam.upnp.UPnPHelper;
import com.neusoft.babymonitor.backend.webcam.util.RuntimeUtils;
import com.neusoft.babymonitor.backend.webcam.util.network.NetworkUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamingServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamingServer.class);

    // configuration settings
    private static Map<String, String> settings = new HashMap<String, String>(20);

    // streams by name
    private static Map<String, ControlledStream> streams = new HashMap<String, ControlledStream>();

    private static int httpPort;
    private static int rtspPort;
    private static ServerSocket socket;
    private static StreamingServer instance = null;
    private static WebcamThread webcamThread;
    private static String externalIp = Constants.INTERNAL_IP;
    private static UPnPHelper weUPnPHelper;
    // It looks like some routers accepts rtsp only on this port
    private static final int RTSP_PORT = 554;
    private static final int RTSP_PORT_ALT = 8554;

    static {
        settings.put("streams.first", "true");
        settings.put("streams.first.password", "secret");
        settings.put("streams.first.limit", "100");
    }

    private StreamingServer() {
    }

    public static synchronized StreamingServer getInstance() {
        if (instance == null) {
            instance = new StreamingServer();
            instance.run();
        }
        return instance;
    }

    private void run() {
        httpPort = NetworkUtil.getPort(8081, 10000, 1);
        try {
            socket = new ServerSocket(httpPort);
        } catch (IOException ex) {
            LOGGER.error("http port is not available {}", httpPort);
        }

        int ffmpegVideoPort = NetworkUtil.getPort(10000, 50000, 2);
        int ffmpegAudioPort = NetworkUtil.getPort(ffmpegVideoPort + 2, 50000, 2);

        weUPnPHelper = new UPnPHelper(httpPort, RTSP_PORT);
        try {
            weUPnPHelper.addPortMapping();

        } catch (Exception e) {
            // check if the UPnP didn't work
            if (Constants.INTERNAL_IP.startsWith("172") || Constants.INTERNAL_IP.startsWith("192")) {
                weUPnPHelper.setRtspPort(NetworkUtil.getPort(ffmpegAudioPort + 1, 50000, 2));
                StreamingStatus.getInstance().setUpnpWorked(false);
                // if the UPnP didn't work and the internalIp it's not starting with '172' or '192',
                // that means that the connection is through cable and the stream is working outside the network
            } else {
                LOGGER.info("The streaming is throug cable {}");
                StreamingStatus.getInstance().setUpnpWorked(true);
                RuntimeUtils.destroyProcess(RTSP_PORT);
                RuntimeUtils.destroyProcess(RTSP_PORT_ALT);
                if (NetworkUtil.isAvailable(RTSP_PORT)) {
                    weUPnPHelper.setRtspPort(RTSP_PORT);
                } else if (NetworkUtil.isAvailable(RTSP_PORT_ALT)) {
                    weUPnPHelper.setRtspPort(RTSP_PORT_ALT);
                } else {
                    weUPnPHelper.setRtspPort(NetworkUtil.getPort(ffmpegAudioPort + 1, 50000, 1));
                }
            }
            LOGGER.error("WeUPnP didn' work as UPnP helper");
            LOGGER.error("Exception occured in addPortMapping {}", e);
        }
        externalIp = weUPnPHelper.getExternalIPAddress();
        webcamThread = new WebcamThread(weUPnPHelper.getHttpPort(), ffmpegVideoPort, ffmpegAudioPort,
                weUPnPHelper.getRtspPort(), externalIp);
        webcamThread.start();

        MiniHTTP server = new MiniHTTP(socket);

        LiveHttpResource liveHttp = new LiveHttpResource();
        server.registerResource("/livehttp", liveHttp);

        server.start();

    }

    public String getRtspUrl(String remoteIp) {
        // if the ip where the request for streaming was made is equal with the externalIp,
        // than we send a URL containing the internalIp
        if (remoteIp.equals(externalIp)) {
            return MessageFormat.format(Constants.RTSP_URL, Constants.INTERNAL_IP, weUPnPHelper.getRtspPort());
        } else {
            return MessageFormat.format(Constants.RTSP_URL, externalIp, weUPnPHelper.getRtspPort());
        }
    }

    public String getHttpUrl(String remoteIp) {
        // if the ip where the request for streaming was made is equal with the externalIp,
        // than we send a URL containing the internalIp
        if (remoteIp.equals(externalIp)) {
            return MessageFormat.format(Constants.HTTP_URL, Constants.INTERNAL_IP, weUPnPHelper.getHttpPort());
        } else {
            return MessageFormat.format(Constants.HTTP_URL, externalIp, weUPnPHelper.getHttpPort());
        }
    }

    public void stopFfmpeg() {
        webcamThread.setStopFfmpeg(true);
    }

    /**
     * Servers as a file resource - reads files from disk and sends them on the response output.
     */
    class LiveHttpResource implements HTTPResource {

        private final String CONTENT_TYPE_KEY = "Content-type";
        private final String CONTENT_TYPE = "application/vnd.apple.mpegurl";

        @Override
        public void serve(HTTPRequest request, HTTPResponse response) throws HTTPException {

            // url path will be something like '/livehttp/stream.m3u8'

            // split the url and check if it is correct
            String path[] = request.getPathName().split("/");
            if (path.length != 3) {
                throw new HTTPException(400, "Bad request");
            }

            // filename of requested file
            String fileName = path[2];
            LOGGER.info("Streaming client asking for {} file.", fileName);

            // read streaming files from disk
            File file = new File(Constants.LIVE_HTTP_FILES_PATH + fileName);

            if (!file.exists()) {
                throw new HTTPException(404, "Stream not found");
            }

            // set content type on response
            response.setParameter(CONTENT_TYPE_KEY, CONTENT_TYPE);

            // get stream of bytes from file and write to response output stream
            FileInputStream fin = null;
            try {
                fin = new FileInputStream(file);
                IOUtils.copy(fin, response.getOutputStream());
            } catch (FileNotFoundException e) {
                throw new HTTPException(500, "Failed to start stream");
            } catch (IOException e) {
                throw new HTTPException(500, "Failed to start stream");
            } finally {
                // close stream
                try {
                    if (fin != null) {
                        fin.close();
                    }
                } catch (IOException e) {
                    LOGGER.error("Failed to close stream: {}", e);
                }
            }
        }
    }

}
