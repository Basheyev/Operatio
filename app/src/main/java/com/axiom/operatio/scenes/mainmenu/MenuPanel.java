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
    public static final int AUTO_SAVE_SLOT = 0;

    protected MainMenuScene mainMenuScene;
    protected ProductionScene productionScene = null;
    protected GameSaveLoad gameSaveLoad;
    protected int tickSound;
    protected Button[] buttons;

    protected ClickListener listener = new ClickListener() {
        @Override
        public void onClick(Widget w) {
            int choice = Integer.parseInt(w.getTag());
            SoundRenderer.playSound(tickSound);
            switch (choice) {
                case 0: continueGame(); break;
                case 1: newGame(); break;
                case 2: loadGame(); break;
                case 3: saveGame(); break;
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

        buttons = new Button[5];
        for (int i = 0; i < 5; i++) {
            if (i==0) caption = "Resume game"; else
            if (i==1) caption = "New game"; else
            if (i==2) caption = "Load game"; else
            if (i==3) caption = "Save game"; else caption = "Exit Game";
            button = new Button(caption);
            button.setTag("" + i);
            button.setLocalBounds(50, 610 - ( i * 140), 500, 100);
            button.setColor(Color.GRAY);
            button.setTextColor(1,1,1,1);
            button.setTextScale(1.8f);
            button.setClickListener(listener);
            this.addChild(button);
            buttons[i] = button;
        }

    }


    public void updateUI() {
        int index = 0;
        int row = 0;
        if (gameSaveLoad.getGamesCaptions()[0]==null) {
            buttons[0].visible = false;
            index = 1;
        } else buttons[0].visible = true;
        for (int i = index; i < 5; i++) {
            buttons[i].setLocalBounds(50, 610 - ( row * 140), 500, 100);
            row++;
        }
    }


    public void continueGame() {
        if (productionScene == null) {
            productionScene = gameSaveLoad.loadGame(AUTO_SAVE_SLOT);
            if (productionScene==null) productionScene = gameSaveLoad.newGame();
        } else {
            gameSaveLoad.continueGame();
        }
        SlotsPanel slotsPanel = mainMenuScene.getSlotsPanel();
        slotsPanel.visible = false;
    }


    public void newGame() {
        if (productionScene != null) {
            gameSaveLoad.saveGame(AUTO_SAVE_SLOT, productionScene);
        }
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
