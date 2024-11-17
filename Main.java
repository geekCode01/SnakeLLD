package org.example;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;

public class Main {

    //Package Models -> Board, Coordinate, Food, Snake
    public static class Board {
        private final int size; // Store the size of the board, final ensures immutability

        // Constructor to initialize the size, final modifier for clarity and performance
        public Board(int size) {
            this.size = size; // Assign the passed size to the instance variable
        }

        // Getter method to access the board size, efficient and clear naming
        public int getSize() {
            return size; // Return the stored size value
        }
    }

    public static class Coordinate {
        private final int x; // Immutable x-coordinate
        private final int y; // Immutable y-coordinate

        // Constructor initializes x and y values, makes the object immutable
        public Coordinate(int x, int y) {
            this.x = x; // Set x-coordinate
            this.y = y; // Set y-coordinate
        }

        // Getter for x-coordinate
        public int getX() {
            return x; // Return x-coordinate value
        }

        // Getter for y-coordinate
        public int getY() {
            return y; // Return y-coordinate value
        }

        // Override equals method to compare Coordinate objects by their values
        @Override
        public boolean equals(Object o) {
            if (this == o) return true; // Check if same object reference
            if (o == null || getClass() != o.getClass()) return false; // Check for null and class type
            Coordinate that = (Coordinate) o; // Cast object to Coordinate
            return x == that.x && y == that.y; // Compare x and y values
        }

        // Override hashCode method for proper hash code generation
        @Override
        public int hashCode() {
            return Objects.hash(x, y); // Generate hash code based on x and y
        }
    }

    public static class Food {
        private Coordinate position; // Stores the position of the food
        private static final Random random = new Random();

        // Constructor to initialize Food and generate a random position
        public Food(int boardSize) {
            generateNewPosition(boardSize); // Call method to generate a new position
        }

        // Getter for the position of the food
        public Coordinate getPosition() {
            return position; // Return the position of the food
        }

        // Method to generate a new random position within the board size
        public void generateNewPosition(int boardSize) {
            position = new Coordinate(random.nextInt(boardSize), random.nextInt(boardSize)); // Set new position within the board size
        }
    }

    public static class Snake {
        private final LinkedList<Coordinate> body = new LinkedList<>(); // Stores the snake's body segments
        private final int boardSize; // Size of the board

        // Constructor initializes the snake with the given initial size on a board
        public Snake(int initialSize, int boardSize) {
            this.boardSize = boardSize;
            for (int i = 0; i < initialSize; i++) {
                body.addFirst(new Coordinate(0, i)); // Start the snake at the top-left corner
            }
        }

        // Getter for the snake's body
        public LinkedList<Coordinate> getBody() {
            return body;
        }

        // Getter for the snake's head
        public Coordinate getHead() {
            return body.getFirst(); // The first element of the list is the head
        }

        // Moves the snake in the given direction
        public void move(Direction direction) {
            Coordinate newHead = direction.move(getHead(), boardSize); // Calculate the new head position
            body.addFirst(newHead); // Add the new head to the front of the body
            body.removeLast(); // Remove the tail to maintain the size

            // Check if the new head collides with the body
            if (body.stream().skip(1).anyMatch(segment -> segment.equals(newHead))) {
                throw new HitTrailError("Snake hit its own tail!");
            }
        }

        // Grows the snake by adding a segment at the tail
        public void grow() {
            body.addLast(body.getLast()); // Duplicate the last segment to grow the snake
        }
    }

    // Package Factory -> (BoardFactory , SnakeFactory)
    public static class BoardFactory {
        // Private constructor prevents instantiation
        private BoardFactory() {
        }

        // Static method to create and return a new Board instance
        public static Board createBoard(int size) {
            if (size <= 0) {
                throw new IllegalArgumentException("Board size must be positive.");
            }
            return new Board(size);
        }
    }

    public static class SnakeFactory {
        // Private constructor prevents instantiation
        private SnakeFactory() {
        }

        // Static method to create and return a new Snake instance
        public static Snake createSnake(int initialSize, int boardSize) {
            if (initialSize <= 0 || boardSize <= 0) {
                throw new IllegalArgumentException("Size and board size must be positive.");
            }
            return new Snake(initialSize, boardSize);
        }
    }

