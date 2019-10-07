/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sonypjtalk.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class allow communication with a PJTalk device in SDCP.
 * It seems that only video projector from Sony uses this protocol.
 * Even so, this class is made to allow different device type
 * to communicate thru PJTalk - SDCP communication.
 *
 * @author Fabien Philippe - Initial contribution
 *
 */
public class SDCPSocket {
    private final Logger logger = LoggerFactory.getLogger(SDCPSocket.class);

    /**
     * Hostname of the PJTalk device
     */
    private String _hostName;

    /**
     * Community used for PJTalk devices on the network.
     * By default, the community is SONY.
     */
    private String _community;

    /**
     * TCP socket used to communicate with the device.
     *
     * @remark this socket has to be kept open as far as
     *         PJTalk devices seems to accept only one connection
     *         at the same time. every thread have to share this
     *         socket.
     */
    private static Socket _socket;

    /**
     * Output stream used to send messages to the device.
     */
    private static OutputStream _outputStream;

    /**
     * InputStream used to receive messages from the device.
     */
    private static InputStream _inputStream;

    /**
     * The constructor needs to parameters and does nothing other than copy
     * parameters
     *
     * @param hostName of the device
     * @param community used to communicate on the network
     */
    public SDCPSocket(String hostName, String community) {
        this._hostName = hostName;
        this._community = community;
    }

    /**
     * Attempts to open a connection with the device.
     *
     * @remarks TCP communication must be opened in less than 5 seconds.
     *          All other communication must be done in less than 2 seconds.
     */
    public synchronized boolean open() {
        if (isConnected()) {
            logger.debug("open: connection is already open");
            return true;
        } else if (null == _socket) {
            try {
                _socket = new Socket();
                _socket.connect(new InetSocketAddress(this._hostName, 53484), 5000);
                _socket.setSoTimeout(2000);
                _outputStream = _socket.getOutputStream();
                _inputStream = _socket.getInputStream();
                logger.warn("open: connection opened successfully");
                return true;
            } catch (UnknownHostException unknownHostException) {
                logger.warn("open: unknown host. Exception is {}", unknownHostException.getMessage());
                _socket = null;
                return false;
            } catch (IOException ioException) {
                logger.warn("open: connection failed. Exception is {}", ioException.getMessage());
                try {
                    _socket.close();
                } catch (Exception e) {
                }
                _socket = null;
            } catch (Exception e) {
                logger.warn("open: connection failed. Exception is {}", e.getMessage());
                try {
                    _socket.close();
                } catch (Exception innerE) {
                }
                _socket = null;
            }
        }

        return false;
    }

    /**
     * close the communication with the device.
     */
    public synchronized void close() {
        // if there is an old web socket then clean up and destroy
        if (isConnected()) {
            try {
                _socket.close();
                logger.debug("close: connection successfully closed");
            } catch (Exception e) {
                logger.warn("close: Exception during closing the socket {}", e.getMessage());
            }
        }

        _socket = null;
    }

    /**
     * Allow to know if communication is opened.
     *
     * @return true if connected
     */
    public synchronized boolean isConnected() {
        if (null == _socket) {
            return false;
        }

        return _socket.isConnected();
    }

