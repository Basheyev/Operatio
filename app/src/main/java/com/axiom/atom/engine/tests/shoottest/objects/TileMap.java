package com.axiom.atom.engine.tests.shoottest.objects;


import android.util.Log;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameObject;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.physics.PhysicsRender;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Ортогональная карта плиток (tilemap)
 */
public class TileMap {

    //----------------------------------------------------------------------
    // Данные карты
    //----------------------------------------------------------------------
    private int[] tileMap = null;     // Данные карты
    private GameObject[] objectsGrid; // Ссылки на объекты
    private int columns;              // Количество столбцов
    private int rows;                 // Количество строк
    private float scale;

    //----------------------------------------------------------------------
    // Данные отображения
    //----------------------------------------------------------------------
    private int tileWidth;            // ширина спрайта (пиксели)
    private int tileHeight;           // высота спрайта (пиксели)
    private Sprite sprite;            // спрайт
    //----------------------------------------------------------------------

    public TileMap(GameScene gameScene, float scale) {
        this.scale = scale;
        InputStream mapFile = gameScene.getResources().openRawResource(R.raw.level);
        loadMap(mapFile);
        sprite = new Sprite(gameScene.getResources(),R.drawable.square,4,1);
        generateObjectsGrid(gameScene, sprite, scale);
    }

    public float getTileWidth() {
        return sprite.getWidth() * scale;
    }

    public float getTileHeight() {
        return sprite.getHeight() * scale;
    }

    public int getColumns() { return columns; }

    public int getRows() {return rows; }

    public int getValue(int col, int row) {
        if (col < 0 || col >= columns) return -1;
        if (row < 0 || row >= rows) return -1;
        return tileMap[row * columns + col];
    }

    //------------------------------------------------------------------------

    protected void generateObjectsGrid(GameScene scene, Sprite tilesheet, float scale) {
        GameObject[] grid = new GameObject[tileMap.length];
        TileObject obj;
        int tileType;
        for (int y=0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                // Создать объект
                tileType = tileMap[(rows-1-y)*columns+x];
                float w = sprite.getWidth();
                float h = sprite.getHeight();
                obj = new TileObject(scene, this, sprite, tileType);
                obj.x = x * (w * scale) - w/2;
                obj.y = y * (h * scale) - h/2;
                obj.scale = scale;
                obj.setLocalBounds(-w/2 * scale,-h/2 * scale,w/2 * scale,h/2 * scale);
                obj.column = x;
                obj.row = y;
                if (tileType == 0) obj.bodyType = PhysicsRender.BODY_VOID;
                grid[(rows-1-y) * columns + x] = obj;
                scene.addObject(obj);
            }
        }
        objectsGrid = grid;
    }

    protected void removeObjectsGrid(GameScene scene) {
        for (GameObject obj:objectsGrid) {
            scene.removeObject(obj);
        }
    }

    //------------------------------------------------------------------------

    /**
     * Загрузка данных карты из файла
     */
    protected void loadMap(InputStream rsc) {
        String line;
        String[] data;

        String spriteFileName;
        int spriteWidth, spriteHeight;
        int columns, rows;
        int[] tilemap;

        //------------------------------------------------------------------------
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(rsc)); //

            // Первая строка - имя файла со спрайтом
            line = input.readLine();
            data = line.split(",");
            spriteFileName = data[0];

            // Второая строка размеры спрайта в пикселях: ширина, высота
            line = input.readLine();
            data = line.split(",");
            spriteWidth = Integer.parseInt(data[0]);
            spriteHeight = Integer.parseInt(data[1]);
            Log.i("TILEMAP:", "Loading tilemap header: " +
                    spriteFileName + " (" + spriteWidth +"x"+ spriteHeight +")...");

            // Третья строка размеры карты в ячейках: столбцов, строк
            line = input.readLine();
            data = line.split(",");
            columns = Integer.parseInt(data[0]);
            rows = Integer.parseInt(data[1]);
            Log.i("TILEMAP", "Loaded tilemap dimensions: " + columns +"x"+ rows);

            // Формируем массив для хранения данных карты
            tilemap = new int[columns*rows];

            // Считываем карту
            for (int i=0; i<rows; i++) {
                line = input.readLine();
                data = line.split(",");
                for (int j=0; j<columns; j++) {
                    tilemap[i*columns+j] = Integer.parseInt(data[j]);
                }
            }

            input.close();

        } catch (Exception e) {
            Log.e("TILEMAP", "Error while reading map file: ");
            e.printStackTrace();
            return;
        }

        // Присваеваем значения
        this.columns = columns;
        this.rows = rows;
        tileMap = tilemap;

        // Загружаем спрайт
        tileWidth = spriteWidth;
        tileHeight = spriteHeight;

    }

}
