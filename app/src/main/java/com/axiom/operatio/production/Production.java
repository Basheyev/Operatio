package com.axiom.operatio.production;

import com.axiom.operatio.production.block.Block;

import java.util.ArrayList;

public class Production {

    protected static Production instance;
    protected ArrayList<Block> blocks;
    protected Block[][] grid;
    protected int columns, rows;
    protected long cycle;


    public static Production getInstance(int columns, int rows) {
        if (instance==null) instance = new Production(columns, rows);
        return instance;
    }


    private Production(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
        grid = new Block[rows][columns];

        blocks = new ArrayList<Block>(100);
    }


    public void cycle() {
        Block block;
        int size = blocks.size();
        for (int i=0; i<size; i++) {
            block = blocks.get(i);
            block.process();
        }
        cycle++;
    }


    public boolean setBlock(Block block, int col, int row) {
        if (block == null) return false;
        if (col < 0 || col >= columns) return false;
        if (row < 0 || row >= rows) return false;
        block.column = col;
        block.row = row;
        blocks.add(block);
        grid[row][col] = block;
        return true;
    }


    public Block getBlockAt(int col, int row) {
        if (col < 0 || col >= columns) return null;
        if (row < 0 || row >= rows) return null;
        return grid[row][col];
    }


    public Block getBlockAt(Block relativeTo, int direction) {
        switch (direction) {
            case Block.LEFT:
                return getBlockAt(relativeTo.column - 1, relativeTo.row);
            case Block.RIGHT:
                return getBlockAt(relativeTo.column + 1, relativeTo.row);
            case Block.UP:
                return getBlockAt(relativeTo.column, relativeTo.row + 1);
            case Block.DOWN:
                return getBlockAt(relativeTo.column, relativeTo.row - 1);
            default:
                return null;
        }
    }


    public boolean removeBlock(Block block) {
        grid[block.row][block.column] = null;
        blocks.remove(block);
        return true;
    }


    public void clearBlocks() {
        for (int row=0; row < rows;row++) {
            for (int col=0; col < columns; col++) {
                grid[row][col] = null;
            }
        }
        blocks.clear();
    }


    public int getTotalItems() {
        Block block;
        int size = blocks.size();
        int total = 0;
        for (int i=0; i<size; i++) {
            block = blocks.get(i);
            total += block.getItemsAmount();
        }
        return total;
    }


    public static long getCurrentCycle() {
        if (instance==null) return 0;
        return instance.cycle;
    }


    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

}
