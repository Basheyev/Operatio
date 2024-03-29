package com.basheyev.atom.engine.graphics.renderers;

import android.content.res.Resources;
import android.opengl.GLES20;

import com.basheyev.atom.engine.core.geometry.AABB;
import com.basheyev.atom.engine.graphics.gles2d.Camera;
import com.basheyev.atom.engine.graphics.gles2d.Program;
import com.basheyev.atom.engine.graphics.gles2d.Shader;
import com.basheyev.atom.engine.graphics.gles2d.Texture;
import com.basheyev.atom.engine.graphics.gles2d.TextureAtlas;
import com.basheyev.atom.engine.graphics.gles2d.TextureRegion;

import java.util.ArrayList;

/**
 * Sprite - это основной класс выполняющий работу с 2D изображениями.
 * Реализует такие функции как загрузка изображений в текстуру,
 * поддержка листов спрайтов и атласов, покадровая анимации спрайтов с,
 * разным FPS, быстрый рендеринг спрайта и анимации и многое другое.
 * <br><br>
 * (С) Atom Engine, Bolat Basheyev 2020-2022
 */
public class Sprite extends Quad {

    protected static Program texturedProgram = null;  // Программа со стандартным шейдером
    protected static Program coloredProgram = null;   // Программа со цветным шейдером

    protected TextureAtlas atlas;                     // Атлас текстуры
    public boolean useColor = false;                  // Использовать ли цвет при отрисовке

    protected int activeFrame = -1;                   // Текущий активный кадр
    protected float activeFrameWidth = 0;             // Ширина активного кадра
    protected float activeFrameHeight = 0;            // Высота активного кадра
    protected boolean horizontalFlip = false;         // Горизонтальное отражение спрайта
    protected boolean verticalFlip = false;           // Вертикальное отражение спрайта

    public boolean animationPaused = false;           // Анимация на паузе
    protected ArrayList<Animation> animations = null; // Список анимаций спрайта
    protected int activeAnimation = -1;               // Текущая активная анимация
    protected int timesPlayed = 0;                    // Сколько раз проиграна текущая анимация
    protected long lastFrameTime = 0;                 // Время отрисовки последнего кадра

    //-----------------------------------------------------------------------------------
    // Координаты вершины прямоугольника для отрисовки спрайта
    //-----------------------------------------------------------------------------------
    public static class Animation {
        public int startFrame;                     // Начальный кадр
        public int stopFrame;                      // Конечный кадр
        public float framesPerSecond;              // Количество кадров в секунду
        public float speed = 1;                    // Скорость проигрывания анимации
        public boolean loop;                       // Зациклена ли анимация (должна повторятся)
    }


