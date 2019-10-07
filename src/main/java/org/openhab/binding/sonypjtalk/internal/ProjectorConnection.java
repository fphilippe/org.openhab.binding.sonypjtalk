/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sonypjtalk.internal;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class allow to communicate with a video projector using
 * PJTalk - SDCP protocols from Sony.
 *
 * @author Fabien Philippe - Initial contribution
 *
 */
public class ProjectorConnection {
    private final Logger logger = LoggerFactory.getLogger(ProjectorConnection.class);

    /**
     * SDCP client socket used to communicate with the projector.
     */
    private SDCPSocket _socket;

    /**
     *
     * @param hostName of the device
     * @param community used to communicate on the network
     */
    public ProjectorConnection(String hostName, String community) {
        _socket = new SDCPSocket(hostName, community);
    }

    /**
     * This has to be called to free resources.
     */
    public void close() {
        try {
            _socket.close();
        } catch (Exception ex) {
        }
    }

    /**
     * Depending on the powerStatus value, attempts to power on or off
     * the projector.
     *
     * @param powerStatus must be true to power on the projector.
     */
    public synchronized void setPowerStatus(boolean powerStatus) {
        try {
            _socket.open();

            if (powerStatus) {
                _socket.sendSetCommand(0x172e, new byte[] {});
            } else {
                _socket.sendSetCommand(0x172f, new byte[] {});
            }
        } catch (Exception e) {
        }
    }

    /**
     * Attempts to deliver the power status of the projector.
     *
     * @return a string that can be : standby, startup, startup lamp,
     *         power on, cooling 1, cooling 2, saving cooling 1, saving cooling 2,
     *         saving standby or unknown. null is returned if communication
     *         failed.
     */
    public synchronized String getPowerStatus() {
        try {
            _socket.open();

            byte[] powerStatus = _socket.sendGetCommand(0x0102, new byte[] {});
            if (null == powerStatus) {
                return null;
            }

            switch (powerStatus[1]) {
                case 0:
                    return "standby";
                case 1:
                    return "startup";
                case 2:
                    return "startup lamp";
                case 3:
                    return "power on";
                case 4:
                    return "cooling 1";
                case 5:
                    return "cooling 2";
                case 6:
                    return "saving cooling 1";
                case 7:
                    return "saving cooling 2";
                case 8:
                    return "saving standby";
                default:
                    return "unknown";
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Attempts to retrieve model name of the projector.
     *
     * @return model name of the projector.
     */
    public synchronized String getModelName() {
        try {
            _socket.open();

            byte[] model = _socket.sendGetCommand(0x8001, new byte[] {});
            if (null == model) {
                return null;
            }

            String modelName = "";
            for (byte octet : model) {
                if (0 == octet) {
                    break;
                }
                modelName += (char) (octet & 0xFF);
            }

            return modelName;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Attempts to retrieve the lamp timer of the projector.
     *
     * @return lamp timer in hour or null if communication failed.
     */
    public synchronized Integer getLampTimer() {
        try {
            _socket.open();

            byte[] timer = _socket.sendGetCommand(0x0113, new byte[] {});
            if (null == timer || timer.length != 2) {
                return null;
            }

            return (timer[0] & 0xff) * 256 + (timer[1] & 0xff);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Attempts to retrieve the ip of the projector.
     *
     * @return ip of the projector or null if communication failed.
     */
    public synchronized InetAddress getIp() {
        try {
            _socket.open();

            byte[] ip = _socket.sendGetCommand(0x9001, new byte[] {});
            if (null == ip) {
                return null;
            }

            return InetAddress.getByAddress(ip);
        } catch (UnknownHostException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
