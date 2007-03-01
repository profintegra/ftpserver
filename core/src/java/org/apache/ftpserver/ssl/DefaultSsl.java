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

package org.apache.ftpserver.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.HashMap;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.ftpserver.FtpServerConfigurationException;
import org.apache.ftpserver.interfaces.Ssl;
import org.apache.ftpserver.util.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Ssl implementation. This class encapsulates all 
 * the SSL functionalities.
 */
public class DefaultSsl implements Ssl {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultSsl.class);
    
    private File keystoreFile = new File("./res/.keystore");
    private String keystorePass = "password";   // TODO should we really default this value?
    private String keystoreType = "JKS";
    private String keystoreAlgorithm = "SunX509";
    
    private String sslProtocol = "TLS";
    private boolean clientAuthReqd = false;
    private String keyPass = "password";   // TODO should we really default this value?

    private KeyStore keyStore;
    private KeyManagerFactory keyManagerFactory;
    private TrustManagerFactory trustManagerFactory;
    
    private HashMap sslContextMap;
    
    public void setKeystoreFile(File keyStoreFile) {
        this.keystoreFile = keyStoreFile;
    }
    
    public void setKeystorePassword(String keystorePass) {
        this.keystorePass = keystorePass;
    }
    
    public void setKeystoreType(String keystoreType) {
        this.keystoreType = keystoreType;
    }
    
    public void setKeystoreAlgorithm(String keystoreAlgorithm) {
        this.keystoreAlgorithm = keystoreAlgorithm;
    }
    
    public void setSslProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }
    
    public void setClientAuthentication(boolean clientAuthReqd) {
        this.clientAuthReqd = clientAuthReqd;
    }
    
    public void setKeyPassword(String keyPass) {
        this.keyPass = keyPass;
    }
    
    
    /**
     * Configure secure server related properties. 
     */
    public synchronized void init() {
        
        try {
            // initialize keystore
            FileInputStream fin = null;
            try {
                fin = new FileInputStream(keystoreFile);
                keyStore = KeyStore.getInstance(keystoreType);
                keyStore.load(fin, keystorePass.toCharArray());
            }
            finally {
                IoUtils.close(fin);
            }
            
            // initialize key manager factory
            keyManagerFactory = KeyManagerFactory.getInstance(keystoreAlgorithm);
            keyManagerFactory.init(keyStore, keyPass.toCharArray());
            
            // initialize trust manager factory
            trustManagerFactory = TrustManagerFactory.getInstance(keystoreAlgorithm);
            trustManagerFactory.init(keyStore);
            
            // create ssl context map - the key is the 
            // SSL protocol and the value is SSLContext.
            sslContextMap = new HashMap();
        }
        catch(Exception ex) {
            LOG.error("DefaultSsl.configure()", ex);
            throw new FtpServerConfigurationException("DefaultSsl.configure()", ex);
        }
    }
    
    private void lazyInit() {
        if(keyManagerFactory == null) {
            init();
        }
    }
    
    /**
     * Get SSL Context.
     */
    public synchronized SSLContext getSSLContext(String protocol) throws GeneralSecurityException {
        lazyInit();
        
        // null value check
        if(protocol == null) {
            protocol = sslProtocol;
        }
        
        // if already stored - return it
        SSLContext ctx = (SSLContext)sslContextMap.get(protocol);
        if(ctx != null) {
            return ctx;
        }
        
        // create SSLContext
        ctx = SSLContext.getInstance(protocol);
        ctx.init(keyManagerFactory.getKeyManagers(), 
                 trustManagerFactory.getTrustManagers(), 
                 null);

        // store it in map
        sslContextMap.put(protocol, ctx);
        return ctx;
    }

    /**
     * Create secure server socket.
     */
    public ServerSocket createServerSocket(String protocol,
                                           InetAddress addr, 
                                           int port) throws Exception {
        lazyInit();
        
        // get server socket factory
        SSLContext ctx = getSSLContext(protocol);
        SSLServerSocketFactory ssocketFactory = ctx.getServerSocketFactory();
        
        // create server socket
        SSLServerSocket serverSocket = null;
        if(addr == null) {
            serverSocket = (SSLServerSocket) ssocketFactory.createServerSocket(port, 100);
        }
        else {
            serverSocket = (SSLServerSocket) ssocketFactory.createServerSocket(port, 100, addr);
        }
        
        // initialize server socket
        String cipherSuites[] = serverSocket.getSupportedCipherSuites();
        serverSocket.setEnabledCipherSuites(cipherSuites);
        serverSocket.setNeedClientAuth(clientAuthReqd);
        return serverSocket;
    }
 
    /**
     * Returns a socket layered over an existing socket.
     */
    public Socket createSocket(String protocol,
                               Socket soc, 
                               boolean clientMode) throws Exception {
        lazyInit();
        
        // already wrapped - no need to do anything
        if(soc instanceof SSLSocket) {
            return soc;
        }
        
        // get socket factory
        SSLContext ctx = getSSLContext(protocol);
        SSLSocketFactory socFactory = ctx.getSocketFactory();
        
        // create socket
        String host = soc.getInetAddress().getHostAddress();
        int port = soc.getLocalPort();
        SSLSocket ssoc = (SSLSocket)socFactory.createSocket(soc, host, port, true);
        ssoc.setUseClientMode(clientMode);
        
        // initialize socket
        String cipherSuites[] = ssoc.getSupportedCipherSuites();
        ssoc.setEnabledCipherSuites(cipherSuites);
        ssoc.setNeedClientAuth(clientAuthReqd);
        
        return ssoc;
    }

    /**
     * Create a secure socket.
     */
    public Socket createSocket(String protocol,
                               InetAddress addr, 
                               int port,
                               boolean clientMode) throws Exception {
        lazyInit();
        
        // get socket factory
        SSLContext ctx = getSSLContext(protocol);
        SSLSocketFactory socFactory = ctx.getSocketFactory();
        
        // create socket
        SSLSocket ssoc = (SSLSocket)socFactory.createSocket(addr, port);
        ssoc.setUseClientMode(clientMode);
        
        // initialize socket
        String cipherSuites[] = ssoc.getSupportedCipherSuites();
        ssoc.setEnabledCipherSuites(cipherSuites);
        return ssoc;
    } 
    
    /**
     * Create a secure socket.
     */
    public Socket createSocket(String protocol,
                               InetAddress host,
                               int port,
                               InetAddress localhost,
                               int localport,
                               boolean clientMode) throws Exception {
        lazyInit();
        
        // get socket factory
        SSLContext ctx = getSSLContext(protocol);
        SSLSocketFactory socFactory = ctx.getSocketFactory();
        
        // create socket
        SSLSocket ssoc = (SSLSocket)socFactory.createSocket(host, port, localhost, localport);
        ssoc.setUseClientMode(clientMode);
        
        // initialize socket
        String cipherSuites[] = ssoc.getSupportedCipherSuites();
        ssoc.setEnabledCipherSuites(cipherSuites);
        return ssoc;
    }
    
    /**
     * Dispose - does nothing.
     */
    public void dispose() {
    }

    public boolean getClientAuthenticationRequired() {
        return clientAuthReqd;
    }

    public SSLContext getSSLContext() throws GeneralSecurityException {
        return getSSLContext(sslProtocol);
    }
}