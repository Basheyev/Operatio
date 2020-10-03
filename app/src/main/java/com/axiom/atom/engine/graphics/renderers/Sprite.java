package com.axiom.atom.engine.graphics.renderers;

import android.content.res.Resources;
import android.opengl.GLES20;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.gles2d.Program;
import com.axiom.atom.engine.graphics.gles2d.Shader;
import com.axiom.atom.engine.graphics.gles2d.Texture;
import com.axiom.atom.engine.graphics.gles2d.TextureAtlas;
import com.axiom.atom.engine.graphics.gles2d.TextureRegion;

import java.util.ArrayList;

/**
 * Sprite - это основной класс выполняющий работу с 2D изображениями.
 * Реализует такие функции как загрузка изображений в текстуру,
 * поддержка листов спрайтов, покадровая анимации спрайтов с,
 * разным FPS, быстрый рендеринг спрайта и анимации.
 * <br><br>
 * (С) Atom Engine, Bolat Basheyev 2020
 */
public class Sprite {

    protected static Program texturedProgram = null;       // Программа со скомпилированными шейдерами
    protected static Program coloredProgram = null;       // Программа со скомпилированными шейдерами

    protected Texture texture;                     // Текстура спрайта
    protected TextureAtlas atlas;                  // Атлас текстуры
    protected float[] color = {0,0,0,1};           // Прозрачность спрайта
    protected float rotation = 0;                  // Угол поворота в радианах
    public int zOrder = 0;                         // Порядок сортировки при отрисовке спрайта
    public boolean useColor = false;               // Использовать ли цвет при отрисовке

    protected int activeFrame = -1;                // Текущий активный кадр
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


