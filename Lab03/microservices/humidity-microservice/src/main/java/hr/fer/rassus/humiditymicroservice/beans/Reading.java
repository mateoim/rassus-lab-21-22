package hr.fer.rassus.humiditymicroservice.beans;

import reactor.util.annotation.NonNull;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Reading {

    private static final String name = "Humidity";

    private static final String unit = "%";

    @Id
    @GeneratedValue
    @NonNull
    private long id;

    private double value;

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
