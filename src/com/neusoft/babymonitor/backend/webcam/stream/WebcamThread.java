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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.neusoft.babymonitor.backend.webcam.Constants;
import com.neusoft.babymonitor.backend.webcam.ui.observer.StreamingStatus;
import com.neusoft.babymonitor.backend.webcam.util.PlatformUtil;
import com.neusoft.babymonitor.backend.webcam.util.RuntimeUtils;
import com.neusoft.babymonitor.backend.webcam.util.WebcamUtil;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import static com.neusoft.babymonitor.backend.webcam.Constants.*;

public class WebcamThread extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebcamThread.class);

    private int httpPort;
    private VlcHelper vlcHelper;
    private int ffmpegVideoPort;
    private int ffmpegAudioPort;
    private int rtspPort;
    private String audioVideoCommand;
    private Process ffmpegProcess;
    private boolean stopFfmpeg;
    private String httpIp;

    /**
     * @param httpPort the port for http streaming
     * @param ffmpegVideoPort the port where the ffmpeg drops the video frames
     * @param ffmpegAudioPort the port where the ffmpeg drops the audio frames
     * @param rtspPort the port where the vlc will stream
     * @param httpIp ip for http streaming(this is the externalIp or internalIP)
     */
    public WebcamThread(int httpPort, int ffmpegVideoPort, int ffmpegAudioPort, int rtspPort, String httpIp) {
        this.httpPort = httpPort;
        this.ffmpegVideoPort = ffmpegVideoPort;
        this.ffmpegAudioPort = ffmpegAudioPort;
        this.vlcHelper = new VlcHelper();
        this.rtspPort = rtspPort;
        this.httpIp = httpIp;
        // killFFmpegProcess();
        Runtime.getRuntime().addShutdownHook(new Thread(new FFmpegDestroyer()));
        LOGGER.info("the httpPort {}, the ffmpegVideoPort {}, the ffmpegAudioPort {}, the rtspPort {}", httpPort,
                ffmpegAudioPort, ffmpegVideoPort, rtspPort);
    }

    @Override
    public void run() {
        startWebcam(httpPort);
    }

    /**
     * @param port
     * @param ffmpegpath
     */
    private void startWebcam(int port) {
        // start webCam on Windows
        if (System.getProperty("os.name").startsWith("Windows")) {
            // Get the ffmpeg
            // ffmpeg -y -f vfwcap -i -an -vcodec libxvid -b:v 1M -threads 2 -f rtp rtp://172.18.64.232:5004
            // File ffmpegFile = getFFmpeg("ffmpeg.exe");
            // get the path location of ffmpeg
            // String ffmpegPath = ffmpegFile.getParent() + File.separator;
            // String ffmpegPath = "ffmpeg" + File.separator + "bin" + File.separator;
            // run continuously
            try {
                vlcHelper.createHttpOutputFolder();
            } catch (IOException e1) {
                LOGGER.error("Error when creating http output folder {}", e1);
            }
            audioVideoCommand = getAudioVideoCommand(Constants.FFMPEG_PATH);
            String commandsStr[] = { Constants.FFMPEG_PATH + "ffmpeg", "-f", "dshow", "-i", audioVideoCommand, "-s",
                    "320x240", "-r", "10", "-an", "-vcodec", "libx264", "-crf", "20", "-preset", "ultrafast",
                    "-pix_fmt", "yuv420p", "-x264opts", "keyint=25:min-keyint=25", "-profile:v", "baseline", "-b:v",
                    "96k", "-threads", "0", "-f", "rtp", "rtp://" + Constants.INTERNAL_IP + ":" + ffmpegVideoPort,
                    "-strict", "experimental", "-acodec", "aac", "-b:a", "28k", "-vn", "-f", "rtp",
                    "rtp://" + Constants.INTERNAL_IP + ":" + ffmpegAudioPort };
            vlcHelper.generateSdpInfoAndStartWebcam(commandsStr);
        } else {
            try {
                vlcHelper.createHttpOutputFolder();
            } catch (IOException e1) {
                LOGGER.error("Error when creating http output folder {}", e1);
            }
            String commandStr[] = { Constants.FFMPEG_PATH + "ffmpeg", "-f", "video4linux2", "-s", "320x240", "-r",
                    "16", "-an", "-i", WebcamUtil.getInstance().getWebcam(), "-vcodec", "h264", "-f", "rtp",
                    "rtp://" + Constants.INTERNAL_IP + ":" + ffmpegVideoPort, "-strict", "experimental", "-acodec",
                    "aac", "-b:a", "56k", "-f", "rtp", "rtp://" + Constants.INTERNAL_IP + ":" + ffmpegAudioPort };
            vlcHelper.generateSdpInfoAndStartWebcam(commandStr);
        }
        // TODO Mac
    }

    /**
     * Finds the video and audio devices, and returns the missing argument to launch the streaming with audio.
     * 
     * @param ffmpegPath
     * @return an argument looking like this
     *         <b>video="HP Basic Starter Camera":audio="Microphone (High Definition Aud"</b>
     */
    private static String getAudioVideoCommand(String ffmpegPath) {
        // get selected devices
        String video = WebcamUtil.getInstance().getWebcam();
        String audio = WebcamUtil.getInstance().getMicrophone();
        LOGGER.info("the webcam name is {} ", video);
        LOGGER.info("the audio device name is {} ", audio);

        StreamingStatus status = StreamingStatus.getInstance();

        // set the webcam name
        status.setWebcam(video);

        return "video=" + video + ":audio=" + audio;
    }

    /**
     * Extracts the ffmpeg from jar and adds it to "user.home" directory. It the ffmpeg already exists there we don't
     * extract it anymore.
     * 
     * @param ffmpegExec is the executable of the ffmpeg that is operating system dependent
     * @return
     */
    private File getFFmpeg(String ffmpegExec) {
        String ffmpegName = File.separator + ffmpegExec;
        File file = new File(Constants.FFMPEG_PATH + ffmpegName);
        if (!file.exists()) {
            LOGGER.info("the ffmpeg doesn't exist on client machine so we have to extract it from jar..");
            InputStream stream = WebcamThread.class.getResourceAsStream("/ffmpeg.exe");
            try {
                FileUtils.copyInputStreamToFile(stream, file);
            } catch (IOException e) {
                LOGGER.error("error copying the ffmpeg from jar {} " + e);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        LOGGER.error("Error closing the stream {}", e);
                    }
                }
            }
        } else {
            LOGGER.info("the ffmpeg exists");
        }

        return file;

    }

    public void setStopFfmpeg(boolean stopFfmpeg) {
        this.stopFfmpeg = stopFfmpeg;
    }

    /**
     * This method is called when the externalIp was found.
     */
    public void startVlc() {
        vlcHelper.stopMediaPlayer();
        vlcHelper.startMediaPlayer();
    }

    private void killFFmpegProcess() {
        LOGGER.debug("Killing ffmpeg process if it's open ....");
        try {
            if (System.getProperty("os.name").startsWith(WINDOWS)) {
                Runtime.getRuntime().exec("taskkill /F /IM " + Constants.FFMPEG_PROCESS_NAME);
            }
        } catch (IOException e) {
            LOGGER.error("Error when trying to kill ffmpeg process {}", e);
        }
    }

    private class FFmpegDestroyer implements Runnable {

        @Override
        public void run() {
            destroyFfmpeg();
            setStopFfmpeg(true);
            killFFmpegProcess();
        }

        private void destroyFfmpeg() {
            if (ffmpegProcess != null) {
                LOGGER.info("destroy the ffmpeg process....................");
                ffmpegProcess.destroy();
            }
        }

    }

    private class VlcHelper implements Runnable {
        private boolean vlcStarted;
        private MediaPlayer player;

        public void run() {
            launchVlc();
        }

        /**
         * @throws IOException
         */
        private void createHttpOutputFolder() throws IOException {
            File httpOutputFile = new File(Constants.LIVE_HTTP_FOLDER_NAME);
            if (httpOutputFile.exists()) {
                FileUtils.cleanDirectory(httpOutputFile);
            } else {
                httpOutputFile.mkdir();
            }

        }

        // ffmpeg -f dshow -i video="HP Basic Starter Camera":audio="Microphone (High Definition Aud" -s 480x360 -an
        // -vcodec libx264 -preset ultrafast -pix_fmt yuv420p -x264opts keyint=123:min-keyint=20 -profile:v baseline
        // -b:v 128k -threads 0 -f rtp rtp://169.254.213.227:6002 -strict experimental -acodec aac -b:a 24k -vn -f rtp
        // rtp://169.254.213.227:6004
        // FIXME
        private void generateSdpInfoAndStartWebcam(String commandsStr[]) {
            while (!stopFfmpeg) {
                Process runWebCam = null;
                try {
                    runWebCam = Runtime.getRuntime().exec(commandsStr);
                    BufferedReader errorStreamReader = new BufferedReader(new InputStreamReader(
                            runWebCam.getErrorStream()));
                    BufferedReader processReader = new BufferedReader(new InputStreamReader(runWebCam.getInputStream()));
                    // generate the SDP file
                    generateSdpInfoFile(processReader);
                    // try to start the webcam
                    startWebcam(errorStreamReader);
                } catch (IOException e) {
                    LOGGER.error("error when trying to start the ffmpeg procces {} ", e);
                }
            }
        }

        private void generateSdpInfoFile(BufferedReader processReader) {
            File sdpInfoFile = new File(Constants.SDP_FILE_PATH);
            String sdpInfoLine;
            try {
                sdpInfoLine = processReader.readLine();
                FileUtils.writeStringToFile(sdpInfoFile, "");
                while (sdpInfoLine != null) {
                    LOGGER.debug("sdpInfoLine {}", sdpInfoLine);
                    sdpInfoLine = processReader.readLine();
                    if (!sdpInfoLine.equals("")) {
                        LOGGER.debug("entered where string is not empty {}", sdpInfoLine);
                        FileUtils.writeStringToFile(sdpInfoFile, sdpInfoLine + "\n", true);
                        if (System.getProperty("os.name").startsWith(WINDOWS)) {
                            if (sdpInfoLine.contains("config")) {
                                break;
                            }
                        } else {
                            if (sdpInfoLine.contains("rtpmap")) {
                                break;
                            }
                        }
                    }

                }
                processReader.close();
            } catch (IOException e) {
                LOGGER.error("error when trying to create the sdp info file ", e);
            }

        }

        private void startWebcam(BufferedReader errorStreamReader) {
            try { // try to start the webcam
                LOGGER.info("Starting the ffmpeg process with sound..........................................");
                String proccessOutput = errorStreamReader.readLine();
                while (proccessOutput != null) {
                    LOGGER.debug("processOutput {}", proccessOutput);
                    if (proccessOutput.contains("Stream #0:1") || proccessOutput.contains("frame")) {
                        if (!vlcHelper.vlcStarted) {
                            Thread vlcThread = new Thread(vlcHelper);
                            vlcThread.start();
                        }
                    }
                    proccessOutput = errorStreamReader.readLine();
                }
                // Set the state of streaming as not started
                StreamingStatus.getInstance().setState("");
                LOGGER.error("The ffmpeg process has colapsed");
                LOGGER.info("Starting again...........");
            } catch (IOException e) {
                LOGGER.error(
                        "error finding the ffmpeg folder at this path, check if the ffmpeg folder is not missing {}",
                        Constants.FFMPEG_PATH, e);
            }

        }

        private void launchVlc() {
            if (!vlcStarted) {
                LOGGER.info("..........launchVlc entered .................");
                vlcHelper.vlcStarted = true;
                NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), PlatformUtil.getVLCNativeLibsPath());
                Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
                if (System.getProperty("os.name").startsWith(WINDOWS)) {
                    MediaPlayerFactory factory = new MediaPlayerFactory("--rtsp-timeout=86400", "--clock-jitter=0",
                            "--ffmpeg-hurry-up", "--ffmpeg-skip-frame=4", "--skip-frames", "--clock-synchro=1",
                            "--vout=opengl", "--deinterlace-mode=linear", "--high-priority", "--no-one-instance",
                            "--ffmpeg-skip-idct=4", "--ffmpeg-skiploopfilter=4", "--no-ps-trust-timestamps");
                    player = factory.newHeadlessMediaPlayer();
                } else if (System.getProperty("os.name").startsWith(LINUX)) {
                    MediaPlayerFactory factory = new MediaPlayerFactory("--rtsp-timeout=86400");
                    player = factory.newHeadlessMediaPlayer();
                }
                startMediaPlayer();
            }
        }

        /**
         * Starts the vlc media player. This method is used when the application starts and when the externalIp is
         * found. The externalIp is needed for HLS(http live streaming).WP and iOS needs access to the streaming
         * fragments(the streaming-#######ts).
         */
        private void startMediaPlayer() {
            LOGGER.info(".............vlc start playing..........");
            RuntimeUtils.destroyProcess(rtspPort);
            String opts = "";
            if (System.getProperty("os.name").startsWith(WINDOWS)) {
                opts = ":sout=#duplicate{dst=standard{access=livehttp{seglen=5,delsegs=true,numsegs=3,index="
                        + Constants.LIVE_HTTP_FILES_PATH + Constants.LIVE_HTTP_PLAYLIST_NAME + ",index-url=http://"
                        + httpIp + ":" + httpPort + Constants.LIVE_HTTP_URL
                        + "/streaming-########.ts},mux=ts{use-key-frames},dst=" + Constants.LIVE_HTTP_FILES_PATH
                        + "streaming-########.ts}, dst=\"transcode{vcodec=mp4vm, vb=96}:rtp{sdp=rtsp://"
                        + Constants.INTERNAL_IP + ":" + rtspPort + "/test.sdp}\"}";
            } else if (System.getProperty("os.name").startsWith(LINUX)) {
                opts = ":sout=#duplicate{dst=standard{access=livehttp{seglen=5,delsegs=true,numsegs=3,index="
                        + Constants.LIVE_HTTP_FILES_PATH + Constants.LIVE_HTTP_PLAYLIST_NAME + ",index-url=http://"
                        + httpIp + ":" + httpPort + Constants.LIVE_HTTP_URL
                        + "/streaming-########.ts},mux=ts{use-key-frames},dst=" + Constants.LIVE_HTTP_FILES_PATH
                        + "streaming-########.ts}, dst=rtp{sdp=rtsp://" + Constants.INTERNAL_IP + ":" + rtspPort
                        + "/test.sdp}\"}";
            }
            player.playMedia(Constants.SDP_FILE_PATH, opts, ":sout-all", ":sout-keep", ":sout-mux-caching=10");
            /*
             * String[] opts = { ":sout=#duplicate{dst=rtp{sdp=rtsp://" + Constants.INTERNAL_IP + ":" + rtspPort +
             * "/test.sdp}, dst=standard{access=livehttp{seglen=5,delsegs=true,numsegs=10,index=" +
             * Constants.LIVE_HTTP_FILES_PATH + Constants.LIVE_HTTP_PLAYLIST_NAME + ",index-url=http://" + httpIp + ":"
             * + httpPort + Constants.LIVE_HTTP_URL + "/streaming-########.ts},mux=ts{use-key-frames},dst=" +
             * Constants.LIVE_HTTP_FILES_PATH + "streaming-########.ts}}" };
             */
            StreamingStatus.getInstance().setState("START");
        }

        private void stopMediaPlayer() {
            if (player != null) {
                player.stop();
            }
        }
    }
}
