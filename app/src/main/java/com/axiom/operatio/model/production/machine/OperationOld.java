package com.axiom.operatio.model.production.machine;

import android.content.res.Resources;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.data.CSVFile;
import com.axiom.operatio.model.production.materials.Material;

import java.util.ArrayList;


/**
 * Описывает операцию выполняемую машиной
 * TODO Реализовать загрузку данных о машинах
 */
public class OperationOld {



    public static final int NONE = 0;          // Ничего не делает
    public static final int PROCESS = 1;       // Обработка материала (1->1)
    public static final int ASSEMBLE = 2;      // Сборка/смешивание (2->1)
    public static final int SPLIT = 3;         // Разделение на составляющие (1->2)

    protected int code;                        // Код операции
    protected Material input, output;
    protected int inputCount;


    public OperationOld(int OP, Material input, Material output, int inCount, int outCount) {

        code = OP;
        this.input = input;
        this.inputCount = inCount;
        this.output = output;
        this.inputCount = outCount;
    }




}
