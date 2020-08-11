package com.axiom.atom.engine.data;

import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 * Загружает файл CSV в память и предоставляет простой доступ к данным
 * <br><br>
 * (С) Atom Engine, Bolat Basheyev 2020
 */
public class CSVFile {

    protected ArrayList<String[]> csv;

    public CSVFile(Resources resources, int resID) {
        csv = new ArrayList<>(100);
        load(resources, resID);
    }

    protected void load(Resources resources, int resID) {
        BufferedReader input = null;
        char startSymbol;
        String line;
        String[] data;
        try {
            input = new BufferedReader(new InputStreamReader(resources.openRawResource(resID)));
            while ((line = input.readLine()) != null) {
                startSymbol = line.charAt(0);
                if (startSymbol=='/' || startSymbol=='!') continue; // Пропускаем комментарии
                data = line.split(",");
                if (data.length==0) continue;
                csv.add(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getValue(int row, int col) {
        if (row < 0 || row >= csv.size()) return null;
        String[] data = csv.get(row);
        if (col < 0 || col >= data.length) return null;
        return data[col];
    }

    public int getRowCount() {
        return csv.size();
    }

    public int getColCount(int row) {
        if (row < 0 || row >= csv.size()) return 0;
        return csv.get(row).length;
    }

}
