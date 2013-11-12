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

package com.neusoft.babymonitor.backend.webcam.util.stream;

/**
 * A <code>StreamProcessor</code> processes data from a buffer which holds a section of a coninuous stream. The fashion
 * in which the implementing class handles a case where the given offset is not at a start of a data structure (in some
 * terminologies this is an out-of-sync stream) depends on the implementation. As some binary protocols can recover form
 * this situation (e.g. MPEG Transport Stream) and others can not.
 */
public abstract class StreamProcessor {

    /**
     * Process binary data from this buffer.
     * 
     * @param buffer The buffer of data.
     * @param offset The offset of the first byte which needs to be processed within the buffer.
     * @param buffer The number of data bytes need to be processed.
     * @return Number of bytes succesfully processed.
     */
    public abstract int process(byte[] buffer, int offset, int length);

    /**
     * Process binary data from this buffer.
     * 
     * @param buffer The buffer of data.
     * @return Number of bytes succesfully processed.
     */
    public int process(StreamBuffer buffer) {
        return process(buffer.getData(), buffer.getOffset(), buffer.getLength());
    }

    /**
     * Checks if this processor has finished its job.
     * 
     * @return <code>true</code> if this processor extracted all the data.
     */
    public abstract boolean finished();
}
