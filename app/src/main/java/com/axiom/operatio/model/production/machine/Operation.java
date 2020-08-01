package com.axiom.operatio.model.production.machine;

import com.axiom.operatio.model.production.materials.Material;

/**
 * Описывает операцию выполняемую машиной
 */
public class Operation {

    public static final int NONE = 0;          // Ничего не делает
    public static final int PROCESS = 1;       // Обработка материала (1->1)
    public static final int ASSEMBLE = 2;      // Сборка/смешивание (2->1)
    public static final int SPLIT = 3;         // Разделение на составляющие (1->2)

    protected int code;                        // Код операции
    protected Material input, output;
    protected int inputCount;

    public Operation(int OP, Material input, Material output, int inCount, int outCount) {
        code = OP;
        this.input = input;
        this.inputCount = inCount;
        this.output = output;
        this.inputCount = outCount;
    }

}
