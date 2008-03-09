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

package org.apache.ftpserver.ftplet;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * Empty configuration - always returns the default values.
 */
public 
class EmptyConfiguration implements Configuration {

    public final static EmptyConfiguration INSTANCE = new EmptyConfiguration();
    
    /**
     * Private constructor - use INSTANCE static variable.
     */
    private EmptyConfiguration() {
    }
    
    /**
     * Is empty?
     */
    public boolean isEmpty() {
        return true;
    }
    
    /**
     * Throw exception.
     */
    public String getString(String param) throws FtpException {
        throw new FtpException();
    }

    /**
     * Return default value.
     */
    public String getString(String patram, String defaultVal) {
        return defaultVal;
    }

    /**
     * Throw exception.
     */
    public int getInt(String patram) throws FtpException {
        throw new FtpException();
    }

    /**
     * Return default value.
     */
    public int getInt(String patram, int defaultVal) {
        return defaultVal;
    }

    /**
     * Throw exception.
     */
    public long getLong(String param) throws FtpException {
        throw new FtpException();
    }

    /**
     * Return default value.
     */
    public long getLong(String param, long defaultVal) {
        return defaultVal;
    }

    /**
     * Throw exception.
     */
    public boolean getBoolean(String patram) throws FtpException {
        throw new FtpException();
    }

    /**
     * Return default value.
     */
    public boolean getBoolean(String patram, boolean defaultVal) {
        return defaultVal;
    }

    /**
     * Throw exception.
     */
    public double getDouble(String patram) throws FtpException {
        throw new FtpException();
    }

    /**
     * Get default value.
     */
    public double getDouble(String patram, double defaultVal) {
        return defaultVal;
    }

    /**
     * Throw exception.
     */
    public Configuration subset(String param) {
        return this;
    }
    
    /**
     * Get the keys.
     */
    public Iterator<String> getKeys() {
        return new ArrayList<String>(1).iterator();
    }

}