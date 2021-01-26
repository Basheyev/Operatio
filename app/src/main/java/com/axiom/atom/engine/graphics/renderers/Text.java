package com.axiom.atom.engine.graphics.renderers;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.gles2d.Texture;
import com.axiom.atom.engine.graphics.gles2d.TextureAtlas;
import com.axiom.atom.engine.graphics.gles2d.TextureRegion;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Отрисовывает текст, на основе шрифта в виде спрайта
 * (C) Atom Engine, Bolat Basheyev 2020
 */
public class Text {

    protected static HashMap<String, RasterizedFont> fonts = new HashMap<>();
    protected static int fontSize = 24;     // Размер генерируемого шрифта в пикселях

    public static class RasterizedFont {
        protected int totalChars = 121;     // Количество генерируемых символов (11x11)
        protected Sprite fontSprite;        // Спрайт где каждый кадр это отдельный символ
        protected float[] xOffset;          // Горизонтальное смещение символа относительно курсора
        protected float[] yOffset;          // Вертикальное смещение символа относительно курсора
        protected float maxLineHeight;      // Высота строки этого шрифта
        protected float spacing;            // Расстояние между символами
    }

    public int zOrder = 0;
    protected RasterizedFont font;


    /**
     * Конструктор чтобы сформировать шрифт по названию
     * @param fontName название шрифта
     */
    public Text(String fontName) {
        this.font = rasterizeFont(fontName, fontSize);
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
        RasterizedFont fnt = new RasterizedFont();
        fnt.fontSprite = fontSprite;
        fnt.spacing = spacing;
        fnt.totalChars = fontSprite.getFramesAmount();
        fnt.xOffset = new float[fnt.totalChars];
        fnt.yOffset = new float[fnt.totalChars];
        fnt.maxLineHeight = getTextHeight("|", 1);
        this.font = fnt;
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
        font.fontSprite.zOrder = zOrder;
        // Начальные координаты
        float cursorX = x;
        float cursorY = y;

        char symbol, lastSymbol = 0;
        int symbolIndex;

        for (int i=0; i< text.length(); i++) {                // Для каждого символа в строке

            symbol = text.charAt(i);                          // Берём очередной символ в строке
            symbolIndex = symbol - ' ';                       // Вычисляем его индекс (кадра)

            if (symbol=='\n') {                               // Если это перенос строки,
                cursorY -= font.maxLineHeight * scale;        // Смещаемся по вертикали вниз
                // fixme зафиксировано выравнивание по левому краму
                cursorX = x;                                  // Переходим на начало строки
                lastSymbol = symbol;
                continue;                                     // Переходим к следующему символу
            }

            // Если по какой-то причине индекс больше количества символов, идём дальше
            if (symbolIndex < 0 || symbolIndex >= font.totalChars) continue;

            // Если это не первый символ в строке добавляем смещение курсора предыдущего символа
            if (i!=0 && lastSymbol!='\n') {
                cursorX += font.fontSprite.getWidth() * scale * font.spacing;
            }

            // Переходим на кадр нужного нам символа
            font.fontSprite.setActiveFrame(symbolIndex);

            // Считаем смещение символа относительно курсора по высоте
            float yPos = cursorY + (font.yOffset[symbolIndex] * scale);

            // Отрисовываем символ
            font.fontSprite.draw(camera, cursorX, yPos,
                    font.fontSprite.getWidth() * scale,
                    font.fontSprite.getHeight() * scale, scissor);

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
        ArrayList<TextureRegion> regions = font.fontSprite.getAtlas().getRegions();
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
            totalLineWidth += symbolWidth * scale * font.spacing;
        }
        if (totalLineWidth > maxLineWidth) maxLineWidth = totalLineWidth;
        return maxLineWidth;
    }

    public float getTextHeight(CharSequence text, float scale) {
        // Получаем все кадры (символы) спрайта
        ArrayList<TextureRegion> regions = font.fontSprite.getAtlas().getRegions();
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


    public void setColor(int rgba) {
        setColor(((rgba >> 24) & 0xff) / 255.0f,
                ((rgba >> 16) & 0xff) / 255.0f,
                ((rgba >>  8) & 0xff) / 255.0f,
                ((rgba      ) & 0xff) / 255.0f);
    }

    public void setColor(float r, float g, float b, float a) {
        font.fontSprite.setColor(r, g, b, a);
    }

    /**
     * Генерирует текстуру шрифта на базе указанного
     * @param fontName названией шрифта
     * @param size размер шрифта в пикселях
     * @return спрайт где каждому региону соответствует символ
     */
    protected RasterizedFont rasterizeFont(String fontName, int size) {

        // Если уже растеризовали этот шрифт то просто его возвращаем
        RasterizedFont rasterizedFont = fonts.get(fontName);
        if (rasterizedFont!=null) return rasterizedFont;

        // Если такого не делали, начинаем создавать растеризованный шрифт
        rasterizedFont = new RasterizedFont();
        rasterizedFont.xOffset = new float[rasterizedFont.totalChars];
        rasterizedFont.yOffset = new float[rasterizedFont.totalChars];

        // Чтобы генерируемая текстура была квадратная
        // считаем сколько символов по вертикали/горизонтали будет
        int matrixSize = (int) Math.ceil(Math.sqrt(rasterizedFont.totalChars));

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

        rasterizedFont.maxLineHeight = size;

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
                rasterizedFont.yOffset[symbolIndex] = -symbolRectangle.bottom;

                // Считаем высоту и ширину символа
                int width = x2 - x1;
                int height = y2 - y1;

                // Если ширина или высота нулевая - задаем ширину и высоту
                if (width==0) { x1 = x*size; width = size / 3;}
                if (height==0) { y1 = y*size; height = 1;}

                // Добавляем регион символа в атлас с именем символа
                textureAtlas.addRegion(symbolStr, x1, y1, width, height);

                // Переходим к следующему символу
                symbol++;
            }
        }

        // Дополняем информацию растеризованного шрифта
        rasterizedFont.fontSprite = new Sprite(Texture.getInstance(bitmap, true), textureAtlas);
        rasterizedFont.fontSprite.useColor = true;  // Флаг для шейдера закрашивающего цветом
        rasterizedFont.spacing = 1;

        // Добавляем в общий список растеризованых шрифтов
        fonts.put(fontName, rasterizedFont);

        // Возвращаем растеризованый шрифт
        return rasterizedFont;
    }


}
