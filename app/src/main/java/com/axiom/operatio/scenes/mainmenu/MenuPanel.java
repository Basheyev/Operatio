package com.axiom.operatio.scenes.mainmenu;

import android.graphics.Color;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.gameplay.GameSaveLoad;
import com.axiom.operatio.scenes.production.ProductionScene;

import static com.axiom.operatio.scenes.mainmenu.SlotsPanel.MODE_LOAD_GAME;
import static com.axiom.operatio.scenes.mainmenu.SlotsPanel.MODE_SAVE_GAME;

public class MenuPanel extends Panel {

    public final int panelColor = 0xCC505050;
    protected MainMenuScene mainMenuScene;
    protected ProductionScene productionScene = null;
    protected GameSaveLoad gameSaveLoad;
    protected int tickSound;

    protected ClickListener listener = new ClickListener() {
        @Override
        public void onClick(Widget w) {
            int choice = Integer.parseInt(w.getTag());
            SoundRenderer.playSound(tickSound);
            switch (choice) {
                case 1: continueGame(); break;
                case 2: newGame(); break;
                case 3: loadGame(); break;
                case 4: saveGame(); break;
                default: exitGame();
            }
        }
    };


    public MenuPanel(MainMenuScene menuScene) {
        super();
        mainMenuScene = menuScene;
        gameSaveLoad = menuScene.getGameSaveLoad();
        setLocalBounds(50,50,600,800);
        setColor(panelColor);
        buildUI();
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);
    }


    private void buildUI() {
        Button button;
        String caption;

        for (int i =1; i<6; i++) {
            if (i==1) caption = "Continue"; else
            if (i==2) caption = "New Game"; else
            if (i==3) caption = "Load Game"; else
            if (i==4) caption = "Save Game"; else caption = "Exit Game";
            button = new Button(caption);
            button.setTag(""+i);
            button.setLocalBounds(50, 750 - ( i * 140), 500, 100);
            button.setColor(Color.GRAY);
            button.setTextColor(1,1,1,1);
            button.setTextScale(1.8f);
            button.setClickListener(listener);
            this.addChild(button);
        }

    }

    public void continueGame() {
        if (productionScene == null) {
            productionScene = gameSaveLoad.loadGame(0);
            if (productionScene==null) productionScene = gameSaveLoad.newGame();
        } else {
            gameSaveLoad.continueGame();
        }
        SlotsPanel slotsPanel = mainMenuScene.getSlotsPanel();
        slotsPanel.visible = false;
    }


    public void newGame() {
        productionScene = gameSaveLoad.newGame();
        SlotsPanel slotsPanel = mainMenuScene.getSlotsPanel();
        slotsPanel.visible = false;
    }


    public void loadGame() {
        SlotsPanel slotsPanel = mainMenuScene.getSlotsPanel();
        if (slotsPanel.getMode()==MODE_LOAD_GAME) {
            slotsPanel.visible = !slotsPanel.visible;
        } else {
            slotsPanel.setMode(MODE_LOAD_GAME);
            slotsPanel.visible = true;
        }
    }

    public void saveGame() {
        SlotsPanel slotsPanel = mainMenuScene.getSlotsPanel();
        if (slotsPanel.getMode()==MODE_SAVE_GAME) {
            slotsPanel.visible = !slotsPanel.visible;
        } else {
            slotsPanel.setMode(MODE_SAVE_GAME);
            slotsPanel.visible = true;
        }
    }

    public void exitGame() {
        if (productionScene != null) {
            gameSaveLoad.saveGame(0, productionScene);
        }
        SceneManager.exitGame();
    }

    public ProductionScene getProductionScene() {
        return productionScene;
    }

    public void setProductionScene(ProductionScene scene) {
        productionScene = scene;
    }

}
