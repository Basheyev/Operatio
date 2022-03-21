package com.axiom.operatio.model.production;

import android.content.res.Resources;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.gles2d.Texture;
import com.axiom.operatio.scenes.production.view.MoneyParticles;
import com.axiom.operatio.scenes.production.view.DustParticles;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.production.block.BlockRenderer;

/**
 * Рендер производства
 */
public class ProductionRenderer {

    public static final int Z_ORDER_FLOOR = 0;
    public static final int Z_ORDER_SHADOWS = 1;

    public static final int Z_ORDER_DUST_PARTICLES = 10;
    public static final int Z_ORDER_JOINTS = 11;
    public static final int Z_ORDER_CONVEYORS = 12;
    public static final int Z_ORDER_ITEMS = 13;
    public static final int Z_ORDER_MACHINES = 14;

    public static final int Z_ORDER_DIRECTIONS = 20;
    public static final int Z_ORDER_EXCLAMATION = 21;
    public static final int Z_ORDER_SELECTION = 21;

    public static final int Z_ORDER_MONEY_PARTICLES = 23;


    public static int MIN_CELL_SIZE = 48;
    public static final int MAX_CELL_SIZE = 384;
    public static final int INITIAL_CELL_WIDTH = (MAX_CELL_SIZE + MIN_CELL_SIZE) / 2;
    public static final int INITIAL_CELL_HEIGHT = (MAX_CELL_SIZE + MIN_CELL_SIZE) / 2;

    private static Sprite tiles = null;

    private final Production production;
    private final Sprite tile, tileBlocked, outsideTile, selection, wrongSelection;

    private final DustParticles dustParticles;
    private final MoneyParticles moneyParticles;

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
        tile.setZOrder(Z_ORDER_FLOOR);
        tileBlocked = tiles.getAsSprite(70);
        tileBlocked.setZOrder(Z_ORDER_FLOOR);
        outsideTile = tiles.getAsSprite(87);
        outsideTile.setZOrder(Z_ORDER_FLOOR);
        selection = tiles.getAsSprite(67);
        selection.setZOrder(Z_ORDER_SELECTION);
        wrongSelection = tiles.getAsSprite(69);
        wrongSelection.setZOrder(Z_ORDER_SELECTION);

        Sprite particleSprite = tiles.getAsSprite(86);
        dustParticles = new DustParticles(particleSprite,16, 1000, 100);
        dustParticles.zOrder = Z_ORDER_DUST_PARTICLES;

        moneyParticles = new MoneyParticles(production,32, 50, ProductionRenderer.Z_ORDER_MONEY_PARTICLES + 1);

        this.production = production;
        this.cellWidth = INITIAL_CELL_WIDTH;
        this.cellHeight = INITIAL_CELL_HEIGHT;
    }


    public void draw(Camera camera) {

        int columns = production.getColumns();
        int rows = production.getRows();
        int minCol = (int) (camera.getMinX() / cellWidth) - 1;
        int minRow = (int) (camera.getMinY() / cellHeight) - 1;
        int maxCol = minCol + (int) (Camera.WIDTH / cellWidth) + 2;
        int maxRow = minRow + (int) (Camera.HEIGHT / cellHeight) + 2;
        int selectedRow = production.getSelectedRow();
        int selectedCol = production.getSelectedCol();

        for (int row = minRow; row <= maxRow; row++) {
            for (int col = minCol; col <= maxCol; col++) {
                // Отрисовываем плитку
                drawTile(camera, col, row, columns, rows);
                // Отрисовываем блок
                Block block = production.getBlockAt(col, row);
                if (block != null) drawBlock(camera, block, col, row);
                // Отрисовываем выделение и частицы
                if (production.isBlockSelected() && row==selectedRow && col==selectedCol) {
                    drawSelection(camera, col, row);
                    drawParticles(camera, col, row);
                }
            }
        }

        // Отрисовать частицы денег
        moneyParticles.draw(cellWidth * 0.25f);

        // Отрисовываем передвигаемый блок поверх остальных
        if (movingBlock!=null) drawMovingBlock();
    }


    private void drawTile(Camera camera, int col, int row, int columns, int rows) {
        //  Чтобы убрать артефакты на низких разрешениях
        //  добавляем наложение на соседние плитки по 0.5px
        float x1 = col * cellWidth - 0.5f;
        float y1 = row * cellHeight - 0.5f;
        float x2 = x1 + cellWidth + 0.5f;
        float y2 = y1 + cellHeight + 0.5f;
        if (col < 0 || col >= columns || row < 0 || row >= rows) {
            outsideTile.drawExact(camera, x1, y1, x2, y2);
        } else if (!production.isUnlocked(col, row))
            tileBlocked.drawExact(camera, x1, y1, x2, y2);
        else tile.drawExact(camera, x1, y1, x2, y2);
    }


    private void drawBlock(Camera camera, Block block, int col, int row) {
        BlockRenderer renderer = block.getRenderer();
        if (renderer != null) {
            float speed = ((float) Production.CYCLE_TIME) / production.getCycleMilliseconds();
            renderer.setAnimationSpeed(speed);
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
        Sprite requiredSelection;

        if (underlyingBlock==null || movingBlock==null) {
            requiredSelection = selection;
        } else {
            requiredSelection = wrongSelection;
        }

        float fluctuation = (float) (Math.cos(System.currentTimeMillis() / 100.0d) + 1f) / 20f;
        requiredSelection.draw(camera,
                col * cellWidth - (cellWidth * fluctuation),
                row * cellHeight - (cellHeight * fluctuation),
                cellWidth * (1.0f + fluctuation * 2),
                cellHeight * (1.0f + fluctuation * 2));
    }


    private void drawParticles(Camera camera, int col, int row) {
        dustParticles.draw(camera,
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


    public DustParticles getParticles() {
        return dustParticles;
    }


    public MoneyParticles getMoneyParticles() {
        return moneyParticles;
    }


}
