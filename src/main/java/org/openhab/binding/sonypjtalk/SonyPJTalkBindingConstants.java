/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sonypjtalk;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SonyPJTalkBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Fabien Philippe - Initial contribution
 */
public class SonyPJTalkBindingConstants {

    private static final String BINDING_ID = "sonypjtalk";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PROJECTOR = new ThingTypeUID(BINDING_ID, "projector");

    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_POWERSTATUS = "powerStatus";
    public static final String CHANNEL_MODELNAME = "modelName";
    public static final String CHANNEL_LAMPTIMER = "lampTimer";

}
