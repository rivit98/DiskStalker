package org.agh.diskstalker.model.statisctics;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Type {
    private SimpleStringProperty type;
    private SimpleIntegerProperty quantity;

    public Type(String fileType) {
        this.type = new SimpleStringProperty(fileType);
        this.quantity = new SimpleIntegerProperty(1);
    }

    public String getType() {
        return type.get();
    }

    public int getQuantity() {
        return quantity.get();
    }

    public void increment() {
        quantity.set(quantity.getValue() + 1);
    }

    public void decrement() {quantity.set(quantity.getValue() - 1);}
}