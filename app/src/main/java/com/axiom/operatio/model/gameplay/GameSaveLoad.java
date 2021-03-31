package com.axiom.operatio.model.gameplay;

import android.content.Context;
import android.content.SharedPreferences;

import com.axiom.atom.engine.core.SceneManager;
import com.axiom.operatio.MainActivity;
import com.axiom.operatio.model.common.FormatUtils;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.scenes.production.ProductionScene;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Сохранение и загрузка игры
 */
public class GameSaveLoad {

    private static final String KEY_PREFIX = "SLOT";
    private static final String KEY_CAPTIONS = "SLOT CAPTIONS";
    public static final int MAX_SLOTS = 5;
    private String[] gamesCaptions;

    public GameSaveLoad() {
        loadGameCaptions();
    }

    public synchronized void continueGame() {
        SceneManager sceneManager = SceneManager.getInstance();
        sceneManager.setActiveScene(ProductionScene.SCENE_NAME);
    }


    public synchronized ProductionScene newGame() {
        ProductionScene scene = new ProductionScene();
        startScene(scene);
        return scene;
    }


    public String[] getGamesCaptions() {
        return gamesCaptions;
    }

    public synchronized ProductionScene loadGame(int slot) {
        if (slot < 0 || slot >= MAX_SLOTS) return null;
        String slotName = KEY_PREFIX + slot;
        SharedPreferences sharedPref = getPreferences();
        String loadedGame = sharedPref.getString(slotName, null);
        if (loadedGame==null) return null;
        try {
            ProductionScene scene = new ProductionScene(new JSONObject(loadedGame));
            startScene(scene);
            return scene;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    public synchronized boolean saveGame(int slot, ProductionScene productionScene) {
        if (slot < 0 || slot >= MAX_SLOTS || productionScene==null) return false;
        String slotName = KEY_PREFIX + slot;
        SharedPreferences sharedPref = getPreferences();
        SharedPreferences.Editor editor = sharedPref.edit();
        Production production = productionScene.getProduction();
        if (!production.isPaused()) production.setPaused(true);
        String savedGame = production.toJSON().toString();
        if (sharedPref.contains(slotName)) editor.remove(slotName);
        editor.putString(slotName, savedGame);
        boolean success = editor.commit();
        if (success) {
            gamesCaptions[slot] = FormatUtils.formatDateAndTime();
            saveGameCaptions();
            return true;
        }
        return false;
    }


    private void startScene(ProductionScene scene) {
        SceneManager sceneManager = SceneManager.getInstance();
        sceneManager.removeGameScene(ProductionScene.SCENE_NAME);
        sceneManager.addGameScene(scene);
        sceneManager.setActiveScene(ProductionScene.SCENE_NAME);
    }


    private SharedPreferences getPreferences() {
        MainActivity mainActivity = MainActivity.getActivity();
        return mainActivity.getPreferences(Context.MODE_PRIVATE);
    }


    private boolean saveGameCaptions() {
        JSONArray jsonCaptions = new JSONArray();
        for (int i=0; i<MAX_SLOTS; i++) {
            jsonCaptions.put(gamesCaptions[i]);
        }
        String value = jsonCaptions.toString();
        SharedPreferences preferences = getPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        if (preferences.contains(KEY_CAPTIONS)) editor.remove(KEY_CAPTIONS);
        editor.putString(KEY_CAPTIONS, value);
        return editor.commit();
    }


    private void loadGameCaptions() {
        gamesCaptions = new String[MAX_SLOTS];
        SharedPreferences preferences = getPreferences();
        if (preferences.contains(KEY_CAPTIONS)) {
            try {
                String value = preferences.getString(KEY_CAPTIONS, null);
                if (value != null) {
                    JSONArray jsonCaptions = new JSONArray(value);
                    for (int i = 0; i < MAX_SLOTS; i++) {
                        String slotName = jsonCaptions.getString(i);
                        String key = KEY_PREFIX + i;
                        if (preferences.contains(key)) {
                            gamesCaptions[i] = slotName;
                        } else gamesCaptions[i] = null;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }

        // Если названий слотов нет, просто создаем названия как номера слотов
        for (int i = 0; i < MAX_SLOTS; i++) {
            String key = KEY_PREFIX + i;
            if (preferences.contains(key)) {
                gamesCaptions[i] = key;
            } else gamesCaptions[i] = null;
        }
    }

}
