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

import java.util.zip.*;
import java.io.*;

public class HTTPZipResource implements HTTPResource {

    protected ZipFile zip;

    public HTTPZipResource(String fileName) throws ZipException, IOException {
        zip = new ZipFile(fileName);
    }

    public void serve(HTTPRequest request, HTTPResponse response) throws HTTPException {
        final int BUFFER_LENGTH = 16 * 1024;

        // only GET is allowed
        if (!request.getMethod().equals(HTTPRequest.METHOD_GET))
            throw new HTTPException(400, "Bad Request");

        String entryName = request.getPathName().substring(request.getResourcePath().length() + 1);
        ZipEntry entry = zip.getEntry(entryName);

        if (entry == null)
            throw new HTTPException(404, "Not Found");

        try {
            InputStream is = zip.getInputStream(entry);
            byte[] buffer = new byte[BUFFER_LENGTH];

            response.setParameter("X-Powered-By", "HTTPZipResource");

            response.setParameter("Content-length", Long.toString(entry.getSize()));
            OutputStream os = response.getOutputStream();
            int red;
            while ((red = is.read(buffer)) != -1) {
                os.write(buffer, 0, red);
            }

            is.close();
            os.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
