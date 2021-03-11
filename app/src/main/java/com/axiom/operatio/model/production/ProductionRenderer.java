package com.axiom.operatio.model.production;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Particles;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.production.block.BlockRenderer;


public class ProductionRenderer {

    private Production production;
    private Sprite tile, tileBlocked, outsideTile, selection;

    private Particles particles;

    private float cellWidth;                  // Ширина клетки
    private float cellHeight;                 // Высота клетки

    private Block movingBlock;
    private float cursorX, cursorY;


    public ProductionRenderer(Production production, float cellWidth, float cellHeight) {
        tile = new Sprite(SceneManager.getResources(), R.drawable.blocks, 8, 11);
        tile.setActiveFrame(68);
        tile.zOrder = 0;
        tileBlocked = new Sprite(SceneManager.getResources(), R.drawable.blocks, 8, 11);
        tileBlocked.setActiveFrame(70);
        tileBlocked.zOrder = 0;
        outsideTile = new Sprite(SceneManager.getResources(), R.drawable.blocks, 8, 11);
        outsideTile.setActiveFrame(87);
        outsideTile.zOrder = 0;
        selection = new Sprite(SceneManager.getResources(), R.drawable.blocks, 8, 11);
        selection.setActiveFrame(67);
        selection.zOrder = 500;

        Sprite particleSprite = new Sprite(SceneManager.getResources(), R.drawable.blocks, 8, 11);
        particleSprite.setActiveFrame(86);
        particles = new Particles(particleSprite,16, 1000, 100);
        particles.zOrder = 8;

        this.production = production;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
    }


    public void draw(Camera camera) {
        Block block;
        int columns = production.getColumns();
        int rows = production.getRows();
        int minCol = (int) (camera.getMinX() / cellWidth) - 1;
        int minRow = (int) (camera.getMinY() / cellHeight) - 1;
        int maxCol = minCol + (int) (Camera.WIDTH / cellWidth) + 2;
        int maxRow = minRow + (int) (Camera.HEIGHT / cellHeight) + 2;
        int selectedRow = production.getSelectedRow();
        int selectedCol = production.getSelectedCol();

        GraphicsRender.clear();

        for (int row = minRow; row <= maxRow; row++) {
            for (int col = minCol; col <= maxCol; col++) {
                // Отрисовываем плитку
                drawTile(camera, col, row, columns, rows);
                // Отрисовываем блок
                block = production.getBlockAt(col, row);
                if (block!=null) drawBlock(camera, block, col, row);
                // Отрисовываем выделение и частицы
                if (production.isBlockSelected() && row==selectedRow && col==selectedCol) {
                    drawSelection(camera, col, row);
                    drawParticles(camera, col, row);
                }
            }
        }

        // Отрисовываем передвигаемый блок поверх остальных
        if (movingBlock!=null) drawMovingBlock();
    }


    private void drawTile(Camera camera, int col, int row, int columns, int rows) {
        if (col < 0 || col >= columns || row < 0 || row >= rows) {
            outsideTile.draw(camera, col * cellWidth, row * cellHeight, cellWidth , cellHeight );
        } else if (!production.isUnlocked(col, row)) {
            tileBlocked.draw(camera, col * cellWidth, row * cellHeight, cellWidth , cellHeight );
        } else {
            tile.draw(camera, col * cellWidth, row * cellHeight, cellWidth, cellHeight );
        }
    }


    private void drawBlock(Camera camera, Block block, int col, int row) {
        BlockRenderer renderer = block.getRenderer();
        if (renderer != null) {
            renderer.draw(camera,col * cellWidth, row * cellHeight, cellWidth, cellHeight);
        }
    }


    private void drawMovingBlock() {
        BlockRenderer blockRenderer = movingBlock.getRenderer();
        blockRenderer.draw(Camera.getInstance(),
                cursorX - cellWidth / 2, cursorY - cellHeight / 2,
                cellWidth, cellHeight);
    }


    private void drawSelection(Camera camera, int col, int row) {
        Block underlyingBlock = production.getBlockAt(col,row);

        if (underlyingBlock==null || movingBlock==null) {
            selection.setActiveFrame(67);
        } else {
            selection.setActiveFrame(69);
        }

        float fluctuation = (float) (Math.cos(System.currentTimeMillis() / 100.0d) + 1f) / 20f;
        selection.draw(camera,
                col * cellWidth - (cellWidth * fluctuation),
                row * cellHeight - (cellHeight * fluctuation),
                cellWidth * (1.0f + fluctuation * 2),
                cellHeight * (1.0f + fluctuation * 2));
    }


    private void drawParticles(Camera camera, int col, int row) {
        particles.draw(camera,
                col * cellWidth + cellWidth * 0.5f,
                row * cellHeight + cellHeight * 0.5f,
                cellWidth * 0.25f);
    }


    public int getProductionColumn(float worldX) {
        int column = (int) (worldX / cellWidth);
        if (column >= production.getColumns()) column = -1;
        return column;
    }


    public int getProductionRow(float worldY) {
        int row = (int) (worldY / cellHeight);
        if (row >= production.getRows()) row = -1;
        return row;
    }


    public void doScale(float scaleFactor) {
        float newCellWidth = cellWidth * scaleFactor;
        float newCellHeight = cellHeight * scaleFactor;
        if (newCellWidth<128 || newCellHeight<128) { newCellWidth = 128; newCellHeight = 128; }
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


    public Particles getParticles() {
        return particles;
    }


    public void setParticles(Particles particles) {
        this.particles = particles;
    }

}
