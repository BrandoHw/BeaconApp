package org.altbeacon.network;

import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A Time stamp entry that notes the time and employee left a location, including the duration an
 * employee was at the location
 */
public class LocationTimeStamp {
    private static final String TAG = LocationTimeStamp.class.getSimpleName();

    public final String location;
    public final String timeStamp;
    public final String duration;

    public LocationTimeStamp(
            String location, String timeStamp, String duration) {
        this.location = location;
        this.timeStamp = timeStamp;
        this.duration = duration;
    }

}