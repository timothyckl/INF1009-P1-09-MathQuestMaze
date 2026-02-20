# Abstract Simulation Engine

An abstract simulation engine built with [libGDX](https://libgdx.com/), following OOP and SOLID principles. The engine is designed to be extended — its core classes are intentionally abstract and provide no runnable behaviour on their own.

## Engine Architecture

The engine is organised around a central orchestrator, abstract base classes, and concrete manager implementations:

- **`Engine`** – Central orchestrator managing the lifecycle and update loop of all managers

**Abstract base classes**

- **`Manager`** – Abstract base class providing standard init/shutdown lifecycle for all subsystems
- **`Entity`** – Abstract base for all game objects, assigned unique IDs and active state
- **`Scene`** – Abstract base for game states with lifecycle hooks (onEnter, onExit, update, submitRenderable)

**Concrete manager implementations**

- `EntityManager` – handles entity creation, removal, and repository access
- `SceneManager` – manages scene transitions and the active scene
- `MovementManager` – updates positions of all registered movable entities
- `CollisionManager` – detects and handles collisions between collidable entities
- `RenderManager` – processes the render queue and draws entities to screen
- `InputOutputManager` – maps input events to action identifiers

Entities implement capability interfaces (`IMovable`, `ICollidable`, `IRenderable`, `ITransformable`) and are registered with the appropriate managers, allowing flexible composition of behaviours.

## Project Structure

```bash
core/src/main/java/com/p1_7/
├── abstractengine/    # the core simulation engine
│   ├── collision/     # collision detection and collidable interface
│   ├── engine/        # core engine, manager base classes, and settings
│   ├── entity/        # entity abstraction and entity management
│   ├── input/         # input mapping and query interfaces
│   ├── movement/      # movement management and movable interface
│   ├── render/        # render queue and renderable interface
│   ├── scene/         # scene abstraction and scene management
│   └── transform/     # position/rotation/scale transforms
└── demo/              # "catch the droplet" demo application
```

## Demo: Catch the Droplet

The included demo is a simple arcade game that exercises the engine's core systems. It demonstrates entity composition, collision detection, scene transitions, and input handling in a working application built entirely on top of the abstract engine layer.

## Getting Started

Run the demo application:

```bash
./gradlew run
```

## Requirements

- Java 8 or higher
- Gradle (libGDX is managed automatically as a Gradle dependency — no manual setup required)
