package com.axiom.operatio.model.materials;

import android.content.res.Resources;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.data.CSVTable;

/**
 * Содержит данные и изображения материалов (materials.png, materials.csv)
 */
public class Material {

    protected static boolean initialized;     // Флаг инициализации системы материалов
    protected static Sprite image;            // Изображения всех материалов
    protected static Material[] materials;    // Перечень всех материалов

    public int materialID;                    // Код материала
    public String name;                       // Наименование материала

    /**
     * Конструктор материала
     *
     * @param ID   код материала
     * @param name название материала
     */
    private Material(int ID, String name) {
        materialID = ID;
        this.name = name;
    }


    /**
     * Возвращает информацию о материале по коду
     *
     * @param ID код материала
     * @return информация о материале
     */
    public static Material getMaterial(int ID) {
        if (!initialized) loadMaterialsData(SceneManager.getResources());
        if (ID < 0 || ID >= materials.length) return null;
        return materials[ID];
    }

    /**
     * Возвращает изображение материала
     *
     * @return спрайт с выставленным кадром материала
     */
    public Sprite getImage() {
        image.setActiveFrame(materialID);
        return image;
    }

    /**
     * Возвращает название материала
     *
     * @return название материала
     */
    public String getName() {
        return name;
    }

    /**
     * Загружает изображение и описание 64 материалов (8x8)
     *
     * @param resources ресурсы приложения
     */
    protected static void loadMaterialsData(Resources resources) {
        // Загружаем спрайт с изображегиями всех материалов
        image = new Sprite(resources, R.drawable.materials, 8, 8);
        // Загружаем массив материалов
        CSVTable csv = new CSVTable(resources, R.raw.materials);
        int ID, rows = csv.getRowCount();
        materials = new Material[rows];
        for (int i = 0; i < rows; i++) {
            ID = csv.getIntValue(i, 0);
            if (ID >= 0 && ID < materials.length) {
                materials[ID] = new Material(ID, csv.getValue(i, 1).trim());
            }
        }
        initialized = true;
    }

}