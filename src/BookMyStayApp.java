import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.Set;

public class BookMyStayApp {
    public static void main(String[] args) {
        Room singleRoom = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suiteRoom = new SuiteRoom();
        Room[] roomCatalog = {singleRoom, doubleRoom, suiteRoom};
        RoomInventory roomInventory = new RoomInventory(singleRoom, 5, doubleRoom, 3, suiteRoom, 0);
        InventoryService inventoryService = new InventoryService(roomInventory);
        InvalidBookingValidator invalidBookingValidator = new InvalidBookingValidator(inventoryService);
        SearchService searchService = new SearchService(inventoryService);
        BookingRequestQueue bookingRequestQueue = new BookingRequestQueue(invalidBookingValidator);
        BookingHistory bookingHistory = new BookingHistory();
        BookingService bookingService = new BookingService(
                bookingRequestQueue,
                inventoryService,
                bookingHistory,
                invalidBookingValidator
        );
        CancellationService cancellationService = new CancellationService(
                bookingHistory,
                bookingService,
                inventoryService
        );
        BookingReportService bookingReportService = new BookingReportService(bookingHistory);
        AddOnServiceManager addOnServiceManager = new AddOnServiceManager();

        System.out.println("====================================");
        System.out.println("   Welcome to the Hotel Booking System");
        System.out.println("   Application Version: 1.0");
        System.out.println("====================================");
        System.out.println("Guest Room Search");
        System.out.println("====================================");
        searchService.displayAvailableRooms(roomCatalog);

        System.out.println("Booking Request Intake");
        System.out.println("====================================");
        submitBookingRequest(bookingRequestQueue, new Reservation("Aarav", singleRoom.getRoomType(), 2));
        submitBookingRequest(bookingRequestQueue, new Reservation("Maya", doubleRoom.getRoomType(), 3));
        submitBookingRequest(bookingRequestQueue, new Reservation("Rohan", suiteRoom.getRoomType(), 1));
        submitBookingRequest(bookingRequestQueue, new Reservation("", singleRoom.getRoomType(), 2));
        submitBookingRequest(bookingRequestQueue, new Reservation("Kiara", "Penthouse", 1));
        submitBookingRequest(bookingRequestQueue, new Reservation("Isha", doubleRoom.getRoomType(), 0));
        bookingRequestQueue.displayPendingRequests();

        System.out.println("Booking Allocation");
        System.out.println("====================================");
        bookingService.processAllRequests();

        System.out.println("Cancellation Requests");
        System.out.println("====================================");
        processCancellation(cancellationService, "RSV-002");
        processCancellation(cancellationService, "RSV-002");
        processCancellation(cancellationService, "RSV-999");
        cancellationService.displayRollbackHistory();

        System.out.println("Admin Booking History Review");
        System.out.println("====================================");
        bookingReportService.displayBookingHistory();

        System.out.println("Add-On Service Selection");
        System.out.println("====================================");
        attachSampleAddOns(bookingHistory, addOnServiceManager);
        addOnServiceManager.displaySelectedServices(bookingHistory.getActiveReservations());

        System.out.println("Booking Summary Report");
        System.out.println("====================================");
        bookingReportService.displaySummaryReport();

        System.out.println("Allocated Room Records");
        System.out.println("====================================");
        bookingService.displayAllocatedRooms(roomCatalog);

        System.out.println("Inventory After Allocation And Add-Ons");
        System.out.println("====================================");
        inventoryService.displayInventory();
    }

    private static void attachSampleAddOns(BookingHistory bookingHistory, AddOnServiceManager addOnServiceManager) {
        List<ConfirmedReservation> confirmedReservations = bookingHistory.getActiveReservations();
        if (confirmedReservations.isEmpty()) {
            System.out.println("No confirmed reservations available for add-on selection.");
            System.out.println("------------------------------------");
            return;
        }

        ConfirmedReservation firstReservation = confirmedReservations.get(0);
        addOnServiceManager.addServiceToReservation(firstReservation.getReservationId(),
                new AddOnService("Breakfast Buffet", 18.50));
        addOnServiceManager.addServiceToReservation(firstReservation.getReservationId(),
                new AddOnService("Airport Pickup", 35.00));

        if (confirmedReservations.size() > 1) {
            ConfirmedReservation secondReservation = confirmedReservations.get(1);
            addOnServiceManager.addServiceToReservation(secondReservation.getReservationId(),
                    new AddOnService("Late Checkout", 20.00));
        }
    }

