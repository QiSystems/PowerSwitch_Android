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

package eu.power_switch.database.handler;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.database.table.apartment.ApartmentGatewayRelationTable;
import eu.power_switch.database.table.gateway.GatewayTable;
import eu.power_switch.obj.gateway.BrematicGWY433;
import eu.power_switch.obj.gateway.ConnAir;
import eu.power_switch.obj.gateway.EZControl_XS1;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.gateway.ITGW433;
import eu.power_switch.obj.gateway.RaspyRFM;
import eu.power_switch.shared.exception.gateway.GatewayAlreadyExistsException;
import eu.power_switch.shared.exception.gateway.GatewayUnknownException;

/**
 * Provides database methods for managing Gateways
 */
abstract class GatewayHandler {

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private GatewayHandler() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }

    /**
     * Adds Gateway information to Database
     *
     * @param gateway the new Gateway
     * @return ID of new Database entry
     * @throws GatewayAlreadyExistsException
     */
    protected static long add(Gateway gateway) throws Exception {
        for (Gateway existingGateway : getAll()) {
            if (existingGateway.hasSameLocalAddress(gateway)) {
                throw new GatewayAlreadyExistsException(existingGateway.getId());
            }
        }

        ContentValues values = new ContentValues();
        values.put(GatewayTable.COLUMN_ACTIVE, gateway.isActive());
        values.put(GatewayTable.COLUMN_NAME, gateway.getName());
        values.put(GatewayTable.COLUMN_MODEL, gateway.getModel());
        values.put(GatewayTable.COLUMN_FIRMWARE, gateway.getFirmware());
        values.put(GatewayTable.COLUMN_LAN_ADDRESS, gateway.getLocalHost());
        values.put(GatewayTable.COLUMN_LAN_PORT, gateway.getLocalPort());
        values.put(GatewayTable.COLUMN_WAN_ADDRESS, gateway.getWanHost());
        values.put(GatewayTable.COLUMN_WAN_PORT, gateway.getWanPort());

        long newId = DatabaseHandler.database.insert(GatewayTable.TABLE_NAME, null, values);
        return newId;
    }

    /**
     * Enables an existing Gateway
     *
     * @param id ID of Gateway
     */
    protected static void enable(Long id) throws Exception {
        ContentValues values = new ContentValues();
        values.put(GatewayTable.COLUMN_ACTIVE, true);
        DatabaseHandler.database.update(GatewayTable.TABLE_NAME, values, GatewayTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Disables an existing Gateway
     *
     * @param id ID of Gateway
     */
    protected static void disable(Long id) throws Exception {
        ContentValues values = new ContentValues();
        values.put(GatewayTable.COLUMN_ACTIVE, false);
        DatabaseHandler.database.update(GatewayTable.TABLE_NAME, values, GatewayTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Updates an existing Gateway
     *
     * @param id           ID of Gateway
     * @param name         new Name
     * @param model        new Model
     * @param localAddress new local Address (Host)
     * @param localPort    new local Port
     * @param wanAddress   new WAN Address (Host)
     * @param wanPort      new WAN Port
     */
    protected static void update(Long id, String name, String model, String localAddress, Integer localPort, String wanAddress, Integer wanPort) throws Exception {
        ContentValues values = new ContentValues();
        values.put(GatewayTable.COLUMN_NAME, name);
        values.put(GatewayTable.COLUMN_MODEL, model);
        values.put(GatewayTable.COLUMN_LAN_ADDRESS, localAddress);
        values.put(GatewayTable.COLUMN_LAN_PORT, localPort);
        values.put(GatewayTable.COLUMN_WAN_ADDRESS, wanAddress);
        values.put(GatewayTable.COLUMN_WAN_PORT, wanPort);
        DatabaseHandler.database.update(GatewayTable.TABLE_NAME, values, GatewayTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Deletes Gateway information from Database
     *
     * @param id ID of Gateway
     */
    protected static void delete(Long id) throws Exception {
        // delete from associations with apartments
        DatabaseHandler.database.delete(ApartmentGatewayRelationTable.TABLE_NAME, ApartmentGatewayRelationTable
                .COLUMN_GATEWAY_ID + "=" + id, null);

        DatabaseHandler.database.delete(GatewayTable.TABLE_NAME, GatewayTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Gets Gateway from Database
     *
     * @param id ID of Gateway
     * @return Gateway
     */
    protected static Gateway get(Long id) throws Exception {
        Gateway gateway = null;
        Cursor cursor = DatabaseHandler.database.query(GatewayTable.TABLE_NAME, GatewayTable.ALL_COLUMNS, GatewayTable.COLUMN_ID + "=" + id, null, null,
                null, null);

        if (cursor.moveToFirst()) {
            gateway = dbToGateway(cursor);
        }

        cursor.close();
        return gateway;
    }

    /**
     * Gets all Gateways from Database
     *
     * @return List of Gateways
     */
    protected static List<Gateway> getAll() throws Exception {
        List<Gateway> gateways = new ArrayList<>();
        Cursor cursor = DatabaseHandler.database.query(GatewayTable.TABLE_NAME, GatewayTable.ALL_COLUMNS, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            gateways.add(dbToGateway(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return gateways;
    }

    /**
     * Gets all Gateways from Database
     *
     * @param isActive true if Gateway is enabled
     * @return List of enabled/disabled Gateways
     */
    protected static List<Gateway> getAll(boolean isActive) throws Exception {
        List<Gateway> gateways = new ArrayList<>();
        int isActiveInt = isActive ? 1 : 0;
        Cursor cursor = DatabaseHandler.database.query(GatewayTable.TABLE_NAME, GatewayTable.ALL_COLUMNS, GatewayTable.COLUMN_ACTIVE + "=" + isActiveInt,
                null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            gateways.add(dbToGateway(cursor));
            cursor.moveToNext();
        }

        cursor.close();
        return gateways;
    }

    /**
     * Checks if a gateway is associated with at least one apartment
     *
     * @param id ID of Gateway
     * @return true if the gateway is associated with at least one apartment, false otherwise
     */
    public static boolean isAssociatedWithAnyApartment(Long id) throws Exception {
        Cursor cursor = DatabaseHandler.database.query(ApartmentGatewayRelationTable.TABLE_NAME,
                ApartmentGatewayRelationTable.ALL_COLUMNS, ApartmentGatewayRelationTable.COLUMN_GATEWAY_ID + "=" + id, null, null, null, null);

        boolean hasElement = cursor.moveToFirst();
        cursor.close();

        return hasElement;
    }

    /**
     * Creates a Gateway Object out of Database information
     *
     * @param c cursor pointing to a gateway database entry
     * @return Gateway, can be null
     */
    private static Gateway dbToGateway(@NonNull Cursor c) throws Exception {
        Gateway gateway;
        Long id = c.getLong(0);
        boolean active = c.getInt(1) > 0;
        String name = c.getString(2);
        String rawModel = c.getString(3);
        String firmware = c.getString(4);
        String localAddress = c.getString(5);
        int localPort = c.getInt(6);
        String wanAddress = c.getString(7);
        int wanPort = c.getInt(8);

        switch (rawModel) {
            case BrematicGWY433.MODEL:
                gateway = new BrematicGWY433(id, active, name, firmware, localAddress, localPort, wanAddress, wanPort);
                break;
            case ConnAir.MODEL:
                gateway = new ConnAir(id, active, name, firmware, localAddress, localPort, wanAddress, wanPort);
                break;
            case EZControl_XS1.MODEL:
                gateway = new EZControl_XS1(id, active, name, firmware, localAddress, localPort, wanAddress, wanPort);
                break;
            case ITGW433.MODEL:
                gateway = new ITGW433(id, active, name, firmware, localAddress, localPort, wanAddress, wanPort);
                break;
            case RaspyRFM.MODEL:
                gateway = new RaspyRFM(id, active, name, firmware, localAddress, localPort, wanAddress, wanPort);
                break;
            default:
                throw new GatewayUnknownException(rawModel);
        }

        return gateway;
    }
}
