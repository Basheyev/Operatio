package com.axiom.operatio.model.production;

import android.content.res.Resources;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.gles2d.Texture;
import com.axiom.atom.engine.graphics.renderers.Particles;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.graphics.renderers.Text;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.production.block.BlockRenderer;


public class ProductionRenderer {

    public static int MIN_CELL_SIZE = 64;
    public static final int MAX_CELL_SIZE = 384;
    public static final int INITIAL_CELL_WIDTH = (MAX_CELL_SIZE + MIN_CELL_SIZE) / 2;
    public static final int INITIAL_CELL_HEIGHT = (MAX_CELL_SIZE + MIN_CELL_SIZE) / 2;

    private static Sprite tiles = null;

    private Production production;
    private Sprite tile, tileBlocked, outsideTile, selection;

    private Particles particles;

    private float cellWidth;                  // Ширина клетки
    private float cellHeight;                 // Высота клетки

    private Block movingBlock;
    private float cursorX, cursorY;


    protected ProductionRenderer(Production production) {
        if (tiles==null) {
            Resources resources = SceneManager.getResources();
            Texture texture = Texture.getInstance(resources, R.drawable.blocks, false);
            // Создаём спрайт с tilemap 8x16 нарезанным через центр текселей текстуры
            // чтобы избежать артефактов на границах при низком разрешении экрана
            tiles = new Sprite(texture, 8, 16, true);
        }
        tile = tiles.getAsSprite(68);
        tile.setZOrder(0);
        tileBlocked = tiles.getAsSprite(70);
        tileBlocked.setZOrder(0);
        outsideTile = tiles.getAsSprite(87);
        outsideTile.setZOrder(0);
        selection = tiles.getAsSprite(67);
        selection.setZOrder(500);

        Sprite particleSprite = tiles.getAsSprite(86);
        particles = new Particles(particleSprite,16, 1000, 100);
        particles.zOrder = 8;

        this.production = production;
        this.cellWidth = INITIAL_CELL_WIDTH;
        this.cellHeight = INITIAL_CELL_HEIGHT;
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

        int cellType;
        float x1 = minCol * cellWidth;
        float y1 = minRow * cellHeight;
        float x2 = x1 + cellWidth;
        float y2 = y1 + cellHeight;

        GraphicsRender.clear();
        for (int row = minRow; row <= maxRow; row++) {
            for (int col = minCol; col <= maxCol; col++) {

                if (col < 0 || col >= columns || row < 0 || row >= rows) cellType = 0;
                else if (!production.isUnlocked(col, row)) cellType = 1; else cellType = 2;

                // Отрисовываем плитку fixme костыль убрать артефакты путем добавления +-1px
                drawTile(camera, x1-1, y1-1, x2+1, y2+1, cellType);

                // Отрисовываем блок
                block = production.getBlockAt(col, row);
                if (block != null) drawBlock(camera, block, col, row);

                // Отрисовываем выделение и частицы
                if (production.isBlockSelected() && row==selectedRow && col==selectedCol) {
                    drawSelection(camera, col, row);
                    drawParticles(camera, col, row);
                }

                x1 = x2;
                x2 += cellWidth;
            }
            y1 = y2;
            y2 += cellHeight;
            x1 = minCol * cellWidth;
            x2 = x1 + cellWidth;
        }

        // Отрисовываем передвигаемый блок поверх остальных
        if (movingBlock!=null) drawMovingBlock();
    }


    private void drawTile(Camera camera, float x1, float y1, float x2, float y2, int type) {
        switch (type) {
            case 0: outsideTile.drawExact(camera, x1, y1, x2, y2); break;
            case 1: tileBlocked.drawExact(camera, x1, y1, x2, y2); break;
            case 2: tile.drawExact(camera, x1, y1, x2, y2);
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
        float min = MIN_CELL_SIZE;
        float max = MAX_CELL_SIZE;
        if (newCellWidth < min || newCellHeight < min) { newCellWidth = min; newCellHeight = min; }
        if (newCellWidth > max || newCellHeight > max) { newCellWidth = max; newCellHeight = max; }
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


    public void setCellSize(float width, float height) {
        if (width<MIN_CELL_SIZE || height<MIN_CELL_SIZE) { width = MIN_CELL_SIZE; height = MIN_CELL_SIZE; }
        if (width>MAX_CELL_SIZE || height>MAX_CELL_SIZE ) { width = MAX_CELL_SIZE ; height = MAX_CELL_SIZE ; }
        cellWidth = width;
        cellHeight = height;
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
