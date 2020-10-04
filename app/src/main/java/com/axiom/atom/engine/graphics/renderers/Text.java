package com.axiom.atom.engine.graphics.renderers;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.gles2d.Texture;
import com.axiom.atom.engine.graphics.gles2d.TextureAtlas;
import com.axiom.atom.engine.graphics.gles2d.TextureRegion;

import java.util.ArrayList;

/**
 * Отрисовывает текст, на основе шрифта в виде спрайта
 * (C) Atom Engine, Bolat Basheyev 2020
 */
public class Text {

    public int zOrder = 0;
    protected Sprite fontSprite;           // Спрайт где каждый кадр это отдельный символ
    protected int totalChars = 121;        // Количество генерируемых символов
    protected int fontSize = 24;           // Размер генерируемого шрифта в пикселях
    protected float[] xOffset;             // Горизонтальное смещение символа относительно курсора
    protected float[] yOffset;             // Вертикальное смещение символа относительно курсора
    protected float maxLineHeight;         // Высота строки этого шрифта
    protected float spacing;               // Расстояние между символами

    /**
     * Закрытый конструктор чтобы создать массивы смещений
     */
    private Text() {
        xOffset = new float[totalChars];
        yOffset = new float[totalChars];
    }

    /**
     * Конструктор чтобы сформировать шрифт по названию
     * @param fontName название шрифта
     */
    public Text(String fontName) {
        this();
        this.fontSprite = generateFontSprite(fontName, fontSize);
        this.fontSprite.useColor = true;
        this.spacing = 1;
    }

    /**
     * Конструктор чтобы сформировать шрифт из спрайта
     * @param fontSprite спрайт который содержит шрифт
     */
    public Text(Sprite fontSprite) {
        this(fontSprite, 1);
    }

    /**
     * Конструктор чтобы сформировать шрифт из спрайта с отсутпами
     * @param fontSprite спрайт который содержит шрифт
     * @param spacing множитель отступа
     */
    public Text(Sprite fontSprite, float spacing) {
        this();
        this.fontSprite = fontSprite;
        this.spacing = spacing;
        this.totalChars = fontSprite.getFramesAmount();
        this.maxLineHeight = getTextHeight("|", 1);
    }

    /**
     * Отрисовывает текст без экранной области отсечения
     * @param camera камера для получения матрицы отрисовки спрайта
     * @param text последовательность символов (String, StringBuffer и т.д.)
     * @param x горизонтальная координата от которой нужно отрисовать текст
     * @param y вертикальная координата от которой нужно отрисовать текст
     * @param scale множитель масштабирования (1 - если использовать собственные размеры)
     */
    public void draw(Camera camera, CharSequence text, float x, float y, float scale) {
        draw(camera,text,x,y,scale, null);
    }

    /**
     * Отрисовывает текст с экранной областью отсечения
     * @param camera камера для получения матрицы отрисовки спрайта
     * @param text последовательность символов (String, StringBuffer и т.д.)
     * @param x горизонтальная координата от которой нужно отрисовать текст
     * @param y вертикальная координата от которой нужно отрисовать текст
     * @param scale множитель масштабирования (1 - если использовать собственные размеры)
     * @param scissor прямоугольник экранной области отсечения в физических координатах экрана
     */
    public void draw(Camera camera, CharSequence text, float x, float y, float scale, AABB scissor) {
        // Указываем спрайту z-слой на котором рисуем
        fontSprite.zOrder = zOrder;
        // Начальные координаты
        float cursorX = x;
        float cursorY = y;

        char symbol, lastSymbol = 0;
        int symbolIndex;

        for (int i=0; i< text.length(); i++) {                // Для каждого символа в строке

            symbol = text.charAt(i);                          // Берём очередной символ в строке
            symbolIndex = symbol - ' ';                       // Вычисляем его индекс (кадра)

            if (symbol=='\n') {                               // Если это перенос строки,
                // Тут нужна правка по высоте
                cursorY -= maxLineHeight * scale;             // Смещаемся по вертикали вниз
                cursorX = x;                                  // Переходим на начало строки
                lastSymbol = symbol;
                continue;                                     // Переходим к следующему символу
            }

            // Если по какой-то причине индекс больше количества символов, идём дальше
            if (symbolIndex < 0 || symbolIndex >= totalChars) continue;

            // Если это не первый символ в строке добавляем смещение курсора предыдущего символа
            if (i!=0 && lastSymbol!='\n') {
                cursorX += fontSprite.getWidth() * scale * spacing;
            }

            fontSprite.setActiveFrame(symbolIndex);

            float yPos = cursorY + (yOffset[symbolIndex] * scale);

            fontSprite.draw(camera, cursorX, yPos,
                    fontSprite.getWidth() * scale,
                    fontSprite.getHeight() * scale, scissor);

            lastSymbol = symbol;
        }
    }


