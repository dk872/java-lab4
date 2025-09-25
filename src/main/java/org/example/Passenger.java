package org.example;

public class Passenger {
    private final String name;

    public Passenger(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }

        this.name = name;
    }

    public String getName() { return name; }
}
