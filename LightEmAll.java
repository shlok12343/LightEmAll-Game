import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiConsumer;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.Random;
import java.util.HashMap;

// creats a lightemALL
class LightEmAll extends World {
  // a list of columns of GamePieces,
  // i.e., represents the board in column-major order
  ArrayList<ArrayList<GamePiece>> board;
  // a list of all nodes
  ArrayList<GamePiece> nodes;
  // a list of edges of the minimum spanning tree
  ArrayList<Edge> mst;
  // the width and height of the board
  int width;
  int height;
  // the current location of the power station,
  // as well as its effective radius
  int powerRow;
  int powerCol;
  int radius;
  int row;
  int col;
  int size;
  Random rand;
  boolean testgrid;
  WorldScene startingScene;

  LightEmAll(int width, int height, int size) {
    this.width = width;
    this.height = height;
    this.row = height / size;
    this.col = width / size;
    this.size = size;
    this.board = new ArrayList<>();
    this.nodes = new ArrayList<>();
    this.mst = new ArrayList<>();
    this.powerCol = 0;
    this.powerRow = 0;
    this.radius = 0;
    this.rand = new Random();
    this.startingScene = new WorldScene(width, height);

    makeGrid();

    minimumSpaningTree();
    newConnectBoards();

    randomGamePice();

  }

  LightEmAll(int width, int height, int size, Random rand) {
    this.width = width;
    this.height = height;
    this.row = height / size;
    this.col = width / size;
    this.size = size;
    this.board = new ArrayList<>();
    this.nodes = new ArrayList<>();
    this.mst = new ArrayList<>();
    this.powerCol = 0;
    this.powerRow = 0;
    this.radius = 0;
    this.rand = rand;
    this.startingScene = new WorldScene(width, height);

    makeGrid();

    minimumSpaningTree();
    newConnectBoards();

    randomGamePiceSeeded();

  }

  LightEmAll(int width, int height, int size, boolean testgrid) {
    this.width = width;
    this.height = height;
    this.row = height / size;
    this.col = width / size;
    this.size = size;
    this.board = new ArrayList<>();
    this.nodes = new ArrayList<>();
    this.powerCol = 0;
    this.powerRow = 0;
    this.radius = 0;
    this.testgrid = testgrid;

    makeGrid();

  }

  void makeGrid() {

    this.board = new ArrayList<>();

    for (int i = 0; i < this.row; i++) {

      ArrayList<GamePiece> rows = new ArrayList<>();

      for (int j = 0; j < this.col; j++) {

        GamePiece piece = new GamePiece(i, j, false, false, false, false);

        if (i == 0 && j == 0) {

          piece.station();

          piece.power();

        }

        rows.add(piece);

        nodes.add(piece);

      }

      this.board.add(rows);

    }
  }

  // random game pices
  void randomGamePice() {
    for (ArrayList<GamePiece> t : this.board) {
      for (GamePiece g : t) {
        int r = this.rand.nextInt(4);
        for (int i = 0; r > i; i++) {
          g.rotate();
        }
      }
    }
  }

  // random seeded game pice
  void randomGamePiceSeeded() {
    for (ArrayList<GamePiece> t : this.board) {
      for (GamePiece g : t) {
        int r = this.rand.nextInt(4);
        for (int i = 0; r > i; i++) {
          g.rotate();
        }
      }
    }
  }

  // check win codiion for ending game state
  boolean checkWinCondition() {
    Queue<GamePiece> queue = new LinkedList<>();
    HashSet<GamePiece> visited = new HashSet<>();

    // Start from the power station
    GamePiece start = this.board.get(powerRow).get(powerCol);
    queue.add(start);
    visited.add(start);

    while (!queue.isEmpty()) {
      GamePiece current = queue.remove();

      // Iterate over all possible moves from the current piece
      List<GamePiece> neighbors = current.getConnectedNeighbors(this.board);
      for (GamePiece neighbor : neighbors) {
        if (!visited.contains(neighbor)) {
          queue.add(neighbor);
          visited.add(neighbor);
        }
      }
    }

    return visited.size() == this.row * this.col;
  }

