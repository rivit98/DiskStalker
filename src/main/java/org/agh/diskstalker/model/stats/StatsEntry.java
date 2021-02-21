package org.agh.diskstalker.model.stats;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
public class StatsEntry {
    private final SimpleStringProperty typeProperty = new SimpleStringProperty();
    private final SimpleLongProperty longProperty = new SimpleLongProperty();

    public StatsEntry(String type, Long value) {
        this.typeProperty.set(type);
        this.longProperty.set(value);
    }

    public void increment(){
        add(1);
    }

    public void decrement() {
        add(-1);
    }

    public void add(long i){
        longProperty.set(getValue() + i);
    }

    public long getValue(){
        return longProperty.longValue();
    }

    public String getType(){
        return typeProperty.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatsEntry statsEntry = (StatsEntry) o;
        return Objects.equals(typeProperty, statsEntry.typeProperty) && Objects.equals(longProperty, statsEntry.longProperty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeProperty);
    }
}
