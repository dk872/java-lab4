package org.example;

public abstract class Car<T extends Passenger> extends Transport<T> {
    protected Car(int capacity) {
        super(capacity);
    }
}
