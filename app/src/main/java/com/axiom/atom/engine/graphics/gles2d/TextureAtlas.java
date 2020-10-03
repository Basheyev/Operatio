package com.axiom.atom.engine.graphics.gles2d;

// Strip - линия спрайтов одинакового размера (автоматически генерируется деля на количество)
// Tileset - сетка спрайтов одинакового размера (автоматически генерируется для на столбцы и строки)
// Atlas - регионы с разными размерами и координатами (загружается JSON с регионами)
// Сначала надо вынести из Sprite вычисление текстурных координат по Strip/Tileset
// Затем реализовать Atlas

import java.util.ArrayList;

public class TextureAtlas {

    public static final int MIN_CAPACITY = 16;

    protected float textureWidth, textureHeight;
    protected ArrayList<TextureRegion> regions;

    public TextureAtlas(float textureWidth, float textureHeight) {
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        regions = new ArrayList<TextureRegion>(MIN_CAPACITY);
    }

    public TextureAtlas(float textureWidth, float textureHeight, int columns, int rows) {
        this(textureWidth, textureHeight);
        generateTilemap(columns, rows);
    }

    public TextureAtlas(Texture texture) {
        this.textureWidth = texture.width;
        this.textureHeight = texture.height;
        regions = new ArrayList<TextureRegion>(MIN_CAPACITY);
    }

    public TextureAtlas(Texture texture, int columns, int rows) {
        this(texture);
        generateTilemap(columns, rows);
    }

    public TextureRegion addRegion(String name, int x, int y, int width, int height) {
        // Уходим если не валидные параметры
        if (name==null || (x < 0) || (y < 0) || (width<1) || (height<1)) return null;
        // Сохраняем информацию о регионе
        // TODO Тут надо переварачивать координаты
        TextureRegion region = new TextureRegion();
        region.name = name;
        region.x = x;
        region.y = y;
        region.width = width;
        region.height = height;
        region.textureCoordinates = new float[12];
        // Нормируем координаты на текстурные координаты - единицу (0.0-1.0)
        // и переворачиваем (в Bitmap Y=0 сверху, а в текстуре Y=0 снизу)
        float x1 = x / textureWidth;
        float y1 = 1.0f - ((y + height) / textureHeight);
        float x2 = (x + width) / textureWidth;
        float y2 = 1.0f - (y / textureHeight);
        // ===== Треугольник 1
        region.textureCoordinates[0] = x1;  // левый верхний угол
        region.textureCoordinates[1] = y2;
        region.textureCoordinates[2] = x1;  // левый нижний угол
        region.textureCoordinates[3] = y1;
        region.textureCoordinates[4] = x2;  // правый верхний угол
        region.textureCoordinates[5] = y2;
        // ===== Треугольник 2
        region.textureCoordinates[6] = x1;  // левый нижний угол
        region.textureCoordinates[7] = y1;
        region.textureCoordinates[8] = x2;  // правый верхний угол
        region.textureCoordinates[9] = y2;
        region.textureCoordinates[10] = x2; // правый нижний угол
        region.textureCoordinates[11] = y1;
        // Добавляем в список
        regions.add(region);
        return region;
    }

    public TextureRegion getRegion(String name) {
        TextureRegion region;
        for (int i=0; i<regions.size(); i++) {
            region = regions.get(i);
            if (region.name.equals(name)) return region;
        }
        return null;
    }

    public ArrayList<TextureRegion> getRegions() {
        return regions;
    }

    public TextureRegion getRegion(int index) {
        return regions.get(index);
    }

    public boolean removeRegion(TextureRegion region) {
        return regions.remove(region);
    }

    public int size() {
        return regions.size();
    }

    public void clear() {
        regions.clear();
    }

    /**
     * Делит текстуру на регионы одинакового размера (TileMap)
     * @param columns количество столбцов
     * @param rows количество строк
     */
    protected void generateTilemap(int columns, int rows) {
        int width = (int) textureWidth;
        int height = (int) textureHeight;
        int spriteWidth = width / columns;
        int spriteHeight = height / rows;
        int i = 0;
        for (int y=0; y<height; y+=spriteHeight) {
            for (int x=0; x<width; x+=spriteWidth) {
                addRegion("Frame " + i, x, y, spriteWidth, spriteHeight);
                i++;
            }
        }
    }


}