  // BFS algorithm for power distribution
  void distributePowerBFS() {
    Queue<GamePiece> queue = new LinkedList<>();

    // Reset all GamePieces to unpowered
    for (ArrayList<GamePiece> col : board) {
      for (GamePiece cell : col) {
        cell.powered = false;
      }
    }

    // Power the initial GamePiece and add it to the queue
    GamePiece powerStation = board.get(powerRow).get(powerCol);
    powerStation.power();
    queue.add(powerStation);

    while (!queue.isEmpty()) {
      GamePiece current = queue.remove();

      // Helper lambda to check connection and power
      BiConsumer<Integer, Integer> tryPower = (Integer rowOffset, Integer colOffset) -> {
        int newRow = current.row + rowOffset;
        int newCol = current.col + colOffset;
        if (newRow >= 0 && newRow < this.row && newCol >= 0 && newCol < this.col) {
          GamePiece neighbor = board.get(newRow).get(newCol);
          // Check if the neighbor can be powered from the current GamePiece
          if (current.isConnected(neighbor) && !neighbor.powered) {
            neighbor.power();
            queue.add(neighbor);
          }
        }
      };

      // Try to power the neighbors
      tryPower.accept(-1, 0); // Up
      tryPower.accept(1, 0); // Down
      tryPower.accept(0, -1); // Left
      tryPower.accept(0, 1); // Right
    }
  }

  // creating world scene
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(this.width, this.height);
    WorldImage cell = null;

    for (int i = 0; i < row; i++) {
      for (int j = 0; j < col; j++) {

        if (powerRow == i && powerCol == j) {
          cell = this.board.get(i).get(j).draw(this.size, true);
        }
        else {
          cell = this.board.get(i).get(j).draw(this.size, false);
        }

        scene.placeImageXY(cell, j * size + size / 2, i * size + size / 2);
      }
    }

    return scene;

  }

  // clicking to rotate
  public void onMouseClicked(Posn pos) {
    int x = pos.x / size;
    int y = pos.y / size;

    this.board.get(y).get(x).rotate();

    // Reset all pieces to unpowered state except for the power station
    resetPowerStates();
    // Redistribute power
    distributePowerBFS();
  }

  void resetPowerStates() {
    for (ArrayList<GamePiece> row : board) {
      for (GamePiece piece : row) {
        piece.powered = false;
      }
    }
    // Ensure the power station itself is powered
    board.get(powerRow).get(powerCol).powered = true;

    if (checkWinCondition()) {
      // Player has won

      this.endOfWorld("Win");
      this.lastScene("Gameover");// Ends the game and prints the message
    }
  }

  public WorldScene lastScene(String msg) {
    this.startingScene.placeImageXY(
        new TextImage("Game over you won", 15, FontStyle.BOLD_ITALIC, Color.RED), this.width / 2,
        this.height / 2);
    return this.startingScene;
  }

  // moving the power station with keys
  public void onKeyEvent(String key) {
    boolean moved = false;
    if (key.equals("up") && powerRow > 0 && this.board.get(powerRow - 1).get(powerCol).bottom
        && this.board.get(powerRow).get(powerCol).top) {
      powerRow--;
      moved = true;
    }
    else if (key.equals("down") && powerRow < row - 1
        && this.board.get(powerRow + 1).get(powerCol).top
        && this.board.get(powerRow).get(powerCol).bottom) {
      powerRow++;
      moved = true;
    }
    else if (key.equals("right") && powerCol < col - 1
        && this.board.get(powerRow).get(powerCol + 1).left
        && this.board.get(powerRow).get(powerCol).right) {
      powerCol++;
      moved = true;
    }
    else if (key.equals("left") && powerCol > 0 && this.board.get(powerRow).get(powerCol - 1).right
        && this.board.get(powerRow).get(powerCol).left) {
      powerCol--;
      moved = true;
    }

    if (moved) {
      // Reset all pieces to state except for the power station
      resetPowerStates();
      // Redistribute power
      distributePowerBFS();
    }
  }

  // gets all neiebours of pices

  ArrayList<GamePiece> neighbors(GamePiece piece) {

    ArrayList<GamePiece> neighbors = new ArrayList<>();

    int numr = piece.row;

    int numc = piece.col;

    if (piece.top && numr > 0 && board.get(numr - 1).get(numc).bottom) {

      neighbors.add(board.get(numr - 1).get(numc));

    }

    if (piece.right && numc < col - 1 && board.get(numr).get(numc + 1).left) {

      neighbors.add(board.get(numr).get(numc + 1));

    }

    if (piece.bottom && numr < row - 1 && board.get(numr + 1).get(numc).top) {

      neighbors.add(board.get(numr + 1).get(numc));

    }

    if (piece.left && numc > 0 && board.get(numr).get(numc - 1).right) {

      neighbors.add(board.get(numr).get(numc - 1));

    }

    return neighbors;

  }

  // Creats all nodes as sets and all possible edges
  void minimumSpaningTree() {

    ArrayList<Edge> edges = new ArrayList<>();

    UnionDataCreate uf = new UnionDataCreate();

    for (GamePiece node : nodes) {

      uf.createSet(node);

      int nodeRow = node.row;

      int nodeCol = node.col;

      if (nodeCol + 1 < col) { // right

        edges.add(new Edge(node, board.get(nodeRow).get(nodeCol + 1), rand.nextInt(100)));

      }

      if (nodeRow + 1 < row) { // down

        edges.add(new Edge(node, board.get(nodeRow + 1).get(nodeCol), rand.nextInt(100)));

      }

    }

    Collections.sort(edges, Comparator.comparingInt(e -> e.weight));

    // Applying Kruskal's Algorithm

    for (Edge edge : edges) {

      if (uf.findNode(edge.fromNode) != uf.findNode(edge.toNode)) {

        mst.add(edge);

        uf.unionData(edge.fromNode, edge.toNode);

      }

    }

  }

  void newConnectBoards() {

    for (Edge edges : mst) {

      GamePiece fromedge = edges.fromNode;

      GamePiece toedge = edges.toNode;

      if (fromedge.row == toedge.row) {

        if (fromedge.col < toedge.col) {

          fromedge.right = true;

          toedge.left = true;

        }
        else {

          fromedge.left = true;

          toedge.right = true;

        }

      }
      else if (fromedge.col == toedge.col) {

        if (fromedge.row < toedge.row) {

          fromedge.bottom = true;

          toedge.top = true;

        }
        else {

          fromedge.top = true;

          toedge.bottom = true;

        }

      }

    }

  }

}