    /**
     * Конструктор спрайта
     * @param texture текстура
     */
    public Sprite(Texture texture) {
        //----------------------------------------------------------------
        // Загружаем текстуру если она еще не была загружна
        //----------------------------------------------------------------
        this.texture = texture;
        //----------------------------------------------------------------
        // Компилируем шейдеры и линкуем программы, если её еще нет
        //----------------------------------------------------------------
        if (texturedProgram ==null) {
            texturedProgram = new Program(
                    new Shader(GLES20.GL_VERTEX_SHADER, vertexShaderCode),
                    new Shader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode));

            coloredProgram = new Program(
                    new Shader(GLES20.GL_VERTEX_SHADER, vertexShaderCode),
                    new Shader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderColoredCode));

        }
        // Если не указано, то используются текстурные координаты по умолчанию
    }


    /**
     * Конструктор спрайта
     * @param resources ресурсы приложения
     * @param resource ресурс изображение
     * @param columns количество столбцов в листе спрайтов
     * @param rows количество строк в листе спрайтов
     */
    public Sprite(Resources resources, int resource, int columns, int rows) {
        this(Texture.getInstance(resources, resource), columns, rows);
    }

    public Sprite(Resources resources, int resource) {
        this(resources,resource,1,1);
    }


    public Sprite(Texture texture, TextureAtlas atlas) {
        this(texture);
        this.atlas = atlas;
        setActiveFrame(0);
    }

    public Sprite(Texture texture, int columns, int rows) {
        this(texture);
        // Сгенерировать текстурный атлас по количеству столбцов и строк
        atlas = new TextureAtlas(texture, columns, rows);
        setActiveFrame(0);
    }


    /**
     * Отрисовывает спрайт с соответствующими параметрами с аппаратным ускорением
     * @param camera матрица с проекцией камеры
     * @param x положение центра спрайта по x
     * @param y положение центра спрайта по y
     * @param scale масштабирование размера спрайта, 1 - исходный
     * @param scissor область обрезки в физических экранных координатах
     */
    public void draw(Camera camera, float x, float y, float scale, AABB scissor) {
        float scaledWidth = getWidth() * scale;
        float scaledHeight = getHeight() * scale;
        float halfWidth = scaledWidth * 0.5f;
        float halfHeight = scaledHeight * 0.5f;
        //-------------------------------------------------------------------------------
        // Проверка на экране ли спрайт, если нет то не отрисовывать
        //-------------------------------------------------------------------------------
        if (camera.isVisible(x-halfWidth,y-halfHeight, x+halfWidth,y+halfHeight)) {
            initializeVertices();
            if (rotation!=0) evaluateRotation(rotation);
            evaluateScale(scaledWidth, scaledHeight);
            evaluateOffset(x, y);
            // Добавляем в список отрисовки
            Program program = useColor ? coloredProgram : texturedProgram;
            BatchRender.addTexturedQuad(program, texture, vertices, textureCoordinates, color, zOrder, scissor);
        }
        animationNextFrame();
    }

    public void draw(Camera camera, float x, float y, float scale) {
        draw(camera,x,y,scale,null);
    }

    /**
     * Отрисовывает спрайт с соответствующими параметрами с аппаратным ускорением
     * @param camera матрица с проекцией камеры
     * @param x положение центра спрайта по x
     * @param y положение центра спрайта по y
     * @param width ширина спрайта
     * @param height ширина спрайта
     */
    public void draw(Camera camera, float x, float y, float width, float height, AABB scissor) {
        //-------------------------------------------------------------------------------
        // Проверка на экране ли спрайт, если нет то не отрисовывать
        //-------------------------------------------------------------------------------
        if (camera.isVisible(x,y, x+width,y+height)) {
            //-------------------------------------------------------------------------------
            float sx = x + width * 0.5f;
            float sy = y + height * 0.5f;
            initializeVertices();
            if (rotation!=0) evaluateRotation(rotation);
            evaluateScale(width, height);
            evaluateOffset(sx, sy);
            Program program = useColor ? coloredProgram : texturedProgram;
            BatchRender.addTexturedQuad(program, texture, vertices, textureCoordinates, color, zOrder, scissor);
        }
        animationNextFrame();
    }

    public void draw(Camera camera, float x, float y, float width, float height) {
        draw(camera, x,y,width,height,null);
    }

    public void draw(Camera camera, AABB bounds, AABB scissors) {
        draw(camera, bounds.min.x, bounds.min.y, bounds.width, bounds.height,scissors);
    }

    /**
     * Инициализирует массив координат вершин
     */
    private void initializeVertices() {
        // Треугольник 1
        vertices[0] = -0.5f;
        vertices[1] = 0.5f;
        vertices[3] = -0.5f;
        vertices[4] = -0.5f;
        vertices[6] = 0.5f;
        vertices[7] = 0.5f;
        // Треугольник 2
        vertices[9] = -0.5f;
        vertices[10] = -0.5f;
        vertices[12] = 0.5f;
        vertices[13] = 0.5f;
        vertices[15] = 0.5f;
        vertices[16] = -0.5f;
    }


    /**
     * Рассчитать вершины с учетом размера спрайта
     * @param scaledWidth ширина спрайта
     * @param scaledHeight высота спрайта
     */
    private void evaluateScale(float scaledWidth, float scaledHeight) {
        for (int i=0; i<18; i+=3) {
            vertices[i] *= scaledWidth;
            vertices[i+1] *= scaledHeight;
        }
    }


    /**
     * Смещение координат вершин
     * @param x смещение
     * @param y смещение
     */
    private void evaluateOffset(float x, float y) {
        for (int i=0; i<18; i+=3) {
            vertices[i] += x;
            vertices[i+1] += y;
        }
    }


    /**
     * Поворот вершин на заданный угол в радианах
     * @param rotation угол поворота в радианах
     */
    private void evaluateRotation(float rotation) {
        float cosR = (float) Math.cos(rotation);
        float sinR = (float) Math.sin(rotation);
        float x, y;
        for (int i=0; i<18; i+=3) {
            x = vertices[i];
            y = vertices[i+1];
            vertices[i] = x * cosR - y * sinR;
            vertices[i+1] = x * sinR + y * cosR;
        }
    }

    /**
     * Возвращает ширину спрайта
     * @return ширина спрайта в пикселях
     */
    public float getWidth() {
        return atlas.getRegion(activeFrame).width;
    }

    /**
     * Возвращает высоту спрайта
     * @return высота спрайта в пикселях
     */
    public float getHeight() {
        return atlas.getRegion(activeFrame).height;
    }

    /**
     * Возвращает общее количество кадров в листе спрайтов
     * @return количество кадров в листе спрайтов
     */
    public int getFramesAmount() {
        return atlas.size();
    }

    //------------------------------------------------------------------------------------------
    // Управление цветом и прозрачностью
    //------------------------------------------------------------------------------------------
    public void setAlpha(float alpha) {
        if (alpha < 0) alpha = 0;
        if (alpha > 1) alpha = 1;
        color[3] = alpha;
    }

    public float getAlpha() {
        return color[3];
    }

    public void setColor(float r, float g, float b, float a) {
        color[0] = r;
        color[1] = g;
        color[2] = b;
        color[3] = a;
    }

    //------------------------------------------------------------------------------------------


    public void setRotation(float radians) {
        this.rotation = radians;
    }

    public float getRotation() {
        return rotation;
    }

    //----------------------------------------------------------------------------------------
    // Выбор активного кадра (вычисление текстурных координат на листе спрайтов)
    //----------------------------------------------------------------------------------------

    /**
     * Устанавливает активным указанный кадр и копирует из атласа текстурные координаты
     * @param frame номер кадра
     */
    public void setActiveFrame(int frame) {
        if (activeFrame==frame) return;
        if (frame < 0 || frame >= atlas.size()) return;
        activeFrame = frame;
        TextureRegion region = atlas.getRegion(activeFrame);
        System.arraycopy(region.textureCoordinates, 0, textureCoordinates, 0, 12);
        if (horizontalFlip) flipHorizontally();
        if (verticalFlip) flipVertically();
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
    // Горизонтальное / вертикальное отражение спрайта (отражение текстурных координат)
    //-------------------------------------------------------------------------------------------

    public void flipHorizontally(boolean flipped) {
        if (horizontalFlip != flipped) {
            flipHorizontally();
            horizontalFlip = flipped;
        }
    }

    public void flipVertically(boolean flipped) {
        if (verticalFlip != flipped) {
            flipVertically();
            verticalFlip = flipped;
        }
    }

    private void flipHorizontally() {
         // Поменять местами X координаты
        float tmp = textureCoordinates[0];                 // tmp = x1
        textureCoordinates[0] = textureCoordinates[4];     // x1 = x2
        textureCoordinates[4] = tmp;                       // x2 = tmp
        tmp = textureCoordinates[2];                       // tmp = x1
        textureCoordinates[2] = textureCoordinates[10];    // x1 = x2
        textureCoordinates[10] = tmp;                      // x2 = tmp
        textureCoordinates[6] = textureCoordinates[2];     // копируем в данные во второй
        textureCoordinates[8] = textureCoordinates[4];     // трегуольник с общим ребром
    }

    private void flipVertically() {
         // Поменять местами Y координаты
        float tmp = textureCoordinates[1];                 // tmp = y1
        textureCoordinates[1] = textureCoordinates[3];     // y1 = y2
        textureCoordinates[3] = tmp;                       // y2 = tmp
        tmp = textureCoordinates[5];                       // tmp = y1
        textureCoordinates[5] = textureCoordinates[11];    // y1 = y2
        textureCoordinates[11] = tmp;                      // y2 = tmp
        textureCoordinates[7] = textureCoordinates[3];     // копируем в данные во второй
        textureCoordinates[9] = textureCoordinates[5];     // трегуольник с общим ребром
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
        if (timeInterval < (1_000_000_000 / anim.framesPerSecond)) return;

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
     */
    public void setActiveAnimation(int index) {
        if (index<0 || index >= animations.size()) return;
        activeAnimation = index;
        timesPlayed = 0;
        setActiveFrame(animations.get(index).startFrame);
    }

    /**
     * Возвращает активную анимацию
     * @return индекс активной анимации
     */
    public int getActiveAnimation() {
        return activeAnimation;
    }

    /**
     * Возвращает количество исполненных повторений анимации
     * @return количество повторений анимации
     */
    public int getTimesPlayed() {
        return timesPlayed;
    }


    public TextureAtlas getAtlas() {
        return atlas;
    }

    //-----------------------------------------------------------------------------------
    // Координаты вершины прямоугольника для отрисовки спрайта
    //-----------------------------------------------------------------------------------
    private static float[] vertices =                 // прямоугольника спрайта
            {      -0.5f,  0.5f,  0.0f,               // левый верхний угол
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
    // Код пиксельного шейдера спрайта отрисовывающего текстуру
    //-----------------------------------------------------------------------------------
    private final String fragmentShaderCode =
            "precision mediump float; " +
                    "uniform vec4 vColor;" +
                    "uniform sampler2D TexCoordIn; " +
                    "varying vec2 TexCoordOut;" +
                    "void main() {" +
                    "  vec4 col = texture2D(TexCoordIn, TexCoordOut);" +
                    "  col.a *= vColor.a;" +
                    "  gl_FragColor = col; " +
                    "}";

    //-----------------------------------------------------------------------------------
    // Код пиксельного шейдера спрайта отрисовывающего текстуру закрашенную цветом
    //-----------------------------------------------------------------------------------
    private final String fragmentShaderColoredCode =
            "precision mediump float; " +
                    "uniform vec4 vColor;" +
                    "uniform sampler2D TexCoordIn; " +
                    "varying vec2 TexCoordOut;" +
                    "void main() {" +
                    "  vec4 col = texture2D(TexCoordIn, TexCoordOut);" +
                    "  col.r = vColor.r;" +
                    "  col.g = vColor.g;" +
                    "  col.b = vColor.b;" +
                    "  col.a *= vColor.a;" +
                    "  gl_FragColor = col; " +
                    "}";

}
