package com.axiom.atom.engine.sound;

import android.annotation.TargetApi;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

/**
 * Инкапсулирует работу со звуковыми эффектами и музыкой
 */
public class SoundRenderer {

    private static SoundRenderer renderer;

    protected SoundPool sounds;
    protected MediaPlayer music;


    private SoundRenderer() {
        createSoundPool();
    }


    public static SoundRenderer getInstance() {
        if (renderer == null) renderer = new SoundRenderer();
        return renderer;
    }


    //-----------------------------------------------------------------------------------------
    // Инициализация SoundPool
    //-----------------------------------------------------------------------------------------
    protected void createSoundPool() {
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
    private void createNewSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        sounds = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();
    }

    /**
     * Инициализация SoundPool для верси ниже 21
     */
    @SuppressWarnings("deprecation")
    private void createOldSoundPool(){
        sounds = new SoundPool(5, AudioManager.STREAM_MUSIC,0);
    }

    //-------------------------------------------------------------------------------------

}
