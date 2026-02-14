# Abstract Simulation Engine

An abstract simulation engine built with [libGDX](https://libgdx.com/), following OOP and SOLID principles.

## Engine Architecture

The engine is organised around several key abstract classes that define its structure:

- **`Engine`** – Central orchestrator managing the lifecycle and update loop of all managers
- **`Manager`** – Abstract base class providing standard init/shutdown lifecycle for all subsystems
  - `EntityManager` – handles entity creation, removal, and repository access
  - `SceneManager` – manages scene transitions and the active scene
  - `MovementManager` – updates positions of all registered movable entities
  - `CollisionManager` – detects and handles collisions between collidable entities
  - `RenderManager` – processes the render queue and draws entities to screen
  - `InputOutputManager` – maps input events to action identifiers
- **`Entity`** – Abstract base for all game objects, assigned unique IDs and active state
- **`Scene`** – Abstract base for game states with lifecycle hooks (onEnter, onExit, update, submitRenderable)

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

The included demo is a simple arcade game where the player controls a bucket to catch falling water droplets:

- **Objective** – catch falling droplets with your bucket before they hit the ground
- **Controls** – move the bucket left and right using keyboard or touch input
- **Lives** – start with 3 lives; miss a droplet and lose a life
- **Game over** – the game ends when all lives are lost, displaying your final score
- **Mechanics** – droplets spawn continuously at random positions and fall at a constant speed

The demo showcases the engine's entity system, collision detection, scene management, and input handling.

## Getting Started

Run the demo application:

```bash
./gradlew run
```

## Requirements

- Java 8 or higher
- Gradle
