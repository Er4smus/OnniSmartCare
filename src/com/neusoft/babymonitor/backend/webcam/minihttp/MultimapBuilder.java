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

package com.neusoft.babymonitor.backend.webcam.minihttp;

import java.util.*;

/**
 * This class builds a multimap where more than one value can be associated to a key.
 */
class MultimapBuilder {

    private Map<String, Vector<String>> buffer = new HashMap<String, Vector<String>>(10);

    /**
     * Adds a key-value pair to the multimap.
     * 
     * @param key The key.
     * @param value A value associated to the given key.
     */
    public void add(String key, String value) {
        Vector<String> v = buffer.get(key);
        if (v == null) {
            v = new Vector<String>(10);
            buffer.put(key, v);
        }
        v.add(value);
    }

    /**
     * Creates a new (multi)map where values are stored in arrays of strings instead of vectors of strings. The
     * advantage is a smaller memory footprint and faster access, but the conversion process adds its own overhead. Use
     * this if you are likely to access these values many times and they will be needed long enogh for the GC to
     * actually free up the memory used by the working map.
     */
    public Map<String, String[]> getCompactMap() {
        Map<String, String[]> result = new HashMap<String, String[]>(buffer.size());
        Iterator<Map.Entry<String, Vector<String>>> iterator = buffer.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Vector<String>> entry = iterator.next();
            Vector<String> v = entry.getValue();
            String[] values = new String[v.size()];
            for (int i = 0; i < v.size(); i++)
                values[i] = v.get(i);
            result.put(entry.getKey(), values);
        }
        return result;
    }
}