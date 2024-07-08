package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Ticket;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserBookingService {

    private ObjectMapper objectMapper = new ObjectMapper();

    private List<User> userList;
    private User user;

    private final String USER_FILE_PATH = "app/src/main/java/ticket/booking/localDb/users.json";

    public UserBookingService(User user) throws IOException {
        this.user = user;
        loadUserListFromFile();
    }

    public UserBookingService() throws IOException {
        loadUserListFromFile();
    }

    private void loadUserListFromFile() throws IOException {
        try {
            File usersFile = new File(USER_FILE_PATH);
            if (usersFile.exists()) {
                userList = objectMapper.readValue(usersFile, new TypeReference<List<User>>() {});
            } else {
                System.out.println("Users file not found. Creating new list.");
                userList = new ArrayList<>();
            }
        } catch (IOException e) {
            System.out.println("Error loading user list: " + e.getMessage());
            throw e;
        }
    }

    public Boolean loginUser() {
        Optional<User> foundUser = userList.stream()
                .filter(u -> u.getName().equals(user.getName()) &&
                        UserServiceUtil.checkPassword(user.getPassword(), u.getHashedPassword()))
                .findFirst();
        return foundUser.isPresent();
    }

    public Boolean signUp(User user1) {
        try {
            userList.add(user1);
            saveUserListToFile();
            return true;
        } catch (IOException ex) {
            System.out.println("Error saving user list: " + ex.getMessage());
            return false;
        }
    }

    private void saveUserListToFile() throws IOException {
        objectMapper.writeValue(new File(USER_FILE_PATH), userList);
    }

    public void fetchBookings() {
        Optional<User> foundUser = userList.stream()
                .filter(u -> u.getName().equals(user.getName()) &&
                        UserServiceUtil.checkPassword(user.getPassword(), u.getHashedPassword()))
                .findFirst();
        foundUser.ifPresent(User::printTickets);
    }

    public Boolean cancelBooking(String ticketId) {
        if (ticketId == null || ticketId.isEmpty()) {
            System.out.println("Ticket ID cannot be null or empty.");
            return false;
        }

        Optional<User> foundUser = userList.stream()
                .filter(u -> u.getName().equals(user.getName()) &&
                        UserServiceUtil.checkPassword(user.getPassword(), u.getHashedPassword()))
                .findFirst();

        if (foundUser.isPresent()) {
            Optional<Ticket> ticketToRemove = foundUser.get().getTicketsBooked().stream()
                    .filter(ticket -> ticket.getTicketId().equals(ticketId))
                    .findFirst();

            if (ticketToRemove.isPresent()) {
                foundUser.get().getTicketsBooked().remove(ticketToRemove.get());
                try {
                    saveUserListToFile();
                    System.out.println("Ticket with ID " + ticketId + " has been canceled.");
                    return true;
                } catch (IOException ex) {
                    System.out.println("Error saving user list: " + ex.getMessage());
                    return false;
                }
            } else {
                System.out.println("No ticket found with ID " + ticketId);
                return false;
            }
        } else {
            System.out.println("User not found.");
            return false;
        }
    }

    public List<Train> getTrains(String source, String destination) {
        try {
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source, destination);
        } catch (IOException ex) {
            System.out.println("Error searching trains: " + ex.getMessage());
            return new ArrayList<>();
        }
    }

    public List<List<Integer>> fetchSeats(Train train) {
        // Assuming train.getSeats() never returns null based on previous discussions
        return train.getSeats();
    }

    public Boolean bookTrainSeat(Train train, int row, int seat) {
        try {
            List<List<Integer>> seats = train.getSeats();
            if (seats != null && row >= 0 && row < seats.size() && seat >= 0 && seat < seats.get(row).size()) {
                if (seats.get(row).get(seat) == 0) {
                    seats.get(row).set(seat, 1);
                    train.setSeats(seats);
                    TrainService trainService = new TrainService();
                    trainService.addTrain(train);
                    return true; // Booking successful
                } else {
                    System.out.println("Seat " + row + ", " + seat + " is already booked.");
                    return false; // Seat is already booked
                }
            } else {
                System.out.println("Invalid seat selection.");
                return false; // Invalid row or seat index
            }
        } catch (IOException ex) {
            System.out.println("Error booking train seat: " + ex.getMessage());
            return false;
        }
    }
}