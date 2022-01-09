package hr.fer.rassus.temperaturemicroservice.beans;

import reactor.util.annotation.NonNull;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Reading {

    @Id
    @GeneratedValue
    @NonNull
    private long id;

    private double temperature;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
}