    /**
     * Конструктор спрайта
     * @param texture текстура
     */
    private Sprite(Texture texture) {
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
     * Конструктор спрайта с автоматическим созданиям текстурного атласа на базе листа спрайтов
     * @param resources ресурсы приложения
     * @param resource ресурс изображения
     * @param columns количество столбцов в листе спрайтов
     * @param rows количество строк в листе спрайтов
     */
    public Sprite(Resources resources, int resource, int columns, int rows) {
        this(Texture.getInstance(resources, resource), columns, rows);
    }


    /**
     * Конструктор спрайта с автоматическим созданиям текстурного атласа на базе листа спрайтов
     * @param resources ресурсы приложения
     * @param resource ресурс изображения
     * @param columns количество столбцов в листе спрайтов
     * @param rows количество строк в листе спрайтов
     */
    public Sprite(Resources resources, int resource, int columns, int rows, boolean texelCenter) {
        this(Texture.getInstance(resources, resource), columns, rows, texelCenter);
    }

    /**
     * Конструктор спрайта
     * @param resources ресурсы приложения
     * @param resource ресурс изображения
     */
    public Sprite(Resources resources, int resource) {
        this(resources,resource,1,1);
    }


    /**
     * Конструктор спрайта на базе текстуры и текстурного атласа
     * @param texture уже созданная текстура
     * @param atlas уже созданный текстурный атлас
     */
    public Sprite(Texture texture, TextureAtlas atlas) {
        this(texture);
        this.atlas = atlas;
        setActiveFrame(0);
    }

    /**
     * Конструктор спрайта на базе текстуры с генерацией текстурного атласа
     * @param texture уже созданная текстура
     * @param columns количество столбцов в листе спрайтов
     * @param rows количество строк в листе спрайтов
     */
    public Sprite(Texture texture, int columns, int rows) {
        this(texture, columns, rows, false);
    }

    /**
     * Конструктор спрайта на базе текстуры с генерацией текстурного атласа
     * @param texture уже созданная текстура
     * @param columns количество столбцов в листе спрайтов
     * @param rows количество строк в листе спрайтов
     * @param texelCenter нарезать ли тайлы по центру текселя
     */
    public Sprite(Texture texture, int columns, int rows, boolean texelCenter) {
        this(texture);
        // Сгенерировать текстурный атлас по количеству столбцов и строк
        atlas = new TextureAtlas(texture, columns, rows, texelCenter);
        setActiveFrame(0);
    }


    /**
     * Отрисовывает спрайт с соответствующими параметрами
     * @param camera камера с матрицей проекции камеры
     * @param x положение центра спрайта по x
     * @param y положение центра спрайта по y
     * @param scale масштабирование размера спрайта (множитель), 1.0 - исходный
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
            if (rotation!=0 || centerX != 0 || centerY != 0) evaluateRotation(rotation);
            evaluateScale(scaledWidth, scaledHeight);
            evaluateOffset(x, y);
            // Добавляем в список отрисовки
            Program program = useColor ? coloredProgram : texturedProgram;
            BatchRender.addTexturedQuad(program, texture, vertices, texCoords, color, zOrder, scissor);
        }
        animationNextFrame();
    }

    /**
     * Отрисовывает спрайт с соответствующими параметрами
     * @param camera камера с матрицей проекции камеры
     * @param x положение центра спрайта по x
     * @param y положение центра спрайта по y
     * @param width ширина спрайта
     * @param height ширина спрайта
     * @param scissor область обрезки в физических экранных координатах
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
            if (rotation!=0 || centerX != 0 || centerY != 0) evaluateRotation(rotation);
            evaluateScale(width, height);
            evaluateOffset(sx, sy);
            Program program = useColor ? coloredProgram : texturedProgram;
            BatchRender.addTexturedQuad(program, texture, vertices, texCoords, color, zOrder, scissor);
        }
        animationNextFrame();
    }

    /**
     * Отрисовывает спрайт с соответствующими параметрами
     * @param camera камера с матрицей проекции камеры
     * @param x положение центра спрайта по x
     * @param y положение центра спрайта по y
     * @param scale  масштабирование размера спрайта (множитель), 1.0 - исходный
     */
    public void draw(Camera camera, float x, float y, float scale) {
        draw(camera,x,y,scale,null);
    }

    /**
     * Отрисовывает спрайт с соответствующими параметрами
     * @param camera камера с матрицей проекции камеры
     * @param x положение центра спрайта по x
     * @param y положение центра спрайта по y
     * @param width ширина спрайта
     * @param height ширина спрайта
     */
    public void draw(Camera camera, float x, float y, float width, float height) {
        draw(camera, x,y,width,height,null);
    }

    /**
     * Отрисовывает спрайт с соответствующими параметрами
     * @param camera камера с матрицей проекции камеры
     * @param bounds AABB прямоугольник
     * @param scissor область обрезки в физических экранных координатах
     */
    public void draw(Camera camera, AABB bounds, AABB scissor) {
        draw(camera, bounds.minX, bounds.minY, bounds.width, bounds.height,scissor);
    }


    /**
     * Отрисовывает спрайт с точно указанными вершинами прямоугольника, без поворота и обрезки
     * @param camera камера с матрицей проекции камеры
     * @param x1 левая координата
     * @param y1 нижняя координата
     * @param x2 правая координата
     * @param y2 верхняя координата
     * @param scissors область обрезки (null - если не нужно)
     */
    public void drawExact(Camera camera, float x1, float y1, float x2, float y2, AABB scissors) {
        //-------------------------------------------------------------------------------
        // Проверка на экране ли спрайт, если нет то не отрисовывать
        //-------------------------------------------------------------------------------
        if (camera.isVisible(x1,y1,x2,y2)) {
            //-------------------------------------------------------------------------------
            initializeVertices(x1, y1, x2, y2);
            Program program = useColor ? coloredProgram : texturedProgram;
            BatchRender.addTexturedQuad(program, texture, vertices, texCoords, color, zOrder, scissors);
        }
        animationNextFrame();
    }

    public void drawExact(Camera camera, float x1, float y1, float x2, float y2) {
        drawExact(camera, x1, y1, x2, y2, null);
    }
    //------------------------------------------------------------------------------------------


    /**
     * Возвращает ширину спрайта
     * @return ширина спрайта в пикселях
     */
    public float getWidth() {
        return activeFrameWidth;
    }

    /**
     * Возвращает высоту спрайта
     * @return высота спрайта в пикселях
     */
    public float getHeight() {
        return activeFrameHeight;
    }

    /**
     * Возвращает общее количество кадров в листе спрайтов
     * @return количество кадров в листе спрайтов
     */
    public int getFramesAmount() {
        return atlas.size();
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
        TextureRegion region = atlas.getRegion(frame);
        activeFrame = frame;
        activeFrameWidth = region.width;
        activeFrameHeight = region.height;
        System.arraycopy(region.texCoords, 0, texCoords, 0, 12);
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

    /**
     * Перевернут ли спрайт по горизонтали
     * @return true - перевернут, false - не перевернут
     */
    public boolean isFlippedHorizontally() {
        return horizontalFlip;
    }

    /**
     * Перевернут ли спрайт по вертикали
     * @return true - перевернут, false - не перевернут
     */
    public boolean isFlippedVertically() {
        return verticalFlip;
    }

    /**
     * Переворачивает изображение спрайта по горизонтали
     * @param flipped false - не перевернут, true - перевёрнут
     */
    public void flipHorizontally(boolean flipped) {
        if (horizontalFlip != flipped) {
            flipHorizontally();
            horizontalFlip = flipped;
        }
    }

    /**
     * Переворачивает изображение спрайта по вертикали
     * @param flipped false - не перевернут, true - перевёрнут
     */
    public void flipVertically(boolean flipped) {
        if (verticalFlip != flipped) {
            flipVertically();
            verticalFlip = flipped;
        }
    }

    /**
     * Переворачивает текстурные координаты по горизонтали
     */
    private void flipHorizontally() {
         // Поменять местами X координаты
        float tmp = texCoords[0];        // tmp = x1
        texCoords[0] = texCoords[4];     // x1 = x2
        texCoords[4] = tmp;              // x2 = tmp
        tmp = texCoords[2];              // tmp = x1
        texCoords[2] = texCoords[10];    // x1 = x2
        texCoords[10] = tmp;             // x2 = tmp
        texCoords[6] = texCoords[2];     // копируем в данные во второй
        texCoords[8] = texCoords[4];     // трегуольник с общим ребром
    }

    /**
     * Переворачивает текстурные координаты по вертикали
     */
    private void flipVertically() {
         // Поменять местами Y координаты
        float tmp = texCoords[1];        // tmp = y1
        texCoords[1] = texCoords[3];     // y1 = y2
        texCoords[3] = tmp;              // y2 = tmp
        tmp = texCoords[5];              // tmp = y1
        texCoords[5] = texCoords[11];    // y1 = y2
        texCoords[11] = tmp;             // y2 = tmp
        texCoords[7] = texCoords[3];     // копируем в данные во второй
        texCoords[9] = texCoords[5];     // трегуольник с общим ребром
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
    public int addAnimation(int startFrame, int stopFrame, float fps, boolean loop) {
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
        if (animations==null || activeAnimation==-1 || animationPaused) return;
        Animation anim = animations.get(activeAnimation);
        // Сколько времени прошло с переключения текущего кадра (в наносекундах)
        long timeInterval = System.nanoTime() - lastFrameTime;
        float frameDuration = 1_000_000_000 / (anim.framesPerSecond * anim.speed);
        // Если не пришло время переключения кадра, то уходим
        if (timeInterval < frameDuration) return;

        // Если пришло время следующего кадра, то переключаем
        int framesPassed = Math.round(timeInterval / frameDuration) % (anim.stopFrame - anim.startFrame + 1);
        int nextFrame = getActiveFrame() + framesPassed;

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
     * Возвращает данные анимации
     * @param index номер анимации
     * @return объект с данными анимации
     */
    public Animation getAnimation(int index) {
        if (index<0 || index >= animations.size()) return null;
        return animations.get(index);
    }

    /**
     * Возвращает количество исполненных повторений анимации
     * @return количество повторений анимации
     */
    public int getTimesPlayed() {
        return timesPlayed;
    }

    /**
     * Проверяет является ли кадр последним в анимации
     * @return true - если является, false - если не является
     */
    public boolean isAnimationLastFrame() {
        if (animations==null || activeAnimation==-1) return false;
        return activeFrame == animations.get(activeAnimation).stopFrame;
    }

    /**
     * Возвращает текстурный атлас для возможности его изменения
     * @return текстурный атлас
     */
    public TextureAtlas getAtlas() {
        return atlas;
    }


    /**
     * Возвращает текустуру спрайта
     * @return текстура спрайта
     */
    public Texture getTexture() {
        return texture;
    }


    /**
     * Сгенерировать новый спрайт на той же текстуре на основе указанного кадра (региона)
     * @param frame индекс региона (номер кадра)
     * @return новый спрайт с указанным регионом
     */
    public Sprite getAsSprite(int frame) {
        if (frame < 0 || frame > atlas.size()) return null;
        TextureAtlas newAtlas = new TextureAtlas(texture);
        TextureRegion region = atlas.getRegion(frame);
        newAtlas.addRegion(region.name, region.x, region.y, region.width, region.height);
        return new Sprite(texture, newAtlas);
    }

    /**
     * Сгенерировать новый спрайт на той же текстуре на основе диапазона кадров (регионов)
     * @param startFrame начальный индекс региона (номер кадра)
     * @param stopFrame конечный индекс региона (номер кадра)
     * @return новый спрайт с указанными регионами
     */
    public Sprite getAsSprite(int startFrame, int stopFrame) {
        int maxIndex = getFramesAmount() - 1;
        if ((startFrame > maxIndex) || (stopFrame > maxIndex) ||
            (startFrame < 0) || (stopFrame < 0) || (startFrame > stopFrame)) return null;
        TextureAtlas newAtlas = new TextureAtlas(texture);
        for (int i = startFrame; i <= stopFrame; i++) {
            TextureRegion region = atlas.getRegion(i);
            newAtlas.addRegion(region.name, region.x, region.y, region.width, region.height);
        }
        return new Sprite(texture, newAtlas);
    }


    public Sprite getAsSprite(int x, int y, int w, int h) {
        TextureAtlas newAtlas = new TextureAtlas(texture);
        String name = "ID" + System.currentTimeMillis();
        if (newAtlas.addRegion(name, x, y, w, h)==null) return null;
        return new Sprite(texture, newAtlas);
    }

    //-----------------------------------------------------------------------------------
    // Код вершинного шейдера спрайта
    //-----------------------------------------------------------------------------------
    private final String vertexShaderCode =
                    "uniform mat4 " + Program.MATRIX + "; " +
                    "attribute vec4 " + Program.VERTICES + "; " +
                    "attribute vec2 " + Program.TEXCOORDIN + ";" +
                    "varying vec2 TexCoordOut;" +
                    "void main() { " +
                    "    gl_Position = " + Program.MATRIX + " * " + Program.VERTICES + "; " +
                    "    TexCoordOut = " + Program.TEXCOORDIN + "; " +
                    "}";

    //-----------------------------------------------------------------------------------
    // Код пиксельного шейдера спрайта отрисовывающего текстуру
    //-----------------------------------------------------------------------------------
    private final String fragmentShaderCode =
                    "precision mediump float; " +
                    "uniform vec4 " + Program.COLOR + ";" +
                    "uniform sampler2D " + Program.TEXCOORDIN + "; " +
                    "varying vec2 TexCoordOut;" +
                    "void main() {" +
                    "  vec4 col = texture2D(" + Program.TEXCOORDIN + ", TexCoordOut);" +
                    "  col.a *= " + Program.COLOR + ".a;" +
                    "  gl_FragColor = col; " +
                    "}";

    //-----------------------------------------------------------------------------------
    // Код пиксельного шейдера спрайта отрисовывающего текстуру закрашенную цветом
    //-----------------------------------------------------------------------------------
    private final String fragmentShaderColoredCode =
                    "precision mediump float; " +
                    "uniform vec4 " + Program.COLOR + ";" +
                    "uniform sampler2D " + Program.TEXCOORDIN + "; " +
                    "varying vec2 TexCoordOut;" +
                    "void main() {" +
                    "  vec4 col = texture2D(" + Program.TEXCOORDIN + ", TexCoordOut);" +
                    "  col.r = " + Program.COLOR + ".r;" +
                    "  col.g = " + Program.COLOR + ".g;" +
                    "  col.b = " + Program.COLOR + ".b;" +
                    "  col.a *= " + Program.COLOR + ".a;" +
                    "  gl_FragColor = col; " +
                    "}";

}
