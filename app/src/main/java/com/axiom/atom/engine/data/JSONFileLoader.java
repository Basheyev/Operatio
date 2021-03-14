package com.axiom.atom.engine.data;

import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public class JSONFileLoader {

    private String jsonFile;

    public JSONFileLoader(Resources resources, int resourceID) {
        InputStream inputStream = resources.openRawResource(resourceID);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
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
        jsonFile = writer.toString();
    }


    public String getJsonFile() {
        return jsonFile;
    }

}
