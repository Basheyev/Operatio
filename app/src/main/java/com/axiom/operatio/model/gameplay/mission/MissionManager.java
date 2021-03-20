package com.axiom.operatio.model.gameplay.mission;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.data.JSONFile;

import org.json.JSONArray;
import org.json.JSONException;

public class MissionManager {

    private static Mission[] missions = null;


    public static Mission getMission(int index) {
        if (missions==null) loadMissions();
        if (index < 0 || index >= missions.length) return null;
        return missions[index];
    }


    public static int size() {
        if (missions==null) loadMissions();
        return missions.length;
    }


    private static void loadMissions() {
        JSONArray jsonMissions = JSONFile.loadArray(SceneManager.getResources(), R.raw.missions);
        missions = new Mission[jsonMissions.length()];
        try {
            for (int i = 0; i < jsonMissions.length(); i++) {
                missions[i] = new Mission(jsonMissions.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
