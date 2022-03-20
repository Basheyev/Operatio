package com.axiom.atom.engine.sound;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.SoundPool;
import android.os.Build;

import com.axiom.atom.engine.core.GameView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Инкапсулирует работу со звуковыми эффектами и музыкой
 */
public class SoundRenderer {

    private static final int MAX_STREAMS = 4;
    private static final int NO_TRACK    = -1;

    private static GameView gameView;                        // Компонент рендера
    private static AudioManager audioManager;                // Аудио менеджер
    private static SoundPool soundPool;                      // Пул звуков
    private static MediaPlayer mediaPlayer;                  // Проигрыватель музыки
    private static HashMap<Integer, Integer> loadedSounds;   // Список загруженных звуков
    private static ArrayList<Integer> playlist = null;       // Список ресурсов музыки
    private static MusicCompleteListener completeListener;   // Обработчик при завершении трека
    private static int currentTrack;                         // Текущий трэк
    private static boolean trackShuffling = false;           // Флаг перемешивания проигрывания
    private static boolean initialized = false;              // Флаг инициализации

    private static float soundLeftVolume = 1;                // Громкость левого канала
    private static float soundRightVolume = 1;               // Громкость правого канала

    //-----------------------------------------------------------------------------------------
    // Инициализации и финализация рендера
    //-----------------------------------------------------------------------------------------
    /**
     * Инициализация звукового ренедра
     * @param view компонент графического рендера
     */
    public static void initialize(GameView view) {
        gameView = view;
        audioManager = (AudioManager) gameView.getContext().getSystemService(Context.AUDIO_SERVICE);
        // инициализация звуков
        loadedSounds = new HashMap<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            createNewSoundPool();
        } else {
            createOldSoundPool();
        }
        // Инициализация музыки
        playlist = new ArrayList<>();
        completeListener = new MusicCompleteListener();
        currentTrack = NO_TRACK;