// creats gamepices 
class GamePiece {
  // in logical coordinates, with the origin
  // at the top-left corner of the screen
  int row;
  int col;
  // whether this GamePiece is connected to the
  // adjacent left, right, top, or bottom pieces
  boolean left;
  boolean right;
  boolean top;
  boolean bottom;
  // whether the power station is on this piece
  boolean powerStation;
  boolean powered;

  GamePiece(int row, int col, boolean left, boolean right, boolean top, boolean bottom) {
    this.row = row;
    this.col = col;
    this.left = left;
    this.right = right;
    this.top = top;
    this.bottom = bottom;
    this.powerStation = false;
    this.powered = false;
  }

  // makes the game pice a station
  public void station() {
    this.powerStation = true;

  }

  // draws the world imgae
  public WorldImage draw(int size, boolean station) {
    // TODO Auto-generated method stub
    if (this.powered) {
      return this.tileImage(size, size / 6, Color.YELLOW, station);
    }
    else {
      return this.tileImage(size, size / 6, Color.LIGHT_GRAY, station);
    }

  }

  // Checks if this GamePiece is connected to another GamePiece
  boolean isConnected(GamePiece other) {
    // Check all four directions for a connection
    if (this.row == other.row - 1 && this.col == other.col && this.bottom && other.top) {
      return true;
    }
    if (this.row == other.row + 1 && this.col == other.col && this.top && other.bottom) {
      return true;
    }
    if (this.col == other.col - 1 && this.row == other.row && this.right && other.left) {
      return true;
    }
    return this.col == other.col + 1 && this.row == other.row && this.left && other.right;

  }

  // gets all connected Neighbors nearby
  List<GamePiece> getConnectedNeighbors(ArrayList<ArrayList<GamePiece>> board) {
    List<GamePiece> neighbors = new ArrayList<>();

    // Use this object's properties, because 'this' refers to the current GamePiece
    // instance
    if (this.top && this.row > 0 && board.get(this.row - 1).get(this.col).isConnected(this)) {
      neighbors.add(board.get(this.row - 1).get(this.col));
    }
    if (this.bottom && this.row < board.size() - 1
        && board.get(this.row + 1).get(this.col).isConnected(this)) {
      neighbors.add(board.get(this.row + 1).get(this.col));
    }
    if (this.left && this.col > 0 && board.get(this.row).get(this.col - 1).isConnected(this)) {
      neighbors.add(board.get(this.row).get(this.col - 1));
    }
    if (this.right && this.col < board.get(0).size() - 1
        && board.get(this.row).get(this.col + 1).isConnected(this)) {
      neighbors.add(board.get(this.row).get(this.col + 1));
    }

    return neighbors;
  }

  // Rotate all the comments
  public void rotate() {
    boolean previousTop = this.top;
    boolean previousBottom = this.bottom;
    boolean previousLeft = this.left;
    boolean previousRight = this.right;

    // Rotate 90 degrees clockwise
    this.top = previousLeft;
    this.right = previousTop;
    this.bottom = previousRight;
    this.left = previousBottom;
  }

  void power() {
    this.powered = true;
  }

