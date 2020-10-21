package com.axiom.operatio.scenes.mainmenu;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.MainActivity;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.scenes.production.ProductionScene;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class MenuPanel extends Panel {

    public final int panelColor = 0xCC505050;
    protected ProductionScene productionScene = null;
    protected String toggledButton;
    protected int tickSound;

    protected ClickListener listener = new ClickListener() {
        @Override
        public void onClick(Widget w) {
            int choice = Integer.parseInt(w.getTag());
            SoundRenderer.playSound(tickSound);

            MainActivity mainActivity = MainActivity.getActivity();
            SharedPreferences sharedPref = mainActivity.getPreferences(Context.MODE_PRIVATE);
            SceneManager sm = SceneManager.getInstance();

            switch (choice) {
                case 1:
                    if (productionScene == null) {
                        String savedGame = sharedPref.getString("Game", null);
                        if (savedGame==null) {
                            productionScene = new ProductionScene();
                        } else {
                            try {
                                System.out.println("LOADING GAME:\n " + savedGame);
                                productionScene = new ProductionScene(new JSONObject(savedGame));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                break;
                            }
                        }
                        sm.addGameScene(productionScene);
                    }
                    sm.setActiveScene(productionScene.getSceneName());
                    break;
                case 2:
                    // Создать новую пустую сцену производства
                    if (productionScene!=null) {
                        sm.removeGameScene(productionScene.getSceneName());
                        productionScene = new ProductionScene();
                        sm.addGameScene(productionScene);
                    }
                    break;
                default:
                    // Добавить сохранение
                    if (productionScene!=null) {
                        Production production = productionScene.getProduction();
                        SharedPreferences.Editor editor = sharedPref.edit();
                        String savedGame = production.serialize().toString();
                        System.out.println("SAVING GAME:\n " + savedGame);
                        editor.remove("Game");
                        editor.putString("Game", savedGame);
                        editor.commit();
                    }
                    SceneManager.exitGame();
            }
        }
    };


    public MenuPanel() {
        super();
        setLocalBounds(Camera.WIDTH/2 - 300,Camera.HEIGHT/2 - 300,600,600);
        setColor(panelColor);
        buildButtons();
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);
    }


    private void buildButtons() {
        Button button;
        String caption;

        for (int i =1; i<4; i++) {
            if (i==1) caption = "Start Game"; else
            if (i==2) caption = "Reset Game"; else caption = "Exit Game";
            button = new Button(caption);
            button.setTag(""+i);
            button.setLocalBounds(50, 550 - ( i * 150), 500, 120);
            button.setColor(Color.GRAY);
            button.setTextColor(1,1,1,1);
            button.setClickListener(listener);
            this.addChild(button);
        }

    }

    public String getToggledButton() {
        return toggledButton;
    }

}
