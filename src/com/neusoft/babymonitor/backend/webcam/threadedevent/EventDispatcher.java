/*
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

package com.neusoft.babymonitor.backend.webcam.threadedevent;

/*
 * Author: User
 * Created: 2010. oct 26. 13:29:14
 * Modified: 2010. oct 26. 13:29:14
 */

import com.neusoft.babymonitor.backend.webcam.threadedevent.EventListener;
import com.neusoft.babymonitor.backend.webcam.threadedevent.EventQueue;

import java.util.Vector;

public class EventDispatcher implements Runnable {

    private boolean runs;

    private EventQueue eventQueue;

    private Vector<EventListener> listeners = new Vector<EventListener>();

    public EventDispatcher(EventQueue eventQueue) {
        this.eventQueue = eventQueue;
    }

    public void addListener(EventListener listener) {
        listeners.add(listener);
        // System.out.println("added: " + listener);
    }

    public void removeListener(EventListener listener) {
        listeners.remove(listener);
        // System.out.println("removed: " + listener);
    }

    public int countListeners() {
        return listeners.size();
    }

    public void start() {
        runs = true;
        new Thread(this).start();
    }

    public void run() {
        while (runs) {
            Event event = eventQueue.poll();

            for (int i = 0; i < listeners.size(); i++) {
                // System.out.println("E: " + i);
                try {
                    listeners.get(i).handleEvent(event);
                } catch (Exception e) {
                    e.printStackTrace();
                    listeners.remove(i);
                    System.out.println("Exception wile processing event. Affected listener removed.");
                }
            }
        }
    }

    public boolean runs() {
        return runs;
    }

    public void stop() {
        runs = true;
    }

}
