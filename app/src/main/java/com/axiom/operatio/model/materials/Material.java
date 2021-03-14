package com.axiom.operatio.model.materials;

import android.content.res.Resources;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.data.JSONFileLoader;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.data.CSVTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Содержит данные и изображения материалов (materials.png, materials.csv)
 */
public class Material {

    protected static boolean initialized;      // Флаг инициализации системы материалов
    protected static Material[] materials;     // Перечень всех материалов

    private int materialID;                    // Код материала
    private Sprite image;                      // Изображения всех материалов
    private String name;                       // Наименование материала
    private double price;                      // Цена материала

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
        if (!initialized) loadMaterialsData(SceneManager.getResources());
        return materials;
    }

    /**
     * Конструктор материала
     *
     * @param ID   код материала
     * @param name название материала
     * @param price цена материала
     */
    private Material(int ID, Sprite image, String name, double price) {
        materialID = ID;
        this.image = image;
        this.name = name;
        this.price = price;
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

    public double getPrice() { return price; }

    public static int getMaterialsAmount() {
        if (!initialized) loadMaterialsData(SceneManager.getResources());
        return materials.length;
    }

    /**
     * Загружает изображение и описание 64 материалов (8x8)
     *
     * @param resources ресурсы приложения
     */
    protected static void loadMaterialsData(Resources resources) {
        Sprite allMaterials = new Sprite(resources, R.drawable.materials, 8, 8);
        // Загружаем массив материалов
        JSONFileLoader jsonFileLoader = new JSONFileLoader(resources, R.raw.materials);
        try {
            JSONArray jsonMaterials = new JSONArray(jsonFileLoader.getJsonFile());
            int ID, totalMaterials = jsonMaterials.length();
            materials = new Material[totalMaterials];
            for (int i=0; i < totalMaterials; i++) {
                JSONObject jsonMaterial = jsonMaterials.getJSONObject(i);
                ID = jsonMaterial.getInt("ID");
                if (ID >= 0 && ID < materials.length) {
                    Sprite image = allMaterials.getAsSprite(ID);
                    String name = jsonMaterial.getString("name");
                    double price = jsonMaterial.getInt("price");
                    materials[ID] = new Material(ID,  image, name, price);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        initialized = true;
    }

}