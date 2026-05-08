package com.ofitserov.beans;

import java.util.EventListener;

public interface DataSheetChangeListener extends EventListener {
    void dataChanged(DataSheetChangeEvent e);
}