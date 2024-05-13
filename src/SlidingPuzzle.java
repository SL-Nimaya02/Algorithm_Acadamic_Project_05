/*
 * Student ID: 20221392/w1956125
 * Student Name: I.M.N.Hansani 
 */

package src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Main class for solving sliding puzzle problem.
 */
public class SlidingPuzzle {

    /**
     * Main method to initiate the puzzle solving process.
     * param:- args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        // Specify the path to the input file
        String filePath = "puzzle_2560.txt";
        processInputFile(new File(filePath));
    }

    /**
     * Process the input file to read the puzzle and solve it.
     * param:- file The input file containing the puzzle.
     */
    private static void processInputFile(File file) {
        if (!file.exists()) {
            System.err.println("Error: File not found.");
            return;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            // Parse map dimensions and content
            List<String> lines = new ArrayList<>();
            String line;
            int width = -1;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    System.err.println("Error: Empty line found in input file.");
                    reader.close();
                    return;
                }
                lines.add(line);
                if (width == -1) {
                    width = line.length();
                } else if (line.length() != width) {
                    System.err.println("Error: Inconsistent line lengths in input file.");
                    reader.close();
                    return;
                }
            }
            int height = lines.size();
            if (height == 0 || width == -1) {
                System.err.println("Error: Empty input file.");
                reader.close();
                return;
            }
            /**
             * Create a 2D array to represent the map.
             * The array dimensions are determined by the height (number of rows) and width
             * (number of columns) of the map.
             * Each cell of the array will hold a character representing the contents of the
             * corresponding square on the map.
             * 'height' represents the number of rows, and 'width' represents the number of
             * columns.
             */
            char[][] map = new char[height][width];

            // Find start and finish positions
            int startRow = -1, startCol = -1, finishRow = -1, finishCol = -1;
            for (int row = 0; row < height; row++) {
                line = lines.get(row);
                for (int col = 0; col < width; col++) {
                    char square = line.charAt(col);
                    map[row][col] = square;

                    if (square == 'S') {
                        if (startRow != -1 || startCol != -1) {
                            System.err.println("Error: Multiple start positions found in input file.");
                            reader.close();
                            return;
                        }
                        startRow = row;
                        startCol = col;
                    } else if (square == 'F') {
                        if (finishRow != -1 || finishCol != -1) {
                            System.err.println("Error: Multiple finish positions found in input file.");
                            reader.close();
                            return;
                        }
                        finishRow = row;
                        finishCol = col;
                    } else if (square != '.' && square != '0') {
                        System.err.println("Error: Invalid character '" + square + "' found in input file.");
                        reader.close();
                        return;
                    }
                }
            }

            reader.close();
            // Check if start and finish positions are found
            if (startRow == -1 || startCol == -1) {
                System.err.println("Error: Start position 'S' not found in input file.");
                return;
            }
            if (finishRow == -1 || finishCol == -1) {
                System.err.println("Error: Finish position 'F' not found in input file.");
                return;
            }

