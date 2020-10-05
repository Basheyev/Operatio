package com.axiom.operatio.model.block;


import com.axiom.atom.engine.data.Channel;
import com.axiom.operatio.model.Production;
import com.axiom.operatio.model.materials.Item;

// TODO Рендерить под блоком конвейер - как основу движения материалов
/**
 * Базовый блок производства реализующий примитивную механику
 */
public abstract class Block {

    protected Production production;                  // Производство к которомуо относится блок
    protected int state = IDLE;                       // Текущее состояние блока
    protected int inputDirection, outputDirection;    // Направление ввода и вывода
    protected int inputCapacity, outputCapacity;      // Максимальая вместимость блока в предметах
    protected Channel<Item> input;                    // Буферы ввода предметов
    protected Channel<Item> output;                   // Буферы вывода предметов
    public int column, row;                           // Координаты блока в сетке карты
    protected BlockRenderer renderer;                      // Отрисовщик

    public boolean directionFlip = false;

    /**
     * Конструктор блока производства
     * @param production производство
     * @param inCapacity размер буфера ввода в количестве предметов
     * @param outCapacity размер буфера вывода в количестве предметов
     */
    public Block(Production production, int inDir, int inCapacity, int outDir, int outCapacity) {
        this.production = production;
        this.inputDirection = inDir;
        this.inputCapacity = inCapacity;
        this.outputDirection = outDir;
        this.outputCapacity = outCapacity;
        this.input = new Channel<Item>(inCapacity);
        this.output = new Channel<Item>(outCapacity);
    }


    public void setOutputDirection(int outDir) {
        this.outputDirection = outDir;
    }

    public void setInputDirection(int inDir) {
        this.inputDirection = inDir;
    }

    public void setDirections(int inDir, int outDir) {
        setInputDirection(inDir);
        setOutputDirection(outDir);
    }

    /**
     * Добавляет предмет во входную очередь блока
     * @param item предмет
     * @return true - если блок принял предмет, false - если нет
     */
    public boolean push(Item item) {
        if (item==null) return false;
        if (state==BUSY) return false;
        if (input.size()>=inputCapacity) return false;
        item.setOwner(this);
        input.add(item);
        return true;
    }


    /**
     * Возвращает обработанный предмет из блока, но не удаляет из выходной очереди
     * @return первый в очереди готовый предмет или null если такого нет
     */
    public Item peek() {
        return output.peek();
    }


    /**
     * Возвращает обработанный предмет из блока и удаляет из выходной очереди
     * @return первый в очереди готовый предмет или null если такого нет
     */
    public Item poll() {
        Item item = output.peek();
        if (item==null) return null;
        return output.poll();
    }


    /**
     * Забирает один предмет из блока по направлению входа
     * @return true - если получилось забрать, false - если нет
     */
    protected Item getItemFromInputDirection() {
        Block inputBlock = production.getBlockAt(this, inputDirection);
        if (inputBlock==null) return null;   // Если на входящем направление ничего нет
        Item item = inputBlock.peek();       // Пытаемся взять предмет из блока входа
        if (item==null) return null;         // Если ничего нет возвращаем false
        if (!push(item)) return null;        // Если не получилось добавить к себе - false
        return inputBlock.poll();            // Если получилось - удаляем из блока входа
    }

    /**
     * Возвращает состояние блока
     * @return состояние блока
     */
    public int getState() {
        return state;
    }

    /**
     * Возвращает общее количество предметов в блоке
     * @return общее количество предметов в блоке
     */
    public int getItemsAmount() {
        return input.size() + output.size();
    }

    public int getCapacity() {
        return inputCapacity + outputCapacity;
    }

    public int getInputDirection() {
        return inputDirection;
    }

    public int getOutputDirection() {
        return outputDirection;
    }

    public BlockRenderer getRenderer() {
        return renderer;
    }

    /**
     * Обрабатывает входной поток предметов в выходной поток предметов
     * @return true - если успешно, false - если произошел сбой
     */
    public abstract void process();


    //---------------------------------------------------------------------------------------
    // Состояние блока
    //---------------------------------------------------------------------------------------
    public static final int IDLE = 0;
    public static final int BUSY = 1;
    public static final int FAULT = -1;