        initialized = true;
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
     * Инициализация SoundPool для версии ниже 21
     */
    private static void createOldSoundPool(){
        soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC,0);
    }


    /**
     * Финализация звукового рендера
     */
    public static void release() {
        unloadSounds();
        soundPool.release();
        soundPool = null;
        stopTrack();
        playlist.clear();
        initialized = false;
    }


    //-----------------------------------------------------------------------------------------
    // Управление общей громкостью
    //-----------------------------------------------------------------------------------------

    /**
     * Получить уровень громкости
     * @return уровень громкости (0.0-1.0)
     */
    public static float getVolume() {
        if (!initialized) return 0.0f;
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    /**
     * Установить громкость
     * @param level 0.0-1.0;
     */
    public static void setVolume(float level) {
        if (level < 0 || level > 1) return;
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int seventyVolume = (int) (maxVolume * level);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, seventyVolume, 0);
    }


    //-----------------------------------------------------------------------------------------
    // Инициализация звуками
    //-----------------------------------------------------------------------------------------


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

    /**
     * Устанавливает уровень громкости для следующего проигрываемого звука
     * @param left левый канал
     * @param right правый канал
     */
    public static void setSoundVolume(float left, float right) {
        soundLeftVolume = left;
        soundRightVolume = right;
    }


    /**
     * Проиграть звук
     * @param soundID ID загруженного звука
     */
    public static void playSound(int soundID) {
        if (!initialized) return;
        soundPool.play(soundID,soundLeftVolume,soundRightVolume,0,0,1);
    }


    /**
     * Выгружает из памяти все загруженные звуки
     */
    public static void unloadSounds() {
        for (Map.Entry<Integer,Integer> entry:loadedSounds.entrySet()) {
            soundPool.unload(entry.getValue());
        }
        loadedSounds.clear();
    }


    /**
     * Выгружает из пемяти звук
     * @param soundID ID звука
     */
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
    // Управление музыкой и плейлистом
    //-------------------------------------------------------------------------------------

    /**
     * Возвращает количество треков в плейлисте
     * @return количество треков в плейлисте
     */
    public static int getTracksCount() {
        return playlist.size();
    }


    /**
     * Выставить режим проигрывания: последовательно или перемешивать треки
     * @param shuffling true - перемешивать, false - последовательно
     */
    public static void setTrackShuffling(boolean shuffling) {
        trackShuffling = shuffling;
    }


    /**
     * Возвращает режим проигрывания: последовательно или перемешивать треки
     * @return true - перемешивать, false - последовательно
     */
    public static boolean getTrackShuffling() {
        return trackShuffling;
    }


    /**
     * Добавляет трек в список проигрывателя
     * @param resourceID ресурс с музыкой
     * @return ID трека
     */
    public static int addTrack(int resourceID) {
        if (playlist.contains(resourceID)) return playlist.indexOf(resourceID);
        playlist.add(resourceID);
        return playlist.size() - 1;
    }


    /**
     * Удаляет трек из списка проигрывателя
     * @param trackID номер трека
     * @return true если удален и false если не найден
     */
    public static boolean removeTrack(int trackID) {
        if (trackID < 0 || trackID >= playlist.size()) return false;
        return playlist.remove(trackID) != null;
    }


    /**
     * Начинает проигрывание указанного трека в плейлисте
     * @param trackID номер трека
     */
    public static void playTrack(int trackID) {
        if (isTrackPlaying()) stopTrack();
        int resourceID = playlist.get(trackID);
        mediaPlayer = MediaPlayer.create(gameView.getContext(), resourceID);
        if (mediaPlayer==null) return;
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(completeListener);
        currentTrack = trackID;
    }


    /**
     * Начинает проигрывание следующего трека в плейлисте
     */
    public static void playNextTrack() {
        if (playlist.size()==0) return;
        int random = trackShuffling ? (int) (1 + Math.random() * playlist.size()) : 1;
        int nextTrack = (currentTrack + random) % playlist.size();
        playTrack(nextTrack);
    }


    /**
     * Устанавливает режим повторения трека
     * *@param looping зацикливать ли проигрывание
     */
    public static void setTrackLooping(boolean looping) {
        if (mediaPlayer==null) return;
        mediaPlayer.setLooping(looping);
    }

    /**
     * Проигрывается ли сейчас трек
     * @return true - да, false - нет
     */
    public static boolean isTrackPlaying() {
        if (mediaPlayer==null) return false;
        return mediaPlayer.isPlaying();
    }


    /**
     * Возвращает номер текущего трека
     * @return номер проигрываемого трека или NO_TRACK если нет текущего трека
     */
    public static int getCurrentTrack() {
        return currentTrack;
    }


    /**
     * Возвращает длительность трека в миллисекундах
     * @return длительность трека в миллисекундах
     */
    public static int getTrackDuration() {
        if (mediaPlayer==null) return 0;
        return mediaPlayer.getDuration();
    }


    /**
     * Возвращает текущую позицию проигрываемого трека в миллисекундах
     * @return позиция в миллисекундах
     */
    public static int getTrackPosition() {
        if (mediaPlayer==null) return 0;
        return mediaPlayer.getCurrentPosition();
    }


    /**
     * Установить текущую позицию проигрываемого трека в миллисекундах
     * @param milliseconds требуемая позиция в треке в миллисекундах
     */
    public static void setTrackPosition(int milliseconds) {
        if (mediaPlayer==null || milliseconds < 0) return;
        if (milliseconds >= mediaPlayer.getDuration()) return;
        mediaPlayer.seekTo(milliseconds);
    }


    /**
     * Ставит проигрывание трека на паузу или продолжает проигрывание
     */
    public static void pauseTrack(boolean pause) {
        if (mediaPlayer==null) return;
        if (pause) mediaPlayer.pause(); else mediaPlayer.start();
    }


    /**
     * Останавливает проигрывание трека и высвобождает ресурсы проигрывателя
     */
    public static void stopTrack() {
        currentTrack = NO_TRACK;
        if (mediaPlayer==null) return;
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }


    /**
     * Устанавливает громкость проигрывателя
     * @param leftVolume левый канал
     * @param rightVolume правый канал
     */
    public static void setMusicVolume(float leftVolume, float rightVolume) {
        if (mediaPlayer==null) return;
        mediaPlayer.setVolume(leftVolume, rightVolume);
    }


    private static class MusicCompleteListener implements OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            if (mediaPlayer==null) return;
            if (!mediaPlayer.isLooping()) playNextTrack();
        }
    }


}
