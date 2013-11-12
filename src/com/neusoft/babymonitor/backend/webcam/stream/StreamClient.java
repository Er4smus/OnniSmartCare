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

import java.io.OutputStream;
import java.util.Date;

class StreamClient {

    private Stream stream;
    private OutputStream output;

    private boolean runs = true;

    private long offset = 0;
    private int fragmentSequence = 1;

    public StreamClient(Stream stream, OutputStream output) {
        this.stream = stream;
        this.output = output;
    }

    public void run() {

        // report the starting of this client
        stream.postEvent(new ServerEvent(this, stream, ServerEvent.CLIET_START));

        // waiting for header
        byte[] header = stream.getHeader();
        while (header == null && stream.running()) {

            // sleeping 200 ms
            try {
                Thread.sleep(200);
            } catch (Exception e) {
            }

            header = stream.getHeader();
        }

        // writing header
        try {
            output.write(header);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        /*
         * Web browsers may try to detect some features of the server and open multiple connections. They may close the
         * connection eventually so we wait for a little bit to ensure this request is serious. (And the client needs
         * more than just the header).
         */

        // sleeping 1000 ms
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }

        // sending fragments
        int myAge = 0;
        while (runs && stream.running()) {

            // while there is a new fragment is available
            int streamAge;
            while (runs && stream.running() && myAge < (streamAge = stream.getFragmentAge())) {

                // notification if a fragment was skipped
                if (myAge > 0 && streamAge - myAge > 1)
                    stream.postEvent(new ServerEvent(this, stream, ServerEvent.CLIET_FRAGMENT_SKIP));

                myAge = streamAge;

                // getting current movie fragment
                MovieFragment fragment = stream.getFragment();

                // send the fragment data to the client
                try {
                    // writing the data for one shot (less cpu usage)
                    // output.write(fragment.getData(), 0, fragment.length());

                    /*
                     * Writing the data in packets (cunks of arbitrary size) to get a better time-resolution for the
                     * outputbandwidth used.
                     */

                    final int PACKET_SIZE = 24 * 1024;
                    int fragLength = fragment.length();
                    int offset = 0;
                    while (offset < fragLength) {

                        // current packet size
                        int length = fragLength - offset;
                        if (length >= 1.5 * PACKET_SIZE)
                            length = PACKET_SIZE;

                        // starting time of the transfer
                        long transferStart = new Date().getTime();

                        // writing data packet
                        output.write(fragment.getData(), offset, length);

                        // notification about the transfer
                        stream.postEvent(new TransferEvent(this, stream, TransferEvent.STREAM_OUTPUT, length,
                                new Date().getTime() - transferStart));

                        // next packet (chunk) start
                        offset += length;
                    }

                } catch (Exception e) {

                    // closed connection
                    runs = false;
                }
            }

            // currently no new fragment, sleeping 200 ms
            try {
                Thread.sleep(200);
            } catch (Exception e) {
            }

        }

        // report the stopping of thes client
        stream.postEvent(new ServerEvent(this, stream, ServerEvent.CLIET_STOP));

        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