    //---------------------------------------------------------------------------------------
    // Ориентация (по часовой стрелке)
    //---------------------------------------------------------------------------------------
    public static final int NONE = 0;
    public static final int LEFT = 1;
    public static final int UP = 2;
    public static final int RIGHT = 3;
    public static final int DOWN = 4;

    /**
     * Следующее направление по часовой стрелке
     * @param direction текущее направление
     * @return следующее направление по часовой стрелке
     */
    public static int nextClockwiseDirection(int direction) {
        if (direction==NONE || direction < 0) return NONE;
        direction++;
        if (direction > DOWN) direction = LEFT;
        return direction;
    }

    /**
     * Противоположное направление
     * @param direction направление
     * @return противоположное направление
     */
    public static int oppositeDirection(int direction) {
        if (direction==LEFT) return RIGHT;
        if (direction==RIGHT) return LEFT;
        if (direction==UP) return DOWN;
        if (direction==DOWN) return UP;
        return NONE;
    }



    //--------------------------------------------------------------------------------------
    // Установка и повороты направления потока материалов
    //--------------------------------------------------------------------------------------

    public void adjustFlowDirection() {
        Block upper = production.getBlockAt(this, Block.UP);
        Block down = production.getBlockAt(this, Block.DOWN);
        Block left = production.getBlockAt(this, Block.LEFT);
        Block right = production.getBlockAt(this, Block.RIGHT);
        int neighborsCount =
                (upper!= null ? 1 : 0) + (down != null ? 1 : 0) +
                (left != null ? 1 : 0) + (right != null ? 1 : 0);
        boolean hasNeighbors = neighborsCount > 0;

        if (!hasNeighbors || neighborsCount > 2) return;

        if (neighborsCount==1) {
            Block neighbor = (upper != null) ? upper : (down != null) ? down : (left != null) ? left : right;
            int neighborSide = (upper != null) ? UP : (down != null) ? DOWN : (left != null) ? LEFT : RIGHT;
            adjustDirectionOneNeighbor(neighbor, neighborSide);
        }
        if (neighborsCount==2) {
            if (left!=null && right!=null) adjustDirectionTwoNeighbors(left, Block.LEFT, right, Block.RIGHT);
            if (left!=null && upper!=null) adjustDirectionTwoNeighbors(left, Block.LEFT, upper, Block.UP);
            if (left!=null && down!=null) adjustDirectionTwoNeighbors(left, Block.LEFT, down, Block.DOWN);
            if (right!=null && upper!=null) adjustDirectionTwoNeighbors(right, Block.RIGHT, upper, Block.UP);
            if (right!=null && down!=null) adjustDirectionTwoNeighbors(right, Block.RIGHT, down, Block.DOWN);
            if (upper!=null && down!=null) adjustDirectionTwoNeighbors(upper, Block.UP, down, Block.DOWN);
        }


    }



    private void adjustDirectionOneNeighbor(Block neighbor, int neighborSide) {
        Block neighborOutput = production.getBlockAt(neighbor, neighbor.getOutputDirection());
        Block neighborInput = production.getBlockAt(neighbor, neighbor.getInputDirection());
        if (neighborOutput==this) {
            setDirections(neighborSide, oppositeDirection(neighborSide));
        } else if (neighborInput==this) {
            setDirections(oppositeDirection(neighborSide), neighborSide);
        }
    }



