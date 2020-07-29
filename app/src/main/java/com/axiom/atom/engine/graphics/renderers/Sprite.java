package com.axiom.atom.engine.graphics.renderers;

import android.content.res.Resources;
import android.opengl.GLES20;

import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.gles2d.Program;
import com.axiom.atom.engine.graphics.gles2d.Shader;
import com.axiom.atom.engine.graphics.gles2d.Texture;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Sprite - это основной класс выполняющий работу с 2D изображениями.
 * Реализует такие функции как загрузка изображений в текстуру,
 * поддержка листов спрайтов, покадровая анимации спрайтов с,
 * разным FPS, быстрый рендеринг спрайта и анимации.
 *
 * <br><br>
 * (С) Atom Engine, Bolat Basheyev 2020
 */
public class Sprite {

    // Список всех загруженных текстур, для исключения повторной загрузки одной текстуры
    protected static HashMap<Integer, Texture> loadedTextures = new HashMap<>();

    public static Program program = null;          // Программа со скомпилированными шейдерами
    protected Texture texture;                     // Текстура спрайта
    public int zOrder = 0;                         // Порядок сортировки при отрисовке спрайта

    protected int activeFrame = -1;                // Текущий активный кадр
    protected int columns, rows;                   // Количество столбцов и строк в листе спрайтов
    protected float spriteWidth;                   // Ширина спрайта в текстурных координатах (0-1)
    protected float spriteHeight;                  // Высота спрайта в текстурных координатах (0-1)
    protected boolean horizontalFlip = false;      // Горизонтальное отражение спрайта
    protected boolean verticalFlip = false;        // Вертикальное отражение спрайта

    protected ArrayList<Animation> animations = null;  // Список анимаций спрайта
    protected int activeAnimation = -1;            // Текущая активная анимация
    protected int timesPlayed = 0;                 // Сколько раз проиграна текущая анимация
    protected long lastFrameTime = 0;              // Время отрисовки последнего кадра

    //-----------------------------------------------------------------------------------
    // Координаты вершины прямоугольника для отрисовки спрайта
    //-----------------------------------------------------------------------------------
    public static class Animation {
        int startFrame;                     // Начальный кадр
        int stopFrame;                      // Конечный кадр
        int framesPerSecond;                // Количество кадров в секунду
        boolean loop;                       // Зациклена ли анимация (должна повторятся)
    }

    //-----------------------------------------------------------------------------------
    // Координаты вершины прямоугольника для отрисовки спрайта
    //-----------------------------------------------------------------------------------
    private static float[] vertices =                 // прямоугольника спрайта
                 { -0.5f,  0.5f,  0.0f,               // левый верхний угол
                   -0.5f, -0.5f,  0.0f,               // левый нижний угол
                    0.5f,  0.5f,  0.0f,               // правый верхний угол

                   -0.5f, -0.5f,  0.0f,               // левый нижний угол
                    0.5f,  0.5f,  0.0f,               // правый верхний угол
                    0.5f, -0.5f,  0.0f,               // правый нижний угол
                 };

    //-----------------------------------------------------------------------------------
    // Текстурные координаты для отрисовки спрайта (по умолчанию)
    //-----------------------------------------------------------------------------------
    private float[] textureCoordinates =              // текстуры спрайта по умолчанию
            {
                    0.0f, 1.0f,                       // левый верхний угол
                    0.0f, 0.0f,                       // левый нижний угол
                    1.0f, 1.0f,                       // правый верхний угол

                    0.0f, 0.0f,                       // левый нижний угол
                    1.0f, 1.0f,                       // правый верхний угол
                    1.0f, 0.0f                        // правый нижний угол
            };

    //-----------------------------------------------------------------------------------
    // Код вершинного шейдера спрайта
    //-----------------------------------------------------------------------------------
    private final String vertexShaderCode =
                    "uniform mat4 u_MVPMatrix; " +
                    "attribute vec4 vPosition;" +
                    "attribute vec2 TexCoordIn;" +
                    "varying vec2 TexCoordOut;" +
                    "void main() { " +
                    "    gl_Position = u_MVPMatrix * vPosition; " +
                    "    TexCoordOut = TexCoordIn; " +
                    "}";

