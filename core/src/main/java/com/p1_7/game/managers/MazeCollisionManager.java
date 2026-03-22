package com.p1_7.game.managers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.p1_7.game.entities.Player;
import com.p1_7.game.maze.WallCollidable;

import com.p1_7.abstractengine.collision.CollisionManager;
import com.p1_7.abstractengine.collision.CollisionPair;
import com.p1_7.abstractengine.collision.ICollidable;
import com.p1_7.abstractengine.engine.IManager;
import com.p1_7.abstractengine.transform.ITransform;

/**
 * concrete CollisionManager that resolves player-to-wall collisions in the maze.
 *
 * registered walls and the player are maintained separately so resolve() can
 * identify which side of each collision pair is a wall without instanceof checks.
 * the player is pushed out of penetrating walls using a minimum translation
 * vector (MTV) computed from axis-aligned overlap distances.
 *
 * declaring GameMovementManager as a dependency ensures this manager's onUpdate()
 * runs after GameMovementManager.onUpdate() — which calls player.move() and
 * clamps to viewport bounds — so position corrections are applied to the
 * post-integration position rather than the pre-movement position.
 */
public class MazeCollisionManager extends CollisionManager {

    /** set of all registered wall collidables for fast membership tests in resolve() */
    private final Set<ICollidable> registeredWalls = new HashSet<>();

    /** the player collidable; set by registerPlayer() and cleared by unregisterPlayer() */
    private Player player;

    /**
     * declares GameMovementManager as this manager's sole dependency so the engine
     * schedules this manager's onUpdate() after GameMovementManager.onUpdate() —
     * which calls player.move() and clamps to viewport bounds — each frame.
     *
     * @return array containing GameMovementManager.class
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends IManager>[] getDependencies() {
        return new Class[]{ GameMovementManager.class };
    }

    // ── registration helpers ──────────────────────────────────────

    /**
     * registers the player entity for collision detection.
     *
     * @param p the player to register; must not be null
     */
    public void registerPlayer(Player p) {
        this.player = p;
        registerCollidable(p);
    }

    /**
     * registers a wall collidable for collision detection.
     *
     * @param wall the wall to register; must not be null
     */
    public void registerWall(WallCollidable wall) {
        registeredWalls.add(wall);
        registerCollidable(wall);
    }

    /**
     * removes the player from collision detection and clears the player reference.
     */
    public void unregisterPlayer() {
        if (player != null) {
            unregisterCollidable(player);
        }
        player = null;
    }

    /**
     * removes a wall from collision detection.
     *
     * @param wall the wall to unregister
     */
    public void unregisterWall(WallCollidable wall) {
        registeredWalls.remove(wall);
        unregisterCollidable(wall);
    }

    // ── resolution ───────────────────────────────────────────────

    /**
     * resolves all detected collisions by pushing the player out of any
     * penetrating wall using the minimum translation vector.
     *
     * @param collisions the list of detected collision pairs for this frame
     */
    @Override
    protected void resolve(List<CollisionPair> collisions) {
        if (player == null) return;

        for (CollisionPair pair : collisions) {
            // identify which side of the pair is a registered wall
            ICollidable wall;
            ICollidable other;
            if (registeredWalls.contains(pair.getEntityA())) {
                wall  = pair.getEntityA();
                other = pair.getEntityB();
            } else if (registeredWalls.contains(pair.getEntityB())) {
                wall  = pair.getEntityB();
                other = pair.getEntityA();
            } else {
                // neither entity is a known wall — nothing to resolve
                continue;
            }

            // only push out the registered player; ignore wall-to-non-player pairs
            if (other != player) continue;

            pushPlayerOut(wall);
        }
    }

    /**
     * computes the minimum translation vector between the player and a wall,
     * then applies it to the player's transform to eliminate penetration.
     *
     * the axis with the smaller overlap is chosen so the correction is minimal
     * and feels natural at corners.
     *
     * @param wall the wall the player has penetrated
     */
    private void pushPlayerOut(ICollidable wall) {
        float[] pMin = player.getBounds().getMinPosition();
        float[] pExt = player.getBounds().getExtent();
        float[] wMin = wall.getBounds().getMinPosition();
        float[] wExt = wall.getBounds().getExtent();

        // compute overlap depth on each axis
        float overlapX = Math.min(pMin[0] + pExt[0], wMin[0] + wExt[0]) - Math.max(pMin[0], wMin[0]);
        float overlapY = Math.min(pMin[1] + pExt[1], wMin[1] + wExt[1]) - Math.max(pMin[1], wMin[1]);

        ITransform transform = player.getTransform();

        if (overlapX <= overlapY) {
            // push along x — smaller overlap axis
            float correction = (pMin[0] < wMin[0]) ? -overlapX : overlapX;
            transform.setPosition(0, transform.getPosition(0) + correction);
        } else {
            // push along y — smaller overlap axis
            float correction = (pMin[1] < wMin[1]) ? -overlapY : overlapY;
            transform.setPosition(1, transform.getPosition(1) + correction);
        }
    }
}