    private void adjustDirectionTwoNeighbors(Block neighbor1, int neightbor1Side,
                                            Block neighbor2, int neightbor2Side) {

        Block neighbor1Output = production.getBlockAt(neighbor1, neighbor1.getOutputDirection());
        Block neighbor1Input = production.getBlockAt(neighbor1, neighbor1.getInputDirection());
        Block neighbor2Output = production.getBlockAt(neighbor2, neighbor2.getOutputDirection());
        Block neighbor2Input = production.getBlockAt(neighbor2, neighbor2.getInputDirection());

        // если ни один вход/выход двух соседей не направлен на это блок
        // поворачиваем как будто соседей нет
        if (neighbor1Input!=this && neighbor1Output!=this
                && neighbor2Input!=this && neighbor2Output!=this) return;

        // Если только первого блока вход/выход направлен на этот блок
        // поворачиваем как будто у нас один сосед
        if ((neighbor1Input==this || neighbor1Output==this)
                && neighbor2Input!=this && neighbor2Output!=this) {
            adjustDirectionOneNeighbor(neighbor1, neightbor1Side);
        } else if (neighbor1Input!=this && neighbor1Output!=this) {
            // Если только второго блока вход/выход направлен на этот блок
            // поворачиваем как будто у нас один сосед
            adjustDirectionOneNeighbor(neighbor2, neightbor2Side);
        } else if (neighbor1Input==this && neighbor2Output==this) {
            setDirections(neightbor2Side, neightbor1Side);
        } else if (neighbor1Output==this && neighbor2Input==this) {
            setDirections(neightbor1Side, neightbor2Side);
        }


    }




    public void rotateFlowDirection() {
        Block upper = production.getBlockAt(this, Block.UP);
        Block down = production.getBlockAt(this, Block.DOWN);
        Block left = production.getBlockAt(this, Block.LEFT);
        Block right = production.getBlockAt(this, Block.RIGHT);

        int neighborsCount =
                (upper!= null ? 1 : 0) + (down != null ? 1 : 0) +
                (left != null ? 1 : 0) + (right != null ? 1 : 0);

        boolean hasNeighbors = neighborsCount > 0;

        // Если нет соседей
        if (!hasNeighbors) {
            rotateFlowDirection90();
        } else {
            // Если один сосед
            if (neighborsCount==1) {
                if (upper!=null) rotateFlowDirectionOneNeighbor(upper, Block.UP); else
                if (down!=null) rotateFlowDirectionOneNeighbor(down, Block.DOWN); else
                if (left!=null) rotateFlowDirectionOneNeighbor(left, Block.LEFT);
                else rotateFlowDirectionOneNeighbor(right, Block.RIGHT);
            } if (neighborsCount==2) {
                if (left!=null && right!=null) rotateFlowDirectionTwoNeighbors(left, Block.LEFT, right, Block.RIGHT);
                if (left!=null && upper!=null) rotateFlowDirectionTwoNeighbors(left, Block.LEFT, upper, Block.UP);
                if (left!=null && down!=null) rotateFlowDirectionTwoNeighbors(left, Block.LEFT, down, Block.DOWN);
                if (right!=null && upper!=null) rotateFlowDirectionTwoNeighbors(right, Block.RIGHT, upper, Block.UP);
                if (right!=null && down!=null) rotateFlowDirectionTwoNeighbors(right, Block.RIGHT, down, Block.DOWN);
                if (upper!=null && down!=null) rotateFlowDirectionTwoNeighbors(upper, Block.UP, down, Block.DOWN);
            } if (neighborsCount>2) {
                rotateFlowDirectionFree();
            }
        }

    }


    /**
     * Проворачивает поочередно все 12 вариантов направления потока материалов
     */
    private void rotateFlowDirectionFree() {

        int currentInpDir = getInputDirection();
        int currentOutDir = getOutputDirection();

        if (!directionFlip) {
            setDirections(currentOutDir, currentInpDir);
            directionFlip = true;
        } else {
            int newInpDir = Block.nextClockwiseDirection(currentInpDir);
            int newOutDir = Block.nextClockwiseDirection(currentOutDir);
            if (newInpDir==currentOutDir) {
                setOutputDirection(newOutDir);
            } else {
                setInputDirection(newInpDir);
            }
            directionFlip = false;
        }

    }


    /**
     * Поворачивает направление потока материалов на 90 градусов
     */
    private void rotateFlowDirection90() {
        int currentInpDir = getInputDirection();
        int currentOutDir = getOutputDirection();
        int newInpDir = Block.nextClockwiseDirection(currentInpDir);
        int newOutDir = Block.nextClockwiseDirection(currentOutDir);
        setDirections(newInpDir, newOutDir);
    }