  // Generate an image of this, the given GamePiece.
  // - size: the size of the tile, in pixels
  // - wireWidth: the width of wires, in pixels
  // - wireColor: the Color to use for rendering wires on this
  // - hasPowerStation: if true, draws a fancy star on this tile to represent the
  // power station
  //
  WorldImage tileImage(int size, int wireWidth, Color wireColor, boolean hasPowerStation) {
    // Start tile image off as a blue square with a wire-width square in the middle,
    // to make image "cleaner" (will look strange if tile has no wire, but that
    // can't be)
    WorldImage image = new OverlayImage(
        new RectangleImage(wireWidth, wireWidth, OutlineMode.SOLID, wireColor),
        new RectangleImage(size, size, OutlineMode.SOLID, Color.DARK_GRAY));
    WorldImage vWire = new RectangleImage(wireWidth, (size + 1) / 2, OutlineMode.SOLID, wireColor);
    WorldImage hWire = new RectangleImage((size + 1) / 2, wireWidth, OutlineMode.SOLID, wireColor);

    if (this.top) {
      image = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP, vWire, 0, 0, image);
    }

    if (this.right) {
      image = new OverlayOffsetAlign(AlignModeX.RIGHT, AlignModeY.MIDDLE, hWire, 0, 0, image);

    }

    if (this.bottom) {
      image = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM, vWire, 0, 0, image);

    }
    if (this.left) {
      image = new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.MIDDLE, hWire, 0, 0, image);
    }

    if (hasPowerStation) {
      image = new OverlayImage(
          new OverlayImage(new StarImage(size / 3, 7, OutlineMode.OUTLINE, new Color(255, 128, 0)),
              new StarImage(size / 3, 7, OutlineMode.SOLID, new Color(0, 255, 255))),
          image);
    }
    return image;
  }

}

class Edge {

  GamePiece fromNode;

  GamePiece toNode;

  int weight;

  Edge(GamePiece fromNode, GamePiece toNode, int weight) {

    this.fromNode = fromNode;

    this.toNode = toNode;

    this.weight = weight;

  }
}

class UnionDataCreate {

  private HashMap<GamePiece, GamePiece> parent = new HashMap<>();

  public void createSet(GamePiece node) {

    parent.put(node, node);

  }

  public GamePiece findNode(GamePiece node) {

    GamePiece r = node;

    while (r != parent.get(r)) {

      r = parent.get(r);

    }

    while (node != r) {

      GamePiece rest = parent.get(node);

      parent.put(node, r);

      node = rest;

    }

    return r;

  }

  public void unionData(GamePiece node1, GamePiece node2) {

    GamePiece r1 = findNode(node1);

    GamePiece r2 = findNode(node2);

    if (r1 != r2) {

      parent.put(r1, r2);

    }

  }

}

class ExamplesLight {

  ExamplesLight() {
  }

  LightEmAll ex1 = new LightEmAll(400, 400, 100);
  GamePiece p1 = new GamePiece(0, 0, true, true, false, false);
  GamePiece p2 = new GamePiece(1, 0, false, true, true, true);
  GamePiece p3 = new GamePiece(0, 1, true, false, true, false);

  GamePiece p4 = new GamePiece(0, 0, true, true, false, false);
  GamePiece p5 = new GamePiece(1, 0, false, true, true, true);
  GamePiece p6 = new GamePiece(0, 1, true, false, true, false);

  void testMakeGrid(Tester t) {
    // Create a new game with specific dimensions
    LightEmAll game = new LightEmAll(400, 400, 40, new Random(11)); // Assume size parameter
    // specifies number of cells

    // Check the total number of rows and columns
    t.checkExpect(game.board.size(), game.row);
    for (ArrayList<GamePiece> row : game.board) {
      t.checkExpect(row.size(), game.col);
    }

    // Check the properties of the first piece (power station)
    GamePiece firstPiece = game.board.get(0).get(0);
    t.checkExpect(firstPiece.powerStation, true);
    t.checkExpect(firstPiece.powered, true);

    // Check the properties of a regular piece
    if (game.row > 1 && game.col > 1) {
      GamePiece regularPiece = game.board.get(1).get(1);
      t.checkExpect(regularPiece.powerStation, false);
      t.checkExpect(regularPiece.powered, false);
      t.checkExpect(regularPiece.top, true);
      t.checkExpect(regularPiece.bottom, true);
      t.checkExpect(regularPiece.left, false);
      t.checkExpect(regularPiece.right, false);
    }
  }

