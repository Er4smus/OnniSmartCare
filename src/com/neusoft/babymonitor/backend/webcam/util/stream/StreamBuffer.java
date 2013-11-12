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

package com.neusoft.babymonitor.backend.webcam.util.stream;

/**
 * A byte array based buffer. The payload can be an arbitrrary region of the array.
 */
public class StreamBuffer {

    private byte[] data;
    private int offset;
    private int length;

    /**
     * Creates a </code>StreamBuffer</code> with the given size.
     */
    public StreamBuffer(int size) {
        data = new byte[size];

    }

    /**
     * Moves the payload of this buffer to the start of the array.
     */
    public void compact() {
        System.arraycopy(data, offset, data, 0, length);
        offset = 0;
    }

    /**
     * Gets the array containing the data of this buffer.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Gets the offset of the first byte containing the data of this buffer inside the buffer array.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Signals that the first <code>numOfBytes</code> bytes has been processed and should not be considered as part of
     * the payload anymore.
     * 
     * @param numOfBytes The number of bytes processed.
     */
    public void markProcessed(int numOfBytes) {
        if (numOfBytes < 0)
            throw new RuntimeException("Negative number of bytes is unsupported: " + numOfBytes);
        if (numOfBytes > length)
            throw new RuntimeException("Region too big: " + numOfBytes + " (length is: " + length + ").");

        length -= numOfBytes;
        offset += numOfBytes;
    }

    /**
     * Signals that <code>numOfBytes</code> bytes of new data has been appended to the end of the payload.
     * 
     * @param numOfBytes The number of bytes appended.
     */
    public void markAppended(int numOfBytes) {
        if (numOfBytes < 0)
            throw new RuntimeException("Negative number of bytes is unsupported: " + numOfBytes);
        if (numOfBytes > data.length - (offset + length))
            throw new RuntimeException("Region too big: " + numOfBytes + " (empty space is: "
                    + (data.length - (offset + length)) + ").");

        length += numOfBytes;
    }

    /**
     * Gets the number of bytes containing the data of this buffer.
     */
    public int getLength() {
        return length;
    }

}
