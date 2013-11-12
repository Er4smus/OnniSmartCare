package com.neusoft.babymonitor.backend.webcam.stream;

/*
 This file is part of “Onni smart care desktop application” software”.

 Copyright (C) <2013>  Erasmus van Niekerk <erasmus.van.niekerk@sepsolutions.fi>

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

import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Date;

class StreamInput {

    private Stream stream;
    private InputStream input;
    private boolean runs;

    private byte[] header;

    private StreamInputState currentState;

    public StreamInput(Stream stream, InputStream input) {
        this.stream = stream;
        this.input = input;
    }

    public void run() {
        runs = true;

        // notification about starting the input process
        stream.postEvent(new ServerEvent(this, stream, ServerEvent.INPUT_START));

        changeState(new HeaderDetectionState(this, stream));

        byte[] buffer = new byte[65535];
        int offset = 0;
        int length = 0;
        System.out.println("stream input started");
        while (runs && stream.running()) {

            try {

                // starting time of the transfer
                long transferStart = new Date().getTime();

                // reading data
                int numBytes = input.read(buffer, offset, buffer.length - offset);
                // notification about the transfer
                stream.postEvent(new TransferEvent(this, stream, TransferEvent.STREAM_INPUT, numBytes, new Date()
                        .getTime() - transferStart));

                if (numBytes == -1)
                    runs = false;
                length += numBytes;

                int newOffset = currentState.processData(buffer, 0, length);
                if (newOffset < offset + length) {
                    length = length - newOffset;
                    System.arraycopy(buffer, newOffset, buffer, 0, length);
                    offset = length;
                } else {
                    length = 0;
                    offset = 0;
                }

            } catch (SocketTimeoutException e) {
                System.out.println("exception occured: " + e.getMessage());
                continue;
            } catch (Exception e) {
                System.out.println("exception!!!!" + e.getMessage());
                runs = false;
            }
        }

        try {
            input.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // notification about ending the input process
        stream.postEvent(new ServerEvent(this, stream, ServerEvent.INPUT_STOP));
    }

    public void changeState(StreamInputState newState) {
        currentState = newState;
    }

}
