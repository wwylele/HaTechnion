package com.wwylele.hatechnion;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;


public class DeviceInfo {
    static public String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    static public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model.toUpperCase();
        } else {
            return (manufacturer + " " + model).toUpperCase();
        }
    }


}
