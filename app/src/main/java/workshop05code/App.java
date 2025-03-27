package workshop05code;

import java.io.*;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.*;

/**
 * Wordle-style game with database-backed word validation.
 */
public class App {

    private static final Logger logger = Logger.getLogger(App.class.getName());

    static {
        try {
            LogManager.getLogManager().reset();
            FileHandler fileHandler = new FileHandler("wordle.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);

            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.SEVERE);  // Only severe logs appear in console

            logger.addHandler(fileHandler);
            logger.addHandler(consoleHandler);
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            System.err.println("Error setting up logging. Logs will not be saved to file.");
        }
    }

    public static void main(String[] args) {
        SQLiteConnectionManager wordleDatabaseConnection = new SQLiteConnectionManager("words.db");

        wordleDatabaseConnection.createNewDatabase("words.db");
        if (!wordleDatabaseConnection.checkIfConnectionDefined()) {
            logger.severe("Database connection failed.");
            System.out.println("Error: Could not connect to the database.");
            return;
        }

        if (!wordleDatabaseConnection.createWordleTables()) {
            logger.severe("Failed to create database tables.");
            System.out.println("Error: Could not initialize the game.");
            return;
        }

        // Load words from file
        try (BufferedReader br = new BufferedReader(new FileReader("resources/data.txt"))) {
            String line;
            int i = 1;
            while ((line = br.readLine()) != null) {
                if (isValidWordFormat(line)) {
                    wordleDatabaseConnection.addValidWord(i, line);
                    logger.info("Valid word added: " + line);
                    i++;
                } else {
                    logger.severe("Invalid word found in data.txt: " + line);
                }
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to load words from file.", e);
            System.out.println("Error: Could not load words. Please check the file.");
            return;
        }

        // Game loop
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("Enter a 4-letter word for a guess or 'q' to quit: ");
                String guess = scanner.nextLine().trim();

                if (guess.equalsIgnoreCase("q")) {
                    break;
                }

                if (!isValidWordFormat(guess)) {
                    logger.warning("Invalid guess: " + guess);
                    System.out.println("Invalid input. Please enter a valid 4-letter word.");
                    continue;
                }

                if (wordleDatabaseConnection.isValidWord(guess)) {
                    System.out.println("Success! It is in the list.");
                } else {
                    System.out.println("Sorry, this word is NOT in the list.");
                    logger.warning("User entered an invalid guess: " + guess);
                }
            }
        } catch (NoSuchElementException | IllegalStateException e) {
            logger.log(Level.WARNING, "Error reading user input.", e);
            System.out.println("Error: Unable to process input.");
        }
    }

    /**
     * Checks if a word is a valid 4-letter format.
     *
     * @param word The word to check.
     * @return True if the word is valid, otherwise false.
     */
    private static boolean isValidWordFormat(String word) {
        return word != null && word.matches("^[a-zA-Z]{4}$");
    }
}
