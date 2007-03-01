/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */  

package org.apache.ftpserver.command;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.ftpserver.ServerDataConnectionFactory;
import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpReplyOutput;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.listener.Connection;
import org.apache.ftpserver.util.FtpReplyUtil;

/**
 * <code>PASV &lt;CRLF&gt;</code><br>
 *
 * This command requests the server-DTP to "listen" on a data
 * port (which is not its default data port) and to wait for a
 * connection rather than initiate one upon receipt of a
 * transfer command.  The response to this command includes the
 * host and port address this server is listening on.
 */
public 
class PASV extends AbstractCommand {

    /**
     * Execute command
     */
    public void execute(Connection connection, 
                        FtpRequest request,
                        FtpSessionImpl session, 
                        FtpReplyOutput out) throws IOException, FtpException {
        
        // reset state variables
        session.resetState();
        
        // set data connection
        ServerDataConnectionFactory dataCon = session.getServerDataConnection();
        if (!dataCon.setPasvCommand()) {
            out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_425_CANT_OPEN_DATA_CONNECTION, "PASV", null));
            return;   
        }
        
        // get connection info
        InetAddress servAddr = dataCon.getInetAddress();
        int servPort = dataCon.getPort();
        
        // send connection info to client
        String addrStr = servAddr.getHostAddress().replace( '.', ',' ) + ',' + (servPort>>8) + ',' + (servPort&0xFF);
        out.write(FtpReplyUtil.translate(session, FtpReply.REPLY_227_ENTERING_PASSIVE_MODE, "PASV", addrStr));
    }
}