    /**
     * Attempts to send a Set command to the device.
     *
     * @param itemNumber is the item number of the command to be send.
     * @param data must be set to byte[0] if no data has to be sent.
     * @return true if command has been sent successfully.
     */
    public synchronized boolean sendSetCommand(int itemNumber, byte[] data) {
        try {
            if (isConnected()) {
                byte[] message = new byte[10 + data.length];
                int index = 0;
                message[index++] = (byte) 0x02;
                message[index++] = (byte) 0x0A;
                message[index++] = (byte) _community.charAt(0);
                message[index++] = (byte) _community.charAt(1);
                message[index++] = (byte) _community.charAt(2);
                message[index++] = (byte) _community.charAt(3);
                message[index++] = (byte) 0x00;
                message[index++] = (byte) ((itemNumber & 0xFF00) / 256);
                message[index++] = (byte) (itemNumber & 0xFF);
                message[index++] = (byte) data.length;

                for (byte octet : data) {
                    message[index++] = octet;
                }

                _outputStream.write(message, 0, index);
                _outputStream.flush();
                logger.debug("sendSetCommand: command {} successfully sent.", itemNumber);
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            logger.warn("sendSetCommand: IOException {}.", e.toString());
            close();
            return false;
        } catch (Exception e) {
            logger.warn("sendSetCommand: Exception {}.", e.toString());
            close();
            return false;
        }
    }

    /**
     * Attempts to send a Get command to the device and receive the result.
     *
     * @param itemNumber is the item number of the command to be send.
     * @param data must be set to byte[0] if no data has to be sent.
     * @return null if failed, byte array if success (eventually void).
     */
    public synchronized byte[] sendGetCommand(int itemNumber, byte[] data) {
        try {
            if (isConnected()) {
                _socket.setSoTimeout(10);
                try {
                    while (-1 != _inputStream.read()) {
                    }
                } catch (SocketTimeoutException ex) {
                }
                _socket.setSoTimeout(2000);
                byte[] message = new byte[10 + data.length];
                int index = 0;
                message[index++] = (byte) 0x02;
                message[index++] = (byte) 0x0A;
                message[index++] = (byte) _community.charAt(0);
                message[index++] = (byte) _community.charAt(1);
                message[index++] = (byte) _community.charAt(2);
                message[index++] = (byte) _community.charAt(3);
                message[index++] = (byte) 0x01;
                message[index++] = (byte) ((itemNumber & 0xFF00) / 256);
                message[index++] = (byte) (itemNumber & 0xFF);
                message[index++] = (byte) data.length;

                for (byte octet : data) {
                    message[index++] = octet;
                }

                _outputStream.write(message, 0, index);
                _outputStream.flush();
                logger.debug("sendGetCommand: command {} successfully sent.", itemNumber);
                return readGetCommandResult(itemNumber);
            } else {
                return null;
            }
        } catch (IOException e) {
            logger.warn("sendGetCommand: IOException {}.", e.toString());
            close();
            return null;
        } catch (Exception e) {
            logger.warn("sendGetCommand: Exception {}.", e.toString());
            close();
            return null;
        }
    }

    /**
     * Attempts to read the result of a Get command previously sent.
     *
     * @param itemNumberis the item number of the command that has been previously sent.
     * @return null if failed, byte array if success (eventually void).
     */
    private synchronized byte[] readGetCommandResult(int itemNumber) {
        try {
            if (isConnected()) {
                if (0x02 != _inputStream.read()) {
                    return null;
                }
                if (0x0A != _inputStream.read()) {
                    return null;
                }

                if (_community.charAt(0) != _inputStream.read()) {
                    return null;
                }
                if (_community.charAt(1) != _inputStream.read()) {
                    return null;
                }
                if (_community.charAt(2) != _inputStream.read()) {
                    return null;
                }
                if (_community.charAt(3) != _inputStream.read()) {
                    return null;
                }

                if (0x01 != _inputStream.read()) {
                    return null;
                }

                if (((itemNumber & 0xFF00) / 256) != _inputStream.read()) {
                    return null;
                }
                if ((itemNumber & 0xFF) != _inputStream.read()) {
                    return null;
                }

                logger.debug("readGetCommandResult: header read successfully.");

                int dataLength = _inputStream.read();

                byte[] message = new byte[dataLength];
                int index = 0;
                while (index < dataLength) {
                    message[index] = (byte) _inputStream.read();
                    index++;
                }

                logger.debug("readGetCommandResult: message read successfully.");
                return message;
            } else {
                return null;
            }
        } catch (SocketTimeoutException e) {
            logger.warn("readGetCommandResult: SocketTimeoutException {}.", e.toString());
            close();
            return null;
        } catch (IOException e) {
            logger.warn("readGetCommandResult: IOException {}.", e.toString());
            close();
            return null;
        } catch (Exception e) {
            logger.warn("readGetCommandResult: Exception {}.", e.toString());
            close();
            return null;
        }
    }
}