  void testRandomGamePiceSeeded(Tester t) {
    Random fixedRandom = new Random(123);
    LightEmAll game = new LightEmAll(400, 400, 40, fixedRandom);

    // Capture the initial states if needed or just apply rotations
    game.randomGamePiceSeeded();

    GamePiece firstPiece = game.board.get(0).get(0);
    GamePiece secondPiece = game.board.get(0).get(1);

    t.checkExpect(firstPiece.top, false); // After two 90-degree rotations
    t.checkExpect(firstPiece.right, true);
    t.checkExpect(firstPiece.bottom, false);
    t.checkExpect(firstPiece.left, false);

    t.checkExpect(secondPiece.top, false);// After three 90-degree rotations
    t.checkExpect(secondPiece.right, false);
    t.checkExpect(secondPiece.bottom, true);
    t.checkExpect(secondPiece.left, false);
  }

  void testOnMouseClicked(Tester t) {
    LightEmAll game = new LightEmAll(400, 400, 40, new Random(1)); // Initialize game
    // Initial random setup

    // Simulate clicking on the first piece at position (0, 0)
    Posn clickPos = new Posn(10, 10); // Assuming size is sufficiently large
    game.onMouseClicked(clickPos);

    // Check if the piece has been rotated
    GamePiece clickedPiece = game.board.get(0).get(0);
    boolean rotatedCorrectly = clickedPiece.right; // Depending on initial orientation and number of
    // rotations

    // Verify rotation effect and power distribution
    t.checkExpect(rotatedCorrectly, true);
    t.checkExpect(game.board.get(0).get(0).powered, true); // Assuming it should be powered after
    // redistribution
  }

  void testResetPowerStates(Tester t) {
    LightEmAll game = new LightEmAll(400, 400, 40, new Random(1));
    game.makeGrid();
    game.randomGamePiceSeeded();

    // Manually power all pieces
    for (ArrayList<GamePiece> column : game.board) {
      for (GamePiece piece : column) {
        piece.power();
      }
    }

    // Reset power states
    game.resetPowerStates();

    // Check all pieces are unpowered except the power station
    boolean allUnpowered = true;
    for (int i = 0; i < game.row; i++) {
      for (int j = 0; j < game.col; j++) {
        if (i != game.powerRow || j != game.powerCol) {
          allUnpowered &= !game.board.get(i).get(j).powered;
        }
      }
    }
    t.checkExpect(allUnpowered, true);
    t.checkExpect(game.board.get(game.powerRow).get(game.powerCol).powered, true);
  }

  void testOnKeyEvent(Tester t) {
    LightEmAll game = new LightEmAll(400, 400, 40, new Random(148));

    game.board.get(0).get(0).bottom = true;
    game.board.get(1).get(0).top = true;
    // Initial position of the power station
    int initialRow = game.powerRow;
    int initialCol = game.powerCol;

    // Simulate pressing 'down' key
    game.onKeyEvent("down");

    // Check if the power station moved down
    t.checkExpect(game.powerRow, initialRow + 1);
    t.checkExpect(game.powerCol, initialCol);

    // Assume redistribution and resetting works as in onMouseClicked test
    t.checkExpect(game.board.get(initialRow + 1).get(initialCol).powered, true);
  }

  void testCheckWinCondition(Tester t) {
    // Initialize the game with a simple 2x2 grid for clear testing
    LightEmAll game = new LightEmAll(200, 200, 100, new Random(67));

    // Set up a specific board configuration where all pieces are connected in a
    // known pattern
    game.board.clear(); // Clear any default setup

    ArrayList<GamePiece> row1 = new ArrayList<GamePiece>();
    ArrayList<GamePiece> row2 = new ArrayList<GamePiece>();

    // Manually create and configure GamePieces to ensure full connectivity
    GamePiece gp00 = new GamePiece(0, 0, false, true, false, true);
    GamePiece gp01 = new GamePiece(0, 1, true, false, true, false);
    GamePiece gp10 = new GamePiece(1, 0, false, true, true, false);
    GamePiece gp11 = new GamePiece(1, 1, true, false, false, true);

    row1.add(gp00);
    row1.add(gp01);
    row2.add(gp10);
    row2.add(gp11);

    game.board.add(row1);
    game.board.add(row2);

    // Set the power station at the top-left corner and ensure it is powered
    game.powerRow = 0;
    game.powerCol = 0;
    game.board.get(game.powerRow).get(game.powerCol).power();

    // Initially, power only the first piece
    game.resetPowerStates();
    game.distributePowerBFS(); // Should power all due to connectivity

    // Test that all pieces being powered results in a win
    t.checkExpect(game.checkWinCondition(), true);

    LightEmAll game3 = new LightEmAll(200, 200, 100, new Random(67));

    // Now the win condition should fail as not all pieces are connected
    t.checkExpect(game3.checkWinCondition(), false);
  }

