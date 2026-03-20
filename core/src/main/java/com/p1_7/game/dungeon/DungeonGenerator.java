package com.p1_7.game.dungeon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * stateless service that procedurally generates a {@link DungeonMap} from a {@link DungeonConfig}.
 *
 * the generator holds no fields; all mutable state is scoped to the {@code generate} method.
 * supplying the same config (including seed) always produces an identical map.
 */
public class DungeonGenerator {

    /**
     * generates a dungeon map from the supplied configuration.
     *
     * @param config tuning parameters for generation; must not be null
     * @return a fully generated DungeonMap
     * @throws IllegalStateException if no rooms could be placed within the allowed attempts
     */
    public DungeonMap generate(DungeonConfig config) {
        CellType[][] grid = initialiseGrid(config.gridWidth, config.gridHeight);
        List<Room> rooms = placeRooms(grid, config);
        carveCorridors(grid, rooms, config);
        return new DungeonMap(grid, rooms);
    }

    /**
     * creates a row-major grid of the given dimensions, filled entirely with WALL cells.
     *
     * @param width  number of grid columns
     * @param height number of grid rows
     * @return a grid[height][width] with every cell set to WALL
     */
    private CellType[][] initialiseGrid(int width, int height) {
        CellType[][] grid = new CellType[height][width];
        for (CellType[] row : grid) {
            Arrays.fill(row, CellType.WALL);
        }
        return grid;
    }

    /**
     * attempts to place rooms into the grid for up to {@code config.maxAttempts} iterations.
     *
     * room dimensions and top-left positions are constrained to odd values so corridor
     * connections always land on odd-indexed cells. a candidate is rejected if it overlaps
     * any already-placed room by a 1-cell margin.
     *
     * @param grid   the working grid to carve rooms into
     * @param config generation parameters (room size range, grid bounds, attempt limit, seed)
     * @return the list of successfully placed rooms in insertion order
     * @throws IllegalStateException if not a single room was placed within the attempt limit
     */
    private List<Room> placeRooms(CellType[][] grid, DungeonConfig config) {
        List<Room> rooms = new ArrayList<>();
        Random rng = new Random(config.seed);

        for (int attempt = 0; attempt < config.maxAttempts; attempt++) {
            // pick odd dimensions within [minRoomSize, maxRoomSize]
            int roomWidth  = config.minRoomSize + 2 * rng.nextInt((config.maxRoomSize - config.minRoomSize) / 2 + 1);
            int roomHeight = config.minRoomSize + 2 * rng.nextInt((config.maxRoomSize - config.minRoomSize) / 2 + 1);

            // skip if the room cannot fit inside the grid at all
            int maxX = config.gridWidth  - roomWidth;
            int maxY = config.gridHeight - roomHeight;
            if (maxX < 0 || maxY < 0) {
                continue;
            }

            // pick odd top-left coordinates so room centres sit on odd cells
            int roomX = (maxX >= 2) ? 1 + 2 * rng.nextInt(maxX / 2) : 0;
            int roomY = (maxY >= 2) ? 1 + 2 * rng.nextInt(maxY / 2) : 0;

            Room candidate = new Room(roomX, roomY, roomWidth, roomHeight);

            // reject if the candidate overlaps any placed room (1-cell margin)
            boolean overlaps = false;
            for (Room placed : rooms) {
                if (candidate.overlaps(placed, 1)) {
                    overlaps = true;
                    break;
                }
            }
            if (overlaps) {
                continue;
            }

            carveRoom(grid, candidate);
            rooms.add(candidate);
        }

        if (rooms.isEmpty()) {
            throw new IllegalStateException(
                "dungeon generation failed: no rooms placed in " + config.maxAttempts + " attempts"
            );
        }
        return rooms;
    }

    /**
     * sets every cell within the room's bounding box to FLOOR.
     *
     * @param grid the working grid to modify
     * @param room the room whose bounding box should be carved out
     */
    private void carveRoom(CellType[][] grid, Room room) {
        for (int row = room.y; row < room.y + room.height; row++) {
            for (int col = room.x; col < room.x + room.width; col++) {
                grid[row][col] = CellType.FLOOR;
            }
        }
    }

    /**
     * carves L-shaped corridors connecting each consecutive pair of rooms.
     *
     * @param grid   the working grid to modify
     * @param rooms  placed rooms in insertion order
     * @param config provides grid bounds for index clamping
     */
    private void carveCorridors(CellType[][] grid, List<Room> rooms, DungeonConfig config) {
        for (int i = 0; i < rooms.size() - 1; i++) {
            int[] from = rooms.get(i).centre();
            int[] to   = rooms.get(i + 1).centre();
            // from[0]/to[0] are columns; from[1]/to[1] are rows
            carveCorridor(grid, from[0], from[1], to[0], to[1], config);
        }
    }

    /**
     * carves a single L-shaped corridor from (x1, y1) to (x2, y2).
     *
     * the horizontal leg walks along row y1 from column x1 to x2; the vertical
     * leg then walks along column x2 from row y1 to y2. all indices are clamped
     * to valid grid bounds before each write.
     *
     * @param grid   the working grid to modify
     * @param x1     starting column (centre of the source room)
     * @param y1     starting row    (centre of the source room)
     * @param x2     ending column   (centre of the target room)
     * @param y2     ending row      (centre of the target room)
     * @param config provides gridWidth and gridHeight for bound clamping
     */
    private void carveCorridor(CellType[][] grid, int x1, int y1, int x2, int y2, DungeonConfig config) {
        int maxCol = config.gridWidth  - 1;
        int maxRow = config.gridHeight - 1;

        // horizontal leg: walk from x1 to x2 along row y1
        int colStart    = Math.min(x1, x2);
        int colEnd      = Math.max(x1, x2);
        int fixedRow    = Math.max(0, Math.min(y1, maxRow));
        for (int col = colStart; col <= colEnd; col++) {
            grid[fixedRow][Math.max(0, Math.min(col, maxCol))] = CellType.FLOOR;
        }

        // vertical leg: walk from y1 to y2 along column x2
        int rowStart    = Math.min(y1, y2);
        int rowEnd      = Math.max(y1, y2);
        int fixedCol    = Math.max(0, Math.min(x2, maxCol));
        for (int row = rowStart; row <= rowEnd; row++) {
            grid[Math.max(0, Math.min(row, maxRow))][fixedCol] = CellType.FLOOR;
        }
    }
}