    /**
     * Поворачивает направление потока материалов при одном соседе
     */
    private void rotateFlowDirectionOneNeighbor(Block neighbor, int neightborSide) {
        // Считываем текущее направление потока материалов
        int currentInpDir = getInputDirection();
        int currentOutDir = getOutputDirection();
        int newInpDir, newOutDir;

        Block neighborOutput = production.getBlockAt(neighbor, neighbor.getOutputDirection());
        Block neighborInput = production.getBlockAt(neighbor, neighbor.getInputDirection());

        // Если входы/выходы соседа не направлены на этот блок
        if (neighborInput!=this && neighborOutput!=this) {
            rotateFlowDirection90();
            return;
        }

        if (!directionFlip) {
            // Перевернуть направление
            setDirections(currentOutDir, currentInpDir);
            directionFlip = true;
        } else {
            // Переворачиваем направление обратно и делаем поворот
            int tmp = currentInpDir;
            currentInpDir = currentOutDir;
            currentOutDir = tmp;

            // Если направление выхода соседа направлено на этот блок
            if (neighborOutput == this) {
                // Фиксируем вход этого блока со стороны выхода соседа
                newInpDir = neightborSide;
                // Поворачиваем лишь выход материалов этого блока
                newOutDir = Block.nextClockwiseDirection(currentOutDir);
                // Если выход совпал со входом, поворачиваем выход еще раз
                if (newOutDir == newInpDir) newOutDir = Block.nextClockwiseDirection(newOutDir);
                setDirections(newInpDir, newOutDir);
                directionFlip = false;
            }

            // Если направление входа соседа направлено на этот блок
            if (neighborInput == this) {
                // Фиксируем выход этого блока на сторону входа соседа
                newOutDir = neightborSide;
                // Поворачиваем лишь вход материалов этого блока
                newInpDir = Block.nextClockwiseDirection(currentInpDir);
                // Если вход совпал со выходом, поворачиваем вход еще раз
                if (newInpDir == newOutDir) newInpDir = Block.nextClockwiseDirection(newInpDir);
                setDirections(newInpDir, newOutDir);
                directionFlip = false;
            }

        }

    }



    /**
     * Поворачивает направление потока материалов при двух соседях
     */
    private void rotateFlowDirectionTwoNeighbors(Block neighbor1, int neightbor1Side,
                                                 Block neighbor2, int neightbor2Side) {

        // Считываем текущее направление потока материалов
        int currentInpDir = getInputDirection();
        int currentOutDir = getOutputDirection();
        int newInpDir, newOutDir;

        Block neighbor1Output = production.getBlockAt(neighbor1, neighbor1.getOutputDirection());
        Block neighbor1Input = production.getBlockAt(neighbor1, neighbor1.getInputDirection());
        Block neighbor2Output = production.getBlockAt(neighbor2, neighbor2.getOutputDirection());
        Block neighbor2Input = production.getBlockAt(neighbor2, neighbor2.getInputDirection());

        // если ни один вход/выход двух соседей не направлен на это блок
        // поворачиваем как будто соседей нет
        if (neighbor1Input!=this && neighbor1Output!=this
        && neighbor2Input!=this && neighbor2Output!=this) {
            rotateFlowDirection90();
            return;
        }

        // Если только первого блока вход/выход направлен на этот блок
        // поворачиваем как будто у нас один сосед
        if ((neighbor1Input==this || neighbor1Output==this)
                && neighbor2Input!=this && neighbor2Output!=this) {
            rotateFlowDirectionOneNeighbor(neighbor1, neightbor1Side);
            return;
        } else if (neighbor1Input!=this && neighbor1Output!=this) {
            // Если только второго блока вход/выход направлен на этот блок
            // поворачиваем как будто у нас один сосед
            rotateFlowDirectionOneNeighbor(neighbor2, neightbor2Side);
            return;
        }

        if (!directionFlip) {
            // Перевернуть направление
            setDirections(currentOutDir, currentInpDir);
            directionFlip = true;
        } else {
            // Соединяем вход выход двух соседних блоков
            setDirections(neightbor1Side, neightbor2Side);
            directionFlip = false;
        }

    }


}
