package com.p1_7.game.dungeon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * holds the generated dungeon grid and the list of rooms placed within it.
 *
 * grid convention: the internal grid is stored row-major — grid[row][col].
 * getCellAt(x, y) accepts (x, y) where x is the column and y is the row,
 * so it reads grid[y][x].
 *
 * render code must iterate:
 *   for row ... for col ... map.getCellAt(col, row)
 * reversing the loop order produces a transposed map.
 *
 * getRooms() returns rooms in insertion order; rooms.get(0) is always
 * the player spawn room.
 */
public class DungeonMap {

    /** row-major grid of cell types, indexed as grid[row][col] */
    private final CellType[][] grid;

    /** rooms in insertion order; index 0 is the player spawn */
    private final List<Room> rooms;

    /** number of grid columns */
    private final int width;

    /** number of grid rows */
    private final int height;

    /**
     * constructs a dungeon map from a pre-generated grid and room list.
     *
     * a defensive copy of rooms is taken so the caller's list cannot
     * mutate the map after construction.
     *
     * @param grid  row-major cell grid, dimensions [height][width]
     * @param rooms rooms in insertion order; index 0 must be the player spawn
     */
    public DungeonMap(CellType[][] grid, List<Room> rooms) {
        this.grid = grid;
        this.height = grid.length;
        // guard against an empty grid to avoid ArrayIndexOutOfBoundsException
        this.width = (grid.length > 0) ? grid[0].length : 0;
        this.rooms = new ArrayList<>(rooms);
    }

    /**
     * returns the cell type at the given grid coordinate.
     *
     * out-of-bounds coordinates always return WALL rather than throwing,
     * so render and collision code can query freely without bounds checks.
     *
     * @param x grid column (0-based)
     * @param y grid row (0-based)
     * @return the CellType at (x, y), or WALL if the coordinate is outside the grid
     */
    public CellType getCellAt(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return CellType.WALL;
        }
        return grid[y][x];
    }

    /**
     * returns the raw grid array for read-only use by render code.
     *
     * no defensive copy is made; callers must not mutate the returned array.
     *
     * @return the CellType[][] grid, indexed as grid[row][col]
     */
    public CellType[][] getGrid() {
        return grid;
    }

    /**
     * returns an unmodifiable view of the room list.
     *
     * rooms are in insertion order; index 0 is the player spawn room.
     *
     * @return unmodifiable list of Room objects
     */
    public List<Room> getRooms() {
        return Collections.unmodifiableList(rooms);
    }

    /**
     * returns the number of grid columns.
     *
     * @return grid width
     */
    public int getWidth() {
        return width;
    }

    /**
     * returns the number of grid rows.
     *
     * @return grid height
     */
    public int getHeight() {
        return height;
    }
}
