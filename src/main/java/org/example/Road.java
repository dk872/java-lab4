package org.example;

import java.util.ArrayList;
import java.util.List;

public class Road {
    private final List<Transport<? extends Passenger>> carsInRoad = new ArrayList<>();

    public void addCarToRoad(Transport<? extends Passenger> car) {
        if (car == null) {
            throw new IllegalArgumentException("Car cannot be null");
        }
        carsInRoad.add(car);
    }

    public int getCountOfHumans() {
        int total = 0;
        for (Transport<? extends Passenger> car : carsInRoad) {
            total += car.getOccupiedSeats();
        }
        return total;
    }
}
