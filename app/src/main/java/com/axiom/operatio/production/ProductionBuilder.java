package com.axiom.operatio.production;

import com.axiom.operatio.production.materials.Item;
import com.axiom.operatio.production.block.Block;
import com.axiom.operatio.production.buffer.Buffer;
import com.axiom.operatio.production.machines.Machine;
import com.axiom.operatio.production.machines.MachineType;
import com.axiom.operatio.production.machines.Operation;
import com.axiom.operatio.production.materials.Material;
import com.axiom.operatio.production.transport.Conveyor;

public class ProductionBuilder {
    
    public static Production createDemoProduction() {
        Production production = Production.getInstance(15,15);
        MachineType m1 = MachineType.getMachineType(0);
        Operation op1 = m1.getOperations()[0];
        MachineType m2 = MachineType.getMachineType(1);
        Operation op2 = m1.getOperations()[0];
        
        Buffer storage1 = new Buffer( production, 100);
        Machine machineOld1 = new Machine(  production, m1, op1, Block.LEFT, Block.RIGHT);
        Conveyor conv0 = new Conveyor( production, Block.LEFT, Block.RIGHT,5 );
        Machine machineOld2 = new Machine( production, m2, op2,  Block.LEFT, Block.RIGHT);
        Buffer storage3 = new Buffer( production, 100 );

        Conveyor conv = new Conveyor( production, Block.LEFT, Block.RIGHT,5);
        Buffer storage4 = new Buffer( production, 50 );


        Conveyor conv2 = new Conveyor( production, Block.DOWN, Block.UP,5);
        Buffer storage5 = new Buffer( production, 100 );
        Conveyor conv3 = new Conveyor( production, Block.RIGHT, Block.LEFT, 10);


        Conveyor conv4 = new Conveyor( production, Block.RIGHT, Block.LEFT,10);
        Conveyor conv5 = new Conveyor( production, Block.RIGHT, Block.LEFT,10);
        Conveyor conv6 = new Conveyor( production, Block.RIGHT, Block.LEFT,10);
        Conveyor conv7 = new Conveyor( production, Block.RIGHT, Block.LEFT,10);
        Conveyor conv8 = new Conveyor( production, Block.UP, Block.DOWN,10);
        Buffer storage6 = new Buffer( production, 100 );

        int col = 0;
        int row = 0;
        production.setBlock(storage1, col,row+1);
        production.setBlock(machineOld1, col+1, row+1);
        production.setBlock(conv0, col+2, row+1);
        production.setBlock(machineOld2, col+3, row+1);
        production.setBlock(storage3, col+4, row+1);
        production.setBlock(conv, col+5,row+1);
        production.setBlock(storage4, col+6, row+1);
        production.setBlock(conv2, col+6,row+2);
        production.setBlock(storage5, col+6, row+3);
        production.setBlock(conv3, col+5,row+3);
        production.setBlock(conv4, col+4,row+3);
        production.setBlock(conv5, col+3,row+3);
        production.setBlock(conv6, col+2,row+3);
        production.setBlock(conv7, col+1,row+3);
        production.setBlock(storage6, col, row+3);
        production.setBlock(conv8, col, row+2);

        for (int i=0; i<20; i++) storage3.push(new Item(Material.getMaterial(0)));

        return production;
    }
    
}
