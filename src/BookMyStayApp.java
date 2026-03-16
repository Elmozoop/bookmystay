public class BookMyStayApp {
    public static void main(String[] args) {
        Room singleRoom = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suiteRoom = new SuiteRoom();

        int singleRoomAvailability = 5;
        int doubleRoomAvailability = 3;
        int suiteRoomAvailability = 2;

        System.out.println("====================================");
        System.out.println("   Welcome to the Hotel Booking System");
        System.out.println("   Application Version: 1.0");
        System.out.println("====================================");
        System.out.println("Available Room Types");
        System.out.println("====================================");

        printRoomAvailability(singleRoom, singleRoomAvailability);
        printRoomAvailability(doubleRoom, doubleRoomAvailability);
        printRoomAvailability(suiteRoom, suiteRoomAvailability);
    }

    private static void printRoomAvailability(Room room, int availability) {
        System.out.println("Room Type: " + room.getRoomType());
        System.out.println("Beds: " + room.getNumberOfBeds());
        System.out.println("Size: " + room.getSizeInSquareFeet() + " sq ft");
        System.out.printf("Price Per Night: $%.2f%n", room.getPricePerNight());
        System.out.println("Available Rooms: " + availability);
        System.out.println("------------------------------------");
    }
}

abstract class Room {
    private final String roomType;
    private final int numberOfBeds;
    private final int sizeInSquareFeet;
    private final double pricePerNight;

    protected Room(String roomType, int numberOfBeds, int sizeInSquareFeet, double pricePerNight) {
        this.roomType = roomType;
        this.numberOfBeds = numberOfBeds;
        this.sizeInSquareFeet = sizeInSquareFeet;
        this.pricePerNight = pricePerNight;
    }

    public String getRoomType() {
        return roomType;
    }

    public int getNumberOfBeds() {
        return numberOfBeds;
    }

    public int getSizeInSquareFeet() {
        return sizeInSquareFeet;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }
}

class SingleRoom extends Room {
    SingleRoom() {
        super("Single Room", 1, 180, 89.99);
    }
}

class DoubleRoom extends Room {
    DoubleRoom() {
        super("Double Room", 2, 260, 149.99);
    }
}

class SuiteRoom extends Room {
    SuiteRoom() {
        super("Suite Room", 3, 420, 249.99);
    }
}