    /**
     * Считает ширину текста с учетом параметров шрифта
     * @param text последовательность символов
     * @param scale масштаб
     * @return ширина текста
     */
    public float getTextWidth(CharSequence text, float scale) {
        // Получаем все кадры (символы) спрайта
        ArrayList<TextureRegion> regions = fontSprite.getAtlas().getRegions();
        char symbol;
        int symbolIndex;
        int symbolWidth;
        float maxLineWidth = 0;
        float totalLineWidth = 0;
        for (int i=0; i<text.length(); i++) {
            symbol = text.charAt(i);
            if (symbol=='\n') {
                if (totalLineWidth > maxLineWidth) maxLineWidth = totalLineWidth;
                totalLineWidth = 0;
                continue;
            }
            symbolIndex = symbol - ' ';
            symbolWidth = (symbolIndex >= 0 && symbolIndex < regions.size()) ? regions.get(symbolIndex).width : 0;
            totalLineWidth += symbolWidth * scale * spacing;
        }
        if (totalLineWidth > maxLineWidth) maxLineWidth = totalLineWidth;
        return maxLineWidth;
    }

    public float getTextHeight(CharSequence text, float scale) {
        // Получаем все кадры (символы) спрайта
        ArrayList<TextureRegion> regions = fontSprite.getAtlas().getRegions();
        int symbolIndex;
        int symbolHeight;
        int maxHeight = 0;
        for (int i=0; i<text.length(); i++) {
            symbolIndex = text.charAt(i) - ' ';
            symbolHeight = (symbolIndex > 0 && symbolIndex < regions.size()) ? regions.get(symbolIndex).height : 0;
            if (symbolHeight > maxHeight) maxHeight = symbolHeight;
        }
        return maxHeight * scale;
    }



    public void setColor(float r, float g, float b, float a) {
        fontSprite.setColor(r, g, b, a);
    }

    /**
     * Генерирует текстуру шрифта на базе указанного
     * @param fontName названией шрифта
     * @param size размер шрифта в пикселях
     * @return спрайт где каждому региону соответствует символ
     */
    protected Sprite generateFontSprite(String fontName, int size) {
        // Чтобы генерируемая текстура была квадратная
        // считаем сколько символов по вертикали/горизонтали будет
        int matrixSize = (int) Math.ceil(Math.sqrt(totalChars));

        // Создаем соответствующего размера Bitmap с прозрачностью
        Bitmap bitmap = Bitmap.createBitmap(
                matrixSize*size,
                matrixSize*size,
                Bitmap.Config.ARGB_8888);

        // Создаём соответствующего размера текстурный атлас
        TextureAtlas textureAtlas = new TextureAtlas(
                matrixSize*size,
                matrixSize*size);

        // Берем канвас, чтобы мы могли рисовать символы
        Canvas canvas = new Canvas(bitmap);

        // Создаем шрифт которым будем рисовать
        Typeface font = Typeface.create(fontName, Typeface.NORMAL);

        // Создаем стиль, которым будем рисовать
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setTypeface(font);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(size);
        paint.setTextAlign(Paint.Align.LEFT);

        char symbol = ' ';
        int symbolIndex;
        Rect symbolRectangle = new Rect();

        maxLineHeight = size;

        for (int y=1; y<matrixSize-1; y++) {
            for (int x=0; x<matrixSize; x++) {

                // Конвертируем символ в строку
                symbolIndex = symbol - ' ';
                String symbolStr = "" + symbol;

                // Отрисовываем строка на bitmap
                canvas.drawText(symbolStr, 1 + x*size, 1 + y*size, paint);

                // Получаем размеры символа
                paint.getTextBounds(symbolStr,0,1, symbolRectangle);

                // Рассчитываем координаты символа на bitmap
                int x1 = 1 + x * size + symbolRectangle.left;
                int y1 = 1 + y * size + symbolRectangle.top;
                int x2 = 1 + x * size + symbolRectangle.right;
                int y2 = 1 + y * size + symbolRectangle.bottom;

                // Dычисляем смещение символа по вертикали
                yOffset[symbolIndex] = -symbolRectangle.bottom;

                // Считаем высоту и ширину символа
                int width = x2 - x1;
                int height = y2 - y1;

                // Если ширина или высота нулевая - задаем ширину и высоту
                if (width==0) { x1 = x*size; width = size / 2;}
                if (height==0) { y1 = y*size; height = 1;}

                // Добавляем регион символа в атлас с именем символа
                textureAtlas.addRegion(symbolStr, x1, y1, width, height);

                // Переходим к следующему символу
                symbol++;
            }
        }

        return new Sprite(Texture.getInstance(bitmap, true), textureAtlas);
    }


}
