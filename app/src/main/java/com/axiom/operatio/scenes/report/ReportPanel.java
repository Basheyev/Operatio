package com.axiom.operatio.scenes.report;

import android.graphics.Color;
import android.util.Log;

import com.axiom.atom.R;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.graphics.renderers.Text;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.ProgressBar;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.ledger.Ledger;
import com.axiom.operatio.model.common.FormatUtils;
import com.axiom.operatio.model.ledger.LedgerPeriod;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.scenes.common.ItemWidget;

import static android.graphics.Color.GREEN;
import static android.graphics.Color.WHITE;

/**
 * Глваная панель сводной отчётности производства
 */
public class ReportPanel extends Panel {

    public static final float TARGET_VALUATION = 10_000_000f;
    public static final float TARGET_DIVIDENDS = 10_000_000f;

    public static final int ITEM_BACKGROUND = 0x80000000;

    private Production production;
    private Caption panelCaption;
    private Caption salesCaption;
    private Caption manufacturedCaption;
    private Caption purchaseCaption;
    private Caption reportCaption;
    private LineChart chart;
    private ItemWidget[] boughtMaterials;
    private ItemWidget[] manufacturedMaterials;
    private ItemWidget[] soldMaterials;
    private ProgressBar dividendsBar;
    private ProgressBar valuationBar;
    private ProgressBar technologyBar;
    private Button payDividends;
    private StringBuffer summary;

    private double[] revenueData = new double[Ledger.HISTORY_LENGTH];
    private double[] expensesData = new double[Ledger.HISTORY_LENGTH];

    private StringBuffer salesText;
    private StringBuffer purchaseText;
    private StringBuffer manufacturedText;

    private int paySound;


    public ReportPanel(Production production) {
        super();
        this.production = production;

        paySound = SoundRenderer.loadSound(R.raw.cash_snd);
        setLocalBounds(24,50, 1872, 880);
        setColor(0xCC505050);

        summary = new StringBuffer(512);
        salesText = new StringBuffer(32);
        purchaseText = new StringBuffer(32);
        manufacturedText = new StringBuffer(32);

        buildCaption("Dividends ($10M)", 25, 745, 300, 60, 1.3f, WHITE);
        dividendsBar = buildProgressBar(25, 685);
        buildCaption("Valuation ($10M)", 25, 620, 300, 60, 1.3f, WHITE);
        valuationBar = buildProgressBar(25, 565);
        buildCaption("Technology researched", 25, 490, 300, 60, 1.3f, WHITE);
        technologyBar = buildProgressBar(25, 435);

        chart = new LineChart(2);
        chart.setLocalBounds(350,435, 1100, 420);
        Ledger ledger = production.getLedger();
        updateChartData(ledger);
        addChild(chart);

        panelCaption = buildCaption("Game progress", 25, getHeight() - 100, 250, 100, 1.5f, WHITE);

        purchaseCaption = buildCaption("Purchase", 25, 10,300, 100, 1.3f, 0xFFFF7F7F);
        boughtMaterials = buildItemsGrid(25, 320, 280, 16);

        manufacturedCaption = buildCaption("Manufactured",345, 10,400, 100, 1.3f, WHITE);
        manufacturedMaterials = buildItemsGrid(345, 320, 840, 48);

        salesCaption = buildCaption("Sales", 1230, 10, 300, 100, 1.3f, GREEN);
        soldMaterials = buildItemsGrid(1230, 320, 650, 36);

        reportCaption = buildCaption("Summary", 1475, 520, 380, 330, 1.3f, WHITE);
        reportCaption.setVerticalAlignment(Text.ALIGN_TOP);

        payDividends = buildDividendsButton("Pay dividends - 25%", 1475, 440, 380, 70);

    }


    private Caption buildCaption(String text, float x, float y, float w, float h, float scale, int color) {
        Caption caption = new Caption(text);
        caption.setTextScale(scale);
        caption.setTextColor(color);
        caption.setLocalBounds(x, y, w, h);
        addChild(caption);
        return caption;
    }


