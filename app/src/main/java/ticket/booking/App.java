package ticket.booking;

import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.services.UserBookingService;
import ticket.booking.util.UserServiceUtil;

import java.io.IOException;
import java.util.*;

public class App {

    public static void main(String[] args) {
        try {
            System.out.println("Running Train Booking System");
            Scanner scanner = new Scanner(System.in);
            int option = 0;
            UserBookingService userBookingService;
            User loggedInUser = null;
            String username;
            String password;

            try {
                userBookingService = new UserBookingService(null);
            } catch (IOException ex) {
                System.out.println("Error initializing booking service: " + ex.getMessage());
                ex.printStackTrace();
                return;
            }

            while (option != 7) {
                if (loggedInUser == null) {
                    System.out.println("Choose option:");
                    System.out.println("1. Sign up");
                    System.out.println("2. Login");
                    System.out.println("7. Exit the App");
                } else {
                    System.out.println("Choose option:");
                    System.out.println("3. Fetch Bookings");
                    System.out.println("4. Search Trains");
                    System.out.println("5. Cancel my Booking");
                    System.out.println("6. Logout");
                    System.out.println("7. Exit the App");
                }

                try {
                    option = scanner.nextInt(); // Read user input
                    scanner.nextLine(); // Consume newline character after reading integer input

                    switch (option) {
                        case 1:
                            System.out.println("Enter username to signup:");
                            username = scanner.next();
                            System.out.println("Enter password to signup:");
                            password = scanner.next();
                            User userToSignup = new User(username, password,
                                    UserServiceUtil.hashPassword(password), new ArrayList<>(),
                                    UUID.randomUUID().toString());
                            assert userBookingService != null;
                            if (userBookingService.signUp(userToSignup)) {
                                System.out.println("Sign up successful!");
                            } else {
                                System.out.println("Sign up failed. Please try again.");
                            }
                            break;
                        case 2:
                            System.out.println("Enter username to Login:");
                            username = scanner.next();
                            System.out.println("Enter password to Login:");
                            password = scanner.next();
                            loggedInUser = new User(username, password,
                                    UserServiceUtil.hashPassword(password)
                                    , new ArrayList<>(), UUID.randomUUID().toString());
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
                                assert userBookingService != null;
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
                                assert userBookingService != null;
                                List<Train> trains = userBookingService.getTrains(source, dest);

                                if (trains.isEmpty()) {
                                    System.out.println("No trains found between " + source + " and " + dest);
                                } else {
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
                                        Train trainSelectedForBooking = trains.get(trainIndex);
                                        System.out.println("Train selected: " + trainSelectedForBooking.getTrainId());

                                        // Prompt user for further action
                                        System.out.println("1. Book a seat");
                                        System.out.println("2. Go back to main menu");
                                        int choice = scanner.nextInt();

                                        switch (choice) {
                                            case 1:
                                                // Book a seat logic (to be implemented)
                                                    System.out.println("Select a seat out of these seats:");
                                                    List<List<Integer>> seats = userBookingService.fetchSeats(trainSelectedForBooking);

                                                    // Print available seats
                                                    for (List<Integer> row : seats) {
                                                        for (Integer val : row) {
                                                            System.out.print(val + " ");
                                                        }
                                                        System.out.println();
                                                    }

                                                    // Prompt user for seat selection
                                                    System.out.println("Select the seat by typing the row and column:");
                                                    System.out.println("Enter the row:");
                                                    int row = scanner.nextInt();
                                                    System.out.println("Enter the column:");
                                                    int col = scanner.nextInt();

                                                    // Attempt to book the seat
                                                    System.out.println("Booking your seat...");
                                                    Boolean booked =                                                             userBookingService.bookTrainSeat(trainSelectedForBooking,
                                                                    row-1, col-1, source, dest);

                                                    // Display booking result
                                                    if (booked.equals(Boolean.TRUE)) {
                                                        System.out.println("Booked! Enjoy your journey.");
                                                    } else {
                                                        System.out.println("Can't book this seat.");
                                                    }
                                                break;
                                            case 2:
                                                // Go back to main menu
                                                break;
                                            default:
                                                System.out.println("Invalid choice. Going back to main menu.");
                                                continue;
                                        }

                                    } else {
                                        System.out.println("Invalid selection.");
                                    }
                                }
                            } else {
                                System.out.println("Please login first.");
                            }
                            break;

                        case 5:
                            // TODO: Implement cancel booking logic
                            System.out.println("Cancel booking functionality is not yet implemented.");
                            break;
                        case 6:
                            if (loggedInUser != null) {
                                assert userBookingService != null;
                                userBookingService.logout();
                                loggedInUser = null;
                                userBookingService = null;
                            } else {
                                System.out.println("You are not logged in.");
                            }
                            break;
                        case 7:
                            System.out.println("Exiting application.");
                            break;
                        default:
                            System.out.println("Invalid option. Please choose a valid option.");
                    }

                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a valid option.");
                    scanner.nextLine(); // Consume newline character after invalid input
                }
            }

            scanner.close(); // Close the scanner when done

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
