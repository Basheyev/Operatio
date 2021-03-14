package com.axiom.atom.engine.sound;

import android.annotation.TargetApi;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

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
    protected static SoundPool soundPool;
    protected static MediaPlayer music;
    protected static float soundLeftVolume = 1;
    protected static float soundRightVolume = 1;


    public static void initialize(GameView view) {
        gameView = view;
        createSoundPool();
        loadedSounds = new HashMap<>();
        initialized = true;
    }

    public static void release() {
        soundPool.release();
        soundPool = null;
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
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes).setMaxStreams(MAX_STREAMS)
                .build();
    }

    /**
     * Инициализация SoundPool для верси ниже 21
     */
    @SuppressWarnings("deprecation")
    private static void createOldSoundPool(){
        soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC,0);
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
        soundID = soundPool.load(gameView.getContext(), resource, 1);
        loadedSounds.put(resource, soundID);
        return soundID;
    }


    public static void setSoundVolume(float left, float right) {
        soundLeftVolume = left;
        soundRightVolume = right;
    }

    public static void playSound(int soundID) {
        if (!initialized) return;
        soundPool.play(soundID,soundLeftVolume,soundRightVolume,0,0,1);
    }



    public static void unloadSounds() {
        for (Map.Entry<Integer,Integer> entry:loadedSounds.entrySet()) {
            soundPool.unload(entry.getValue());
        }
        loadedSounds.clear();
    }


    public static void unloadSound(int soundID) {
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
            soundPool.unload(soundID);
        }
    }


    //-------------------------------------------------------------------------------------
    // Управление музыкой
    //-------------------------------------------------------------------------------------
    public static boolean loadMusic(int resourceID) {
        if (music!=null) unloadMusic();
        music = MediaPlayer.create(gameView.getContext(), resourceID);
        return music != null;
    }

    public static boolean playMusic() {
        if (music==null) return false;
        music.start();
        return music.isPlaying();
    }

    public static void setMusicVolume(float leftVolume, float rightVolume) {
        if (music==null) return;
        music.setVolume(leftVolume, rightVolume);
    }

    public static boolean isMusicPlaying() {
        if (music==null) return false;
        return music.isPlaying();
    }

    public static void pauseMusic() {
        if (music==null) return;
        music.pause();
    }

    public static void stopMusic() {
        if (music==null) return;
        music.stop();
    }

    public static void resetMusic() {
        if (music==null) return;
        music.reset();
    }

    public static void unloadMusic() {
        if (music==null) return;
        music.release();
        music = null;
    }


}
