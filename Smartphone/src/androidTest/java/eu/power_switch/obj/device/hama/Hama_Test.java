/*
 *     PowerSwitch by Max Rosin & Markus Ressel
 *     Copyright (C) 2015  Markus Ressel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.obj.device.hama;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

import eu.power_switch.R;
import eu.power_switch.obj.ReceiverTest;
import eu.power_switch.obj.receiver.device.hama.Hama;

/**
 * Created by Markus on 08.08.2015.
 */
public class Hama_Test extends ReceiverTest {

    private static Hama receiver;

    @Test
    public void testCodeGeneration0() throws Exception {
        long seed = 0;
        receiver = new Hama(getContext(), (long) 0, "Name", seed, (long) 0);

        Method method = receiver.getClass().getDeclaredMethod("getSignal", argClassesGetSignal);
        method.setAccessible(true);

        Object[] argObjects = new Object[]{connAir, getContext().getString(R.string.on)};
        String generatedMessage = (String) method.invoke(receiver, argObjects);

        // ON
        String expectedMessage = "";
        Assert.assertEquals(expectedMessage, generatedMessage);

        argObjects = new Object[]{connAir, getContext().getString(R.string.off)};
        generatedMessage = (String) method.invoke(receiver, argObjects);

        // OFF
        expectedMessage = "";
        Assert.assertEquals(expectedMessage, generatedMessage);
    }

    @Test
    public void testCodeGeneration100() throws Exception {
        long seed = 100;
        receiver = new Hama(getContext(), (long) 0, "Name", seed, (long) 0);

        String methodName = "getSignal";
        Method method = receiver.getClass().getDeclaredMethod(methodName, argClassesGetSignal);
        method.setAccessible(true);
        Object[] argObjects = new Object[]{connAir, getContext().getString(R.string.on)};
        String generatedMessage = (String) method.invoke(receiver, argObjects);

        // ON
        String expectedMessage = "";
        Assert.assertEquals(expectedMessage, generatedMessage);

        argObjects = new Object[]{connAir, getContext().getString(R.string.off)};
        generatedMessage = (String) method.invoke(receiver, argObjects);

        // OFF
        expectedMessage = "";
        Assert.assertEquals(expectedMessage, generatedMessage);
    }

}