  void testDistributePowerBFS(Tester t) {
    // Initialize the game with a simple 3x3 grid to handle a manageable number of
    // pieces
    LightEmAll game = new LightEmAll(3, 3, 100, new Random());

    // Clear any initial board and manually set up a grid where only specific pieces
    // are connected
    game.board.clear();
    ArrayList<GamePiece> row1 = new ArrayList<GamePiece>();
    ArrayList<GamePiece> row2 = new ArrayList<GamePiece>();
    ArrayList<GamePiece> row3 = new ArrayList<GamePiece>();

    // Create GamePieces with specific connections
    // Row 1
    GamePiece gp00 = new GamePiece(0, 0, false, true, false, false); // Connected to the right
    GamePiece gp01 = new GamePiece(0, 1, true, true, false, false); // Connected right and left
    GamePiece gp02 = new GamePiece(0, 2, true, false, false, false); // Connected left

    // Row 2 - no connections to Row 1 or 3
    GamePiece gp10 = new GamePiece(1, 0, false, false, false, false);
    GamePiece gp11 = new GamePiece(1, 1, false, false, false, false);
    GamePiece gp12 = new GamePiece(1, 2, false, false, false, false);

    // Row 3 - Connected vertically to each other
    GamePiece gp20 = new GamePiece(2, 0, false, false, true, false);
    GamePiece gp21 = new GamePiece(2, 1, false, false, true, true);
    GamePiece gp22 = new GamePiece(2, 2, false, false, true, false);

    row1.add(gp00);
    row1.add(gp01);
    row1.add(gp02);
    row2.add(gp10);
    row2.add(gp11);
    row2.add(gp12);
    row3.add(gp20);
    row3.add(gp21);
    row3.add(gp22);

    game.board.add(row1);
    game.board.add(row2);
    game.board.add(row3);

    // Set the power station to the top-left corner and ensure it's powered
    game.powerRow = 0;
    game.powerCol = 0;
    game.board.get(game.powerRow).get(game.powerCol).power();

    // Reset all pieces to unpowered state and redistribute power from the top-left
    // corner
    game.resetPowerStates();
    game.distributePowerBFS();

    // Check that Row 1 pieces are powered
    t.checkExpect(game.board.get(0).get(0).powered, true);

    t.checkExpect(game.board.get(1).get(2).powered, false);

    // Check that Row 3 pieces remain unpowered

    t.checkExpect(game.board.get(2).get(2).powered, false);
  }

  void testBigBang(Tester t) {

    ex1.bigBang(400, 400);
  }

  // test isconnected
  boolean testIsConnected(Tester t) {
    return t.checkExpect(p1.isConnected(p2), false) && t.checkExpect(p2.isConnected(p3), false)
        && t.checkExpect(p3.isConnected(p1), true);
  }

  // test rotate
  boolean testRotate(Tester t) {
    p4.rotate();
    p5.rotate();
    p6.rotate();
    return t.checkExpect(p4, new GamePiece(0, 0, false, false, true, true))
        && t.checkExpect(p5, new GamePiece(1, 0, true, true, false, true))
        && t.checkExpect(p6, new GamePiece(0, 1, false, true, true, false));
  }

  // test power
  boolean testPower(Tester t) {
    p1.power();
    p2.power();
    p3.power();
    return t.checkExpect(p3.powered, true) && t.checkExpect(p1.powered, true)
        && t.checkExpect(p2.powered, true);
  }

  // test powerstation
  boolean testPowerStation(Tester t) {
    p1.station();
    p2.station();
    p3.station();
    return t.checkExpect(p3.powerStation, true) && t.checkExpect(p1.powerStation, true)
        && t.checkExpect(p2.powerStation, true);
  }

  boolean testtileimage(Tester t) {
    WorldImage ei = new OverlayImage(
        new StarImage(50 / 3, 7, OutlineMode.OUTLINE, new Color(255, 128, 0)),
        new OverlayImage(new StarImage(50 / 3, 7, OutlineMode.SOLID, new Color(0, 255, 255)),
            new OverlayImage(new RectangleImage(50 / 6, 50 / 6, OutlineMode.SOLID, Color.YELLOW),
                new RectangleImage(50, 50, OutlineMode.SOLID, Color.DARK_GRAY))));

    WorldImage ei2 = new OverlayImage(
        new StarImage(60 / 3, 7, OutlineMode.OUTLINE, new Color(255, 128, 0)),
        new OverlayImage(new StarImage(60 / 3, 7, OutlineMode.SOLID, new Color(0, 255, 255)),
            new OverlayImage(new RectangleImage(60 / 6, 60 / 6, OutlineMode.SOLID, Color.YELLOW),
                new RectangleImage(60, 60, OutlineMode.SOLID, Color.DARK_GRAY))));

    WorldImage ai = p1.tileImage(50, 50 / 6, Color.YELLOW, true);
    WorldImage ai2 = p1.tileImage(60, 60 / 6, Color.YELLOW, true);

    return t.checkExpect(ai.getWidth(), ei.getWidth())
        && t.checkExpect(ai.getHeight(), ei.getHeight())
        && t.checkExpect(ai2.getHeight(), ei2.getHeight());
  }

