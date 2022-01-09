package hr.fer.rassus.humiditymicroservice.beans;

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

    private double humidity;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }
}
