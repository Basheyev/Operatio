package com.basheyev.operatio.scenes.mainmenu;

import android.graphics.Typeface;

import com.basheyev.atom.R;
import com.basheyev.atom.engine.core.GameView;
import com.basheyev.atom.engine.core.SceneManager;
import com.basheyev.atom.engine.sound.SoundRenderer;
import com.basheyev.atom.engine.ui.listeners.ClickListener;
import com.basheyev.atom.engine.ui.widgets.Button;
import com.basheyev.atom.engine.ui.widgets.Panel;
import com.basheyev.atom.engine.ui.widgets.Widget;
import com.basheyev.operatio.model.gameplay.GameSaveLoad;
import com.basheyev.operatio.scenes.production.ProductionScene;

import static com.basheyev.operatio.scenes.mainmenu.SlotsPanel.MODE_LOAD_GAME;
import static com.basheyev.operatio.scenes.mainmenu.SlotsPanel.MODE_SAVE_GAME;

import androidx.core.content.res.ResourcesCompat;

/**
 * Кнопки главного меню
 */
public class MenuPanel extends Panel {

    public static final int BUTTON_COLOR   = 0xC0122063;
    public static final int PANEL_COLOR    = 0xD0405060;
    public static final int AUTO_SAVE_SLOT = 0;

    protected MainMenuScene mainMenuScene;
    protected ProductionScene productionScene = null;
    protected GameSaveLoad gameSaveLoad;
    protected int tickSound;
    protected Button[] buttons;
    protected Typeface menuFont;

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
        setColor(PANEL_COLOR);
        buildUI();
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);
    }


    private void buildUI() {
        Button button;
        String caption;

        menuFont = ResourcesCompat.getFont(GameView.getInstance().getContext(), R.font.game_robot);

        buttons = new Button[5];
        for (int i = 0; i < 5; i++) {
            if (i==0) caption = "Resume game"; else
            if (i==1) caption = "New game"; else
            if (i==2) caption = "Load game"; else
            if (i==3) caption = "Save game"; else caption = "Exit Game";
            button = new Button(caption);
            button.setTag("" + i);
            button.setLocalBounds(50, 610 - ( i * 140), 500, 100);
            button.setColor(BUTTON_COLOR);
            button.setTextColor(1,1,1,1);
            button.setTextScale(1.8f);
            button.setClickListener(listener);
            button.setTypeface(menuFont);
            this.addChild(button);
            buttons[i] = button;
        }

    }


    public void updateUI() {
        int index = 0;
        int row = 0;
        if (gameSaveLoad.getGamesCaptions()[0]==null) {
            buttons[0].setVisible(false);
            index = 1;
        } else buttons[0].setVisible(true);
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
        slotsPanel.setVisible(false);
    }


    public void newGame() {
        if (productionScene != null) {
            gameSaveLoad.saveGame(AUTO_SAVE_SLOT, productionScene);
        }
        productionScene = gameSaveLoad.newGame();
        SlotsPanel slotsPanel = mainMenuScene.getSlotsPanel();
        slotsPanel.setVisible(false);
    }


    public void loadGame() {
        SlotsPanel slotsPanel = mainMenuScene.getSlotsPanel();
        if (slotsPanel.getMode()==MODE_LOAD_GAME) {
            slotsPanel.setVisible(!slotsPanel.isVisible());
        } else {
            slotsPanel.setMode(MODE_LOAD_GAME);
            slotsPanel.setVisible(true);
        }
    }

    public void saveGame() {
        SlotsPanel slotsPanel = mainMenuScene.getSlotsPanel();
        if (slotsPanel.getMode()==MODE_SAVE_GAME) {
            slotsPanel.setVisible(!slotsPanel.isVisible());
        } else {
            slotsPanel.setMode(MODE_SAVE_GAME);
            slotsPanel.setVisible(true);;
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
