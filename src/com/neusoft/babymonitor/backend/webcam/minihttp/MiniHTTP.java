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

import com.neusoft.babymonitor.backend.webcam.util.stream.StreamBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class MiniHTTP extends Thread {

    protected Map<String, HTTPResource> resources = new HashMap<String, HTTPResource>();
    private ServerSocket serverSocket;

    public MiniHTTP(ServerSocket serverSocket) {
        super();
        this.serverSocket = serverSocket;
    }

    public void run() {
        try {
            while (true) {
                Socket clientSock = serverSocket.accept();
                new Worker(clientSock).start();
            }
        } catch (IOException e) {
            System.out.println("Error accepting connection on port: " + serverSocket.getLocalPort());
        }

    }

    public void registerResource(String path, HTTPResource resource) {
        if (path.length() > 1 && path.endsWith("/"))
            throw new RuntimeException("Resource identifiers can not end with / character.");

        resources.put(path, resource);
    }

    public void unregisterResource(String path) {
        resources.remove(path);
    }

    class Worker extends Thread {

        private Socket sock;

        public Worker(Socket sock) {
            this.sock = sock;
        }

        public void run() {
            InputStream is;
            OutputStream os;
            try {
                is = sock.getInputStream();
                os = sock.getOutputStream();
            } catch (IOException e) {
                throw new RuntimeException("Error opening streams");
            }

            HTTPResponse response = new HTTPResponse(os);
            HTTPRequestLoader request = null;

            // try {
            request = new HTTPRequestLoader(new StreamBuffer(32 * 1024), is);
            // } catch (HTTPException e) {
            // response.reportException(e);
            // return;
            // }

            request.setRemoteAddress(sock.getInetAddress());
            request.setSocket(sock);

            HTTPResource resource = null;

            String pathName = request.getPathName();
            while ((resource = resources.get(pathName)) == null) {
                int pos = pathName.lastIndexOf("/");
                if (pos == -1)
                    break;
                pathName = pathName.substring(0, pos);
            }
            request.setResourcePath(pathName);

            if (resource == null) {
                response.reportException(new HTTPException(404, "Not Found"));
                return;
            }

            // load all the request headers before processing
            request.loadFully();
            try {
                resource.serve(request, response);
            } catch (HTTPException e) {
                response.reportException(e);
            } catch (Exception e) {
                e.printStackTrace();
                response.reportException(new HTTPException(500, "Internal Server Error"));
            }

            try {
                is.close();
                os.close();
            } catch (IOException e) {
                throw new RuntimeException("Error closing streams.");
            }
        }
    }

}
