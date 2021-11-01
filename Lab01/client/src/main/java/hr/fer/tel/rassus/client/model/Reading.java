package hr.fer.tel.rassus.client.model;

public class Reading {

    private final double temperature;

    private final double pressure;

    private final double humidity;

    private final double co;

    private final double no2;

    private final double so2;

    public Reading(double temperature, double pressure, double humidity, double co, double no2, double so2) {
        this.temperature = temperature;
        this.pressure = pressure;
        this.humidity = humidity;
        this.co = co;
        this.no2 = no2;
        this.so2 = so2;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getPressure() {
        return pressure;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getCo() {
        return co;
    }

    public double getNo2() {
        return no2;
    }

    public double getSo2() {
        return so2;
    }
}
