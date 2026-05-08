package com.ofitserov.beans;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

public class DataSheetGraph extends JPanel implements DataSheetChangeListener {
    private DataSheet dataSheet = null;
    private boolean isConnected = true; // Властивість: чи з'єднувати точки лініями
    private Color color = Color.RED;    // Властивість: колір графіка

    public DataSheetGraph() {
        super();
        dataSheet = new DataSheet();
    }

    // --- Властивості компонента (JavaBeans) ---
    public DataSheet getDataSheet() {
        return dataSheet;
    }

    public void setDataSheet(DataSheet dataSheet) {
        this.dataSheet = dataSheet;
        repaint(); // Якщо підсунули нові дані — перемальовуємо
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        this.isConnected = connected;
        repaint();
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        repaint();
    }

    // --- РЕАКЦІЯ НА ПОДІЮ ---
    @Override
    public void dataChanged(DataSheetChangeEvent e) {
        // Коли прилітає подія від таблиці про зміну даних — просто перемальовуємо графік
        repaint();
    }

    // --- Механізм малювання графіка ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // Вмикаємо згладжування, щоб лінії були гарними
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Малюємо білий фон та чорну рамку
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);
        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, width - 1, height - 1);

        if (dataSheet == null || dataSheet.size() == 0) return;

        // Знаходимо мінімальні та максимальні значення X та Y для масштабування графіка
        double minX = dataSheet.getDataItem(0).getX();
        double maxX = minX;
        double minY = dataSheet.getDataItem(0).getY();
        double maxY = minY;

        for (int i = 1; i < dataSheet.size(); i++) {
            double x = dataSheet.getDataItem(i).getX();
            double y = dataSheet.getDataItem(i).getY();
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }

        // Обчислюємо коефіцієнти масштабування (з відступами по 20 пікселів від країв)
        double xRatio = (maxX == minX) ? 1 : (width - 40) / (maxX - minX);
        double yRatio = (maxY == minY) ? 1 : (height - 40) / (maxY - minY);

        g2.setColor(color);
        int prevX = 0, prevY = 0;

        for (int i = 0; i < dataSheet.size(); i++) {
            double x = dataSheet.getDataItem(i).getX();
            double y = dataSheet.getDataItem(i).getY();

            // Переводимо реальні координати в пікселі на екрані
            int screenX = 20 + (int) ((x - minX) * xRatio);
            int screenY = height - 20 - (int) ((y - minY) * yRatio);

            // Малюємо квадратну точку
            g2.fill(new Rectangle2D.Double(screenX - 3, screenY - 3, 6, 6));

            // Малюємо лінію до попередньої точки
            if (isConnected && i > 0) {
                g2.draw(new Line2D.Double(prevX, prevY, screenX, screenY));
            }
            prevX = screenX;
            prevY = screenY;
        }
    }
}