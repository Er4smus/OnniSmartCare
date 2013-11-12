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

class EBMLElement {

    private long id;
    private long size;
    private byte[] buffer;
    private int offset;
    private int dataOffset;

    protected EBMLElement(byte[] buffer, int offset, int length) {
        this.buffer = buffer;
        this.offset = offset;

        long sizeFlag;
        long num;

        sizeFlag = 0x80;
        num = 0;
        while (((num |= buffer[offset++] & 0xff) & sizeFlag) == 0 && sizeFlag != 0) {
            num <<= 8;
            sizeFlag <<= 7;
        }

        id = num;

        sizeFlag = 0x80;
        num = 0;
        while (((num |= buffer[offset++] & 0xff) & sizeFlag) == 0 && sizeFlag != 0) {
            num <<= 8;
            sizeFlag <<= 7;
        }

        size = num ^ sizeFlag;

        dataOffset = offset;
    }

    public static long loadUnsigned(byte[] buffer, int offset, int length) {
        long num = 0;
        while (length > 0) {
            length--;
            num <<= 8;
            num |= buffer[offset++] & 0xff;
        }

        return num;
    }

    public static long loadEBMLUnsigned(byte[] buffer, int offset, int length) {

        long sizeFlag;
        long num;

        sizeFlag = 0x80;
        num = 0;
        while (((num |= buffer[offset++] & 0xff) & sizeFlag) == 0 && sizeFlag != 0) {
            num <<= 8;
            sizeFlag <<= 7;
        }

        return num ^ sizeFlag;
    }

    public static long loadEBMLSigned(byte[] buffer, int offset, int length) {

        long sizeFlag;
        long num;
        long negBits = -1 << 7;

        sizeFlag = 0x80;
        num = 0;
        while (((num |= buffer[offset++] & 0xff) & sizeFlag) == 0 && sizeFlag != 0) {
            num <<= 8;
            sizeFlag <<= 7;
            negBits <<= 7;
        }

        if ((num & sizeFlag >> 1) != 0)
            num |= negBits;

        return num;
    }

    public long getId() {
        return id;
    }

    public long getDataSize() {
        return size;
    }

    public int getElementSize() {
        if (size == 0x1ffffffffffffffL)
            return -1;

        if (size >= 0x100000000l)
            throw new RuntimeException("Element too long to get array offset.");

        return (int) (dataOffset - offset + size);
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public int getElementOffset() {
        return offset;
    }

    public int getDataOffset() {
        return dataOffset;
    }

    public int getEndOffset() {
        if (size == 0x1ffffffffffffffL)
            return -1;

        if ((dataOffset + size) >= 0x100000000l)
            throw new RuntimeException("Element too long to get array offset.");

        return (int) (dataOffset + size);
    }

    public String toString() {
        return "EBMLElement ID:0x" + Long.toHexString(id) + " size: " + size;
    }
}