    //-----------------------------------------------------------------------------------
    // Код пиксельного шейдера спрайта
    //-----------------------------------------------------------------------------------
    private final String fragmentShaderCode =
                    "precision mediump float; " +
                    "uniform sampler2D TexCoordIn; " +
                    "varying vec2 TexCoordOut;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D(TexCoordIn, TexCoordOut);" +
                    "}";

    public Sprite(Resources resources, int resource) {
        this(resources,resource,1,1);
    }

    /**
     * Конструктор спрайта
     * @param resources ресурсы приложения
     * @param resource ресурс изображение
     * @param columns количество столбцов в листе спрайтов
     * @param rows количество строк в листе спрайтов
     */
    public Sprite(Resources resources, int resource, int columns, int rows) {
        //----------------------------------------------------------------
        // Загружаем текстуру если она еще не была загружна
        //----------------------------------------------------------------
        texture = loadedTextures.get(resource);
        if (texture==null) {
            // Текстуру загружаем только один раз
            texture = new Texture(resources, resource);
            loadedTextures.put(resource,texture);
        }
        //----------------------------------------------------------------
        // Компилируем шейдеры и линкуем программы, если её еще нет
        //----------------------------------------------------------------
        if (program==null) {
            program = new Program(
                    new Shader(GLES20.GL_VERTEX_SHADER, vertexShaderCode),
                    new Shader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode));
        }

        this.columns = columns;
        this.rows = rows;

        spriteWidth = 1.0f / columns;  // Ширина спрайта в текстурных координатах
        spriteHeight = 1.0f / rows;    // Высоата спрайта в текстурных координатах