  void testGetConnectedNeighbors(Tester t) {
    // Initialize the game with a simple 3x3 grid for testing purposes
    LightEmAll game = new LightEmAll(3, 3, 100, new Random());

    // Clear any initial board setup and manually create a grid configuration
    game.board.clear();
    ArrayList<GamePiece> row1 = new ArrayList<GamePiece>();
    ArrayList<GamePiece> row2 = new ArrayList<GamePiece>();
    ArrayList<GamePiece> row3 = new ArrayList<GamePiece>();

    // Manually setting up the connections between GamePieces
    GamePiece gp00 = new GamePiece(0, 0, false, true, false, true); // Right and bottom
    GamePiece gp01 = new GamePiece(0, 1, true, false, false, true); // Left and bottom
    GamePiece gp02 = new GamePiece(0, 2, false, false, false, false); // No connections

    GamePiece gp10 = new GamePiece(1, 0, false, true, true, false); // Right and top
    GamePiece gp11 = new GamePiece(1, 1, true, false, true, false); // Left and top
    GamePiece gp12 = new GamePiece(1, 2, false, false, false, false); // No connections

    GamePiece gp20 = new GamePiece(2, 0, false, false, true, false); // Top
    GamePiece gp21 = new GamePiece(2, 1, false, false, true, false); // Top
    GamePiece gp22 = new GamePiece(2, 2, false, false, true, false); // Top

    row1.add(gp00);
    row1.add(gp01);
    row1.add(gp02);
    row2.add(gp10);
    row2.add(gp11);
    row2.add(gp12);
    row3.add(gp20);
    row3.add(gp21);
    row3.add(gp22);

    game.board.add(row1);
    game.board.add(row2);
    game.board.add(row3);

    // gp02, gp12, and gp22 should have no connected neighbors
    t.checkExpect(gp02.getConnectedNeighbors(game.board), new ArrayList<GamePiece>());
    t.checkExpect(gp12.getConnectedNeighbors(game.board), new ArrayList<GamePiece>());
    t.checkExpect(gp22.getConnectedNeighbors(game.board), new ArrayList<GamePiece>());
  }

  void testNeighbors(Tester t) {
    // Setup a small grid for testing
    LightEmAll game = new LightEmAll(400, 400, 100); // Larger pieces for simplicity
    game.makeGrid(); // Initialize the grid with default setup

    // Manually configure connections for a piece
    GamePiece centerPiece = game.board.get(1).get(1);
    centerPiece.top = true;
    centerPiece.right = true;
    centerPiece.bottom = true;
    centerPiece.left = true;

    // Ensure neighboring pieces exist
    GamePiece topNeighbor = game.board.get(0).get(1); // Top
    topNeighbor.bottom = true;

    GamePiece rightNeighbor = game.board.get(1).get(2); // Right
    rightNeighbor.left = true;

    GamePiece bottomNeighbor = game.board.get(2).get(1); // Bottom
    bottomNeighbor.top = true;

    GamePiece leftNeighbor = game.board.get(1).get(0); // Left
    leftNeighbor.right = true;

    // Get neighbors
    List<GamePiece> neighbors = centerPiece.getConnectedNeighbors(game.board);

    // Test that all valid neighbors are returned
    t.checkExpect(neighbors.contains(topNeighbor), true);
    t.checkExpect(neighbors.contains(rightNeighbor), true);
    t.checkExpect(neighbors.contains(bottomNeighbor), true);
    t.checkExpect(neighbors.contains(leftNeighbor), true);

    // Test total number of neighbors
    t.checkExpect(neighbors.size(), 4);

    // Check behavior for non-connected pieces
    centerPiece.right = false; // Disconnect the right piece
    rightNeighbor.left = false;
    neighbors = centerPiece.getConnectedNeighbors(game.board);

    t.checkExpect(neighbors.contains(rightNeighbor), false);
    t.checkExpect(neighbors.size(), 3);
  }

