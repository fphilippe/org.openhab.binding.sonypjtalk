/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sonypjtalk.internal;

import static org.openhab.binding.sonypjtalk.SonyPJTalkBindingConstants.*;

import java.util.Collections;
import java.util.Set;

import org.openhab.binding.sonypjtalk.handler.SonyPJTalkHandler;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link SonyPJTalkHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Fabien Philippe - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true)
public class SonyPJTalkHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_PROJECTOR);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_PROJECTOR)) {
            return new SonyPJTalkHandler(thing);
        }

        return null;
    }
}
