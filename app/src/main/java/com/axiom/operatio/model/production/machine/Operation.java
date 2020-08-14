package com.axiom.operatio.model.production.machine;

import com.axiom.operatio.model.production.materials.Material;

public class Operation {

    protected int operationTime;
    protected Material[] outputMaterials;
    protected Material[] inputMaterials;
    protected int[] outputAmount;
    protected int[] inputAmount;

}
