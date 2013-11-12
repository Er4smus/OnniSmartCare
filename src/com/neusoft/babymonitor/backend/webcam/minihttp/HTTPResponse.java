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
import java.util.*;

public class HTTPResponse {

    protected OutputStream outputStream;
    protected Map<String, String> parameters = new HashMap<String, String>(10);
    protected int responseCode = 200;
    protected String responseMessage = "OK";

    protected boolean open = true;

    public HTTPResponse(OutputStream outputStream) {
        this.outputStream = outputStream;
        parameters.put("Server", "MiniHTTP");
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public void setParameter(String key, String value) {
        parameters.put(key, value);
    }

    public OutputStream getOutputStream() {
        flushHeaders();
        return outputStream;
    }

    protected synchronized void flushHeaders() {

        // if already flushed
        if (!this.open)
            return;

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream));

        // response code
        writer.println("HTTP/1.1 " + responseCode + " " + responseMessage);

        // parameters
        Iterator<Map.Entry<String, String>> iterator = parameters.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            writer.println(entry.getKey() + ": " + entry.getValue());
        }

        // end of header
        writer.println();

        writer.flush();

        // note the closed state
        this.open = false;
    }

    public void reportException(HTTPException e) {
        setResponseCode(e.getCode());
        setResponseMessage(e.getMessage());
        setParameter("Content-type", "text/html");
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(getOutputStream()));
        writer.println("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">");
        writer.println("<http><head><title>" + e.getMessage() + "</title></head><body><h1>" + e.getMessage()
                + "</h1></body></http>");
        writer.close();
    }

}