    private static void submitBookingRequest(BookingRequestQueue bookingRequestQueue, Reservation reservation) {
        try {
            bookingRequestQueue.submitRequest(reservation);
        } catch (BookingValidationException exception) {
            System.out.println("Booking Validation Error: " + exception.getMessage());
            System.out.println("------------------------------------");
        }
    }

    private static void processCancellation(CancellationService cancellationService, String reservationId) {
        try {
            cancellationService.cancelReservation(reservationId);
        } catch (CancellationException exception) {
            System.out.println("Cancellation Error: " + exception.getMessage());
            System.out.println("------------------------------------");
        } catch (InventoryStateException exception) {
            System.out.println("Cancellation Inventory Error: " + exception.getMessage());
            System.out.println("------------------------------------");
        }
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

    public boolean hasRoomType(String roomType) {
        return roomAvailability.containsKey(roomType);
    }

    public void updateAvailability(String roomType, int updatedCount) throws InventoryStateException {
        if (!roomAvailability.containsKey(roomType)) {
            throw new InventoryStateException("Unknown room type: " + roomType);
        }

        if (updatedCount < 0) {
            throw new InventoryStateException("Availability cannot be negative for " + roomType + ".");
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

    public boolean isSupportedRoomType(String roomType) {
        return roomInventory.hasRoomType(roomType);
    }

    public void allocateRoom(String roomType) throws InventoryStateException {
        if (!roomInventory.hasRoomType(roomType)) {
            throw new InventoryStateException("Cannot allocate an unknown room type: " + roomType);
        }

        int currentAvailability = roomInventory.getAvailability(roomType);
        if (currentAvailability <= 0) {
            throw new InventoryStateException("No " + roomType + " rooms are currently available.");
        }

        roomInventory.updateAvailability(roomType, currentAvailability - 1);
    }

    public void restoreRoom(String roomType) throws InventoryStateException {
        if (!roomInventory.hasRoomType(roomType)) {
            throw new InventoryStateException("Cannot restore an unknown room type: " + roomType);
        }

        int currentAvailability = roomInventory.getAvailability(roomType);
        roomInventory.updateAvailability(roomType, currentAvailability + 1);
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
    private final InvalidBookingValidator invalidBookingValidator;

    BookingRequestQueue(InvalidBookingValidator invalidBookingValidator) {
        bookingRequests = new LinkedList<>();
        this.invalidBookingValidator = invalidBookingValidator;
    }

    public void submitRequest(Reservation reservation) throws BookingValidationException {
        invalidBookingValidator.validateBookingInput(reservation);
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
}

class BookingService {
    private final BookingRequestQueue bookingRequestQueue;
    private final InventoryService inventoryService;
    private final BookingHistory bookingHistory;
    private final InvalidBookingValidator invalidBookingValidator;
    private final Set<String> allocatedRoomIds;
    private final HashMap<String, Set<String>> allocatedRoomsByType;
    private final HashMap<String, Integer> nextRoomSequenceByType;
    private int nextReservationSequence;

    BookingService(BookingRequestQueue bookingRequestQueue, InventoryService inventoryService,
                   BookingHistory bookingHistory, InvalidBookingValidator invalidBookingValidator) {
        this.bookingRequestQueue = bookingRequestQueue;
        this.inventoryService = inventoryService;
        this.bookingHistory = bookingHistory;
        this.invalidBookingValidator = invalidBookingValidator;
        allocatedRoomIds = new LinkedHashSet<>();
        allocatedRoomsByType = new HashMap<>();
        nextRoomSequenceByType = new HashMap<>();
        nextReservationSequence = 1;
    }

    public void processAllRequests() {
        if (!bookingRequestQueue.hasPendingRequests()) {
            System.out.println("No booking requests available for allocation.");
            System.out.println("------------------------------------");
            return;
        }

        while (bookingRequestQueue.hasPendingRequests()) {
            try {
                processNextRequest();
            } catch (BookingValidationException exception) {
                System.out.println("Booking Processing Error: " + exception.getMessage());
                System.out.println("------------------------------------");
            } catch (InventoryStateException exception) {
                System.out.println("Inventory Error: " + exception.getMessage());
                System.out.println("------------------------------------");
            }
        }
    }

    public void processNextRequest() throws BookingValidationException, InventoryStateException {
        Reservation reservation = bookingRequestQueue.dequeueNextRequest();
        if (reservation == null) {
            System.out.println("No request to process.");
            System.out.println("------------------------------------");
            return;
        }

        invalidBookingValidator.validateBookingInput(reservation);
        String roomType = reservation.getRequestedRoomType();
        String assignedRoomId = generateUniqueRoomId(roomType);
        inventoryService.allocateRoom(roomType);

        recordAllocation(roomType, assignedRoomId);
        ConfirmedReservation confirmedReservation = createConfirmedReservation(reservation, assignedRoomId);
        bookingHistory.storeConfirmedReservation(confirmedReservation);
        System.out.println("Reservation confirmed for " + reservation.getGuestName());
        System.out.println("Reservation ID: " + confirmedReservation.getReservationId());
        System.out.println("Requested Room Type: " + roomType);
        System.out.println("Assigned Room ID: " + assignedRoomId);
        System.out.println("Remaining Availability: " + inventoryService.getAvailability(roomType));
        System.out.println("------------------------------------");
    }

    public List<ConfirmedReservation> getConfirmedReservations() {
        return bookingHistory.getConfirmedReservations();
    }

    public void releaseAllocatedRoom(ConfirmedReservation confirmedReservation) throws CancellationException {
        if (confirmedReservation == null) {
            throw new CancellationException("Cannot release allocation for a missing reservation.");
        }

        String roomId = confirmedReservation.getAssignedRoomId();
        String roomType = confirmedReservation.getRoomType();
        Set<String> roomIdsForType = allocatedRoomsByType.get(roomType);

        if (!allocatedRoomIds.contains(roomId) || roomIdsForType == null || !roomIdsForType.contains(roomId)) {
            throw new CancellationException(
                    "Allocated room state is inconsistent for reservation " + confirmedReservation.getReservationId()
            );
        }

        allocatedRoomIds.remove(roomId);
        roomIdsForType.remove(roomId);
        if (roomIdsForType.isEmpty()) {
            allocatedRoomsByType.remove(roomType);
        }
    }

    public void restoreReleasedAllocation(ConfirmedReservation confirmedReservation) {
        if (confirmedReservation == null) {
            return;
        }

        String roomId = confirmedReservation.getAssignedRoomId();
        String roomType = confirmedReservation.getRoomType();
        allocatedRoomIds.add(roomId);
        allocatedRoomsByType.computeIfAbsent(roomType, key -> new LinkedHashSet<>()).add(roomId);
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
        nextRoomSequenceByType.put(roomType, getNextSequenceValue(roomId));
    }

    private ConfirmedReservation createConfirmedReservation(Reservation reservation, String assignedRoomId) {
        String reservationId = "RSV-" + String.format("%03d", nextReservationSequence++);
        return new ConfirmedReservation(
                reservationId,
                reservation.getGuestName(),
                reservation.getRequestedRoomType(),
                reservation.getNumberOfNights(),
                assignedRoomId
        );
    }

    private String generateUniqueRoomId(String roomType) {
        String roomPrefix = getRoomPrefix(roomType);
        int nextSequence = nextRoomSequenceByType.getOrDefault(roomType, 1);
        String roomId = buildRoomId(roomPrefix, nextSequence);

        while (allocatedRoomIds.contains(roomId)) {
            nextSequence++;
            roomId = buildRoomId(roomPrefix, nextSequence);
        }

        return roomId;
    }

    private String buildRoomId(String roomPrefix, int sequenceNumber) {
        return roomPrefix + "-" + String.format("%03d", sequenceNumber);
    }

    private int getNextSequenceValue(String roomId) {
        String numericPortion = roomId.substring(roomId.lastIndexOf('-') + 1);
        return Integer.parseInt(numericPortion) + 1;
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

class ConfirmedReservation {
    private final String reservationId;
    private final String guestName;
    private final String roomType;
    private final int numberOfNights;
    private final String assignedRoomId;
    private boolean cancelled;

    ConfirmedReservation(String reservationId, String guestName, String roomType, int numberOfNights,
                         String assignedRoomId) {
        this.reservationId = reservationId;
        this.guestName = guestName;
        this.roomType = roomType;
        this.numberOfNights = numberOfNights;
        this.assignedRoomId = assignedRoomId;
        this.cancelled = false;
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRoomType() {
        return roomType;
    }

    public int getNumberOfNights() {
        return numberOfNights;
    }

    public String getAssignedRoomId() {
        return assignedRoomId;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public String getBookingStatus() {
        return cancelled ? "Cancelled" : "Confirmed";
    }

    void markCancelled() {
        cancelled = true;
    }

    void restoreConfirmedStatus() {
        cancelled = false;
    }
}

class InvalidBookingValidator {
    private final InventoryService inventoryService;

    InvalidBookingValidator(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public void validateBookingInput(Reservation reservation) throws BookingValidationException {
        if (reservation == null) {
            throw new BookingValidationException("Reservation input cannot be null.");
        }

        if (reservation.getGuestName() == null || reservation.getGuestName().isBlank()) {
            throw new BookingValidationException("Guest name is required.");
        }

        if (reservation.getRequestedRoomType() == null || reservation.getRequestedRoomType().isBlank()) {
            throw new BookingValidationException("Room type is required.");
        }

        if (!inventoryService.isSupportedRoomType(reservation.getRequestedRoomType())) {
            throw new BookingValidationException(
                    "Unsupported room type requested: " + reservation.getRequestedRoomType()
            );
        }

        if (reservation.getNumberOfNights() <= 0) {
            throw new BookingValidationException("Number of nights must be greater than zero.");
        }
    }
}

class BookingHistory {
    private final List<ConfirmedReservation> confirmedReservations;

    BookingHistory() {
        confirmedReservations = new ArrayList<>();
    }

    public void storeConfirmedReservation(ConfirmedReservation confirmedReservation) {
        if (confirmedReservation == null) {
            return;
        }

        confirmedReservations.add(confirmedReservation);
    }

    public List<ConfirmedReservation> getConfirmedReservations() {
        return new ArrayList<>(confirmedReservations);
    }

    public List<ConfirmedReservation> getActiveReservations() {
        List<ConfirmedReservation> activeReservations = new ArrayList<>();
        for (ConfirmedReservation confirmedReservation : confirmedReservations) {
            if (!confirmedReservation.isCancelled()) {
                activeReservations.add(confirmedReservation);
            }
        }

        return activeReservations;
    }

    public boolean hasConfirmedReservations() {
        return !confirmedReservations.isEmpty();
    }

    public ConfirmedReservation getCancellableReservation(String reservationId) throws CancellationException {
        if (reservationId == null || reservationId.isBlank()) {
            throw new CancellationException("Reservation ID is required for cancellation.");
        }

        for (ConfirmedReservation confirmedReservation : confirmedReservations) {
            if (!confirmedReservation.getReservationId().equals(reservationId)) {
                continue;
            }

            if (confirmedReservation.isCancelled()) {
                throw new CancellationException("Reservation " + reservationId + " has already been cancelled.");
            }

            return confirmedReservation;
        }

        throw new CancellationException("Reservation " + reservationId + " does not exist.");
    }

    public void markReservationCancelled(ConfirmedReservation confirmedReservation) throws CancellationException {
        if (confirmedReservation == null) {
            throw new CancellationException("Cannot cancel a missing reservation.");
        }

        if (confirmedReservation.isCancelled()) {
            throw new CancellationException(
                    "Reservation " + confirmedReservation.getReservationId() + " has already been cancelled."
            );
        }

        confirmedReservation.markCancelled();
    }
}

class BookingReportService {
    private final BookingHistory bookingHistory;

    BookingReportService(BookingHistory bookingHistory) {
        this.bookingHistory = bookingHistory;
    }

    public void displayBookingHistory() {
        List<ConfirmedReservation> storedReservations = bookingHistory.getConfirmedReservations();
        if (storedReservations.isEmpty()) {
            System.out.println("No booking history available.");
            System.out.println("------------------------------------");
            return;
        }

        int bookingPosition = 1;
        for (ConfirmedReservation confirmedReservation : storedReservations) {
            System.out.println("Booking " + bookingPosition++);
            System.out.println("Reservation ID: " + confirmedReservation.getReservationId());
            System.out.println("Guest: " + confirmedReservation.getGuestName());
            System.out.println("Room Type: " + confirmedReservation.getRoomType());
            System.out.println("Assigned Room ID: " + confirmedReservation.getAssignedRoomId());
            System.out.println("Nights: " + confirmedReservation.getNumberOfNights());
            System.out.println("Status: " + confirmedReservation.getBookingStatus());
            System.out.println("------------------------------------");
        }
    }

    public void displaySummaryReport() {
        List<ConfirmedReservation> storedReservations = bookingHistory.getConfirmedReservations();
        if (storedReservations.isEmpty()) {
            System.out.println("No booking data available for reporting.");
            System.out.println("------------------------------------");
            return;
        }

        HashMap<String, Integer> bookingsByRoomType = new HashMap<>();
        int totalActiveNights = 0;
        int activeReservations = 0;
        int cancelledReservations = 0;

        for (ConfirmedReservation confirmedReservation : storedReservations) {
            if (confirmedReservation.isCancelled()) {
                cancelledReservations++;
                continue;
            }

            activeReservations++;
            bookingsByRoomType.merge(confirmedReservation.getRoomType(), 1, Integer::sum);
            totalActiveNights += confirmedReservation.getNumberOfNights();
        }

        System.out.println("Total Booking Records: " + storedReservations.size());
        System.out.println("Active Reservations: " + activeReservations);
        System.out.println("Cancelled Reservations: " + cancelledReservations);
        System.out.println("Total Active Nights Booked: " + totalActiveNights);
        System.out.println("Active Bookings By Room Type");
        for (Map.Entry<String, Integer> bookingEntry : bookingsByRoomType.entrySet()) {
            System.out.println(bookingEntry.getKey() + ": " + bookingEntry.getValue());
        }
        System.out.println("------------------------------------");
    }
}

class CancellationService {
    private final BookingHistory bookingHistory;
    private final BookingService bookingService;
    private final InventoryService inventoryService;
    private final Stack<String> releasedRoomIds;

    CancellationService(BookingHistory bookingHistory, BookingService bookingService, InventoryService inventoryService) {
        this.bookingHistory = bookingHistory;
        this.bookingService = bookingService;
        this.inventoryService = inventoryService;
        releasedRoomIds = new Stack<>();
    }

    public void cancelReservation(String reservationId) throws CancellationException, InventoryStateException {
        ConfirmedReservation confirmedReservation = bookingHistory.getCancellableReservation(reservationId);

        bookingService.releaseAllocatedRoom(confirmedReservation);
        releasedRoomIds.push(confirmedReservation.getAssignedRoomId());
        boolean inventoryRestored = false;
        boolean historyUpdated = false;

        try {
            inventoryService.restoreRoom(confirmedReservation.getRoomType());
            inventoryRestored = true;
            bookingHistory.markReservationCancelled(confirmedReservation);
            historyUpdated = true;
        } catch (CancellationException | InventoryStateException exception) {
            rollbackCancellation(confirmedReservation, inventoryRestored, historyUpdated);
            throw exception;
        }

        System.out.println("Reservation cancelled for " + confirmedReservation.getGuestName());
        System.out.println("Reservation ID: " + confirmedReservation.getReservationId());
        System.out.println("Released Room ID: " + confirmedReservation.getAssignedRoomId());
        System.out.println("Restored Availability: "
                + inventoryService.getAvailability(confirmedReservation.getRoomType()));
        System.out.println("------------------------------------");
    }

    public void displayRollbackHistory() {
        if (releasedRoomIds.isEmpty()) {
            System.out.println("No released room IDs recorded for rollback.");
            System.out.println("------------------------------------");
            return;
        }

        System.out.println("Released Room IDs (Most Recent First)");
        for (int stackIndex = releasedRoomIds.size() - 1; stackIndex >= 0; stackIndex--) {
            System.out.println(releasedRoomIds.get(stackIndex));
        }
        System.out.println("------------------------------------");
    }

    private void rollbackCancellation(ConfirmedReservation confirmedReservation, boolean inventoryRestored,
                                      boolean historyUpdated) throws InventoryStateException {
        if (!releasedRoomIds.isEmpty()
                && releasedRoomIds.peek().equals(confirmedReservation.getAssignedRoomId())) {
            releasedRoomIds.pop();
        } else {
            releasedRoomIds.remove(confirmedReservation.getAssignedRoomId());
        }

        bookingService.restoreReleasedAllocation(confirmedReservation);
        if (historyUpdated && confirmedReservation.isCancelled()) {
            confirmedReservation.restoreConfirmedStatus();
        }

        if (inventoryRestored) {
            inventoryService.allocateRoom(confirmedReservation.getRoomType());
        }
    }
}

class AddOnService {
    private final String serviceName;
    private final double serviceCost;

    AddOnService(String serviceName, double serviceCost) {
        this.serviceName = serviceName;
        this.serviceCost = serviceCost;
    }

    public String getServiceName() {
        return serviceName;
    }

    public double getServiceCost() {
        return serviceCost;
    }
}

class AddOnServiceManager {
    private final HashMap<String, List<AddOnService>> servicesByReservationId;

    AddOnServiceManager() {
        servicesByReservationId = new HashMap<>();
    }

    public void addServiceToReservation(String reservationId, AddOnService addOnService) {
        if (!isValidServiceSelection(reservationId, addOnService)) {
            System.out.println("Invalid add-on selection skipped.");
            return;
        }

        servicesByReservationId.computeIfAbsent(reservationId, key -> new ArrayList<>()).add(addOnService);
        System.out.println("Added " + addOnService.getServiceName() + " to " + reservationId);
    }

    public double calculateAdditionalCost(String reservationId) {
        List<AddOnService> selectedServices = servicesByReservationId.get(reservationId);
        if (selectedServices == null) {
            return 0.0;
        }

        double totalAdditionalCost = 0.0;
        for (AddOnService addOnService : selectedServices) {
            totalAdditionalCost += addOnService.getServiceCost();
        }

        return totalAdditionalCost;
    }

    public void displaySelectedServices(List<ConfirmedReservation> confirmedReservations) {
        if (confirmedReservations.isEmpty()) {
            System.out.println("No reservations available for add-on reporting.");
            System.out.println("------------------------------------");
            return;
        }

        for (ConfirmedReservation confirmedReservation : confirmedReservations) {
            String reservationId = confirmedReservation.getReservationId();
            List<AddOnService> selectedServices = servicesByReservationId.get(reservationId);
            if (selectedServices == null || selectedServices.isEmpty()) {
                continue;
            }

            System.out.println("Reservation ID: " + reservationId);
            System.out.println("Guest: " + confirmedReservation.getGuestName());
            for (AddOnService addOnService : selectedServices) {
                System.out.printf("Service: %s - $%.2f%n",
                        addOnService.getServiceName(), addOnService.getServiceCost());
            }
            System.out.printf("Total Additional Cost: $%.2f%n", calculateAdditionalCost(reservationId));
            System.out.println("------------------------------------");
        }
    }

    private boolean isValidServiceSelection(String reservationId, AddOnService addOnService) {
        return reservationId != null
                && !reservationId.isBlank()
                && addOnService != null
                && addOnService.getServiceName() != null
                && !addOnService.getServiceName().isBlank()
                && addOnService.getServiceCost() >= 0;
    }
}

class BookingValidationException extends Exception {
    BookingValidationException(String message) {
        super(message);
    }
}

class InventoryStateException extends Exception {
    InventoryStateException(String message) {
        super(message);
    }
}

class CancellationException extends Exception {
    CancellationException(String message) {
        super(message);
    }
}
