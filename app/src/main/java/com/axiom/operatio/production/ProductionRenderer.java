package com.axiom.operatio.production;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.production.block.Block;
import com.axiom.operatio.production.block.Renderer;

public class ProductionRenderer implements Renderer {

    protected Production production;
    protected Sprite tile;
    private float cellWidth;                  // Ширина клетки
    private float cellHeight;                 // Высота клетки

    public ProductionRenderer(Production production, float cellWidth, float cellHeight) {
        tile = new Sprite(SceneManager.getResources(), R.drawable.tile);
        tile.zOrder = 0;
        this.production = production;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
    }

    @Override
    public void draw(Camera camera, float x, float y, float width, float height) {
        int columns = production.getColumns();
        int rows = production.getRows();
        Block block;
        Renderer renderer;
        GraphicsRender.clear();
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
            }
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

}
