package com.axiom.operatio.model.matflow.materials;

/**
 * Класс содержащий информацию о материале (сырье, комплектующее, полуфабрикат, продукт).
 */
public class Material {

    public int materialID;      // Код материала
    public String name;         // Название материала
    public String description;  // Описание материала
    public int measurement;     // Единица измерения
    public long price;          // Цена материала

    public static final int MEASURE_UNITS = 0;        // Количество (штук)
    public static final int MEASURE_KILOGRAMS = 1;    // Вес (1 тонна = 1000 кг)
    public static final int MEASURE_LITERS = 2;       // Объем (1 кубический метр = 1000 литров)

    public Material(int ID, String name, String description, int measure, long price) {
        this.materialID = ID;
        this.name = name;
        this.description = description;
        this.measurement = measure;
        this.price = price;
    }

}