    private ItemWidget[] buildItemsGrid(float posX, float posY, float width, int count) {
        int cellWidth = 70;
        ItemWidget[] itemGrid = new ItemWidget[count];
        float bx = posX, by = posY;
        for (int i=0; i<count; i++) {
            itemGrid[i] = buildItemWidget(bx, by);
            addChild(itemGrid[i]);
            bx += cellWidth;
            if (bx + cellWidth > (posX + width)) {
                bx = posX;
                by -= cellWidth;
            }
        }
        return itemGrid;
    }


    private ItemWidget buildItemWidget(float posX, float posY) {
        ItemWidget im = new ItemWidget("");
        im.setLocalBounds(posX, posY, 64, 64);
        im.setColor(ITEM_BACKGROUND);
        im.setTextColor(WHITE);
        im.setTextScale(0.9f);
        return im;
    }


    private ProgressBar buildProgressBar(float posX, float posY) {
        ProgressBar pb = new ProgressBar();
        pb.setLocalBounds(posX, posY, 300, 50);
        pb.setProgress(0);
        addChild(pb);
        return pb;
    }

    private Button buildDividendsButton(String caption, float x, float y, float width, float height) {
        Button button = new Button(caption);
        button.setTag(caption);
        button.setLocalBounds(x, y, width, height);
        button.setTextScale(1.3f);
        button.setTextColor(WHITE);
        button.setClickListener(payDividendsListener);
        addChild(button);
        return button;
    }

    /**
     * Обновляет данные
     */
    public void updateData() {
        Ledger ledger = production.getLedger();
        double salesSum = ledger.getLastPeriod().getRevenue();
        double manufactureCost = ledger.getLastPeriod().getMaintenanceCost();
        double purchaseSum = ledger.getLastPeriod().getExpenses() - manufactureCost;
        updateStringBuffer(salesText, "Sold today: ", salesSum);
        updateStringBuffer(manufacturedText, "Manufacturing costs today: ", manufactureCost);
        updateStringBuffer(purchaseText, "Purchased: ", purchaseSum);

        synchronized (this) {
            // Обновить график
            updateChartData(ledger);
            // Очистить все ячейки отображения материалов
            updateItemGridsData(ledger);
            // Обновить сводную информацию
            updateSummary(ledger);
            salesCaption.setText(salesText);
            manufacturedCaption.setText(manufacturedText);
            purchaseCaption.setText(purchaseText);
            reportCaption.setText(summary);
        }
    }


    /**
     * Обновить статистику по материалам (закуплено, произведено, продано)
     * @param ledger главная книга производства
     */
    private void updateItemGridsData(Ledger ledger) {

        // fixme какое количество пробегать?
        for (int i = 0; i < manufacturedMaterials.length; i++) {
            if (i < boughtMaterials.length) boughtMaterials[i].setActive(false);
            if (i < manufacturedMaterials.length) manufacturedMaterials[i].setActive(false);
            if (i < soldMaterials.length) soldMaterials[i].setActive(false);
        }

        int manufacturedCounter = 0;
        int soldCounter = 0;
        int boughtCounter = 0;
        for (int i = 0; i < Material.COUNT; i++) {
            // Вывести объем закупа
            int boughtAmount = ledger.getMaterialRecord(i).getBoughtAmountByPeriod();
            if (boughtAmount > 0 && boughtCounter < boughtMaterials.length) {
                updateItemWidget(boughtMaterials[boughtCounter], i, boughtAmount);
                boughtCounter++;
            }
            // Вывести продуктивность производства
            int manufacturedAmount = ledger.getMaterialRecord(i).getProductivity();
            if (manufacturedAmount > 0 && manufacturedCounter < manufacturedMaterials.length) {
                updateItemWidget(manufacturedMaterials[manufacturedCounter], i, manufacturedAmount);
                manufacturedCounter++;
            }
            // Вывести объем продаж
            int soldAmount = ledger.getMaterialRecord(i).getSoldAmountByPeriod();
            if (soldAmount > 0 && soldCounter < soldMaterials.length) {
                updateItemWidget(soldMaterials[soldCounter], i, soldAmount);
                soldCounter++;
            }
        }
    }


