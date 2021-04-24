package com.axiom.operatio.model.gameplay;

import android.util.Log;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameLoop;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.data.events.GameEvent;
import com.axiom.atom.engine.data.events.GameEventSubscriber;
import com.axiom.atom.engine.data.json.JSONFile;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.block.Block;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Менеджер миссий
 */
public class MissionManager implements GameEventSubscriber {

    private static MissionManager missionManager;

    private GameMission[] missions = null;
    private int levelCompletedSound = -1;


    public static MissionManager getInstance() {
        if (missionManager==null) missionManager = new MissionManager();
        return missionManager;
    }


    private MissionManager() {
        loadMissions();
        // Подписаться на все игровые события
        GameLoop.getInstance().addGameEventSubscriber(this);
    }


    private void loadMissions() {
        levelCompletedSound = SoundRenderer.loadSound(R.raw.yes_snd);
        JSONArray jsonMissions = JSONFile.loadArray(SceneManager.getResources(), R.raw.missions);
        missions = new GameMission[jsonMissions.length()];
        try {
            for (int i = 0; i < jsonMissions.length(); i++) {
                missions[i] = new GameMission(jsonMissions.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getMissionCount() {
        return missions.length;
    }

    public GameMission getMission(int index) {
        if (index < 0 || index >= missions.length) return null;
        return missions[index];
    }


    public void process(Production production) {
        int currentMissionID = production.getCurrentMissionID();
        GameMission mission = getMission(currentMissionID);
        if (mission==null) return;
        if (mission.checkWinConditions(production)) {
            SoundRenderer.playSound(levelCompletedSound);
            mission.earnReward(production);
            if (currentMissionID + 1 <= getMissionCount() - 1) {
                currentMissionID++;
                production.setCurrentMissionID(currentMissionID);
            }
        }
    }



    @Override // fixme проверка
    public boolean onEvent(GameEvent event) {
        Block block = (Block) event.getPayload();
        Log.i("GAME EVENT", block.getDescription());
        return false;
    }



}
