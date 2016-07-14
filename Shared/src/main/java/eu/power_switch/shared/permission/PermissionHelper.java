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

package eu.power_switch.shared.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;

import eu.power_switch.shared.R;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.constants.PermissionConstants;

/**
 * Helper class for Permission handling
 * <p/>
 * Created by Markus on 05.03.2016.
 */
public class PermissionHelper {

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private PermissionHelper() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }

    /**
     * Send local broadcast to inform listeners about changed permissions
     *
     * @param context      any suitable context
     * @param grantResults permission change results
     */
    public static void sendPermissionChangedBroadcast(Context context, int requestCode, String[] permissions, int[] grantResults) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_PERMISSION_CHANGED);
        intent.putExtra(PermissionConstants.KEY_REQUEST_CODE, requestCode);
        intent.putExtra(PermissionConstants.KEY_PERMISSIONS, permissions);
        intent.putExtra(PermissionConstants.KEY_RESULTS, grantResults);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Shows a "Missing permission" Dialog for the user to interact with and grant those permissions
     *
     * @param activity    activity used as context
     * @param permissions array of missing permissions
     */
    public static void showMissingPermissionDialog(final Activity activity, final String[] permissions) {
        if (permissions.length == 0) {
            throw new IllegalArgumentException("Missing permission constant(s)");
        }

        final int requestCode = getPermissionRequestCode(permissions);

        new AlertDialog.Builder(activity)
                .setTitle(R.string.missing_permission)
                .setMessage(getPermissionMessage(permissions))
                .setPositiveButton(R.string.grant, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(activity, permissions, requestCode);
                    }
                })
                .setNeutralButton(R.string.close, null)
                .show();
    }

    /**
     * Check if ExternalStorage write permission is available
     *
     * @param context any suitable context
     * @return true if write permission is available, false otherwise
     */
    public static boolean isWriteExternalStoragePermissionAvailable(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            // Marshmallow+
            int hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return hasPermission == PackageManager.PERMISSION_GRANTED;
        } else {
            // Pre-Marshmallow
            return true;
        }
    }

    /**
     * Check if Location permission is available
     *
     * @param context any suitable context
     * @return true if location permission is available, false otherwise
     */
    public static boolean isLocationPermissionAvailable(@NonNull Context context) {
        int hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        return hasPermission == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check if Phone permission is available
     *
     * @param context any suitable context
     * @return true if phone permission is available, false otherwise
     */
    public static boolean isPhonePermissionAvailable(@NonNull Context context) {
        int hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE);
        return hasPermission == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check if SMS permission is available
     *
     * @param context any suitable context
     * @return true if SMS permission is available, false otherwise
     */
    public static boolean isSmsPermissionAvailable(@NonNull Context context) {
        int hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS);
        return hasPermission == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check if Contact permission is available
     *
     * @param context any suitable context
     * @return true if contact permission is available, false otherwise
     */
    public static boolean isContactPermissionAvailable(@NonNull Context context) {
        int hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS);
        return hasPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static int getPermissionRequestCode(String[] permissions) {
        switch (permissions[0]) {
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return PermissionConstants.REQUEST_CODE_STORAGE_PERMISSION;
            case Manifest.permission.ACCESS_FINE_LOCATION:
                return PermissionConstants.REQUEST_CODE_LOCATION_PERMISSION;
            case Manifest.permission.READ_PHONE_STATE:
                return PermissionConstants.REQUEST_CODE_PHONE_PERMISSION;
            case Manifest.permission.RECEIVE_SMS:
                return PermissionConstants.REQUEST_CODE_SMS_PERMISSION;
            default:
                return -1;
        }
    }

    public static int getPermissionMessage(String[] permissions) {
        if (permissions.length > 1) {
            return R.string.missing_multiple_permissions;
        }

        switch (permissions[0]) {
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return R.string.missing_external_storage_permission;
            case Manifest.permission.ACCESS_FINE_LOCATION:
                return R.string.missing_location_permission;
            case Manifest.permission.READ_PHONE_STATE:
                return R.string.missing_phone_permission;
            case Manifest.permission.RECEIVE_SMS:
                return R.string.missing_sms_permission;
            default:
                return R.string.missing_permission;
        }
    }
}