    /**
     * Обновление данных ItemWidget
     * @param item виджет
     * @param materialID код материала
     * @param amount количество материала
     */
    private void updateItemWidget(ItemWidget item, int materialID, int amount) {
        Material material = Material.getMaterial(materialID);
        if (material==null) return;
        Sprite sprite = material.getImage();
        item.setBackground(sprite);
        item.setText("" + amount);
        item.setActive(true);
    }


    /**
     * Обновление строки текста
     * @param sb буфер строки
     * @param txt заголовок
     * @param value значение
     */
    private void updateStringBuffer(StringBuffer sb, String txt, double value) {
        sb.setLength(0);
        sb.append(txt);
        FormatUtils.formatMoneyAppend(Math.round(value), sb);
    }


    /**
     * Загружает данные главной книги в линейный график
     * @param ledger главная книга производства
     */
    private void updateChartData(Ledger ledger) {
        LedgerPeriod[] history = ledger.getHistory();
        for (int i=0; i<ledger.getHistoryCounter(); i++) {
            revenueData[i] = history[i].getRevenue();
            expensesData[i] = history[i].getExpenses();
        }
        chart.updateData(0, revenueData, ledger.getHistoryCounter(), GREEN);
        chart.updateData(1, expensesData, ledger.getHistoryCounter(), Color.RED);
    }


    /**
     * Обновить сводную информацию
     * @param ledger главная книга производства
     */
    private void updateSummary(Ledger ledger) {
        double operRevenue = ledger.getLastPeriod().getRevenue();
        double operMargin = 0;
        double valuation = Math.round(ledger.getValuation());
        int valuationProgress = (int) Math.round(valuation / TARGET_VALUATION * 100.0f);
        float availableMaterials = production.getPermissions().availableMaterialsAmount();
        int technologyProgress = Math.round(availableMaterials / Material.getMaterialsAmount() * 100f);
        double averageMargin = ledger.getHistoryAverageMargin();
        double averageRevenue = ledger.getHistoryAverageRevenue();
        double averageMarginPercent = averageRevenue > 0 ? Math.round(averageMargin / averageRevenue * 100d) : 0;

        int dividendsProgress = (int) (ledger.getDividendsPayed() / TARGET_DIVIDENDS * 100.0F);

        dividendsBar.setProgress(dividendsProgress);
        valuationBar.setProgress(valuationProgress);
        technologyBar.setProgress(technologyProgress);

        if (operRevenue > 0) operMargin = Math.round(ledger.getLastPeriod().getMargin() / operRevenue * 100);
        summary.setLength(0);
        summary.append("Valuation: ");
        FormatUtils.formatMoneyAppend(valuation, summary);
        summary.append("\n\nDaily operations:\n");
        summary.append("- income: ");
        FormatUtils.formatMoneyAppend(Math.round(ledger.getLastPeriod().getRevenue()), summary);
        summary.append("\n- expenses: ");
        FormatUtils.formatMoneyAppend(Math.round(ledger.getLastPeriod().getExpenses()),summary);
        summary.append("\n- margin: ");
        FormatUtils.formatMoneyAppend(Math.round(ledger.getLastPeriod().getMargin()),summary);
        summary.append(" (");
        summary.append(operMargin);
        summary.append("%)\n");
        summary.append("\nAvg. margin: ");
        FormatUtils.formatMoneyAppend(Math.round(averageMargin), summary);
        summary.append(" (");
        summary.append(averageMarginPercent);
        summary.append("%)");
        summary.append("\nTotal assets: ");
        FormatUtils.formatMoneyAppend(Math.round(production.getAssetsValuation()), summary);
        double totalInventory = production.getWorkInProgressValuation() + production.getInventory().getValuation();
        summary.append("\nTotal inventory: ");
        FormatUtils.formatMoneyAppend(Math.round(totalInventory), summary);
    }


    @Override
    public void draw(Camera camera) {
        synchronized (this) {
            super.draw(camera);
        }
    }


    protected ClickListener payDividendsListener = new ClickListener() {
        @Override
        public void onClick(Widget w) {
            SoundRenderer.playSound(paySound);
            Ledger ledger = production.getLedger();
            ledger.payDividends(25.0f);
            updateSummary(ledger);
        }
    };

}
