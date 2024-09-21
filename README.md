
# LightEmAll

## Overview
LightEmAll is a Java-based puzzle game built using object-oriented principles. The objective of the game is to rotate and connect the game pieces (nodes) on a grid to ensure that the power station lights up all connected pieces. The game uses a breadth-first search (BFS) algorithm for distributing power and incorporates a minimum spanning tree (MST) to generate the initial board layout. It is implemented using **Java**, with graphical rendering handled by the **javalib.impworld** library.

## Key Features
- **Randomized Grid Generation**: The game creates a grid of game pieces that can be randomly oriented to form a solvable puzzle.
- **Minimum Spanning Tree (MST)**: Kruskal's algorithm is used to generate an MST, ensuring that all pieces can eventually be connected.
- **Interactive Gameplay**: Players can rotate pieces by clicking on them to align connections and move the power station using arrow keys.
- **Win Condition**: The game ends when the player successfully connects and powers all the game pieces.

## Technologies Used
1. **Java**: Core logic and data structures for managing the game pieces, power distribution, and interactions.
2. **Java Libraries**:
   - `javalib.impworld`: Provides support for creating a graphical world with event handling.
   - `java.awt.Color`: Used for setting colors in the game's graphical components.
   - `javalib.worldimages`: Provides tools for rendering game components visually, like the power station, wires, and tiles.
3. **Data Structures**:
   - `ArrayList`, `Queue`, `HashSet`: Used for storing game pieces, tracking visited nodes, and managing BFS for power distribution.
   - `HashMap`: Used in union-find for managing MST creation and connections between nodes.

## Game Mechanics
- **Game Board**: The game board consists of a 2D grid of `GamePiece` objects. Each piece has four possible connections: top, bottom, left, and right, which can be rotated by clicking.
- **Power Station**: The player starts at a fixed power station, and their goal is to distribute power to the entire grid by rotating the pieces to create connections.
- **User Input**:
   - Mouse clicks rotate game pieces.
   - Arrow keys move the power station to different positions on the grid.
   
## Algorithms
- **Breadth-First Search (BFS)**: Used to distribute power from the power station to connected game pieces.
- **Kruskal’s Algorithm**: Employed to generate the MST, ensuring all pieces can be connected to form a solvable puzzle.
  
## Classes
1. **`LightEmAll`**: The main class managing the game world, rendering the board, handling mouse and keyboard inputs, and checking the win condition.
2. **`GamePiece`**: Represents individual game pieces on the board. Each piece has four possible connections (top, right, bottom, left) and can be rotated.
3. **`Edge`**: Represents connections between game pieces in the MST.
4. **`UnionDataCreate`**: Manages the union-find algorithm for Kruskal’s MST algorithm.
5. **`ExamplesLight`**: Contains test cases for various game mechanics and features using the Tester library.

## Setup and Instructions
### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Java libraries: javalib.impworld, javalib.worldimages, Tester (Ensure all necessary dependencies are installed)

### Running the Game
1. **Compile the Code**: 
   Ensure that all Java files are compiled:
   ```bash
   javac LightEmAll.java
   ```
   
2. **Start the Game**:
   Once compiled, run the game with the following command:
   ```bash
   java LightEmAll
   ```

3. **Gameplay**:
   - Use the mouse to click on game pieces to rotate them.
   - Use the arrow keys to move the power station across the grid.
   - The goal is to connect and power all game pieces.

## Testing
Unit tests are written using the `Tester` library. To run the tests:
1. Compile the test class:
   ```bash
   javac ExamplesLight.java
   ```
   
2. Run the tests:
   ```bash
   java ExamplesLight
   ```

## Future Improvements
- **Difficulty Levels**: Add more challenging levels with larger grids and more complex connections.
- **Score Tracking**: Implement a system to track the number of moves taken and create a leaderboard.
- **User Interface Enhancements**: Improve the visual design and responsiveness of the game for better user experience.

