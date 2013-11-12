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

import java.io.*;
import java.net.*;

public interface HTTPRequest {

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";

    /**
     * Gets request method (e.g. GET, POST, HEAD).
     * 
     * @return The query string.
     */
    public String getMethod();

    /**
     * Gets request URI (request path with query string).
     * 
     * @return The query string.
     */
    public String getRequestURI();

    /**
     * Gets request path: the part of the URI without the query string (and the trailing '?').
     * 
     * @return The query string.
     */
    public String getPathName();

    /**
     * Gets query string: the part ofthe URI after the '?' sign.
     * 
     * @return The query string.
     */
    public String getQueryString();

    /**
     * Gets treuest version. (e.g. <code>HTTP/1.1</code>)
     * 
     * @param key Header key.
     * @return Array containing the values for all header lines with this key.
     */
    public String getVersion();

    /**
     * Returns header fields with this identifier.
     * 
     * @param key Header key.
     * @return Array containing the values for all header lines with this key.
     */
    public String[] getHeaders(String key);

    /**
     * Returns the lastest header field with this identifier.
     * 
     * @param key Header key.
     * @return The latest request header line with the given key.
     */
    public String getHeader(String key);

    /**
     * Returns all the get parameters with this identifier (in the order of appearance).
     * 
     * @param key Key in the query string.
     * @return Array containing the values for all get variables with this key.
     */
    public String[] getParameters(String key);

    /**
     * Returns the latest get parameter with this identifier.
     * 
     * @param key Key in the query string.
     * @return The latest get variable with the given key.
     */
    public String getParameter(String key);

    /**
     * Returns the path bound to the resource, handling this request.
     * 
     * @return The latest get variable with the given key.
     */
    public String getResourcePath();

    /**
     * Returns the address of the remote host.
     * 
     * @return The address of the remote host.
     */
    public InetAddress getRemoteAddress();

    /**
     * Gets input stream to retrieve the request body.
     * 
     * @return InputStream with the request body.
     */
    public InputStream getInputStream();

    /**
     * Gets the socket on which the client is connected.
     * 
     * @return Socket on which the user is connected of <code>null</code> if not an HTTP connection.
     */
    public Socket getSocket();

}
