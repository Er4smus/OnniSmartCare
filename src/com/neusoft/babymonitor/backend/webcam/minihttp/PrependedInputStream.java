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

package com.neusoft.babymonitor.backend.webcam.minihttp;

import java.io.*;

/**
 * Pre-read data is returned if available, then continues reading form the InputStream.
 */
public class PrependedInputStream extends InputStream {

    private InputStream input;
    private byte[] buffer;
    private int bufferOffset;

    /**
     * Constructs an object with a buffer of previously read data and an InputStream to read the rest of the data from.
     */
    public PrependedInputStream(byte[] data, int offset, int length, InputStream is) {
        buffer = new byte[length];
        System.arraycopy(data, offset, buffer, 0, length);
        bufferOffset = 0;
        this.input = is;
    }

    public int read() throws IOException {
        byte[] shortBuffer = new byte[1];
        read(shortBuffer, 0, 1);
        return shortBuffer[0];
    }

    public int read(byte[] data) throws IOException {
        return read(data, 0, data.length);
    }

    public int read(byte[] data, int offset, int length) throws IOException {

        int numBytes = -1;

        if (bufferOffset < buffer.length) {

            // number of bytes to read
            numBytes = Math.min(buffer.length - bufferOffset, length);

            // copy the data
            System.arraycopy(buffer, bufferOffset, data, offset, numBytes);

            // increment buffer offset
            bufferOffset += numBytes;

        } else {

            // read from the real input stream
            numBytes = input.read(data, offset, length);

        }

        return numBytes;

    }

}
