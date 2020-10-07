package com.axiom.operatio.model;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.model.block.Block;
import com.axiom.operatio.model.block.BlockRenderer;

// TODO Оптимизировать рендер (не рисовать того, что заранее не видно
public class ProductionRenderer extends BlockRenderer {

    protected Production production;
    protected Sprite tile, selection;
    private float cellWidth;                  // Ширина клетки
    private float cellHeight;                 // Высота клетки

    private Block movingBlock;
    private float cursorX, cursorY;


    public ProductionRenderer(Production production, float cellWidth, float cellHeight) {
        tile = new Sprite(SceneManager.getResources(), R.drawable.tile);
        tile.zOrder = 0;
        selection = new Sprite(SceneManager.getResources(), R.drawable.selected);
        selection.zOrder = 500;
        this.production = production;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
    }

    @Override
    public void draw(Camera camera, float x, float y, float width, float height) {
        int columns = production.getColumns();
        int rows = production.getRows();
        Block block;
        BlockRenderer renderer;

        for (int row=0; row < rows; row++) {
            for (int col=0; col < columns; col++) {
                tile.draw(camera,col * cellWidth,row * cellHeight, cellWidth, cellHeight);
                block = production.getBlockAt(col, row);
                if (block!=null) {
                    renderer = block.getRenderer();
                    if (renderer != null) {
                        renderer.draw(camera,
                                col * cellWidth,
                                row * cellHeight,
                                cellWidth,
                                cellHeight);
                    }
                }
                if (production.isBlockSelected()) {
                    if (row==production.getSelectedRow() && col==production.getSelectedCol()) {
                        drawSelection(camera, col, row);
                    }
                }
            }
        }

        // Отрисовываем передвигаемый блок
        if (movingBlock!=null) {
            BlockRenderer blockRenderer = movingBlock.getRenderer();
            blockRenderer.draw(Camera.getInstance(),
                    cursorX - cellWidth / 2, cursorY - cellHeight / 2,
                    cellWidth, cellHeight);
        }
    }


    public int getProductionColumn(float worldX) {
        int column = (int) (worldX / cellWidth);
        if (column >= production.columns) column = -1;
        return column;
    }

    public int getProductionRow(float worldY) {
        int row = (int) (worldY / cellHeight);
        if (row >= production.columns) row = -1;
        return row;
    }


    private void drawSelection(Camera camera, int col, int row) {
        float fluctuation = (float) (Math.cos(System.currentTimeMillis() / 100.0d) + 1f) / 20f;
        selection.draw(camera,
                col * cellWidth - (cellWidth * fluctuation),
                row * cellHeight - (cellHeight * fluctuation),
                cellWidth * (1.0f + fluctuation * 2),
                cellHeight * (1.0f + fluctuation * 2));
    }


    public void doScale(float scaleFactor) {
        float newCellWidth = cellWidth * scaleFactor;
        float newCellHeight = cellHeight * scaleFactor;
        if (newCellWidth<32 || newCellHeight<32) { newCellWidth = 32; newCellHeight = 32; }
        if (newCellWidth>512 || newCellHeight>512) { newCellWidth = 512; newCellHeight = 512; }

        Camera camera = Camera.getInstance();
        float cx = camera.getX() * (newCellWidth / cellWidth);
        float cy = camera.getY() * (newCellWidth / cellHeight);
        camera.lookAt(cx, cy);

        cellWidth = newCellWidth;
        cellHeight = newCellHeight;

    }

    public float getCellWidth() {
        return cellWidth;
    }

    public float getCellHeight() {
        return cellHeight;
    }

    public void startBlockMoving(Block block, float cursorX, float cursorY) {
        movingBlock = block;
        this.cursorX = cursorX;
        this.cursorY = cursorY;
    }

    public void stopBlockMoving() {
        movingBlock = null;
    }

}
