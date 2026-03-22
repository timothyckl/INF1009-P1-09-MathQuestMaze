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

    /** minimum number of outer wall segments (createDefault uses 4; generate() uses 18–24) */
    private static final int WALL_COUNT = 4;

    /** width of each traversable corridor passage in pixels — spacious for a 32x32 player */
    private static final float CORRIDOR_WIDTH = 150f;
    private static final float CORRIDOR_HALF  = CORRIDOR_WIDTH / 2f;

    // derived reference lines
    private static final float LEFT_ROOM_RIGHT  = WALL_THICKNESS + ROOM_MARGIN + ROOM_WIDTH;                    // 300
    private static final float RIGHT_ROOM_LEFT  = SCREEN_WIDTH - WALL_THICKNESS - ROOM_MARGIN - ROOM_WIDTH;     // 980
    private static final float TOP_ROOM_BOTTOM  = SCREEN_HEIGHT - WALL_THICKNESS - ROOM_MARGIN - ROOM_HEIGHT;   // 480
    private static final float BOTTOM_ROOM_TOP  = WALL_THICKNESS + ROOM_MARGIN + ROOM_HEIGHT;                   // 240
    private static final float MID_X            = SCREEN_WIDTH  / 2f;                                           // 640
    private static final float MID_Y            = SCREEN_HEIGHT / 2f;                                           // 360
    private static final float CENTRE_LEFT      = MID_X - CORRIDOR_HALF;   // 565
    private static final float CENTRE_RIGHT     = MID_X + CORRIDOR_HALF;   // 715
    private static final float CENTRE_BOTTOM    = MID_Y - CORRIDOR_HALF;   // 285
    private static final float CENTRE_TOP       = MID_Y + CORRIDOR_HALF;   // 435

    // vertical column corridor x-bounds (centred in left/right room x-range)
    private static final float LEFT_COL_CX     = WALL_THICKNESS + ROOM_MARGIN + ROOM_WIDTH / 2f;               // 170
    private static final float LEFT_COL_LEFT   = LEFT_COL_CX - CORRIDOR_HALF;  // 95
    private static final float LEFT_COL_RIGHT  = LEFT_COL_CX + CORRIDOR_HALF;  // 245
    private static final float RIGHT_COL_CX    = SCREEN_WIDTH - WALL_THICKNESS - ROOM_MARGIN - ROOM_WIDTH / 2f; // 1110
    private static final float RIGHT_COL_LEFT  = RIGHT_COL_CX - CORRIDOR_HALF; // 1035
    private static final float RIGHT_COL_RIGHT = RIGHT_COL_CX + CORRIDOR_HALF; // 1185

    // horizontal corridor y-bounds (centred in top/bottom room y-range)
    private static final float TOP_CORR_CY  = SCREEN_HEIGHT - WALL_THICKNESS - ROOM_MARGIN - ROOM_HEIGHT / 2f; // 580
    private static final float TOP_CORR_BOT = TOP_CORR_CY - CORRIDOR_HALF;   // 505
    private static final float TOP_CORR_TOP = TOP_CORR_CY + CORRIDOR_HALF;   // 655
    private static final float BOT_CORR_CY  = WALL_THICKNESS + ROOM_MARGIN + ROOM_HEIGHT / 2f;                  // 140
    private static final float BOT_CORR_BOT = BOT_CORR_CY - CORRIDOR_HALF;   // 65
    private static final float BOT_CORR_TOP = BOT_CORR_CY + CORRIDOR_HALF;   // 215

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
        if (wallBounds.isEmpty()) {
            throw new IllegalArgumentException("wallBounds must not be empty");
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
        List<float[]> wallCopy = new ArrayList<>(wallBounds.size());
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
     * generates a procedural maze layout using a seeded random choice for each corner room.
     *
     * each of the four corner rooms independently picks one of two L-shaped corridor paths
     * to the centre spawn: horizontal (routing along the room's mid-height) or vertical
     * (routing along the room's mid-x). different seeds produce different topologies while
     * the same seed always reproduces the same layout. all corridor passages are 150px wide —
     * spacious relative to the 32x32 player sprite.
     *
     * room positions are identical to createDefault(). spawn is fixed at screen centre.
     *
     * @param seed the random seed controlling which corridor path each corner room uses
     * @return a new immutable MazeLayout with procedurally generated interior walls
     */
    public static MazeLayout generate(long seed) {
        java.util.Random rng = new java.util.Random(seed);

        float[] spawn = { MID_X, MID_Y };

        // room bounds are identical to createDefault()
        List<float[]> rooms = new ArrayList<>(ROOM_COUNT);
        rooms.add(new float[]{ WALL_THICKNESS + ROOM_MARGIN, TOP_ROOM_BOTTOM,              ROOM_WIDTH, ROOM_HEIGHT }); // R0 top-left
        rooms.add(new float[]{ RIGHT_ROOM_LEFT,               TOP_ROOM_BOTTOM,              ROOM_WIDTH, ROOM_HEIGHT }); // R1 top-right
        rooms.add(new float[]{ WALL_THICKNESS + ROOM_MARGIN,  WALL_THICKNESS + ROOM_MARGIN, ROOM_WIDTH, ROOM_HEIGHT }); // R2 bottom-left
        rooms.add(new float[]{ RIGHT_ROOM_LEFT,               WALL_THICKNESS + ROOM_MARGIN, ROOM_WIDTH, ROOM_HEIGHT }); // R3 bottom-right

        // true = corner connects via horizontal corridor at room mid-height
        // false = corner connects via vertical column corridor at room mid-x
        boolean r0h = rng.nextBoolean(); // R0 top-left:     true=right, false=down
        boolean r1h = rng.nextBoolean(); // R1 top-right:    true=left,  false=down
        boolean r2h = rng.nextBoolean(); // R2 bottom-left:  true=right, false=up
        boolean r3h = rng.nextBoolean(); // R3 bottom-right: true=left,  false=up

        return new MazeLayout(spawn, rooms, buildWalls(r0h, r1h, r2h, r3h));
    }

    /**
     * computes the interior and perimeter walls for a given set of corridor choices.
     *
     * the screen is divided into open areas (rooms, corridors, centre) and solid walls.
     * each corner room opens on exactly one side; a short connector arm links each active
     * corridor zone to the centre. all coordinates are derived from the class-level constants.
     *
     * @param r0h true if room 0 routes horizontally (right), false if vertically (down)
     * @param r1h true if room 1 routes horizontally (left), false if vertically (down)
     * @param r2h true if room 2 routes horizontally (right), false if vertically (up)
     * @param r3h true if room 3 routes horizontally (left), false if vertically (up)
     * @return list of wall bounds, each a four-element [x, y, w, h] array
     */
    private static List<float[]> buildWalls(boolean r0h, boolean r1h, boolean r2h, boolean r3h) {
        List<float[]> w = new ArrayList<>();

        // -- section A: outer perimeter (always 4 walls) --
        w.add(new float[]{ 0f,                            0f,                             SCREEN_WIDTH,  WALL_THICKNESS }); // bottom
        w.add(new float[]{ 0f,                            SCREEN_HEIGHT - WALL_THICKNESS, SCREEN_WIDTH,  WALL_THICKNESS }); // top
        w.add(new float[]{ 0f,                            0f,                             WALL_THICKNESS, SCREEN_HEIGHT }); // left
        w.add(new float[]{ SCREEN_WIDTH - WALL_THICKNESS, 0f,                             WALL_THICKNESS, SCREEN_HEIGHT }); // right

        // -- section B: centre enclosure — four corner blocks that bound the centre zone --
        // top-left block: fills between room-right and centre-left, room-bottom and screen-top
        w.add(new float[]{ LEFT_ROOM_RIGHT,  CENTRE_TOP,      CENTRE_LEFT - LEFT_ROOM_RIGHT,   TOP_ROOM_BOTTOM - CENTRE_TOP });
        // top-right block
        w.add(new float[]{ CENTRE_RIGHT,     CENTRE_TOP,      RIGHT_ROOM_LEFT - CENTRE_RIGHT,  TOP_ROOM_BOTTOM - CENTRE_TOP });
        // bottom-left block
        w.add(new float[]{ LEFT_ROOM_RIGHT,  BOTTOM_ROOM_TOP, CENTRE_LEFT - LEFT_ROOM_RIGHT,   CENTRE_BOTTOM - BOTTOM_ROOM_TOP });
        // bottom-right block
        w.add(new float[]{ CENTRE_RIGHT,     BOTTOM_ROOM_TOP, RIGHT_ROOM_LEFT - CENTRE_RIGHT,  CENTRE_BOTTOM - BOTTOM_ROOM_TOP });

        // -- section C: left column corridor (used when R0 or R2 choose vertical) --
        boolean leftColOpen = !r0h || !r2h;
        if (leftColOpen) {
            // left wall of corridor (outer wall to corridor left edge)
            w.add(new float[]{ WALL_THICKNESS,  BOTTOM_ROOM_TOP, LEFT_COL_LEFT - WALL_THICKNESS,          TOP_ROOM_BOTTOM - BOTTOM_ROOM_TOP });
            // right wall of corridor (corridor right edge to room left edge)
            w.add(new float[]{ LEFT_COL_RIGHT,  BOTTOM_ROOM_TOP, LEFT_ROOM_RIGHT - LEFT_COL_RIGHT,        TOP_ROOM_BOTTOM - BOTTOM_ROOM_TOP });
            // horizontal connector linking left column to centre (floor wall)
            w.add(new float[]{ LEFT_COL_RIGHT,  BOTTOM_ROOM_TOP, CENTRE_LEFT - LEFT_COL_RIGHT,            CENTRE_BOTTOM - BOTTOM_ROOM_TOP });
            // horizontal connector ceiling wall
            w.add(new float[]{ LEFT_COL_RIGHT,  CENTRE_TOP,      CENTRE_LEFT - LEFT_COL_RIGHT,            TOP_ROOM_BOTTOM - CENTRE_TOP });
        } else {
            // left column closed — fill entire zone with one solid block
            w.add(new float[]{ WALL_THICKNESS,  BOTTOM_ROOM_TOP, LEFT_ROOM_RIGHT - WALL_THICKNESS,        TOP_ROOM_BOTTOM - BOTTOM_ROOM_TOP });
        }

        // -- section D: right column corridor (mirror of C) --
        boolean rightColOpen = !r1h || !r3h;
        if (rightColOpen) {
            // left wall of right corridor
            w.add(new float[]{ RIGHT_ROOM_LEFT, BOTTOM_ROOM_TOP, RIGHT_COL_LEFT - RIGHT_ROOM_LEFT,        TOP_ROOM_BOTTOM - BOTTOM_ROOM_TOP });
            // right wall of right corridor
            w.add(new float[]{ RIGHT_COL_RIGHT, BOTTOM_ROOM_TOP, SCREEN_WIDTH - WALL_THICKNESS - RIGHT_COL_RIGHT, TOP_ROOM_BOTTOM - BOTTOM_ROOM_TOP });
            // horizontal connector floor
            w.add(new float[]{ CENTRE_RIGHT,    BOTTOM_ROOM_TOP, RIGHT_COL_LEFT - CENTRE_RIGHT,           CENTRE_BOTTOM - BOTTOM_ROOM_TOP });
            // horizontal connector ceiling
            w.add(new float[]{ CENTRE_RIGHT,    CENTRE_TOP,      RIGHT_COL_LEFT - CENTRE_RIGHT,           TOP_ROOM_BOTTOM - CENTRE_TOP });
        } else {
            w.add(new float[]{ RIGHT_ROOM_LEFT, BOTTOM_ROOM_TOP, SCREEN_WIDTH - WALL_THICKNESS - RIGHT_ROOM_LEFT, TOP_ROOM_BOTTOM - BOTTOM_ROOM_TOP });
        }

        // -- section E: top corridor zone (used when R0 or R1 choose horizontal) --
        boolean topOpen = r0h || r1h;
        if (topOpen) {
            // vertical connector arm dropping from corridor zone down to centre top
            w.add(new float[]{ CENTRE_LEFT, CENTRE_TOP, CORRIDOR_WIDTH, TOP_CORR_BOT - CENTRE_TOP });

            // left half of top zone (R0's side, x=[LEFT_ROOM_RIGHT, CENTRE_LEFT])
            if (r0h) {
                // R0 goes horizontal — open corridor passage on left half
                w.add(new float[]{ LEFT_ROOM_RIGHT, TOP_ROOM_BOTTOM, CENTRE_LEFT - LEFT_ROOM_RIGHT, TOP_CORR_BOT - TOP_ROOM_BOTTOM }); // floor
                w.add(new float[]{ LEFT_ROOM_RIGHT, TOP_CORR_TOP,    CENTRE_LEFT - LEFT_ROOM_RIGHT, SCREEN_HEIGHT - WALL_THICKNESS - TOP_CORR_TOP }); // ceiling
            } else {
                // R0 goes vertical — solid block on left half of top zone
                w.add(new float[]{ LEFT_ROOM_RIGHT, TOP_ROOM_BOTTOM, CENTRE_LEFT - LEFT_ROOM_RIGHT, SCREEN_HEIGHT - WALL_THICKNESS - TOP_ROOM_BOTTOM });
            }

            // right half of top zone (R1's side, x=[CENTRE_RIGHT, RIGHT_ROOM_LEFT])
            if (r1h) {
                w.add(new float[]{ CENTRE_RIGHT, TOP_ROOM_BOTTOM, RIGHT_ROOM_LEFT - CENTRE_RIGHT, TOP_CORR_BOT - TOP_ROOM_BOTTOM }); // floor
                w.add(new float[]{ CENTRE_RIGHT, TOP_CORR_TOP,    RIGHT_ROOM_LEFT - CENTRE_RIGHT, SCREEN_HEIGHT - WALL_THICKNESS - TOP_CORR_TOP }); // ceiling
            } else {
                w.add(new float[]{ CENTRE_RIGHT, TOP_ROOM_BOTTOM, RIGHT_ROOM_LEFT - CENTRE_RIGHT, SCREEN_HEIGHT - WALL_THICKNESS - TOP_ROOM_BOTTOM });
            }
        } else {
            // top zone fully closed
            w.add(new float[]{ LEFT_ROOM_RIGHT, TOP_ROOM_BOTTOM, RIGHT_ROOM_LEFT - LEFT_ROOM_RIGHT, SCREEN_HEIGHT - WALL_THICKNESS - TOP_ROOM_BOTTOM });
        }

        // -- section F: bottom corridor zone (mirror of E) --
        boolean botOpen = r2h || r3h;
        if (botOpen) {
            // vertical connector arm rising from centre bottom up to corridor zone top
            w.add(new float[]{ CENTRE_LEFT, BOT_CORR_TOP, CORRIDOR_WIDTH, CENTRE_BOTTOM - BOT_CORR_TOP });

            // left half (R2's side)
            if (r2h) {
                w.add(new float[]{ LEFT_ROOM_RIGHT, BOT_CORR_TOP,  CENTRE_LEFT - LEFT_ROOM_RIGHT, BOTTOM_ROOM_TOP - BOT_CORR_TOP }); // ceiling
                w.add(new float[]{ LEFT_ROOM_RIGHT, WALL_THICKNESS, CENTRE_LEFT - LEFT_ROOM_RIGHT, BOT_CORR_BOT - WALL_THICKNESS }); // floor
            } else {
                w.add(new float[]{ LEFT_ROOM_RIGHT, WALL_THICKNESS, CENTRE_LEFT - LEFT_ROOM_RIGHT, BOTTOM_ROOM_TOP - WALL_THICKNESS });
            }

            // right half (R3's side)
            if (r3h) {
                w.add(new float[]{ CENTRE_RIGHT, BOT_CORR_TOP,  RIGHT_ROOM_LEFT - CENTRE_RIGHT, BOTTOM_ROOM_TOP - BOT_CORR_TOP }); // ceiling
                w.add(new float[]{ CENTRE_RIGHT, WALL_THICKNESS, RIGHT_ROOM_LEFT - CENTRE_RIGHT, BOT_CORR_BOT - WALL_THICKNESS }); // floor
            } else {
                w.add(new float[]{ CENTRE_RIGHT, WALL_THICKNESS, RIGHT_ROOM_LEFT - CENTRE_RIGHT, BOTTOM_ROOM_TOP - WALL_THICKNESS });
            }
        } else {
            w.add(new float[]{ LEFT_ROOM_RIGHT, WALL_THICKNESS, RIGHT_ROOM_LEFT - LEFT_ROOM_RIGHT, BOTTOM_ROOM_TOP - WALL_THICKNESS });
        }

        // -- section G: per-room entrance-closing walls --
        // each room opens on only one side; this closes the other potential opening
        // R0 top-left
        if (r0h) {
            // opened right (horizontal) — close the bottom of R0 against the left column
            w.add(new float[]{ LEFT_COL_RIGHT, TOP_ROOM_BOTTOM, CENTRE_LEFT - LEFT_COL_RIGHT, WALL_THICKNESS });
        } else {
            // opened bottom (vertical) — close right side of R0 against top corridor
            w.add(new float[]{ LEFT_ROOM_RIGHT, TOP_CORR_BOT, WALL_THICKNESS, TOP_CORR_TOP - TOP_CORR_BOT });
        }
        // R1 top-right
        if (r1h) {
            w.add(new float[]{ CENTRE_RIGHT, TOP_ROOM_BOTTOM, RIGHT_COL_LEFT - CENTRE_RIGHT, WALL_THICKNESS });
        } else {
            w.add(new float[]{ RIGHT_ROOM_LEFT - WALL_THICKNESS, TOP_CORR_BOT, WALL_THICKNESS, TOP_CORR_TOP - TOP_CORR_BOT });
        }
        // R2 bottom-left
        if (r2h) {
            w.add(new float[]{ LEFT_COL_RIGHT, BOTTOM_ROOM_TOP - WALL_THICKNESS, CENTRE_LEFT - LEFT_COL_RIGHT, WALL_THICKNESS });
        } else {
            w.add(new float[]{ LEFT_ROOM_RIGHT, BOT_CORR_BOT, WALL_THICKNESS, BOT_CORR_TOP - BOT_CORR_BOT });
        }
        // R3 bottom-right
        if (r3h) {
            w.add(new float[]{ CENTRE_RIGHT, BOTTOM_ROOM_TOP - WALL_THICKNESS, RIGHT_COL_LEFT - CENTRE_RIGHT, WALL_THICKNESS });
        } else {
            w.add(new float[]{ RIGHT_ROOM_LEFT - WALL_THICKNESS, BOT_CORR_BOT, WALL_THICKNESS, BOT_CORR_TOP - BOT_CORR_BOT });
        }

        return w;
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
        List<float[]> copy = new ArrayList<>(wallBounds.size());
        for (float[] rect : wallBounds) {
            copy.add(rect.clone());
        }
        return Collections.unmodifiableList(copy);
    }
}