  void testMinimumSpanningTree(Tester t) {
    // Initialize the game with a predictable random seed
    Random fixedRandom = new Random(42);
    LightEmAll game = new LightEmAll(400, 400, 100, fixedRandom);

    // if the MST is correct
    int expectedNumberOfEdges = game.nodes.size() - 1;
    t.checkExpect(game.mst.size(), expectedNumberOfEdges);

    HashSet<GamePiece> visited = new HashSet<>();
    Queue<GamePiece> toVisit = new LinkedList<>();
    toVisit.add(game.nodes.get(0)); // Start with the first node

    while (!toVisit.isEmpty()) {
      GamePiece current = toVisit.poll();
      visited.add(current);
      for (Edge e : game.mst) {
        if (e.fromNode.equals(current) && !visited.contains(e.toNode)) {
          toVisit.add(e.toNode);
        }
        else if (e.toNode.equals(current) && !visited.contains(e.fromNode)) {
          toVisit.add(e.fromNode);
        }
      }
    }

    // Check if all nodes were visited
    t.checkExpect(visited.size(), game.nodes.size());

  }

  void testNewconnectboards(Tester t) {
    // Create a game with a small grid
    LightEmAll game = new LightEmAll(200, 200, 100, new Random(12));

    // Manually create a small MST
    GamePiece topLeft = game.board.get(0).get(0);
    GamePiece topRight = game.board.get(0).get(1);
    GamePiece bottomLeft = game.board.get(1).get(0);
    GamePiece bottomRight = game.board.get(1).get(1);

    game.mst.add(new Edge(topLeft, topRight, 10));
    game.mst.add(new Edge(topLeft, bottomLeft, 15));

    game.newConnectBoards();

    t.checkExpect(topLeft.right, true);
    t.checkExpect(topRight.left, true);
    t.checkExpect(topLeft.bottom, true);
    t.checkExpect(bottomLeft.top, true);
    t.checkExpect(topRight.bottom, true);
    t.checkExpect(bottomRight.top, true);
  }

  void testCreateSet(Tester t) {
    // Create a game with a small grid to have manageable number of pieces
    LightEmAll game = new LightEmAll(200, 200, 100); // A 2x2 grid

    // Initialize the union-find structure
    UnionDataCreate uf = new UnionDataCreate();

    // For each GamePiece in the grid, create a set and check
    for (ArrayList<GamePiece> row : game.board) {
      for (GamePiece piece : row) {
        uf.createSet(piece);
        // Test that each GamePiece is its own parent
        t.checkExpect(uf.findNode(piece), piece, "Check that piece is its own parent");
      }
    }

    // Optionally, check that no two different pieces have the same parent
    // which ensures they are independent sets at this point
    GamePiece piece1 = game.board.get(0).get(0);
    GamePiece piece2 = game.board.get(1).get(1);
    t.checkExpect(uf.findNode(piece1) != uf.findNode(piece2), true,
        "Each GamePiece should be in its own set");
  }

  void testFindNodeSimple(Tester t) {
    LightEmAll game = new LightEmAll(200, 200, 100);

    UnionDataCreate uf = new UnionDataCreate();

    // Create sets for each game piece
    for (ArrayList<GamePiece> row : game.board) {
      for (GamePiece piece : row) {
        uf.createSet(piece);
      }
    }

    GamePiece gp1 = game.board.get(0).get(0);
    GamePiece gp2 = game.board.get(1).get(1);

    uf.unionData(gp1, gp2);

    // Test direct find
    t.checkExpect(uf.findNode(gp1), gp2);
  }

  void testUnionData(Tester t) {
    // Initialize the game and union-find structure
    LightEmAll game = new LightEmAll(300, 300, 100); // A 3x3 grid for better testing scenarios

    UnionDataCreate uf = new UnionDataCreate();

    // Create sets for each game piece
    for (ArrayList<GamePiece> row : game.board) {
      for (GamePiece piece : row) {
        uf.createSet(piece);
      }
    }

    // Select two nodes to union
    GamePiece root1 = game.board.get(0).get(0);
    GamePiece root2 = game.board.get(0).get(1);

    // Before union, check they are separate
    t.checkExpect(uf.findNode(root1) != uf.findNode(root2), true,
        "Root1 and Root2 should be separate before union");

    // Perform union
    uf.unionData(root1, root2);

    t.checkExpect(uf.findNode(root1) == uf.findNode(root2), true,
        "Root1 and Root2 should be united under the same root");

    GamePiece root3 = game.board.get(1).get(1);
    uf.unionData(root1, root3);

    t.checkExpect(uf.findNode(root3) == uf.findNode(root1), true,
        "Root3 should now also be united under the same root as Root1");

  }

}
