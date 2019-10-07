/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sonypjtalk.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.sonypjtalk.SonyPJTalkBindingConstants;
import org.openhab.binding.sonypjtalk.internal.ProjectorConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonyPJTalkHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Fabien Philippe - Initial contribution
 */
public class SonyPJTalkHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SonyPJTalkHandler.class);
    private ProjectorConnection connection;
    private ScheduledFuture<?> connectionCheckerFuture;
    private int successCheck = 0;

    public SonyPJTalkHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (connectionCheckerFuture != null) {
            connectionCheckerFuture.cancel(true);
        }
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(SonyPJTalkBindingConstants.CHANNEL_POWER)) {
            if (command.equals(OnOffType.ON)) {
                connection.setPowerStatus(true);
            } else if (command.equals(OnOffType.OFF)) {
                connection.setPowerStatus(false);
            } else if (command.equals(RefreshType.REFRESH)) {
                String powerStatus = connection.getPowerStatus();
                updateState(SonyPJTalkBindingConstants.CHANNEL_POWER,
                        ("standby" != powerStatus) ? OnOffType.ON : OnOffType.OFF);
                updateState(SonyPJTalkBindingConstants.CHANNEL_POWERSTATUS, new StringType(powerStatus));
            }
        } else if (channelUID.getId().equals(SonyPJTalkBindingConstants.CHANNEL_POWERSTATUS)) {
            if (command.equals(RefreshType.REFRESH)) {
                String powerStatus = connection.getPowerStatus();
                updateState(SonyPJTalkBindingConstants.CHANNEL_POWER,
                        ("standby" != powerStatus) ? OnOffType.ON : OnOffType.OFF);
                updateState(SonyPJTalkBindingConstants.CHANNEL_POWERSTATUS, new StringType(powerStatus));
            }
        } else if (channelUID.getId().equals(SonyPJTalkBindingConstants.CHANNEL_MODELNAME)) {
            if (command.equals(RefreshType.REFRESH)) {
                String modelName = connection.getModelName();
                updateState(SonyPJTalkBindingConstants.CHANNEL_MODELNAME, new StringType(modelName));
            }
        } else if (channelUID.getId().equals(SonyPJTalkBindingConstants.CHANNEL_LAMPTIMER)) {
            if (command.equals(RefreshType.REFRESH)) {
                Integer lampTimer = connection.getLampTimer();
                updateState(SonyPJTalkBindingConstants.CHANNEL_LAMPTIMER, new DecimalType(lampTimer));
            }
        }
    }

    @Override
    public void initialize() {
        try {
            String host = this.getConfig().get("ipAddress").toString();
            if (host == null || host.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "No network address specified");
                return;
            }

            String community = this.getConfig().get("community").toString();
            if (community == null || community.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No community specified");
                return;
            } else if (community.length() != 4) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Community must 4 characters long");
                return;
            }

            connection = new ProjectorConnection(host, community);

            // Start the connection checker
            Runnable connectionChecker = new Runnable() {
                @Override
                public void run() {
                    try {
                        // InetAddress ip = connection.getIp();
                        String powerStatus = connection.getPowerStatus();
                        if (null == powerStatus) {
                            updateStatus(ThingStatus.OFFLINE);
                        } else {
                            updateStatus(ThingStatus.ONLINE);
                            updateState(SonyPJTalkBindingConstants.CHANNEL_POWER,
                                    ("standby" != powerStatus) ? OnOffType.ON : OnOffType.OFF);
                            updateState(SonyPJTalkBindingConstants.CHANNEL_POWERSTATUS, new StringType(powerStatus));
                            successCheck++;

                            if (successCheck > 10) {
                                // More or less, every minute, we will refresh lamp timer
                                // and model name
                                String modelName = connection.getModelName();
                                if (null != modelName) {
                                    updateState(SonyPJTalkBindingConstants.CHANNEL_MODELNAME,
                                            new StringType(modelName));
                                }

                                Integer lampTimer = connection.getLampTimer();
                                if (null != lampTimer) {
                                    updateState(SonyPJTalkBindingConstants.CHANNEL_LAMPTIMER,
                                            new DecimalType(lampTimer));
                                }

                                successCheck = 0;
                            }
                        }
                    } catch (Exception ex) {
                        logger.warn("Exception in check connection to @{}. Cause: {}", host, ex.getMessage());
                    }
                }
            };
            connectionCheckerFuture = scheduler.scheduleWithFixedDelay(connectionChecker, 1, 5, TimeUnit.SECONDS);

            updateStatus(ThingStatus.INITIALIZING);
        } catch (Exception e) {
            logger.debug("error during opening connection: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }
}
