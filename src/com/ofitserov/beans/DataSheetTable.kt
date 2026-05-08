package com.ofitserov.beans;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;

public class DataSheetTable extends JPanel {
    private JTable table;
    private DataSheetTableModel tableModel;
    private DataSheet dataSheet;
    private ArrayList<DataSheetChangeListener> listeners = new ArrayList<>();

    // Конструктор компонента
    public DataSheetTable() {
        super(new BorderLayout());
        dataSheet = new DataSheet(); // Створюємо порожній набір даних за замовчуванням
        tableModel = new DataSheetTableModel(dataSheet);
        table = new JTable(tableModel);

        // Додаємо таблицю на панель прокрутки
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    // Обов'язкові для JavaBean геттер та сеттер
    public DataSheet getDataSheet() {
        return dataSheet;
    }

    public void setDataSheet(DataSheet dataSheet) {
        this.dataSheet = dataSheet;
        tableModel.setDataSheet(dataSheet);
        tableModel.fireTableDataChanged(); // Оновлюємо візуал таблиці
        fireDataSheetChange(); // Сигналізуємо, що дані повністю змінилися
    }

    // --- Механізм підписки на події ---
    public void addDataSheetChangeListener(DataSheetChangeListener l) {
        listeners.add(l);
    }

    public void removeDataSheetChangeListener(DataSheetChangeListener l) {
        listeners.remove(l);
    }

    // Метод для "пострілу" подією
    protected void fireDataSheetChange() {
        DataSheetChangeEvent event = new DataSheetChangeEvent(this);
        for (DataSheetChangeListener listener : listeners) {
        listener.dataChanged(event);
    }
    }

    // --- Внутрішній клас-модель для нашої таблиці ---
    private class DataSheetTableModel extends AbstractTableModel {
        private DataSheet dataSheet;
        private String[] columnNames = {"X", "Y"};

        public DataSheetTableModel(DataSheet dataSheet) {
            this.dataSheet = dataSheet;
        }

        public void setDataSheet(DataSheet dataSheet) {
            this.dataSheet = dataSheet;
        }

        @Override
        public int getRowCount() {
            return dataSheet.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true; // Дозволяємо редагувати всі комірки
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Data data = dataSheet.getDataItem(rowIndex);
            return columnIndex == 0 ? data.getX() : data.getY();
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            try {
                // Спробуємо перетворити введений текст на число
                double value = Double.parseDouble(aValue.toString());
                Data data = dataSheet.getDataItem(rowIndex);
                if (columnIndex == 0) {
                    data.setX(value);
                } else {
                    data.setY(value);
                }
                fireTableCellUpdated(rowIndex, columnIndex);

                // Найважливіше: повідомляємо про ручну зміну даних!
                fireDataSheetChange();
            } catch (NumberFormatException e) {
                // Якщо користувач ввів літери замість цифр — просто ігноруємо
            }
        }
    }
}