        setActiveFrame(0);
    }

    /**
     * Отрисовывает спрайт с соответствующими параметрами с аппаратным ускорением
     * @param camera матрица с проекцией камеры
     * @param x положение центра спрайта по x
     * @param y положение центра спрайта по y
     * @param scale масштабирование размера спрайта, 1 - исходный
     */
    public void draw(Camera camera, float x, float y, float scale) {
        //-------------------------------------------------------------------------------
        // Проверка на экране ли спрайт, если нет то не отрисовывать
        //-------------------------------------------------------------------------------
        float scaledWidth = getWidth() * scale;
        float scaledHeight = getHeight() * scale;
        float halfWidth = scaledWidth * 0.5f;
        float halfHeight = scaledHeight * 0.5f;
        if (camera.isVisible(x-halfWidth,y-halfHeight, x+halfWidth,y+halfHeight)) {
            // Треугольник 1
            vertices[0] = -0.5f * scaledWidth + x;
            vertices[1] = 0.5f * scaledHeight + y;
            vertices[3] = -0.5f * scaledWidth + x;
            vertices[4] = -0.5f * scaledHeight + y;
            vertices[6] = 0.5f * scaledWidth + x;
            vertices[7] = 0.5f * scaledHeight + y;
            // Треугольник 2
            vertices[9] = -0.5f * scaledWidth + x;
            vertices[10] = -0.5f * scaledHeight + y;
            vertices[12] = 0.5f * scaledWidth + x;
            vertices[13] = 0.5f * scaledHeight + y;
            vertices[15] = 0.5f * scaledWidth + x;
            vertices[16] = -0.5f * scaledHeight + y;
            // Добавляем в список отрисовки
            Batcher.addSprite(texture, vertices, textureCoordinates, zOrder);
        }
        animationNextFrame();
    }

    /**
     * Отрисовывает спрайт с соответствующими параметрами с аппаратным ускорением
     * @param camera матрица с проекцией камеры
     * @param x положение центра спрайта по x
     * @param y положение центра спрайта по y
     * @param width ширина спрайта
     * @param height ширина спрайта
     */
    public void draw(Camera camera, float x, float y, float width, float height) {
        //-------------------------------------------------------------------------------
        // Проверка на экране ли спрайт, если нет то не отрисовывать
        //-------------------------------------------------------------------------------
        if (camera.isVisible(x,y, x+width,y+height)) {
            //-------------------------------------------------------------------------------
            float sx = x + width * 0.5f;
            float sy = y + height * 0.5f;
            // Triangle 1
            vertices[0] = -0.5f * width + sx;
            vertices[1] = 0.5f * height + sy;
            vertices[3] = -0.5f * width + sx;
            vertices[4] = -0.5f * height + sy;
            vertices[6] = 0.5f * width + sx;
            vertices[7] = 0.5f * height + sy;
            // Triangle 2
            vertices[9] = -0.5f * width + sx;
            vertices[10] = -0.5f * height + sy;
            vertices[12] = 0.5f * width + sx;
            vertices[13] = 0.5f * height + sy;
            vertices[15] = 0.5f * width + sx;
            vertices[16] = -0.5f * height + sy;

            Batcher.addSprite(texture, vertices, textureCoordinates, zOrder);

        }
        animationNextFrame();
    }

    /**
     * Возвращает ширину спрайта
     * @return ширина спрайта в пикселях
     */
    public float getWidth() {
        return texture.getWidth() * spriteWidth;
    }

    /**
     * Возвращает высоту спрайта
     * @return высота спрайта в пикселях
     */
    public float getHeight() {
        return texture.getHeight() * spriteHeight;
    }

    /**
     * Возвращает общее количество кадров в листе спрайтов
     * @return количество кадров в листе спрайтов
     */
    public int getFramesAmount() {
        return columns * rows;
    }

    //----------------------------------------------------------------------------------------
    // Выбор активного кадра (вычисление текстурных координат на листе спрайтов)
    //----------------------------------------------------------------------------------------

    /**
     * Устанавливает активным указанный кадр и пересчитывает текстурные координаты
     * @param frame номер кадра
     */
    public void setActiveFrame(int frame) {
        if (activeFrame==frame) return;
        if (frame < 0 || frame >= columns * rows) return;
        activeFrame = frame;
        // Пересчет номера кадра в номер столбца и строки
        int col = activeFrame % columns;
        // Учитываем что строки считаем сверху, а текстурные координаты снизу
        int row = (rows - 1) - activeFrame / columns;
        //--------------------------------------------------
        // текстурные координаты кадра спрайта
        //--------------------------------------------------

        // ===== Треугольник 1
        // левый верхний угол
        textureCoordinates[0] = col * spriteWidth;
        textureCoordinates[1] = (row + 1) * spriteHeight;
        // левый нижний угол
        textureCoordinates[2] = col * spriteWidth;
        textureCoordinates[3] = row * spriteHeight;
        // правый верхний угол
        textureCoordinates[4] = (col + 1) * spriteWidth;
        textureCoordinates[5] = (row + 1) * spriteHeight;

        // ===== Треугольник 2
        // левый нижний угол
        textureCoordinates[6] = textureCoordinates[2];
        textureCoordinates[7] = textureCoordinates[3];
        // правый верхний угол
        textureCoordinates[8] = textureCoordinates[4];
        textureCoordinates[9] = textureCoordinates[5];
        // правый нижний угол
        textureCoordinates[10] = (col + 1) * spriteWidth;
        textureCoordinates[11] = row * spriteHeight;
        //--------------------------------------------------
        // Пересчитать текстурные координаты если есть
        // горизонтальное или вертикальное отражение
        //--------------------------------------------------
        float tmp;
        if (horizontalFlip) {                                  // Поменять местами X координаты
            tmp = textureCoordinates[0];                       // tmp = x1
            textureCoordinates[0] = textureCoordinates[4];     // x1 = x2
            textureCoordinates[4] = tmp;                       // x2 = tmp
            tmp = textureCoordinates[2];                       // tmp = x1
            textureCoordinates[2] = textureCoordinates[10];    // x1 = x2
            textureCoordinates[10] = tmp;                      // x2 = tmp
            textureCoordinates[6] = textureCoordinates[2];     // копируем в данные во второй
            textureCoordinates[8] = textureCoordinates[4];     // трегуольник с общим ребром
        }
        if (verticalFlip) {                                    // Поменять местами Y координаты
            tmp = textureCoordinates[1];                       // tmp = y1
            textureCoordinates[1] = textureCoordinates[3];     // y1 = y2
            textureCoordinates[3] = tmp;                       // y2 = tmp
            tmp = textureCoordinates[5];                       // tmp = y1
            textureCoordinates[5] = textureCoordinates[11];    // y1 = y2
            textureCoordinates[11] = tmp;                      // y2 = tmp
            textureCoordinates[7] = textureCoordinates[3];     // копируем в данные во второй
            textureCoordinates[9] = textureCoordinates[5];     // трегуольник с общим ребром
        }

        lastFrameTime = System.nanoTime();
    }

    /**
     * Возвращает индекс текущего активного кадра
     * @return индекс текущего активного кадра
     */
    public int getActiveFrame() {
        return activeFrame;
    }

    //-------------------------------------------------------------------------------------------
    // Горизонтальное / вертикальное отражение спрайта
    //-------------------------------------------------------------------------------------------

    public void flipHorizontally(boolean flipped) {
        if (horizontalFlip != flipped) {                       // Поменять местами X координаты
            float tmp = textureCoordinates[0];                 // tmp = x1
            textureCoordinates[0] = textureCoordinates[4];     // x1 = x2
            textureCoordinates[4] = tmp;                       // x2 = tmp
            tmp = textureCoordinates[2];                       // tmp = x1
            textureCoordinates[2] = textureCoordinates[10];    // x1 = x2
            textureCoordinates[10] = tmp;                      // x2 = tmp
            textureCoordinates[6] = textureCoordinates[2];     // копируем в данные во второй
            textureCoordinates[8] = textureCoordinates[4];     // трегуольник с общим ребром
            horizontalFlip = flipped;
        }
    }

    public void flipVertically(boolean flipped) {
        if (verticalFlip != flipped) {                         // Поменять местами Y координаты
            float tmp = textureCoordinates[1];                 // tmp = y1
            textureCoordinates[1] = textureCoordinates[3];     // y1 = y2
            textureCoordinates[3] = tmp;                       // y2 = tmp
            tmp = textureCoordinates[5];                       // tmp = y1
            textureCoordinates[5] = textureCoordinates[11];    // y1 = y2
            textureCoordinates[11] = tmp;                      // y2 = tmp
            textureCoordinates[7] = textureCoordinates[3];     // копируем в данные во второй
            textureCoordinates[9] = textureCoordinates[5];     // трегуольник с общим ребром
            verticalFlip = flipped;
        }
    }


    //-------------------------------------------------------------------------------------------
    // Управление анимациями
    //-------------------------------------------------------------------------------------------

    /**
     * Сформировать анимацию по индексам кадров на базе загруженного листа спрайтов
     * @param startFrame - начальный кадр анимации
     * @param stopFrame - завершающий кадр анимации
     * @return индекс анимации или -1 если произошла ошибка
     */
    public int addAnimation(int startFrame, int stopFrame, int fps, boolean loop) {
        if (animations==null) animations = new ArrayList<>();
        int maxIndex = getFramesAmount() - 1;
        if ((startFrame > maxIndex) || (stopFrame > maxIndex) ||
            (startFrame<0) || (stopFrame<0) || (startFrame > stopFrame)) return -1;
        Animation animation = new Animation();
        animation.startFrame = startFrame;
        animation.stopFrame = stopFrame;
        animation.framesPerSecond = fps;
        animation.loop = loop;
        animations.add(animation);
        return animations.size() - 1;
    }

    /**
     * Переключается на следующий кадр активной анимации
     * с учётом прошедшего интервала времени и кадра
     */
    public void animationNextFrame() {
        if (animations==null || activeAnimation==-1) return;
        Animation anim = animations.get(activeAnimation);
        // Сколько времени прошло с переключения текущего кадра (в наносекундах)
        long timeInterval = System.nanoTime() - lastFrameTime;
        // Если не пришло время переключения кадра, то уходим
        if (timeInterval < (1000000000 / anim.framesPerSecond)) return;

        // Если пришло время следующего кадра, то переключаем
        int nextFrame = getActiveFrame() + 1;

        // Циклическое проигрывание анимации по флагу loop анимации
        if (nextFrame > anim.stopFrame) {
            if (anim.loop) {
                nextFrame = anim.startFrame;
                timesPlayed++;
            } else {
                nextFrame = anim.stopFrame;
                timesPlayed=1;
            }
        }
        setActiveFrame(nextFrame);
    }

    /**
     * Установить активную анимацию
     * @return
     */
    public void setActiveAnimation(int index) {
        if (index<0 || index >= animations.size()) return;
        activeAnimation = index;
        timesPlayed = 0;
        setActiveFrame(animations.get(index).startFrame);
    }

    /**
     * Возвращает активну анимацию
     * @return индекс активной анимации
     */
    public int getActiveAnimation() {
        return activeAnimation;
    }

    /**
     * Возвращает количество раз повторение анимации
     * @return количество повторений анимации
     */
    public int getTimesPlayed() {
        return timesPlayed;
    }



}
