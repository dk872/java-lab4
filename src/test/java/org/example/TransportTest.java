package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class TransportTest {

    @BeforeEach
    void clearOccupied() throws Exception {
        // Clear private static Set<String> occupiedPassengers to isolate tests
        Field f = Transport.class.getDeclaredField("occupiedPassengers");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> set = (Set<String>) f.get(null);
        set.clear();
    }

    @Test
    void passengerNullName() {
        assertThrows(IllegalArgumentException.class, () -> new Passenger(null));
    }

    @Test
    void passengerEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> new Passenger("   "));
    }

    @Test
    void transportInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new Bus(0));
        assertThrows(IllegalArgumentException.class, () -> new Taxi(-1));
    }

    @Test
    void getMaxSeats() {
        Bus bus = new Bus(30);
        assertEquals(30, bus.getMaxSeats());

        Taxi taxi = new Taxi(4);
        assertEquals(4, taxi.getMaxSeats());

        FireTruck fireTruck = new FireTruck(5);
        assertEquals(5, fireTruck.getMaxSeats());
    }

    @Test
    void boardPassengerSuccessful() {
        Bus bus = new Bus(2);
        Passenger p = new Passenger("Alice");
        bus.boardPassenger(p);

        assertEquals(1, bus.getOccupiedSeats());
        List<Passenger> passengers = bus.getPassengers();
        assertTrue(passengers.contains(p));
        assertThrows(UnsupportedOperationException.class, () -> passengers.add(new Passenger("X")));
    }

    @Test
    void boardNullPassenger() {
        Bus bus = new Bus(2);
        assertThrows(IllegalArgumentException.class, () -> bus.boardPassenger(null));
    }

    @Test
    void boardWhenFull() {
        Bus bus = new Bus(1);
        bus.boardPassenger(new Passenger("A"));
        assertThrows(IllegalStateException.class, () -> bus.boardPassenger(new Passenger("B")));
    }

    @Test
    void disembarkPassengerSuccessful() {
        Bus bus = new Bus(2);
        Passenger p = new Passenger("Alice");
        bus.boardPassenger(p);
        bus.disembarkPassenger(p);

        assertEquals(0, bus.getOccupiedSeats());
        assertFalse(bus.getPassengers().contains(p));
    }

    @Test
    void disembarkNotOnTransport() {
        Bus bus = new Bus(2);
        Passenger p = new Passenger("Alice");
        assertThrows(IllegalStateException.class, () -> bus.disembarkPassenger(p));
    }

    @Test
    void passengerCannotBeOnTwoTransports() {
        Bus bus = new Bus(2);
        Taxi taxi = new Taxi(2);

        Passenger alice = new Passenger("Alice");
        bus.boardPassenger(alice);

        // boarding same name on another transport should fail
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> taxi.boardPassenger(alice));
        assertTrue(ex.getMessage().contains("already on another transport"));
    }

    @Test
    void differentPassengersAllowed() {
        Bus bus = new Bus(2);
        Taxi taxi = new Taxi(2);

        Passenger a = new Passenger("A");
        Passenger b = new Passenger("B");

        bus.boardPassenger(a);
        taxi.boardPassenger(b);

        assertEquals(1, bus.getOccupiedSeats());
        assertEquals(1, taxi.getOccupiedSeats());
    }

    @Test
    void policeCarAcceptsPoliceman() {
        PoliceCar policeCar = new PoliceCar(2);
        Policeman pm = new Policeman("Officer John");

        // PoliceCar should accept Policeman
        policeCar.boardPassenger(pm);
        assertEquals(1, policeCar.getOccupiedSeats());

        // Bus (generic Passenger) should also accept Policeman
        policeCar.disembarkPassenger(pm);
        Bus bus = new Bus(2);
        bus.boardPassenger(pm);
        assertEquals(1, bus.getOccupiedSeats());
    }

    @Test
    void fireTruckAcceptsFirefighter() {
        FireTruck ft = new FireTruck(2);
        Firefighter ff = new Firefighter("Fire Kate");

        ft.boardPassenger(ff);
        assertEquals(1, ft.getOccupiedSeats());

        ft.disembarkPassenger(ff);
        Bus bus = new Bus(2);
        bus.boardPassenger(ff);
        assertEquals(1, bus.getOccupiedSeats());
    }

    @Test
    void roadCountsPassengers() {
        Road road = new Road();
        Bus bus = new Bus(3);
        Taxi taxi = new Taxi(2);

        Passenger a = new Passenger("A");
        Passenger b = new Passenger("B");
        bus.boardPassenger(a);
        taxi.boardPassenger(b);

        road.addCarToRoad(bus);
        road.addCarToRoad(taxi);

        assertEquals(2, road.getCountOfHumans());
    }

    @Test
    void toStringShowsInfo() {
        Bus bus = new Bus(3);
        Passenger a = new Passenger("A");
        bus.boardPassenger(a);
        String s = bus.toString();

        assertTrue(s.contains("Bus"));
        assertTrue(s.contains("capacity=3"));
        assertTrue(s.contains("occupied=1"));
    }

    @Test
    void canBoardAfterDisembarkSameName() {
        Bus bus = new Bus(2);
        Passenger p = new Passenger("SameName");
        bus.boardPassenger(p);
        bus.disembarkPassenger(p);

        // After disembark, name is free to board again
        bus.boardPassenger(new Passenger("SameName"));
        assertEquals(1, bus.getOccupiedSeats());
    }

    @Test
    void concurrentBoardOnlyOneSucceeds() throws InterruptedException, ExecutionException {
        Bus b1 = new Bus(1);
        Bus b2 = new Bus(1);
        Passenger p = new Passenger("Concurrent");

        ExecutorService ex = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Boolean> task1 = () -> {
            ready.countDown();
            start.await();
            try {
                b1.boardPassenger(p);
                return true;
            } catch (IllegalStateException e) {
                return false;
            }
        };

        Callable<Boolean> task2 = () -> {
            ready.countDown();
            start.await();
            try {
                b2.boardPassenger(p);
                return true;
            } catch (IllegalStateException e) {
                return false;
            }
        };

        Future<Boolean> f1 = ex.submit(task1);
        Future<Boolean> f2 = ex.submit(task2);

        // ensure both threads are ready
        assertTrue(ready.await(1, TimeUnit.SECONDS));
        start.countDown();

        boolean r1 = f1.get();
        boolean r2 = f2.get();

        ex.shutdownNow();

        // exactly one should succeed
        assertTrue(r1 ^ r2, "Exactly one thread must have succeeded in boarding");
    }

    @Test
    void sameNamePassengersRejected() {
        // two distinct instances with same name are considered same passenger by name registry
        Bus bus = new Bus(2);
        Passenger p1 = new Passenger("Dup");
        Passenger p2 = new Passenger("Dup");

        bus.boardPassenger(p1);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> bus.boardPassenger(p2));
        assertTrue(ex.getMessage().contains("already on another transport"));
    }

    @Test
    void boardAfterRemoveFromOtherTransportSucceeds() {
        Bus bus = new Bus(2);
        Taxi taxi = new Taxi(2);
        Passenger joe = new Passenger("Joe");

        bus.boardPassenger(joe);
        assertThrows(IllegalStateException.class, () -> taxi.boardPassenger(joe));

        bus.disembarkPassenger(joe);
        taxi.boardPassenger(joe);
        assertEquals(1, taxi.getOccupiedSeats());
    }

    @Test
    void getPassengersReturnsUnmodifiable() {
        Bus bus = new Bus(2);
        Passenger a = new Passenger("A");
        bus.boardPassenger(a);
        List<Passenger> list = bus.getPassengers();
        assertThrows(UnsupportedOperationException.class, () -> list.add(new Passenger("X")));
    }
}
