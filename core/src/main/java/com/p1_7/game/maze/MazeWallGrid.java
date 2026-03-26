package com.p1_7.game.maze;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * immutable grid decomposition of the generated wall geometry.
 *
 * converts the merged wall rectangles from {@link MazeLayout} into a finer cell
 * grid using every unique wall edge as a break line. this preserves the current
 * layout exactly while also exposing cell adjacency data for future wall-sprite
 * selection.
 */
public final class MazeWallGrid {

    /** tolerance for collapsing nearly identical break values */
    private static final float EDGE_MERGE_EPSILON = 0.01f;

    /** sorted x break positions used to form columns */
    private final List<Float> xBreaks;

    /** sorted y break positions used to form rows */
    private final List<Float> yBreaks;

    /** occupied wall cells indexed as [row][col], with row 0 at the lowest y band */
    private final boolean[][] wallCells;

    /** cached immutable list of occupied cells for render-time iteration */
    private final List<WallCell> occupiedCells;

    private MazeWallGrid(List<Float> xBreaks, List<Float> yBreaks,
                         boolean[][] wallCells, List<WallCell> occupiedCells) {
        this.xBreaks = Collections.unmodifiableList(new ArrayList<>(xBreaks));
        this.yBreaks = Collections.unmodifiableList(new ArrayList<>(yBreaks));
        this.wallCells = copyGrid(wallCells);
        this.occupiedCells = Collections.unmodifiableList(new ArrayList<>(occupiedCells));
    }

    /**
     * builds a wall grid from the current maze layout.
     *
     * @param layout the maze layout supplying wall rectangles
     * @return immutable wall grid matching the layout exactly
     */
    public static MazeWallGrid fromLayout(MazeLayout layout) {
        if (layout == null) {
            throw new IllegalArgumentException("layout must not be null");
        }
        return fromWallBounds(layout.getWallBounds());
    }

    /**
     * builds a wall grid from explicit wall rectangles.
     *
     * @param wallBounds wall rectangles as [x, y, w, h]
     * @return immutable wall grid
     */
    public static MazeWallGrid fromWallBounds(List<float[]> wallBounds) {
        if (wallBounds == null) {
            throw new IllegalArgumentException("wallBounds must not be null");
        }

        List<Float> rawXBreaks = new ArrayList<>(wallBounds.size() * 2);
        List<Float> rawYBreaks = new ArrayList<>(wallBounds.size() * 2);
        for (int i = 0; i < wallBounds.size(); i++) {
            float[] rect = wallBounds.get(i);
            validateRect(rect, "wallBounds[" + i + "]");
            rawXBreaks.add(rect[0]);
            rawXBreaks.add(rect[0] + rect[2]);
            rawYBreaks.add(rect[1]);
            rawYBreaks.add(rect[1] + rect[3]);
        }

        List<Float> xBreaks = collapseBreaks(rawXBreaks);
        List<Float> yBreaks = collapseBreaks(rawYBreaks);
        boolean[][] wallCells = new boolean[yBreaks.size() - 1][xBreaks.size() - 1];
        List<WallCell> occupiedCells = new ArrayList<>();

        for (int row = 0; row < wallCells.length; row++) {
            float y = yBreaks.get(row);
            float h = yBreaks.get(row + 1) - y;
            for (int col = 0; col < wallCells[row].length; col++) {
                float x = xBreaks.get(col);
                float w = xBreaks.get(col + 1) - x;
                float centreX = x + w / 2f;
                float centreY = y + h / 2f;
                boolean occupied = isInsideAnyRect(centreX, centreY, wallBounds);
                wallCells[row][col] = occupied;
                if (occupied) {
                    occupiedCells.add(new WallCell(row, col, x, y, w, h));
                }
            }
        }

        return new MazeWallGrid(xBreaks, yBreaks, wallCells, occupiedCells);
    }

    /**
     * returns all occupied wall cells in ascending row-major order.
     *
     * @return immutable list of occupied wall cells
     */
    public List<WallCell> getWallCells() {
        return occupiedCells;
    }

    /**
     * returns whether the given grid coordinate contains wall geometry.
     *
     * @param row row index, where 0 is the lowest y band
     * @param col column index, where 0 is the leftmost x band
     * @return true if that cell is occupied by wall
     */
    public boolean hasWall(int row, int col) {
        if (row < 0 || row >= wallCells.length) {
            return false;
        }
        if (col < 0 || col >= wallCells[row].length) {
            return false;
        }
        return wallCells[row][col];
    }

    /**
     * returns true when the north edge of the given wall cell borders open space.
     */
    public boolean isTopEdgeExposed(WallCell cell) {
        return !hasWall(cell.getRow() + 1, cell.getCol());
    }

    /**
     * returns true when the south edge of the given wall cell borders open space.
     */
    public boolean isBottomEdgeExposed(WallCell cell) {
        return !hasWall(cell.getRow() - 1, cell.getCol());
    }

    /**
     * returns true when the west edge of the given wall cell borders open space.
     */
    public boolean isLeftEdgeExposed(WallCell cell) {
        return !hasWall(cell.getRow(), cell.getCol() - 1);
    }

    /**
     * returns true when the east edge of the given wall cell borders open space.
     */
    public boolean isRightEdgeExposed(WallCell cell) {
        return !hasWall(cell.getRow(), cell.getCol() + 1);
    }

    private static boolean[][] copyGrid(boolean[][] source) {
        boolean[][] copy = new boolean[source.length][];
        for (int row = 0; row < source.length; row++) {
            copy[row] = source[row].clone();
        }
        return copy;
    }

    private static void validateRect(float[] rect, String label) {
        if (rect == null) {
            throw new IllegalArgumentException(label + " must not be null");
        }
        if (rect.length != 4) {
            throw new IllegalArgumentException(label + " must have exactly 4 elements, got: " + rect.length);
        }
    }

    private static List<Float> collapseBreaks(List<Float> rawBreaks) {
        TreeSet<Float> sortedBreaks = new TreeSet<>(rawBreaks);
        List<Float> collapsed = new ArrayList<>(sortedBreaks.size());
        for (float value : sortedBreaks) {
            if (!collapsed.isEmpty()
                    && Math.abs(value - collapsed.get(collapsed.size() - 1)) <= EDGE_MERGE_EPSILON) {
                continue;
            }
            collapsed.add(value);
        }
        return collapsed;
    }

    private static boolean isInsideAnyRect(float x, float y, List<float[]> rects) {
        for (float[] rect : rects) {
            if (x >= rect[0] && x < rect[0] + rect[2]
                    && y >= rect[1] && y < rect[1] + rect[3]) {
                return true;
            }
        }
        return false;
    }

    /**
     * immutable occupied wall cell with exact world-space bounds and grid indices.
     */
    public static final class WallCell {

        private final int row;
        private final int col;
        private final float x;
        private final float y;
        private final float width;
        private final float height;

        private WallCell(int row, int col, float x, float y, float width, float height) {
            this.row = row;
            this.col = col;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getWidth() {
            return width;
        }

        public float getHeight() {
            return height;
        }

        /**
         * returns this cell as a new rectangle array [x, y, w, h].
         */
        public float[] toRect() {
            return new float[]{ x, y, width, height };
        }
    }
}
