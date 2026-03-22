package com.p1_7.game.maze;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * immutable value object that describes the fixed spatial layout of the math maze.
 *
 * holds the player spawn point, the four answer-room bounding rectangles, and the
 * four outer wall bounding rectangles. all coordinates use a bottom-left origin on
 * a 1280x720 screen.
 *
 * obtain an instance via {@link #createDefault()} for the standard v1 layout, or
 * construct directly (package-private) for testing with alternate values.
 */
public class MazeLayout {

    /** total width of the play area in pixels */
    private static final float SCREEN_WIDTH   = 1280f;

    /** total height of the play area in pixels */
    private static final float SCREEN_HEIGHT  = 720f;

    /** thickness of each outer wall rectangle in pixels */
    private static final float WALL_THICKNESS = 20f;

    /** gap between the screen edge and each answer room */
    private static final float ROOM_MARGIN    = 20f;

    /** width of each answer room rectangle */
    private static final float ROOM_WIDTH     = 260f;

    /** height of each answer room rectangle */
    private static final float ROOM_HEIGHT    = 200f;

    /** expected number of answer rooms */
    private static final int ROOM_COUNT = 4;

    /** expected number of outer wall segments */
    private static final int WALL_COUNT = 4;

    /** spawn position as [x, y]; stored as a defensive copy */
    private final float[] spawnPoint;

    /** four answer-room bounds, each [x, y, w, h]; stored as an unmodifiable deep copy */
    private final List<float[]> roomBounds;

    /** four outer wall bounds, each [x, y, w, h]; stored as an unmodifiable deep copy */
    private final List<float[]> wallBounds;

    /**
     * constructs a maze layout from explicit spawn, room, and wall data.
     *
     * defensive copies are taken of all parameters so the caller cannot mutate the
     * layout after construction. all bounds arrays must have exactly four elements.
     *
     * @param spawnPoint a two-element array [x, y] for the player spawn position; must not be null
     * @param roomBounds a list of exactly four [x, y, w, h] arrays for the answer rooms; must not be null
     * @param wallBounds a list of exactly four [x, y, w, h] arrays for the outer walls; must not be null
     * @throws IllegalArgumentException if any parameter is null, if spawnPoint.length != 2,
     *                                  if roomBounds.size() != 4, if wallBounds.size() != 4,
     *                                  or if any bounds array is null or not length 4
     */
    MazeLayout(float[] spawnPoint, List<float[]> roomBounds, List<float[]> wallBounds) {
        if (spawnPoint == null) {
            throw new IllegalArgumentException("spawnPoint must not be null");
        }
        if (spawnPoint.length != 2) {
            throw new IllegalArgumentException(
                "spawnPoint must have exactly 2 elements, got: " + spawnPoint.length);
        }

        if (roomBounds == null) {
            throw new IllegalArgumentException("roomBounds must not be null");
        }
        if (roomBounds.size() != ROOM_COUNT) {
            throw new IllegalArgumentException(
                "roomBounds must contain exactly " + ROOM_COUNT + " entries, got: " + roomBounds.size());
        }

        if (wallBounds == null) {
            throw new IllegalArgumentException("wallBounds must not be null");
        }
        if (wallBounds.size() != WALL_COUNT) {
            throw new IllegalArgumentException(
                "wallBounds must contain exactly " + WALL_COUNT + " entries, got: " + wallBounds.size());
        }

        // validate and deep-copy each room bounds entry
        List<float[]> roomCopy = new ArrayList<>(ROOM_COUNT);
        for (int i = 0; i < roomBounds.size(); i++) {
            float[] rect = roomBounds.get(i);
            if (rect == null) {
                throw new IllegalArgumentException("roomBounds entry at index " + i + " must not be null");
            }
            if (rect.length != 4) {
                throw new IllegalArgumentException(
                    "roomBounds entry at index " + i + " must have 4 elements, got: " + rect.length);
            }
            roomCopy.add(rect.clone());
        }

        // validate and deep-copy each wall bounds entry
        List<float[]> wallCopy = new ArrayList<>(WALL_COUNT);
        for (int i = 0; i < wallBounds.size(); i++) {
            float[] rect = wallBounds.get(i);
            if (rect == null) {
                throw new IllegalArgumentException("wallBounds entry at index " + i + " must not be null");
            }
            if (rect.length != 4) {
                throw new IllegalArgumentException(
                    "wallBounds entry at index " + i + " must have 4 elements, got: " + rect.length);
            }
            wallCopy.add(rect.clone());
        }

        this.spawnPoint = spawnPoint.clone();
        this.roomBounds = Collections.unmodifiableList(roomCopy);
        this.wallBounds = Collections.unmodifiableList(wallCopy);
    }

    /**
     * creates the standard v1 maze layout for a 1280x720 screen.
     *
     * rooms are positioned in the four corners with a margin of 20px from the screen
     * edge. walls are 20px thick and cover the full perimeter.
     *
     * room indices:
     *   0 — top-left     (40, 480, 260, 200)
     *   1 — top-right    (980, 480, 260, 200)
     *   2 — bottom-left  (40, 40, 260, 200)
     *   3 — bottom-right (980, 40, 260, 200)
     *
     * @return a new immutable MazeLayout with default coordinates
     */
    public static MazeLayout createDefault() {
        float[] spawn = { SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f };

        // x-origin of right rooms: SCREEN_WIDTH - WALL_THICKNESS - ROOM_MARGIN - ROOM_WIDTH = 980
        float rightX = SCREEN_WIDTH - WALL_THICKNESS - ROOM_MARGIN - ROOM_WIDTH;

        // y-origin of top rooms: SCREEN_HEIGHT - WALL_THICKNESS - ROOM_MARGIN - ROOM_HEIGHT = 480
        float topY = SCREEN_HEIGHT - WALL_THICKNESS - ROOM_MARGIN - ROOM_HEIGHT;

        // y-origin of bottom rooms: WALL_THICKNESS + ROOM_MARGIN = 40
        float bottomY = WALL_THICKNESS + ROOM_MARGIN;

        // x-origin of left rooms: WALL_THICKNESS + ROOM_MARGIN = 40
        float leftX = WALL_THICKNESS + ROOM_MARGIN;

        List<float[]> rooms = new ArrayList<>(ROOM_COUNT);
        rooms.add(new float[]{ leftX,   topY,    ROOM_WIDTH, ROOM_HEIGHT }); // index 0: top-left
        rooms.add(new float[]{ rightX,  topY,    ROOM_WIDTH, ROOM_HEIGHT }); // index 1: top-right
        rooms.add(new float[]{ leftX,   bottomY, ROOM_WIDTH, ROOM_HEIGHT }); // index 2: bottom-left
        rooms.add(new float[]{ rightX,  bottomY, ROOM_WIDTH, ROOM_HEIGHT }); // index 3: bottom-right

        List<float[]> walls = new ArrayList<>(WALL_COUNT);
        walls.add(new float[]{ 0f,                             0f,                              SCREEN_WIDTH,  WALL_THICKNESS }); // bottom
        walls.add(new float[]{ 0f,                             SCREEN_HEIGHT - WALL_THICKNESS,  SCREEN_WIDTH,  WALL_THICKNESS }); // top
        walls.add(new float[]{ 0f,                             0f,                              WALL_THICKNESS, SCREEN_HEIGHT }); // left
        walls.add(new float[]{ SCREEN_WIDTH - WALL_THICKNESS,  0f,                              WALL_THICKNESS, SCREEN_HEIGHT }); // right

        return new MazeLayout(spawn, rooms, walls);
    }

    /**
     * returns the player spawn position.
     *
     * @return a new two-element array [x, y] containing the spawn coordinates
     */
    public float[] getSpawnPoint() {
        return spawnPoint.clone();
    }

    /**
     * returns the bounding rectangle for the answer room at the given index.
     *
     * @param roomIndex the room index to look up; must be in the range 0–3 inclusive
     * @return a new four-element array [x, y, w, h] for the requested room
     * @throws IllegalArgumentException if roomIndex is not in [0, 3]
     */
    public float[] getRoomBounds(int roomIndex) {
        if (roomIndex < 0 || roomIndex >= ROOM_COUNT) {
            throw new IllegalArgumentException(
                "roomIndex must be in [0, " + (ROOM_COUNT - 1) + "], got: " + roomIndex);
        }
        return roomBounds.get(roomIndex).clone();
    }

    /**
     * returns an unmodifiable list of deep-copied answer-room bounding rectangles.
     *
     * each element is a new four-element float array [x, y, w, h]; mutating the returned
     * arrays does not affect the stored layout.
     *
     * @return unmodifiable list of four [x, y, w, h] arrays
     */
    public List<float[]> getAllRoomBounds() {
        List<float[]> copy = new ArrayList<>(ROOM_COUNT);
        for (float[] rect : roomBounds) {
            copy.add(rect.clone());
        }
        return Collections.unmodifiableList(copy);
    }

    /**
     * returns an unmodifiable list of deep-copied outer wall bounding rectangles.
     *
     * each element is a new four-element float array [x, y, w, h]; mutating the returned
     * arrays does not affect the stored layout.
     *
     * @return unmodifiable list of four [x, y, w, h] arrays
     */
    public List<float[]> getWallBounds() {
        List<float[]> copy = new ArrayList<>(WALL_COUNT);
        for (float[] rect : wallBounds) {
            copy.add(rect.clone());
        }
        return Collections.unmodifiableList(copy);
    }
}
