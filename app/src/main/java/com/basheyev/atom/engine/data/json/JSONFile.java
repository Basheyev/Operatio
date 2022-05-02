package com.basheyev.atom.engine.data.json;

import android.content.res.Resources;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public class JSONFile {

    private static final int BUFFER_SIZE = 16384;
    private static char[] buffer = new char[BUFFER_SIZE];

    private JSONFile() { }

    public static String loadFile(Resources resources, int resourceID) {
        InputStream inputStream = resources.openRawResource(resourceID);
        Writer writer = new StringWriter();
        try {
            Reader reader = new BufferedReader(new InputStreamReader(inputStream));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return writer.toString();
    }


    public static JSONArray loadArray(Resources resources, int resourceID) {
        JSONArray array = null;
        try {
            array = new JSONArray(loadFile(resources, resourceID));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return array;
    }


    public static JSONObject loadObject(Resources resources, int resourceID) {
        JSONObject obj = null;
        try {
            obj = new JSONObject(loadFile(resources, resourceID));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

}
