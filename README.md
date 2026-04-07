# Abstract Simulation Engine & Math Quest Maze

This repository contains two layers: the **Abstract Simulation Engine**, a reusable game engine built with [libGDX](https://libgdx.com/) following OOP and SOLID principles, and **Math Quest Maze**, a complete game built on top of it as the primary project deliverable. The engine's core classes are intentionally abstract and provide no runnable behaviour on their own; the game layer is a full implementation that exercises every engine subsystem.

## Engine Architecture

![uml-diagram 2](https://github.com/user-attachments/assets/d880053c-964c-4766-872a-ed88503f0ba0)

The engine is organised around a central orchestrator, abstract base classes, and concrete manager implementations:

- **`Engine`** – Central orchestrator managing the lifecycle and update loop of all managers

**Abstract base classes**

- **`Manager`** – Abstract base class providing standard init/shutdown lifecycle for all subsystems
- **`UpdatableManager`** – Extension of `Manager` for subsystems that participate in the per-frame update loop
- **`Entity`** – Abstract base for all game objects, assigned unique IDs and active state
- **`Scene`** – Abstract base for game states with lifecycle hooks (onEnter, onExit, update, submitRenderable)

**Concrete manager implementations**

- `EntityManager` – handles entity creation, removal, and repository access
- `SceneManager` – manages scene transitions and the active scene
- `MovementManager` – updates positions of all registered movable entities
- `CollisionManager` – detects and handles collisions using `SpatialTree` for broadphase and `CollisionDetector` for narrowphase
- `RenderManager` – processes the render queue and draws entities via platform-neutral `IDrawContext`, `ISpriteBatch`, and `IShapeRenderer` interfaces
- `InputManager` – maps raw events from an `IInputSource` to action identifiers, with support for rebindable bindings via `IInputExtension`

**Engine innovations**

- **Topological manager initialisation** — Managers declare their dependencies by type. At startup, `DependencySorter` builds a `DirectedAcyclicGraph` over all registered managers and runs Kahn's algorithm to determine a safe initialisation order automatically. Circular dependencies are detected and reported at startup, not at runtime. Shutdown proceeds in reverse order, ensuring dependants are torn down before their dependencies.
- **Dimension-agnostic spatial tree** — `SpatialTree` generalises the quadtree/octree concept to arbitrary N dimensions. Each node subdivides along all *d* axes at their midpoints, producing 2<sup>d</sup> children (4 for 2D, 8 for 3D). Child placement uses a bitmask: bit *i* is set when an entity's bounds fall entirely in the high half of dimension *i*. Entities spanning a midpoint are retained in the parent node and checked against all children during retrieval.
- **Platform-neutral rendering and input** — The engine defines all rendering and input access through interfaces (`IDrawContext`, `ISpriteBatch`, `IShapeRenderer`, `IAssetStore`, `IInputSource`). The engine core has zero libGDX imports; all GDX coupling is isolated to the game layer's `platform/` package, making the engine independently testable and portable.
- **Type-keyed manager registry** — Managers are indexed by their concrete class, full superclass chain, and implemented interfaces. Any manager can be resolved by type via `Engine.getManager(Class<T>)`, providing a lightweight, compile-safe service locator without a framework dependency.

Entities implement capability interfaces (`IMovable`, `ICollidable`, `IRenderable`, `ITransformable`) and are registered with the appropriate managers, allowing flexible composition of behaviours.

## Project Structure

### Engine layer

```
abstractengine/
├── collision/   # ICollidable, IBounds, CollisionManager, CollisionDetector, SpatialTree, CollisionPair
├── engine/      # Engine, Manager, UpdatableManager, IManager, IUpdatable,
│                #   ManagerResolver, DependencySorter, DirectedAcyclicGraph
├── entity/      # Entity, EntityManager, EntityFactory, IEntityManager, IEntityMutator, IEntityRepository
├── input/       # InputManager, InputMapping, IInputManager, IInputQuery, IInputSource,
│                #   IInputMapping, IInputExtension, IInputExtensionRegistry,
│                #   ActionId, InputBindingSpec, InputEvent, InputState
├── movement/    # MovementManager, IMovable
├── render/      # RenderManager, RenderQueue, IRenderable, IRenderQueue,
│                #   IDrawContext, IAssetStore, ISpriteBatch, IShapeRenderer
├── scene/       # Scene, SceneManager, SceneContext
└── transform/   # ITransform, ITransformable
```

### Game layer

```
game/
├── audio/       # AudioManager, IAudioManager
├── character/   # Player, Goblin, Skeleton, HostileCharacter, EnemyDamageZone,
│                #   GameMovementManager, PlayerDamageListener
├── collectible/ # Item, Heart, ItemCollectionListener
├── font/        # FontManager, IFontManager
├── hud/         # GameHudRenderer, HudStrip, QuestionPanel
├── input/       # GameActions, ICursorSource
├── math/        # Difficulty, Operation, MathQuestion, QuestionGenerator
├── maze/        # MazeLayout, MazeCollisionManager, WallCollidable
├── platform/    # GdxRenderManager, GdxInputSource, GdxDrawContext, GdxAssetStore,
│                #   GdxSpriteBatch, GdxShapeRenderer, GdxCursorSource
├── round/       # GameRound, LevelOrchestrator, GamePhaseController, EnemyController,
│                #   ItemSpawner, MovementPipeline, RoomAssigner, RoomAssignment, RoundPhase
├── scenes/      # MenuScene, GameScene, PauseScene, GameOverScene, LevelCompleteScene,
│                #   HowToPlayScene, SettingScene
├── spatial/     # Transform2D, Bounds2D, IDisposable
└── ui/          # Button, MenuButton, Slider, VolumeSlider, SfxSlider, BrightnessSlider,
                 #   BrightnessOverlay, RemapSlot, Text, BackgroundImage
```

## Game: Math Quest Maze

Math Quest Maze is a maze-exploration game where the player navigates a series of rooms and answers arithmetic questions to progress through levels. It is the primary deliverable of this project and exercises every engine subsystem end-to-end.

**Gameplay**
- The player moves through a maze split into labelled answer rooms, each corresponding to a possible answer to the current maths question
- Entering the correct room advances the round; entering an incorrect room incurs a penalty
- Hostile enemies (Goblin and Skeleton) patrol the maze and deal damage on contact
- Collectible hearts are scattered through the maze and restore health when picked up

**Difficulty**

| Level  | Operand range | Operations |
|--------|---------------|------------|
| Easy   | 1 – 10        | Addition, Subtraction |
| Medium | 1 – 20        | Addition, Subtraction, Multiplication |
| Hard   | 1 – 100       | Addition, Subtraction, Multiplication, Division |

**Scene flow**

Main Menu → Gameplay → Pause / Level Complete / Game Over → (back to menu or next level)

A How To Play screen and a Settings screen (volume, SFX, brightness, and key remapping) are accessible from the main menu.

## CI/CD Pipeline

Our project enforces strict Continuous Integration. Every push and pull request is automatically built and tested against our comprehensive JUnit 5 suite via GitHub Actions. This ensures that the engine's core subsystems remain stable, heavily decoupled from LibGDX, and protected from regressions.

## Getting Started

Run the game:

```bash
./gradlew run
```

## Requirements

- Java 8 or higher
- Gradle (libGDX is managed automatically as a Gradle dependency — no manual setup required)
