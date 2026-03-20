package com.p1_7.game.dungeon;

/**
 * immutable value object holding all tuning parameters for dungeon generation.
 *
 * use defaults() to obtain a sensible configuration for a 51x51 grid,
 * or construct directly with the all-args constructor for custom values.
 */
public class DungeonConfig {

    /** total number of grid columns */
    public final int gridWidth;

    /** total number of grid rows */
    public final int gridHeight;

    /** maximum number of room placement attempts before generation stops */
    public final int maxAttempts;

    /** minimum room side length; must be odd */
    public final int minRoomSize;

    /** maximum room side length; must be odd */
    public final int maxRoomSize;

    /** RNG seed for reproducible generation */
    public final long seed;

    /**
     * constructs a dungeon configuration with explicit values for all parameters.
     *
     * @param gridWidth   total grid columns
     * @param gridHeight  total grid rows
     * @param maxAttempts room placement attempts before giving up
     * @param minRoomSize minimum room side length (odd)
     * @param maxRoomSize maximum room side length (odd)
     * @param seed        RNG seed for reproducibility
     */
    public DungeonConfig(int gridWidth, int gridHeight, int maxAttempts,
                         int minRoomSize, int maxRoomSize, long seed) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.maxAttempts = maxAttempts;
        this.minRoomSize = minRoomSize;
        this.maxRoomSize = maxRoomSize;
        this.seed = seed;
    }

    /**
     * returns a default configuration suitable for a 51×51 dungeon grid.
     *
     * default values: 51x51 grid, 200 placement attempts, room sizes 3-9, random seed.
     *
     * @return a DungeonConfig with sensible default values
     */
    public static DungeonConfig defaults() {
        return new DungeonConfig(
            51,                  // gridWidth
            51,                  // gridHeight
            200,                 // maxAttempts
            3,                   // minRoomSize
            9,                   // maxRoomSize
            System.nanoTime()    // seed
        );
    }
}
