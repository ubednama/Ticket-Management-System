package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Ticket;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class UserBookingService {

    private ObjectMapper objectMapper = new ObjectMapper();

    private List<User> userList;
    private User user;

    private final String USER_FILE_PATH = "app/src/main/java/ticket/booking/localDb/users.json";

    public UserBookingService(User user) throws IOException {
        this.user = user;
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

    public Boolean signUp(User user) throws IOException {
        userList.add(user);
        saveUserListToFile();
        return true;
    }



    public void fetchBookings() {
        System.out.println("Attempting to fetch bookings for user: " + user.getName());

        Optional<User> foundUser = userList.stream()
                .filter(u -> u.getName().equals(user.getName()) &&
                        UserServiceUtil.checkPassword(user.getPassword(), u.getHashedPassword()))
                .findFirst();

        System.out.println("Found user: " + foundUser.isPresent());

        if (foundUser.isPresent()) {
            User loggedInUser = foundUser.get();
            System.out.println("Fetching bookings for user: " + loggedInUser.getName());

            List<Ticket> bookedTickets = loggedInUser.getTicketsBooked();
            if (bookedTickets.isEmpty()) {
                System.out.println("No bookings found for user: " + loggedInUser.getName());
            } else {
                for (Ticket ticket : bookedTickets) {
                    System.out.println(ticket.getTicketInfo());
                }
            }
        } else {
            System.out.println("No user found with the provided credentials.");
        }
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
                saveUserListToFile();
                System.out.println("Ticket with ID " + ticketId + " has been canceled.");
                return true;
            } else {
                System.out.println("No ticket found with ID " + ticketId);
                return false;
            }
        } else {
            System.out.println("User not found.");
            return false;
        }
    }

    public Boolean bookTrainSeat(Train train, int row, int seat, String source, String dest) {
        try {
            List<List<Integer>> seats = train.getSeats();

            if (seats != null && row >= 0 && row < seats.size() && seat >= 0 && seat < seats.get(row).size()) {
                if (seats.get(row).get(seat) == 0) {
                    seats.get(row).set(seat, 1);
                    train.setSeats(seats);
                    train.setSource(source);
                    train.setDestination(dest);

                    TrainService trainService = new TrainService();
                    trainService.updateTrain(train);

                    String ticketId = generateTicketId();
                    Ticket newTicket = new Ticket(ticketId, user.getUserId(), train.getSource(), train.getDestination(), getCurrentDate(), train);

                    user.addTicketBooked(newTicket);
                    System.out.println("New ticket details: " + newTicket.getTicketInfo());
                    System.out.println("Ticket details: " + newTicket);
                    saveUserListToFile();

                    return true;
                } else {
                    System.out.println("Seat " + row + ", " + seat + " is already booked.");
                    return false;
                }
            } else {
                System.out.println("Invalid seat selection.");
                return false;
            }
        } catch (IOException ex) {
            System.out.println("Error booking train seat: " + ex.getMessage());
            return false;
        }
    }

    private void saveUserListToFile() {
        try {
            objectMapper.writeValue(new File(USER_FILE_PATH), userList);
        } catch (IOException e) {
            System.out.println("Error saving user list: " + e.getMessage());
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
        return train.getSeats();
    }

    private String generateTicketId() {
        return UUID.randomUUID().toString();
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(new Date(System.currentTimeMillis()));
    }

    public void logout() {
        this.user = null;
        System.out.println("Logged out successfully.");
    }
}