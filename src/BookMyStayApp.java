import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class BookMyStayApp {
    public static void main(String[] args) {
        Room singleRoom = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suiteRoom = new SuiteRoom();
        Room[] roomCatalog = {singleRoom, doubleRoom, suiteRoom};
        RoomInventory roomInventory = new RoomInventory(singleRoom, 5, doubleRoom, 3, suiteRoom, 0);
        InventoryService inventoryService = new InventoryService(roomInventory);
        SearchService searchService = new SearchService(inventoryService);
        BookingRequestQueue bookingRequestQueue = new BookingRequestQueue();
        BookingService bookingService = new BookingService(bookingRequestQueue, inventoryService);

        System.out.println("====================================");
        System.out.println("   Welcome to the Hotel Booking System");
        System.out.println("   Application Version: 1.0");
        System.out.println("====================================");
        System.out.println("Guest Room Search");
        System.out.println("====================================");
        searchService.displayAvailableRooms(roomCatalog);

        System.out.println("Booking Request Intake");
        System.out.println("====================================");
        bookingRequestQueue.submitRequest(new Reservation("Aarav", singleRoom.getRoomType(), 2));
        bookingRequestQueue.submitRequest(new Reservation("Maya", doubleRoom.getRoomType(), 3));
        bookingRequestQueue.submitRequest(new Reservation("Rohan", suiteRoom.getRoomType(), 1));
        bookingRequestQueue.displayPendingRequests();

        System.out.println("Booking Allocation");
        System.out.println("====================================");
        bookingService.processAllRequests();

        System.out.println("Allocated Room Records");
        System.out.println("====================================");
        bookingService.displayAllocatedRooms(roomCatalog);

        System.out.println("Inventory After Allocation");
        System.out.println("====================================");
        inventoryService.displayInventory();
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

class RoomInventory {
    private final HashMap<String, Integer> roomAvailability;

    RoomInventory(Room singleRoom, int singleAvailability, Room doubleRoom, int doubleAvailability,
                  Room suiteRoom, int suiteAvailability) {
        roomAvailability = new HashMap<>();
        registerRoomType(singleRoom.getRoomType(), singleAvailability);
        registerRoomType(doubleRoom.getRoomType(), doubleAvailability);
        registerRoomType(suiteRoom.getRoomType(), suiteAvailability);
    }

    public void registerRoomType(String roomType, int availableCount) {
        roomAvailability.put(roomType, availableCount);
    }

    public int getAvailability(String roomType) {
        return roomAvailability.getOrDefault(roomType, 0);
    }

    public void updateAvailability(String roomType, int updatedCount) {
        if (!roomAvailability.containsKey(roomType)) {
            System.out.println("Room type not found: " + roomType);
            return;
        }

        if (updatedCount < 0) {
            System.out.println("Availability cannot be negative for: " + roomType);
            return;
        }

        roomAvailability.put(roomType, updatedCount);
    }

    public void displayInventory() {
        for (Map.Entry<String, Integer> inventoryEntry : roomAvailability.entrySet()) {
            System.out.println(inventoryEntry.getKey() + ": " + inventoryEntry.getValue() + " rooms available");
        }
        System.out.println("------------------------------------");
    }
}

class InventoryService {
    private final RoomInventory roomInventory;

    InventoryService(RoomInventory roomInventory) {
        this.roomInventory = roomInventory;
    }

    public int getAvailability(String roomType) {
        return roomInventory.getAvailability(roomType);
    }

    public boolean allocateRoom(String roomType) {
        int currentAvailability = roomInventory.getAvailability(roomType);
        if (currentAvailability <= 0) {
            return false;
        }

        roomInventory.updateAvailability(roomType, currentAvailability - 1);
        return true;
    }

    public void displayInventory() {
        roomInventory.displayInventory();
    }
}

class SearchService {
    private final InventoryService inventoryService;

    SearchService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public void displayAvailableRooms(Room[] rooms) {
        if (rooms == null || rooms.length == 0) {
            System.out.println("No room information available.");
            System.out.println("------------------------------------");
            return;
        }

        boolean availableRoomFound = false;

        for (Room room : rooms) {
            if (!isValidRoom(room)) {
                continue;
            }

            int availability = inventoryService.getAvailability(room.getRoomType());
            if (availability <= 0) {
                continue;
            }

            printRoomDetails(room, availability);
            availableRoomFound = true;
        }

        if (!availableRoomFound) {
            System.out.println("No rooms are currently available.");
            System.out.println("------------------------------------");
        }
    }

    private boolean isValidRoom(Room room) {
        return room != null && room.getRoomType() != null && !room.getRoomType().isBlank();
    }

    private void printRoomDetails(Room room, int availability) {
        System.out.println("Room Type: " + room.getRoomType());
        System.out.println("Beds: " + room.getNumberOfBeds());
        System.out.println("Size: " + room.getSizeInSquareFeet() + " sq ft");
        System.out.printf("Price Per Night: $%.2f%n", room.getPricePerNight());
        System.out.println("Available Rooms: " + availability);
        System.out.println("------------------------------------");
    }
}

class Reservation {
    private final String guestName;
    private final String requestedRoomType;
    private final int numberOfNights;

    Reservation(String guestName, String requestedRoomType, int numberOfNights) {
        this.guestName = guestName;
        this.requestedRoomType = requestedRoomType;
        this.numberOfNights = numberOfNights;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRequestedRoomType() {
        return requestedRoomType;
    }

    public int getNumberOfNights() {
        return numberOfNights;
    }
}

class BookingRequestQueue {
    private final Queue<Reservation> bookingRequests;

    BookingRequestQueue() {
        bookingRequests = new LinkedList<>();
    }

    public void submitRequest(Reservation reservation) {
        if (!isValidReservation(reservation)) {
            System.out.println("Invalid booking request skipped.");
            return;
        }

        bookingRequests.offer(reservation);
        System.out.println("Queued request for " + reservation.getGuestName() + " -> "
                + reservation.getRequestedRoomType());
    }

    public Reservation peekNextRequest() {
        return bookingRequests.peek();
    }

    public Reservation dequeueNextRequest() {
        return bookingRequests.poll();
    }

    public boolean hasPendingRequests() {
        return !bookingRequests.isEmpty();
    }

    public void displayPendingRequests() {
        if (bookingRequests.isEmpty()) {
            System.out.println("No booking requests in queue.");
            System.out.println("------------------------------------");
            return;
        }

        int requestPosition = 1;
        for (Reservation reservation : bookingRequests) {
            System.out.println("Request " + requestPosition++);
            System.out.println("Guest: " + reservation.getGuestName());
            System.out.println("Requested Room Type: " + reservation.getRequestedRoomType());
            System.out.println("Nights: " + reservation.getNumberOfNights());
            System.out.println("------------------------------------");
        }

        Reservation nextRequest = peekNextRequest();
        if (nextRequest != null) {
            System.out.println("Next request to process: " + nextRequest.getGuestName()
                    + " -> " + nextRequest.getRequestedRoomType());
            System.out.println("------------------------------------");
        }
    }

    private boolean isValidReservation(Reservation reservation) {
        return reservation != null
                && reservation.getGuestName() != null
                && !reservation.getGuestName().isBlank()
                && reservation.getRequestedRoomType() != null
                && !reservation.getRequestedRoomType().isBlank()
                && reservation.getNumberOfNights() > 0;
    }
}

class BookingService {
    private final BookingRequestQueue bookingRequestQueue;
    private final InventoryService inventoryService;
    private final Set<String> allocatedRoomIds;
    private final HashMap<String, Set<String>> allocatedRoomsByType;
    private final HashMap<String, Integer> nextRoomSequenceByType;

    BookingService(BookingRequestQueue bookingRequestQueue, InventoryService inventoryService) {
        this.bookingRequestQueue = bookingRequestQueue;
        this.inventoryService = inventoryService;
        allocatedRoomIds = new LinkedHashSet<>();
        allocatedRoomsByType = new HashMap<>();
        nextRoomSequenceByType = new HashMap<>();
    }

    public void processAllRequests() {
        if (!bookingRequestQueue.hasPendingRequests()) {
            System.out.println("No booking requests available for allocation.");
            System.out.println("------------------------------------");
            return;
        }

        while (bookingRequestQueue.hasPendingRequests()) {
            processNextRequest();
        }
    }

    public void processNextRequest() {
        Reservation reservation = bookingRequestQueue.dequeueNextRequest();
        if (reservation == null) {
            System.out.println("No request to process.");
            System.out.println("------------------------------------");
            return;
        }

        String roomType = reservation.getRequestedRoomType();
        if (inventoryService.getAvailability(roomType) <= 0) {
            System.out.println("Unable to confirm reservation for " + reservation.getGuestName()
                    + ". No " + roomType + " rooms are available.");
            System.out.println("------------------------------------");
            return;
        }

        String assignedRoomId = generateUniqueRoomId(roomType);
        if (!inventoryService.allocateRoom(roomType)) {
            System.out.println("Allocation failed for " + reservation.getGuestName()
                    + ". Inventory changed before confirmation.");
            System.out.println("------------------------------------");
            return;
        }

        recordAllocation(roomType, assignedRoomId);
        System.out.println("Reservation confirmed for " + reservation.getGuestName());
        System.out.println("Requested Room Type: " + roomType);
        System.out.println("Assigned Room ID: " + assignedRoomId);
        System.out.println("Remaining Availability: " + inventoryService.getAvailability(roomType));
        System.out.println("------------------------------------");
    }

    public void displayAllocatedRooms(Room[] rooms) {
        if (allocatedRoomIds.isEmpty()) {
            System.out.println("No rooms allocated yet.");
            System.out.println("------------------------------------");
            return;
        }

        for (Room room : rooms) {
            if (room == null) {
                continue;
            }

            Set<String> roomIdsForType = allocatedRoomsByType.get(room.getRoomType());
            if (roomIdsForType == null || roomIdsForType.isEmpty()) {
                continue;
            }

            System.out.println(room.getRoomType() + ": " + roomIdsForType);
        }

        System.out.println("All Allocated Room IDs: " + allocatedRoomIds);
        System.out.println("------------------------------------");
    }

    private void recordAllocation(String roomType, String roomId) {
        allocatedRoomIds.add(roomId);
        allocatedRoomsByType.computeIfAbsent(roomType, key -> new LinkedHashSet<>()).add(roomId);
    }

    private String generateUniqueRoomId(String roomType) {
        String roomPrefix = getRoomPrefix(roomType);
        int nextSequence = nextRoomSequenceByType.getOrDefault(roomType, 1);
        String roomId = buildRoomId(roomPrefix, nextSequence);

        while (allocatedRoomIds.contains(roomId)) {
            nextSequence++;
            roomId = buildRoomId(roomPrefix, nextSequence);
        }

        nextRoomSequenceByType.put(roomType, nextSequence + 1);
        return roomId;
    }

    private String buildRoomId(String roomPrefix, int sequenceNumber) {
        return roomPrefix + "-" + String.format("%03d", sequenceNumber);
    }

    private String getRoomPrefix(String roomType) {
        if ("Single Room".equals(roomType)) {
            return "SNG";
        }

        if ("Double Room".equals(roomType)) {
            return "DBL";
        }

        if ("Suite Room".equals(roomType)) {
            return "STE";
        }

        return "ROM";
    }
}
