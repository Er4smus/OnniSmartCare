package com.neusoft.babymonitor.backend.webcam.stream;

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

import com.neusoft.babymonitor.backend.webcam.threadedevent.GeneralEvent;

import java.util.Date;

public class ServerEvent extends GeneralEvent {

    public static final int INPUT_START = 1;
    public static final int INPUT_STOP = 2;
    public static final int INPUT_FRAGMENT_START = 3;
    public static final int INPUT_FRAGMENT_END = 4;
    public static final int INPUT_FIRST_FRAGMENT = 5;

    public static final int CLIET_START = 1001;
    public static final int CLIET_STOP = 1002;
    public static final int CLIET_FRAGMENT_START = 1003;
    public static final int CLIET_FRAGMENT_END = 1004;
    public static final int CLIET_FRAGMENT_SKIP = 1005;

    private Date startDate;

    private Object sourceStream;

    public ServerEvent(Object source, Object sourceStream, int type) {
        this(source, sourceStream, type, null);
    }

    public ServerEvent(Object source, Object sourceStream, int type, Date startDate) {
        super(source, type);
        this.sourceStream = sourceStream;
        this.startDate = startDate;
    }

    public Object getSourceStreram() {
        return sourceStream;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(40);
        sb.append("{cls:1, type:");
        sb.append(getType());
        if (startDate != null) {
            sb.append(",start:");
            sb.append(startDate.getTime());
        }
        sb.append(",time:");
        sb.append(getDate().getTime());
        sb.append("},");
        return sb.toString();
    }
}
