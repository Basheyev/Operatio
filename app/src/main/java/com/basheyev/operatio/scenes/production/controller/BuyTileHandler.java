package com.basheyev.operatio.scenes.production.controller;

import android.view.MotionEvent;

import com.basheyev.atom.R;
import com.basheyev.atom.engine.sound.SoundRenderer;
import com.basheyev.operatio.model.ledger.Ledger;
import com.basheyev.operatio.model.production.Production;
import com.basheyev.operatio.model.production.ProductionRenderer;

/**
 * Обработчик покупки площади производства
 */
public class BuyTileHandler {

    private InputHandler inputHandler;
    private Production production;
    private ProductionRenderer productionRenderer;

    private boolean actionInProgress = false;
    private int lastCol, lastRow;
    private int boughtSound;

    public BuyTileHandler(InputHandler inputHandler, Production production, ProductionRenderer productionRenderer) {
        this.inputHandler = inputHandler;
        this.production = production;
        this.productionRenderer = productionRenderer;
        boughtSound = SoundRenderer.loadSound(R.raw.cash_snd);
    }

    public void onMotion(MotionEvent event, float worldX, float worldY) {
        int column = productionRenderer.getProductionColumn(worldX);
        int row = productionRenderer.getProductionRow(worldY);
        boolean unlocked = production.isUnlocked(column, row);

        // Если плитка итак разблокирована - вызываем обработчик движения камеры
        if (unlocked) {
            inputHandler.getCameraMoveHandler().onMotion(event, worldX, worldY);
            return;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startAction(column, row);
                break;
            case MotionEvent.ACTION_UP:
                buyTile(column, row);
        }
    }


    private void startAction(int column, int row) {
        lastCol = column;
        lastRow = row;
        actionInProgress = true;
    }


    private void buyTile(int column, int row) {
        if (actionInProgress && column >= 0 && row >= 0 && lastCol==column && lastRow==row) {
            if (production.getLedger().creditCashBalance(Ledger.EXPENSE_TILE_BOUGHT, Production.TILE_PRICE)) {
                production.setAreaUnlocked(column, row, 1, 1, true);
                // todo fire event
                SoundRenderer.playSound(boughtSound);
            }
            actionInProgress = false;
        }
    }

    public void invalidateAction() {
        actionInProgress = false;
    }

}
