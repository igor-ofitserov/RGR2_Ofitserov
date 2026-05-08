package com.ofitserov.beans;

import java.util.ArrayList;

public class DataSheet {
    private ArrayList<Data> dataArray;
    private String dataName;

    public DataSheet() {
        dataArray = new ArrayList<>();
        dataName = "Експериментальні дані";
    }

    public void addData(Data d) {
        dataArray.add(d);
    }

    public void removeData(int index) {
        if (index >= 0 && index < dataArray.size()) {
            dataArray.remove(index);
        }
    }

    public Data getDataItem(int index) {
        return dataArray.get(index);
    }

    public int size() {
        return dataArray.size();
    }

    public String getDataName() {
        return dataName;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    public void clear() {
        dataArray.clear();
    }
}