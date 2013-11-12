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

class Log {

    public static void displaySegment(byte[] buffer, int offset, int maxLength) {
        final int DISPLAY_MAX_LENGTH = 48;

        String s = "(" + maxLength + ")";
        for (int i = 0; i < Math.min(DISPLAY_MAX_LENGTH, maxLength); i++) {
            String digit = new String("0" + Integer.toHexString(buffer[offset + i] & 0xff));
            s += " " + digit.substring(digit.length() - 2);
        }
        s += "  ";
        for (int i = 0; i < Math.min(DISPLAY_MAX_LENGTH, maxLength); i++) {
            int num = buffer[offset + i] & 0xff;
            s += num > 13 ? (char) num : '.';
        }
        if (maxLength > DISPLAY_MAX_LENGTH)
            s += "  >>";

        System.out.println(s);
    }

    public static void reportChunk(byte[] buffer, int offset, int length) {

        int endOffset = offset + length;

        // first NAL Unit
        for (int i = offset; i < endOffset - 4; i++) {
            if (buffer[i] == 0 && buffer[i + 1] == 1) {
                offset = i + 2;
                break;
            }
        }

        while (offset <= endOffset - 4) {
            int startOffset = offset;

            for (int i = offset; i < endOffset - 4; i++) {
                if (buffer[i + 3] == 1 && buffer[i] == 0 && buffer[i + 1] == 0 && buffer[i + 2] == 0) {
                    offset = i;
                    break;
                }
            }

            if (offset == startOffset) {
                Log.displaySegment(buffer, startOffset, endOffset - startOffset);
                offset = endOffset;
            } else {
                Log.displaySegment(buffer, startOffset, offset - startOffset);
            }

            offset += 4;

        }
    }

    public static void displayTime(long time) {
        float sec = time / 90000f;
        String s;
        s = "" + (sec % 60);
        sec /= 60;
        s = ((long) sec % 60) + ":" + s;
        sec /= 60;
        s = (long) sec + ":" + s;
        System.out.println(s);
    }
}
