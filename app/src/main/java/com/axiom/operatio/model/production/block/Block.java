package com.axiom.operatio.model.production.block;


import com.axiom.atom.engine.data.Channel;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.materials.Item;
import com.axiom.operatio.model.production.buffer.Buffer;

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
    protected BlockRenderer renderer;                 // Отрисовщик

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
    protected void grabItemsFromInputDirection() {
        Block inputBlock = production.getBlockAt(this, inputDirection);
        if (inputBlock==null) return;         // Если на входящем направление ничего нет

        // Если выход входного блока смотрит на наш вход
        int neighborOutputDirection = inputBlock.outputDirection;
        Block neighborOutput = production.getBlockAt(inputBlock, neighborOutputDirection);
        if (neighborOutputDirection==NONE || neighborOutput==this) {
            Item item = inputBlock.peek();       // Пытаемся взять предмет из блока входа
            if (item == null) return;            // Если ничего нет возвращаем false
            if (!push(item)) return;             // Если не получилось добавить к себе - false
            inputBlock.poll();                   // Если получилось - удаляем из блока входа
        }
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

    public Production getProduction() {
        return production;
    }

    /**
     * Обрабатывает входной поток предметов в выходной поток предметов
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
    // Установка направления потока материалов
    //--------------------------------------------------------------------------------------

    /**
     * Устанавливает направления потока материалов с учётом соседних блоков
     */
    public void adjustFlowDirection() {

        // Смотрим какие есть соседние блоки на производстве: слева, справа, сверху и снизу
        Block upper = production.getBlockAt(this, UP);
        Block down = production.getBlockAt(this, DOWN);
        Block left = production.getBlockAt(this, LEFT);
        Block right = production.getBlockAt(this, RIGHT);

        // Исключаем соседние блоки выходы/входы которых смотрят не на этот блок
        upper = this.hasInOutFrom(upper) ? upper : null;
        down  = this.hasInOutFrom(down) ? down : null;
        left  = this.hasInOutFrom(left) ? left : null;
        right = this.hasInOutFrom(right) ? right : null;

        // Считаем количество соседей
        int neighborsCount =
                (upper!= null ? 1 : 0) + (down != null ? 1 : 0) +
                (left != null ? 1 : 0) + (right != null ? 1 : 0);

        // Если есть один сосед
        if (neighborsCount==1) {
            Block neighbor = (upper != null) ? upper : (down != null) ? down : (left != null) ? left : right;
            int neighborSide = (upper != null) ? UP : (down != null) ? DOWN : (left != null) ? LEFT : RIGHT;
            adjustDirectionOneNeighbor(neighbor, neighborSide);
        // Если есть два соседа
        } else if (neighborsCount==2) {
            if (left!=null && right!=null) adjustDirectionTwoNeighbors(left, LEFT, right, RIGHT);
            if (left!=null && upper!=null) adjustDirectionTwoNeighbors(left, LEFT, upper, UP);
            if (left!=null && down!=null) adjustDirectionTwoNeighbors(left, LEFT, down, DOWN);
            if (right!=null && upper!=null) adjustDirectionTwoNeighbors(right, RIGHT, upper, UP);
            if (right!=null && down!=null) adjustDirectionTwoNeighbors(right, RIGHT, down, DOWN);
            if (upper!=null && down!=null) adjustDirectionTwoNeighbors(upper, UP, down, DOWN);
        }

    }



    protected boolean hasInOutFrom(Block block) {
        if (block==null) return false;
        boolean hasInput = (production.getBlockAt(block, block.getInputDirection())==this);
        boolean hasOutput = (production.getBlockAt(block, block.getOutputDirection())==this);
        boolean itsBuffer = block instanceof Buffer;
        return hasInput || hasOutput || itsBuffer;
    }


    /**
     * Устанавливает направление потока материалов если есть один сосед
     * @param neighbor сосдний блок
     * @param neighborSide с какой стороны находится соседний блок
     */
    private void adjustDirectionOneNeighbor(Block neighbor, int neighborSide) {
        Block neighborOutput = production.getBlockAt(neighbor, neighbor.getOutputDirection());
        Block neighborInput = production.getBlockAt(neighbor, neighbor.getInputDirection());
        if (neighborOutput==this || (neighbor instanceof Buffer)) {
            setDirections(neighborSide, oppositeDirection(neighborSide));
        } else if (neighborInput==this) {
            setDirections(oppositeDirection(neighborSide), neighborSide);
        }
    }



    private void adjustDirectionTwoNeighbors(Block neighbor1, int neightbor1Side,
                                            Block neighbor2, int neightbor2Side) {

        Block neighbor1Output = production.getBlockAt(neighbor1, neighbor1.getOutputDirection());
        Block neighbor1Input = production.getBlockAt(neighbor1, neighbor1.getInputDirection());
        boolean neighbor1IsBuffer = neighbor1 instanceof Buffer;
        Block neighbor2Output = production.getBlockAt(neighbor2, neighbor2.getOutputDirection());
        Block neighbor2Input = production.getBlockAt(neighbor2, neighbor2.getInputDirection());
        boolean neighbor2IsBuffer = neighbor2 instanceof Buffer;

        // если ни один вход/выход двух соседей не направлен на это блок
        // и ни один из них не является буфером - уходим ничего не делаем
        if (neighbor1Input!=this && neighbor1Output!=this
                && neighbor2Input!=this && neighbor2Output!=this &&
                !neighbor1IsBuffer && !neighbor2IsBuffer) return;

        // Если только первого блока вход/выход направлен на этот блок
        // поворачиваем как будто у нас один сосед
        if ((neighbor1Input==this || neighbor1Output==this || neighbor1IsBuffer) &&
                (neighbor2Input!=this && neighbor2Output!=this && !neighbor2IsBuffer)) {
            adjustDirectionOneNeighbor(neighbor1, neightbor1Side);
        } else if (neighbor1Input!=this && neighbor1Output!=this && !neighbor1IsBuffer) {
            // Если только второго блока вход/выход направлен на этот блок
            // поворачиваем как будто у нас один сосед
            adjustDirectionOneNeighbor(neighbor2, neightbor2Side);
        } else if (neighbor1Input==this && neighbor2Output==this || neighbor1IsBuffer) {
            setDirections(neightbor2Side, neightbor1Side);
        } else if (neighbor1Output==this && neighbor2Input==this || neighbor2IsBuffer) {
            setDirections(neightbor1Side, neightbor2Side);
        }


    }


    //--------------------------------------------------------------------------------------
    // Поворот направления потока материалов
    //--------------------------------------------------------------------------------------

    /**
     * Поворачивает направление потока материалов
     */
    public void rotateFlowDirection() {
        Block upper = production.getBlockAt(this, UP);
        Block down = production.getBlockAt(this, DOWN);
        Block left = production.getBlockAt(this, LEFT);
        Block right = production.getBlockAt(this, RIGHT);

        // Исключаем соседние блоки выходы/входы которых смотрят не на этот блок
        upper = this.hasInOutFrom(upper) ? upper : null;
        down  = this.hasInOutFrom(down) ? down : null;
        left  = this.hasInOutFrom(left) ? left : null;
        right = this.hasInOutFrom(right) ? right : null;

        int neighborsCount =
                (upper!= null ? 1 : 0) + (down != null ? 1 : 0) +
                (left != null ? 1 : 0) + (right != null ? 1 : 0);

        boolean hasNeighbors = neighborsCount > 0;

        // Если нет соседних блоков
        if (!hasNeighbors) {
            rotateFlowDirectionRightAngle();
        } else {
            // Если один соседний блок
            if (neighborsCount==1) {
                if (upper!=null) rotateFlowDirectionOneNeighbor(upper, UP); else
                if (down!=null) rotateFlowDirectionOneNeighbor(down, DOWN); else
                if (left!=null) rotateFlowDirectionOneNeighbor(left, LEFT);
                else rotateFlowDirectionOneNeighbor(right, RIGHT);
            // Если два соседних блока
            } if (neighborsCount==2) {
                if (left!=null && right!=null) rotateFlowDirectionTwoNeighbors(left, LEFT, right, RIGHT);
                if (left!=null && upper!=null) rotateFlowDirectionTwoNeighbors(left, LEFT, upper, UP);
                if (left!=null && down!=null) rotateFlowDirectionTwoNeighbors(left, LEFT, down, DOWN);
                if (right!=null && upper!=null) rotateFlowDirectionTwoNeighbors(right, RIGHT, upper, UP);
                if (right!=null && down!=null) rotateFlowDirectionTwoNeighbors(right, RIGHT, down, DOWN);
                if (upper!=null && down!=null) rotateFlowDirectionTwoNeighbors(upper, UP, down, DOWN);
            // Если больше соседних блоков
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
        // Если еще не переворачивали направление потока материалов
        if (!directionFlip) {
            setDirections(currentOutDir, currentInpDir);
            directionFlip = true;
        } else {
        // Если переворачивали направление потока материалов, прокручиваем по часовой стрелке
            int newInpDir = nextClockwiseDirection(currentInpDir);
            int newOutDir = nextClockwiseDirection(currentOutDir);
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
    private void rotateFlowDirectionRightAngle() {
        int currentInpDir = getInputDirection();
        int currentOutDir = getOutputDirection();
        int newInpDir = nextClockwiseDirection(currentInpDir);
        int newOutDir = nextClockwiseDirection(currentOutDir); // TODO opposite direction
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
        boolean neighborIsBuffer = neighbor instanceof Buffer;

        // Если входы/выходы соседа не направлены на этот блок
        if (neighborInput!=this && neighborOutput!=this && !neighborIsBuffer) {
            rotateFlowDirectionRightAngle();
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
            if (neighborOutput == this || neighborIsBuffer) {
                // Фиксируем вход этого блока со стороны выхода соседа
                newInpDir = neightborSide;
                // Поворачиваем лишь выход материалов этого блока
                newOutDir = nextClockwiseDirection(currentOutDir);
                // Если выход совпал со входом, поворачиваем выход еще раз
                if (newOutDir == newInpDir) newOutDir = nextClockwiseDirection(newOutDir);
                setDirections(newInpDir, newOutDir);
                directionFlip = false;
            // Если направление входа соседа направлено на этот блок
            }

            if (neighborInput == this) {
                // Фиксируем выход этого блока на сторону входа соседа
                newOutDir = neightborSide;
                // Поворачиваем лишь вход материалов этого блока
                newInpDir = nextClockwiseDirection(currentInpDir);
                // Если вход совпал со выходом, поворачиваем вход еще раз
                if (newInpDir == newOutDir) newInpDir = nextClockwiseDirection(newInpDir);
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
        boolean neighbor1IsBuffer = neighbor1 instanceof Buffer;
        Block neighbor2Output = production.getBlockAt(neighbor2, neighbor2.getOutputDirection());
        Block neighbor2Input = production.getBlockAt(neighbor2, neighbor2.getInputDirection());
        boolean neighbor2IsBuffer = neighbor2 instanceof Buffer;

        // если ни один вход/выход двух соседей не направлен на это блок
        // поворачиваем как будто соседей нет
        if (neighbor1Input!=this && neighbor1Output!=this
        && neighbor2Input!=this && neighbor2Output!=this && !neighbor1IsBuffer && !neighbor2IsBuffer) {
            rotateFlowDirectionRightAngle();
            return;
        }

        // Если только первого блока вход/выход направлен на этот блок
        // поворачиваем как будто у нас один сосед
        if ((neighbor1Input==this || neighbor1Output==this || neighbor1IsBuffer)
                && neighbor2Input!=this && neighbor2Output!=this) {
            rotateFlowDirectionOneNeighbor(neighbor1, neightbor1Side);
            return;
        } else if (neighbor1Input!=this && neighbor1Output!=this && neighbor2IsBuffer) {
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
