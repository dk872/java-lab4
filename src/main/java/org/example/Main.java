package org.example;

public class Main {
    public static void main(String[] args) {
        Road road = new Road();

        // Transports
        Bus bus = new Bus(30);
        Taxi taxi = new Taxi(3);
        PoliceCar policeCar = new PoliceCar(4);
        FireTruck fireTruck = new FireTruck(4);

        // General passengers
        Passenger alice = new Passenger("Alice");
        Passenger bob = new Passenger("Bob");
        Passenger charlie = new Passenger("Charlie");
        Passenger edward = new Passenger("Edward");
        Passenger diana = new Passenger("Diana");

        // Policemen
        Policeman officerJohn = new Policeman("Officer John");
        Policeman officerMike = new Policeman("Officer Mike");
        Policeman officerSteve = new Policeman("Officer Steve");

        // Firefighters
        Firefighter firefighterKate = new Firefighter("Firefighter Kate");
        Firefighter firefighterTom = new Firefighter("Firefighter Tom");
        Firefighter firefighterLucy = new Firefighter("Firefighter Lucy");

        // Boarding passengers to transports
        bus.boardPassenger(alice);
        bus.boardPassenger(bob);
        bus.boardPassenger(officerSteve); // allowed, as Policeman extends Passenger

        taxi.boardPassenger(charlie);
        taxi.boardPassenger(firefighterLucy); // allowed, as Firefighter extends Passenger

        policeCar.boardPassenger(officerJohn);
        policeCar.boardPassenger(officerMike);

        fireTruck.boardPassenger(firefighterKate);
        fireTruck.boardPassenger(firefighterTom);

        // Attempt to board already boarded passengers
        try {
            taxi.boardPassenger(alice); // Alice is already on the bus
        } catch (IllegalStateException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }

        try {
            bus.boardPassenger(charlie); // Charlie is already in the taxi
        } catch (IllegalStateException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }

        // Add transports to road
        road.addCarToRoad(bus);
        road.addCarToRoad(taxi);
        road.addCarToRoad(policeCar);
        road.addCarToRoad(fireTruck);

        // Show transport status
        System.out.println("\n--- Transport status ---");
        System.out.println(bus);
        System.out.println(taxi);
        System.out.println(policeCar);
        System.out.println(fireTruck);

        System.out.println("\nTotal humans on the road: " + road.getCountOfHumans());

        // Disembark a passenger
        taxi.disembarkPassenger(charlie);
        System.out.println("\nAfter Charlie left the taxi:");
        System.out.println(bus);
        System.out.println("Total humans on the road: " + road.getCountOfHumans());

        // Attempt to board new passengers into taxi (limited capacity)
        try {
            taxi.boardPassenger(charlie); // OK
            taxi.boardPassenger(diana); // OK
            taxi.boardPassenger(edward); // May fail due to capacity
        } catch (IllegalStateException ex) {
            System.out.println("\n[ERROR] " + ex.getMessage());
        }
    }
}
