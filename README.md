# Math Quest Maze & Abstract Simulation Engine

![CI Pipeline](https://github.com/timothyckl/INF1009-P1-09-AbstractEngine/actions/workflows/CI-test.yml/badge.svg)

Math Quest Maze is an educational game designed to enhance arithmetic fluency through interactive, game-based learning. Players solve maths questions by identifying and entering the correct answer room within a maze, whilst avoiding hostile enemies and collecting health items. It is built on top of the **Abstract Simulation Engine** — a reusable, framework-agnostic game engine following OOP and SOLID principles.

**Target audience:** Primary school students aged 6–10 developing foundational arithmetic skills. The three difficulty tiers accommodate a range of ability levels, from single-digit addition through to four-operation arithmetic with large operands.

---

## Table of Contents

- [Game: Math Quest Maze](#game-math-quest-maze)
- [Engine Architecture](#engine-architecture)
- [Project Structure](#project-structure)
- [CI/CD Pipeline](#cicd-pipeline)
- [Getting Started](#getting-started)
- [Requirements](#requirements)

---

## Game: Math Quest Maze

**Screenshots**

| Menu | Gameplay |
|------|----------|
| <img width="1276" height="715" alt="menu-scene" src="https://github.com/user-attachments/assets/eb520c4c-cb80-4481-abe1-3724838ffd15" /> | <img width="1275" height="698" alt="game-scene" src="https://github.com/user-attachments/assets/45b622a5-7014-40d9-8b09-571fd90bbf92" /> |

| How To Play | Settings |
|-------------|----------|
| <img width="1277" height="714" alt="how-to-play-scene" src="https://github.com/user-attachments/assets/5e5a4773-a644-489c-9ef7-9eb5b9b2be4a" /> | <img width="1275" height="717" alt="setting-scene" src="https://github.com/user-attachments/assets/89ad6f21-9546-4f63-b7d8-eda9f8ec972e" /> |

**Gameplay**
- At the start of each round, a maths question is presented via an animated panel. The maze contains four answer rooms each labelled with a possible answer; entering the correct room scores a point
- A round is won by answering 5 questions correctly; the game ends early if the player's health reaches zero
- Entering an incorrect room costs 1 health point; the same question remains active until answered correctly
- The player starts each round with 3 health points. Collectible hearts are randomly placed (1–3 per round) and restore 1 HP each, up to the maximum of 3

**Difficulty**

Difficulty governs both question complexity and enemy behaviour simultaneously, ensuring the overall challenge scales coherently across both axes.

| Level  | Operand range | Operations | Enemy behaviour |
|--------|---------------|------------|-----------------|
| Easy   | 1 – 10        | Addition, Subtraction | Fewer enemies, placed farther away |
| Medium | 1 – 20        | Addition, Subtraction, Multiplication | Moderate enemy count and aggression |
| Hard   | 1 – 100       | Addition, Subtraction, Multiplication, Division | More enemies, placed in closer proximity |

**Enemy AI**

Two enemy types are implemented, each with three behavioural states (PATROL → CHASE → ATTACK):

| Enemy | Patrol | Detection | Chase | Attack |
|-------|--------|-----------|-------|--------|
| Goblin | 40 px/s; reverses every 1.5 s | 300 px radius | 80 px/s | Within 50 px; 9-frame animation |
| Skeleton | Follows waypoints at 58 px/s | 260 px, requires line-of-sight | 76 px/s | Within 40 px; 7-frame animation |

Enemy hits apply a one-second cooldown to prevent a single contact from consuming multiple health points.

**Controls**

| Action | Primary | Alternate |
|--------|---------|-----------|
| Move Up | W | ↑ |
| Move Down | S | ↓ |
| Move Left | A | ← |
| Move Right | D | → |
| Pause / Back | Escape | Backspace |
| Confirm | Space | — |

All movement bindings are remappable via the Settings screen.

**Scene flow**

Main Menu → Gameplay → Pause / Level Complete / Game Over → (back to menu or next level)

A How To Play screen and a Settings screen (volume, SFX, brightness, and key remapping) are accessible from the main menu.

---

## Engine Architecture

<img width="1500" height="734" alt="engine-uml" src="https://github.com/user-attachments/assets/ad92dbba-c62a-4da1-b619-21e0ff97cad1" />

**Three-layer architecture**

The system is organised into three distinct layers with a strict top-down dependency direction:

| Layer | Package | Responsibility |
|-------|---------|----------------|
| Abstract Engine | `com.p1_7.abstractengine` | All framework-level abstractions. Zero libGDX imports — entirely self-contained |
| Game | `com.p1_7.game` | All Math Quest Maze logic. Depends only on engine abstractions |
| Platform | `com.p1_7.game.platform` | All libGDX-specific implementations. The sole layer with a framework dependency |

The engine can be retargeted to a different backend by replacing only the platform layer, without touching engine or game code.

**Abstract base classes**

- **`Manager`** – Abstract base class providing standard init/shutdown lifecycle for all subsystems
- **`UpdatableManager`** – Extension of `Manager` for subsystems that participate in the per-frame update loop
- **`Entity`** – Abstract base for all game objects, assigned unique IDs and active state
- **`Scene`** – Abstract base for game states with lifecycle hooks (onEnter, onExit, onSuspend, onResume, update, submitRenderable)

**Concrete manager implementations**

- `EntityManager` – handles entity creation, removal, and repository access
- `SceneManager` – manages scene transitions and the active scene; transitions are deferred to the next update tick to prevent mid-frame state changes
- `MovementManager` – updates positions of all registered movable entities each frame; clamps to world bounds when configured
- `CollisionManager` – detects and handles collisions using `SpatialTree` for broadphase and `CollisionDetector` for narrowphase
- `RenderManager` – processes the render queue and draws entities via platform-neutral `IDrawContext`, `ISpriteBatch`, and `IShapeRenderer` interfaces
- `InputManager` – maps raw events from an `IInputSource` to action identifiers, with support for rebindable bindings via `IInputExtension`

**Engine innovations**

- **Topological manager initialisation** — Managers declare their dependencies by type. At startup, `DependencySorter` builds a `DirectedAcyclicGraph` over all registered managers and runs Kahn's algorithm to determine a safe initialisation order automatically. Circular dependencies are detected and reported at startup, not at runtime. Shutdown proceeds in reverse order, ensuring dependants are torn down before their dependencies.
- **Dimension-agnostic spatial tree** — `SpatialTree` generalises the quadtree/octree concept to arbitrary N dimensions. Each node subdivides along all *d* axes at their midpoints, producing 2<sup>d</sup> children (4 for 2D, 8 for 3D). Child placement uses a bitmask: bit *i* is set when an entity's bounds fall entirely in the high half of dimension *i*. Entities spanning a midpoint are retained in the parent node and checked against all children during retrieval.
- **Platform-neutral rendering and input** — The engine defines all rendering and input access through interfaces (`IDrawContext`, `ISpriteBatch`, `IShapeRenderer`, `IAssetStore`, `IInputSource`). The engine core has zero libGDX imports; all GDX coupling is isolated to the game layer's `platform/` package, making the engine independently testable and portable.
- **Type-keyed manager registry** — Managers are indexed by their concrete class, full superclass chain, and implemented interfaces. Any manager can be resolved by type via `Engine.getManager(Class<T>)`, providing a lightweight, compile-safe service locator without a framework dependency.

Entities implement capability interfaces (`IMovable`, `ICollidable`, `IRenderable`, `ITransformable`) and are registered with the appropriate managers, allowing flexible composition of behaviours.

**SOLID & Design Patterns**

*SOLID principles*

- **SRP** — Each class has one clearly defined purpose: `MovementManager` only advances positions; `QuestionGenerator` only produces `MathQuestion` instances; `GamePhaseController` only detects and advances phase transitions
- **OCP** — `Manager` defines `onInit()`/`onShutdown()` as protected hooks; subclasses extend behaviour without modifying the base. `CollisionManager` declares abstract `resolve()` so the engine handles detection and the game supplies resolution logic
- **LSP** — All scene subclasses are managed by `SceneManager` via the `Scene` type with no special-casing. `Player`, `Goblin`, and `Skeleton` are all substitutable through `Character`, which implements `IRenderable`, `IMovable`, and `ICollidable`
- **ISP** — Entity access is split into `IEntityRepository` (read) and `IEntityMutator` (write); input is split into `IInputQuery` (state queries) and `IInputMapping` (runtime remapping); each interface exposes only what its consumer needs
- **DIP** — `Engine` operates on `IManager` references; `SceneManager` depends on `IEntityManager`, `IRenderQueue`, and `IInputQuery` — never concrete classes. `RenderManager` depends on `ISpriteBatch`, `IShapeRenderer`, and `IAssetStore`, enabling libGDX to be swapped without touching the engine

*Design patterns*

- **Template Method** — `Manager.init()` and `shutdown()` are `final`, delegating to protected `onInit()`/`onShutdown()` hooks that subclasses override
- **Factory** — `EntityFactory` decouples entity instantiation from `EntityManager`; `RenderManager` acts as an abstract factory for rendering resources via `createSpriteBatch()`, `createShapeRenderer()`, etc.
- **Observer** — Cross-boundary events use listener interfaces: `PlayerDamageListener`, `ItemCollectionListener`, and `GamePhaseListener` keep producers decoupled from consumers
- **State** — The gameplay round is an explicit FSM; `RoundPhase` defines states (`CHOOSING`, `QUESTION_INTRO`, `FEEDBACK`, `ROUND_RESET`, `LEVEL_COMPLETE`, `GAME_OVER`) and `GamePhaseController` drives transitions
- **Facade** — `LevelOrchestrator` hides `GameRound`, `GamePhaseController`, and `RoomAssignment` behind a single interface for `GameScene`; `SceneContext` wraps all manager instances behind a typed service-lookup API; `IDrawContext` hides the `SpriteBatch`/`ShapeRenderer` pass-switching lifecycle
- **Service Locator** — `Engine.getManager(Class<T>)` and `SceneContext.get(Class<T>)` both resolve dependencies by type at runtime, eliminating hard-coded constructor injection across the engine and scene layers

---

## Project Structure

### Game layer

```bash
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

### Engine layer

```bash
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

---

## CI/CD Pipeline

Every push and pull request is automatically built and tested via GitHub Actions. The test suite comprises 22 test classes written with JUnit 5 and Mockito, covering both the abstract engine layer and the game layer. Engine-layer tests verify dependency resolution, collision detection (brute-force and spatial tree), input state transitions, scene lifecycle callbacks, and entity management. Game-layer tests verify question generation invariants, the `GameRound` state machine, and `GamePhaseController` timer and terminal-phase logic.

## Getting Started

Run the game:

```bash
./gradlew run
```

## Requirements

- Java 8 or higher
- Gradle (libGDX is managed automatically as a Gradle dependency — no manual setup required)
