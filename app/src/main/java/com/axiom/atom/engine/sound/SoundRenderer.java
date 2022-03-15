package com.axiom.atom.engine.sound;

import android.annotation.TargetApi;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.provider.MediaStore;

import com.axiom.atom.engine.core.GameView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Инкапсулирует работу со звуковыми эффектами и музыкой
 */
public class SoundRenderer {

    protected static HashMap<Integer, Integer> loadedSounds;
    protected static ArrayList<MediaPlayer> music = null;

    public static final int MAX_STREAMS = 4;
    protected static boolean initialized = false;
    protected static GameView gameView;
    protected static SoundPool soundPool;
    protected static float soundLeftVolume = 1;
    protected static float soundRightVolume = 1;


    public static void initialize(GameView view) {

        gameView = view;
        createSoundPool();
        loadedSounds = new HashMap<>();
        music = new ArrayList<>();
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
    public static int loadMusic(int resourceID) {
        MediaPlayer mediaPlayer = MediaPlayer.create(gameView.getContext(), resourceID);
        if (mediaPlayer==null) return -1;
        music.add(mediaPlayer);
        return music.size() - 1;
    }

    public static boolean playMusic(int musicID, boolean looping) {
        MediaPlayer mediaPlayer = music.get(musicID);
        if (mediaPlayer==null) return false;
        mediaPlayer.start();
        mediaPlayer.setLooping(looping);
        return mediaPlayer.isPlaying();
    }

    public static void setMusicVolume(int musicID, float leftVolume, float rightVolume) {
        MediaPlayer mediaPlayer = music.get(musicID);
        if (mediaPlayer==null) return;
        mediaPlayer.setVolume(leftVolume, rightVolume);
    }

    public static boolean isMusicPlaying(int musicID) {
        MediaPlayer mediaPlayer = music.get(musicID);
        if (mediaPlayer==null) return false;
        return mediaPlayer.isPlaying();
    }

    public static void pauseMusic(int musicID) {
        MediaPlayer mediaPlayer = music.get(musicID);
        if (mediaPlayer==null) return;
        mediaPlayer.pause();
    }

    public static void stopMusic(int musicID) {
        MediaPlayer mediaPlayer = music.get(musicID);
        if (mediaPlayer==null) return;
        mediaPlayer.stop();
    }

    public static void resetMusic(int musicID) {
        MediaPlayer mediaPlayer = music.get(musicID);
        if (mediaPlayer==null) return;
        mediaPlayer.reset();
    }

    public static void unloadMusic(int musicID) {
        MediaPlayer mediaPlayer = music.get(musicID);
        if (mediaPlayer==null) return;
        mediaPlayer.release();
    }


    public static void setListener(int musicID, MediaPlayer.OnCompletionListener listener) {
        MediaPlayer mediaPlayer = music.get(musicID);
        if (mediaPlayer==null) return;
        mediaPlayer.setOnCompletionListener(listener);
    }


}
