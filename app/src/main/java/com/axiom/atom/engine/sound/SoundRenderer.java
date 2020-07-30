package com.axiom.atom.engine.sound;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

import com.axiom.atom.engine.core.GameLoop;
import com.axiom.atom.engine.core.GameView;

import java.util.HashMap;
import java.util.Map;

/**
 * Инкапсулирует работу со звуковыми эффектами и музыкой
 */
public class SoundRenderer {

    protected static HashMap<Integer, Integer> loadedSounds;

    public static final int MAX_STREAMS = 4;
    protected static boolean initialized = false;
    protected static GameView gameView;
    protected static SoundPool soundEffects;




    public static void initialize(GameView view) {
        gameView = view;
        createSoundPool();
        loadedSounds = new HashMap<>();
        initialized = true;
    }

    public static void dispose() {
        soundEffects.release();
        soundEffects = null;
        initialized = false;
    }


    //-----------------------------------------------------------------------------------------
    // Инициализация SoundPool
    //-----------------------------------------------------------------------------------------
    protected static void createSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            createNewSoundPool();
        } else {
            createOldSoundPool();
        }
    }

    /**
     * Инициализация SoundPool от версии API 21 (LOLLIPOP)
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void createNewSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundEffects = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();
    }

    /**
     * Инициализация SoundPool для верси ниже 21
     */
    @SuppressWarnings("deprecation")
    private static void createOldSoundPool(){
        soundEffects = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC,0);
    }

    //-------------------------------------------------------------------------------------

    /**
     * Загружает в оперативную память короткий звуковой эффект
     * @param resource звуковой эффект
     * @return ID звука
     */
    public static int loadSound(int resource) {
        if (!initialized) return -1;
        Integer soundID = loadedSounds.get(resource);
        if (soundID!=null) return soundID;
        soundID = soundEffects.load(gameView.getContext(), resource, 1);
        loadedSounds.put(resource, soundID);
        return soundID;
    }

    public static void play(int soundID) {
        if (!initialized) return;
        soundEffects.play(soundID,1,1,0,0,1);
    }

    public static void unloadAll() {
        for (Map.Entry<Integer,Integer> entry:loadedSounds.entrySet()) {
            soundEffects.unload(entry.getValue());
        }
        loadedSounds.clear();
    }


    public static void unload(int soundID) {
        if (!initialized) return;
        int key = -1;
        for (Map.Entry<Integer,Integer> entry:loadedSounds.entrySet()) {
            if (entry.getValue()==soundID) {
                key = entry.getKey();
                break;
            }
        }
        if (key!=-1) {
            loadedSounds.remove(key);
            soundEffects.unload(soundID);
        }
    }

}
