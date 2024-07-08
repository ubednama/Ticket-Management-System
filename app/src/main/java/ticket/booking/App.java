package ticket.booking;

import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.services.UserBookingService;
import ticket.booking.util.UserServiceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class App {

    public static void main(String[] args) {
        try {
            System.out.println("Running Train Booking System");
            Scanner scanner = new Scanner(System.in);
            int option = 0;
            UserBookingService userBookingService = null;
            User loggedInUser = null;

            try {
                userBookingService = new UserBookingService();
            } catch (IOException ex) {
                System.out.println("Error initializing booking service: " + ex.getMessage());
                ex.printStackTrace();
                return;
            }

            while (option != 7) {
                System.out.println("Choose option:");
                System.out.println("1. Sign up");
                System.out.println("2. Login");
                System.out.println("3. Fetch Bookings");
                System.out.println("4. Search Trains");
                System.out.println("5. Book a Seat");
                System.out.println("6. Cancel my Booking");
                System.out.println("7. Exit the App");

                option = scanner.nextInt();
                scanner.nextLine(); // Consume newline character

                Train trainSelectedForBooking = new Train();

                switch (option) {
                    case 1:
                        System.out.println("Enter username to signup:");
                        String nameToSignUp = scanner.next();
                        System.out.println("Enter password to signup:");
                        String passwordToSignUp = scanner.next();
                        User userToSignup = new User(nameToSignUp, passwordToSignUp, UserServiceUtil.hashPassword(passwordToSignUp), new ArrayList<>(), UUID.randomUUID().toString());
                        if (userBookingService.signUp(userToSignup)) {
                            System.out.println("Sign up successful!");
                        } else {
                            System.out.println("Sign up failed. Please try again.");
                        }
                        break;
                    case 2:
                        System.out.println("Enter username to Login:");
                        String nameToLogin = scanner.next();
                        System.out.println("Enter password to Login:");
                        String passwordToLogin = scanner.next();
                        loggedInUser = new User(nameToLogin, passwordToLogin, UserServiceUtil.hashPassword(passwordToLogin), new ArrayList<>(), UUID.randomUUID().toString());
                        try {
                            userBookingService = new UserBookingService(loggedInUser);
                            System.out.println("Login successful. Welcome, " + loggedInUser.getName());
                        } catch (IOException ex) {
                            System.out.println("Login failed. Please check your credentials.");
                        }
                        break;
                    case 3:
                        if (loggedInUser != null) {
                            System.out.println("Fetching your bookings:");
                            userBookingService.fetchBookings();
                        } else {
                            System.out.println("Please login first.");
                        }
                        break;
                    case 4:
                        if (loggedInUser != null) {
                            System.out.println("Enter source station:");
                            String source = scanner.next();
                            System.out.println("Enter destination station:");
                            String dest = scanner.next();
                            List<Train> trains = userBookingService.getTrains(source, dest);
                            int index = 1;
                            for (Train t : trains) {
                                System.out.println(index + ". Train ID: " + t.getTrainId());
                                for (Map.Entry<String, String> entry : t.getStationTimes().entrySet()) {
                                    System.out.println("    Station " + entry.getKey() + " Time: " + entry.getValue());
                                }
                                index++;
                            }
                            System.out.println("Select a train by typing 1, 2, 3, ...");
                            int trainIndex = scanner.nextInt() - 1;
                            if (trainIndex >= 0 && trainIndex < trains.size()) {
                                trainSelectedForBooking = trains.get(trainIndex);
                                System.out.println("Train selected: " + trainSelectedForBooking.getTrainId());
                            } else {
                                System.out.println("Invalid selection.");
                            }
                        } else {
                            System.out.println("Please login first.");
                        }
                        break;
                    case 5:
                        if (loggedInUser != null) {
                            System.out.println("Select a seat out of these seats");
                            List<List<Integer>> seats = userBookingService.fetchSeats(trainSelectedForBooking);
                            for (List<Integer> row: seats){
                                for (Integer val: row){
                                    System.out.print(val+" ");
                                }
                                System.out.println();
                            }
                            System.out.println("Select the seat by typing the row and column");
                            System.out.println("Enter the row");
                            int row = scanner.nextInt();
                            System.out.println("Enter the column");
                            int col = scanner.nextInt();
                            System.out.println("Booking your seat....");
                            Boolean booked = userBookingService.bookTrainSeat(trainSelectedForBooking, row, col);
                            if(booked.equals(Boolean.TRUE)){
                                System.out.println("Booked! Enjoy your journey");
                            }else{
                                System.out.println("Can't book this seat");
                            }
                        } else {
                            System.out.println("Please login first.");
                        }
                        break;
                    case 6:
                        // TODO: Implement cancel booking logic
                        System.out.println("Cancel booking functionality is not yet implemented.");
                        break;
                    case 7:
                        System.out.println("Exiting application.");
                        break;
                    default:
                        System.out.println("Invalid option. Please choose a valid option.");
                        break;
                }
            }
            scanner.close();
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