    public enum Direction {
        UP {
            @Override
            public Coordinate move(Coordinate coordinate, int boardSize) {
                // Move up: Decrease Y coordinate, wrapping around if it goes below 0
                return new Coordinate(coordinate.getX(), (coordinate.getY() - 1 + boardSize) % boardSize);
            }
        },
        DOWN {
            @Override
            public Coordinate move(Coordinate coordinate, int boardSize) {
                // Move down: Increase Y coordinate, wrapping around if it exceeds the board size
                return new Coordinate(coordinate.getX(), (coordinate.getY() + 1) % boardSize);
            }
        },
        LEFT {
            @Override
            public Coordinate move(Coordinate coordinate, int boardSize) {
                // Move left: Decrease X coordinate, wrapping around if it goes below 0
                return new Coordinate((coordinate.getX() - 1 + boardSize) % boardSize, coordinate.getY());
            }
        },
        RIGHT {
            @Override
            public Coordinate move(Coordinate coordinate, int boardSize) {
                // Move right: Increase X coordinate, wrapping around if it exceeds the board size
                return new Coordinate((coordinate.getX() + 1) % boardSize, coordinate.getY());
            }
        };

        // Abstract method to be implemented by each direction
        public abstract Coordinate move(Coordinate coordinate, int boardSize);
    }

    // Package Error -> HitTrailError
    public static class HitTrailError extends RuntimeException{
        public HitTrailError(String message) {
            super(message);
        }
    }

    // Main Class -> Game
    public static class Game {
        private static Game instance; // Singleton instance of the game
        private final Board board; // The game board
        private final Snake snake; // The snake object
        private final Food food; // The food object

        // Private constructor to enforce singleton pattern
        private Game(int boardSize, int initialSnakeSize) {
            this.board = BoardFactory.createBoard(boardSize);
            this.snake = SnakeFactory.createSnake(initialSnakeSize, boardSize);
            this.food = new Food(boardSize);
        }

        // Singleton instance getter
        public static Game getInstance(int boardSize, int initialSnakeSize) {
            if (instance == null) {
                instance = new Game(boardSize, initialSnakeSize);
            }
            return instance;
        }

        // Starts the game loop
        public void start() {
            Scanner scanner = new Scanner(System.in);
            boolean isRunning = true; // Controls the game loop

            try {
                while (isRunning) {
                    printBoard(); // Display the board
                    System.out.println("Enter direction (WASD or Q to quit): ");
                    char directionInput = scanner.next().toUpperCase().charAt(0);

                    // Quit command
                    if (directionInput == 'Q') {
                        System.out.println("Game Over: You quit the game!");
                        isRunning = false; // Exit the game loop
                        continue;
                    }

                    Direction direction = getDirectionFromInput(directionInput);

                    if (direction != null) {
                        snake.move(direction);

                        // Check if the snake eats the food
                        if (snake.getHead().equals(food.getPosition())) {
                            snake.grow();
                            food.generateNewPosition(board.getSize());
                        }
                    } else {
                        System.out.println("Invalid input! Use W, A, S, D, or Q to quit.");
                    }
                }
            } catch (HitTrailError e) {
                System.out.println("Game Over: " + e.getMessage());
            }
        }

        // Maps user input to a Direction
        private Direction getDirectionFromInput(char input) {
            return switch (input) {
                case 'W' -> Direction.UP;
                case 'A' -> Direction.LEFT;
                case 'S' -> Direction.DOWN;
                case 'D' -> Direction.RIGHT;
                default -> null; // Invalid input
            };
        }

        // Prints the game board
        private void printBoard() {
            int size = board.getSize();
            char[][] boardArray = new char[size][size];

            // Initialize the board with empty spaces
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    boardArray[i][j] = '.';
                }
            }

            // Place the snake on the board
            for (Coordinate c : snake.getBody()) {
                boardArray[c.getY()][c.getX()] = 'S';
            }

            // Place the food on the board
            Coordinate foodPosition = food.getPosition();
            boardArray[foodPosition.getY()][foodPosition.getX()] = 'F';

            // Print the board to the console
            for (char[] row : boardArray) {
                for (char cell : row) {
                    System.out.print(cell + " ");
                }
                System.out.println();
            }
            System.out.println();
        }

        // Entry point of the application
        public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);

            // Prompt user for game settings
            System.out.println("Enter board size: ");
            int boardSize = scanner.nextInt();
            System.out.println("Enter initial snake size: ");
            int initialSnakeSize = scanner.nextInt();

            // Initialize and start the game
            Game game = Game.getInstance(boardSize, initialSnakeSize);
            game.start();
        }
    }

}