            // Solve the puzzle and output steps
            solvePuzzle(map, startRow, startCol, finishRow, finishCol);

        } catch (IOException e) {
            // Handle the case where the input is not a valid integer
            System.err.println("Error reading the input file: " + e.getMessage());
        }
    }

    /**
     * Solve the sliding puzzle using A* search algorithm.
     * param:- map The map representing the puzzle.
     * param:- startRow The row of the start position.
     * param:- startCol The column of the start position.
     * param:- finishRow The row of the finish position.
     * param:- finishCol The column of the finish position.
     */
    private static void solvePuzzle(char[][] map, int startRow, int startCol, int finishRow, int finishCol) {
        int height = map.length;
        int width = map[0].length;

        // Initialize data structures for pathfinding
        Queue<Node> openList = new LinkedList<>();
        Map<String, Node> allNodes = new HashMap<>();
        Set<Node> closedList = new HashSet<>();

        // Add start node to the open list
        Node startNode = new Node(startRow, startCol, null, 0);
        openList.offer(startNode);
        allNodes.put(startNode.getKey(), startNode);

        // Define directions: up, down, left, right
        int[][] directions = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

        // A* search
        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();
            closedList.add(currentNode);

            if (currentNode.getRow() == finishRow && currentNode.getCol() == finishCol) {
                // Path found, reconstruct and print the path
                printPath(map, startRow, startCol, currentNode);
                return;
            }

            for (int[] direction : directions) {
                int newRow = currentNode.getRow();
                int newCol = currentNode.getCol();
                int steps = currentNode.getSteps() + 1;

                // Keep sliding in the chosen direction until hitting a wall, rock, or boundary
                while (newRow + direction[0] >= 0 && newRow + direction[0] < height &&
                        newCol + direction[1] >= 0 && newCol + direction[1] < width &&
                        map[newRow + direction[0]][newCol + direction[1]] != '0') {
                    newRow += direction[0];
                    newCol += direction[1];
                    // If the current position is ice ('.'), keep sliding in the same direction
                    if (map[newRow][newCol] == '.') {
                        steps++;
                    } else {
                        break; // Exit loop if not ice
                    }
                }

                Node neighbor = new Node(newRow, newCol, currentNode, steps);

                if (!closedList.contains(neighbor)) {
                    if (!allNodes.containsKey(neighbor.getKey())
                            || steps < allNodes.get(neighbor.getKey()).getSteps()) {
                        openList.offer(neighbor);
                        allNodes.put(neighbor.getKey(), neighbor);
                    }
                }
            }

        }

        // If no path is found
        System.out.println("No solution found.");
    }

    /**
     * Print the solution path of the puzzle.
     * param:- map The map representing the puzzle.
     * param:- startRow The row of the start position.
     * param:- startCol The column of the start position.
     * param:- finalNode The final node of the solution path.
     */
    private static void printPath(char[][] map, int startRow, int startCol, Node finalNode) {
        long startTime = System.currentTimeMillis();
        System.out.println("");
        System.out.println(" -----------------------------------------------------------------------------");
        System.out.println("|                               Start The Game!                               |");
        System.out.println(" -----------------------------------------------------------------------------");
        System.out.println("");
        System.out.println("Shortest path:");
        System.out.println("");
        System.out.println("1. Start at (" + (startCol + 1) + "," + (startRow + 1) + ")");
        List<String> path = new ArrayList<>();
        Node currentNode = finalNode;
        while (currentNode != null) {
            path.add(0, currentNode.getRow() + "," + currentNode.getCol());
            currentNode = currentNode.getParent();
        }
        for (int i = 0; i < path.size() - 1; i++) {
            String[] currentCoordinates = path.get(i).split(",");
            String[] nextCoordinates = path.get(i + 1).split(",");
            int currentRow = Integer.parseInt(currentCoordinates[0]);
            int currentCol = Integer.parseInt(currentCoordinates[1]);
            int nextRow = Integer.parseInt(nextCoordinates[0]);
            int nextCol = Integer.parseInt(nextCoordinates[1]);

            String direction;
            if (nextRow > currentRow) {
                direction = "down";
            } else if (nextRow < currentRow) {
                direction = "up";
            } else if (nextCol > currentCol) {
                direction = "right";
            } else {
                direction = "left";
            }

            System.out.println((i + 2) + ". Move " + direction + " to (" + (nextCol + 1) + "," + (nextRow + 1) + ")");
        }
        System.out.println(path.size() + 1 + ". Done!");
        System.out.println("");
        long endTime = System.currentTimeMillis(); // Record end time
        long elapsedTime = endTime - startTime; // Calculate elapsed time
        System.out.println("Puzzle solved in " + elapsedTime + " milliseconds.");
        System.out.println("");
        System.out.println(" -----------------------------------------------------------------------------");
        System.out.println("|                    You can try the next level of the game!                  |");
        System.out.println(" -----------------------------------------------------------------------------");
        System.out.println("");
    }

    /**
     * Represents a node in the puzzle solving algorithm.
     */
    static class Node {
        private int row;
        private int col;
        private Node parent;
        private int steps;

        /**
         * Constructor to initialize a node.
         * param:- row The row of the node.
         * param;- col The column of the node.
         * param:- parent The parent node.
         * param:- steps The number of steps taken to reach this node.
         */
        public Node(int row, int col, Node parent, int steps) {
            this.row = row;
            this.col = col;
            this.parent = parent;
            this.steps = steps;
        }

        // Getters and setters for Node properties
        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        public Node getParent() {
            return parent;
        }

        public int getSteps() {
            return steps;
        }

        public String getKey() {
            return row + "," + col;
        }
    }
}