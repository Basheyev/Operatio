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

    protected static boolean initialized;      // Флаг инициализации системы материалов
    protected static Material[] materials;     // Перечень всех материалов

    private int materialID;                    // Код материала
    private Sprite image;                      // Изображения всех материалов
    private String name;                       // Наименование материала


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

    public static Material[] getMaterials() {
        return materials;
    }

    /**
     * Конструктор материала
     *
     * @param ID   код материала
     * @param name название материала
     */
    private Material(int ID, Sprite image, String name) {
        materialID = ID;
        this.image = image;
        this.name = name;
    }


    public int getMaterialID() {
        return materialID;
    }

    /**
     * Возвращает изображение материала
     *
     * @return спрайт с выставленным кадром материала
     */
    public Sprite getImage() {
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


    public static int getMaterialsAmount() {
        return materials.length;
    }

    /**
     * Загружает изображение и описание 64 материалов (8x8)
     *
     * @param resources ресурсы приложения
     */
    protected static void loadMaterialsData(Resources resources) {
        // Загружаем массив материалов
        CSVTable csv = new CSVTable(resources, R.raw.materials);
        int ID, totalMaterials = csv.getRowCount();
        materials = new Material[totalMaterials];
        for (int i = 0; i < totalMaterials; i++) {
            ID = csv.getIntValue(i, 0);
            if (ID >= 0 && ID < materials.length) {
                // TODO Можно более экономно создавать спрайт не делая атлас каждый раз
                Sprite image = new Sprite(resources, R.drawable.materials, 8, 8);
                image.setActiveFrame(ID);
                materials[ID] = new Material(ID,  image, csv.getValue(i, 1).trim());
            }
        }
        initialized = true;
    }

}