package com.neusoft.babymonitor.backend.webcam.stream;

import com.neusoft.babymonitor.backend.webcam.ui.observer.StreamingStatus;

/*
 This file is part of �Onni smart care desktop application� software�.

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

class HeaderDetectionState implements StreamInputState {

    private static final long ID_EBML = 0x1A45DFA3;
    private static final long ID_SEGMENT = 0x18538067;
    private static final long ID_INFO = 0x1549A966;
    private static final long ID_TRACKS = 0x1654AE6B;
    private static final long ID_TRACKTYPE = 0x83;
    private static final long ID_TRACKNUMBER = 0xD7;
    private static final long TRACK_TYPE_VIDEO = 1;

    private StreamInput input;
    private Stream stream;

    private static final byte[] infiniteSegment = { 0x18, 0x53, (byte) 0x80, 0x67, 0x01, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };

    StreamingStatus streamingStatus = StreamingStatus.getInstance();

    public HeaderDetectionState(StreamInput input, Stream stream) {
        this.input = input;
        this.stream = stream;
    }

    public int processData(byte[] buffer, int offset, int length) {

        int startOffset = offset;
        int endOffset = offset + length;

        byte[] headerBuffer = new byte[65536];
        int headerLength = 0;

        EBMLElement elem;

        // EBML root element
        elem = new EBMLElement(buffer, offset, length);

        // if not EBML
        if (elem.getId() != ID_EBML)
            throw new RuntimeException("First element is not EBML!");

        // COPYING: EBML headerBuffer
        System.arraycopy(buffer, elem.getElementOffset(), headerBuffer, headerLength, elem.getElementSize());
        headerLength += elem.getElementSize();

        offset = elem.getEndOffset();

        // COPYING: infinite Segment
        System.arraycopy(infiniteSegment, 0, headerBuffer, headerLength, infiniteSegment.length);
        headerLength += infiniteSegment.length;

        // looking for: Segment
        do {
            elem = new EBMLElement(buffer, offset, length);
            if (elem.getId() == ID_SEGMENT)
                break;
            offset = elem.getEndOffset();
        } while (offset < endOffset);

        // not found
        if (offset >= endOffset)
            return startOffset;

        System.out.println("Segment found");

        int segmentDataOffset = elem.getDataOffset();

        // looking for: Info
        offset = segmentDataOffset;
        do {
            elem = new EBMLElement(buffer, offset, length);
            offset = elem.getEndOffset();
        } while (offset < endOffset && elem.getId() != ID_INFO);

        // not found
        if (offset >= endOffset)
            return startOffset;

        System.out.println("Info found");

        // COPYING: Info headerBuffer
        System.arraycopy(buffer, elem.getElementOffset(), headerBuffer, headerLength, elem.getElementSize());
        headerLength += elem.getElementSize();

        // looking for: Tracks
        offset = segmentDataOffset;
        do {
            elem = new EBMLElement(buffer, offset, length);
            offset = elem.getEndOffset();
        } while (offset < endOffset && elem.getId() != ID_TRACKS);

        // not found
        if (offset >= endOffset)
            return startOffset;

        System.out.println("Tracks found");

        // COPYING: Tracks headerBuffer
        System.arraycopy(buffer, elem.getElementOffset(), headerBuffer, headerLength, elem.getElementSize());
        headerLength += elem.getElementSize();

        // searching for video track's id
        long videoTrackNumber = 0;
        int endOfTracks = elem.getEndOffset();
        offset = elem.getDataOffset();
        while (offset < endOfTracks) {
            EBMLElement track = new EBMLElement(buffer, offset, endOfTracks - offset);
            offset = track.getDataOffset();
            int endOfTrack = track.getEndOffset();

            long trackType = 0;
            long trackNumber = 0;
            while (offset < endOfTrack) {
                EBMLElement property = new EBMLElement(buffer, offset, endOfTrack - offset);
                if (property.getId() == ID_TRACKTYPE) {
                    trackType = buffer[property.getDataOffset()] & 0xff;
                } else if (property.getId() == ID_TRACKNUMBER) {
                    trackNumber = EBMLElement.loadUnsigned(buffer, property.getDataOffset(),
                            (int) property.getDataSize());
                }
                offset = property.getEndOffset();
            }
            System.out.println("track no: " + trackNumber + ", type: " + trackType);
            if (trackType == TRACK_TYPE_VIDEO)
                videoTrackNumber = trackNumber;

            offset = track.getEndOffset();
        }

        System.out.println("ALL'S WELL");
        streamingStatus.setState("START");

        // setting header for the stream
        byte[] header = new byte[headerLength];
        System.arraycopy(headerBuffer, 0, header, 0, headerLength);
        stream.setHeader(header);

        // change state
        input.changeState(new StreamingState(input, stream, videoTrackNumber));

        return segmentDataOffset;
    }

}
