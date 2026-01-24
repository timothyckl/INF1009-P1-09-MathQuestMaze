package com.p1_7.abstractengine;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.p1_7.abstractengine.core.Entity;
import com.p1_7.abstractengine.core.Tag;
import com.p1_7.abstractengine.managers.impl.EntityIndex;
import com.p1_7.abstractengine.managers.impl.EntityManager;
import com.p1_7.abstractengine.managers.impl.EventManager;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class Main extends ApplicationAdapter {
    EventManager eventManager;
    EntityManager entityManager;
    EntityIndex entityIndex;

    // example tag enum for demonstration
    private enum SimulationTag implements Tag {
        DYNAMIC, STATIC
    }

    @Override
    public void create() {

        // create and initialise managers
        eventManager = new EventManager();
        eventManager.init();

        entityManager = new EntityManager();
        entityManager.setEventManager(eventManager);
        entityManager.init();

        // create entity index and subscribe to events
        entityIndex = new EntityIndex(entityManager);
        entityIndex.subscribeToEvents(eventManager);

        // verify index is empty before adding entities
        System.out.println("1. Initial state (no entities added yet):");
        System.out.println("   Active count: " + entityIndex.countActiveEntities());
        System.out.println("   Dynamic count: " + entityIndex.countEntitiesBy(SimulationTag.DYNAMIC));

        // add entity and verify automatic indexing via ADDED event
        System.out.println("\n2. Adding entityA with DYNAMIC tag...");
        Entity entityA = new Entity();
        entityA.addTag(SimulationTag.DYNAMIC);
        entityManager.addEntity(entityA);
        System.out.println("   Dynamic count after add: " + entityIndex.countEntitiesBy(SimulationTag.DYNAMIC));
        System.out.println("   (Expected: 1, so ADDED event triggered correctly :0000)");

        // verify active state change via ACTIVE_CHANGED event
        System.out.println("\n3. Setting entityA active...");
        System.out.println("   Active count before: " + entityIndex.countActiveEntities());
        entityManager.setEntityActive(entityA, true);
        System.out.println("   Active count after: " + entityIndex.countActiveEntities());
        System.out.println("   (Expected: 0 -> 1, so ACTIVE_CHANGED event triggered correctly :0000)");

        // add second entity
        System.out.println("\n4. Adding entityB (no tags)...");
        Entity entityB = new Entity();
        entityManager.addEntity(entityB);
        entityManager.setEntityActive(entityB, true);
        System.out.println("   Active count: " + entityIndex.countActiveEntities());
        System.out.println("   (Expected: 2)");

        // query by tag
        System.out.println("\n5. Querying entities by DYNAMIC tag:");
        Array<Entity> dynamicEntities = entityIndex.getEntitiesBy(SimulationTag.DYNAMIC, false);
        System.out.println("   Found: " + dynamicEntities.size + " entities");
        System.out.println("   (Expected: 1)");

        // remove entity and verify automatic unindexing via REMOVED event
        System.out.println("\n6. Removing entityB...");
        System.out.println("   Active count before remove: " + entityIndex.countActiveEntities());
        entityManager.removeEntity(entityB.getID());
        System.out.println("   Active count after remove: " + entityIndex.countActiveEntities());
        System.out.println("   (Expected: 2 -> 1, REMOVED event triggered yas)");

        // deactivate entity
        System.out.println("\n7. Deactivating entityA...");
        entityManager.setEntityActive(entityA, false);
        System.out.println("   Active count: " + entityIndex.countActiveEntities());
        System.out.println("   (Expected: 0)");
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
    }

    @Override
    public void dispose() {
        // cleanup
        entityIndex.unsubscribeFromEvents();
        entityManager.shutdown();
        eventManager.shutdown();
    }
}
