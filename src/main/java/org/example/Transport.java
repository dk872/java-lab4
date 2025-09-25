package org.example;

import java.util.*;

public abstract class Transport<T extends Passenger> {
    private final int capacity;
    private final List<T> passengers = new ArrayList<>();
    private static final Set<String> occupiedPassengers = new HashSet<>();

    protected Transport(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be > 0");

        this.capacity = capacity;
    }

    public int getMaxSeats() {
        return capacity;
    }

    public int getOccupiedSeats() {
        return passengers.size();
    }

    public List<T> getPassengers() {
        return Collections.unmodifiableList(passengers);
    }

    public void boardPassenger(T passenger) {
        if (passenger == null) throw new IllegalArgumentException("Passenger cannot be null");

        if (passengers.size() >= capacity) {
            throw new IllegalStateException("No free seats");
        }

        ensurePassengerNotSeated(passenger);
        passengers.add(passenger);
        synchronized (occupiedPassengers) {
            occupiedPassengers.add(passenger.getName());
        }
    }

    private void ensurePassengerNotSeated(T passenger) {
        String name = passenger.getName();
        synchronized (occupiedPassengers) {
            if (occupiedPassengers.contains(name)) {
                throw new IllegalStateException(
                        "Passenger with name '" + name + "' is already on another transport"
                );
            }
        }
    }

    public void disembarkPassenger(T passenger) {
        if (passenger == null) throw new IllegalArgumentException("Passenger cannot be null");

        synchronized (occupiedPassengers) {
            boolean removed = passengers.remove(passenger);
            if (!removed) {
                throw new IllegalStateException("Passenger is not on this transport");
            }

            occupiedPassengers.remove(passenger.getName());
        }
    }

    @Override
    public String toString() {
        return String.format("%s (capacity=%d, occupied=%d)",
                this.getClass().getSimpleName(), capacity, getOccupiedSeats());
    }
}
