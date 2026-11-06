# Skybound Terraforge Minecraft Mod

Skybound Terraforge is a Fabric Minecraft mod for Minecraft 1.21.1 that captures a randomized chunk of terrain, turns it into a floating island, lifts it through the world with block collision, preserves captured block states, and leaves a matching hole in the source ground.

## Install

1. Install Java 21.
2. Install Minecraft 1.21.1 with Fabric Loader.
3. Install Fabric API for Minecraft 1.21.1.
4. Build the mod from the project directory:

```bash
./gradlew build
```

5. Copy the generated JAR from `build/libs` into the Minecraft `mods` folder.
6. Start Minecraft with the Fabric profile.

The mod ID is `skyfoundry`. The machine item is available with:

```text
/give @s skyfoundry:foundry
```

## Use

1. Place the Skyfoundry machine on terrain.
2. Right-click the machine to open its control menu.
3. Use `Change radius` to cycle the island radius between 4 and 16 blocks.
4. Select `Lift terrain` to capture and remove the selected terrain.
5. The captured blocks rise with continuous sub-block velocity. World blocks are updated when the island crosses block boundaries.
6. Sneak-right-click the machine to change its radius without opening the menu.
7. Use `/island throw` near a moving island to launch it in the direction you are looking.
8. Use `/island steer <x> <y> <z>` to set a normalized movement direction for the nearest moving island.

The generated island has a randomized irregular edge and varied underside depth. Captured block states remain unchanged, while air, bedrock, the machine block, and blocks with block entities are excluded. Cloud and end-rod particles accompany the movement.

The mod currently provides translational movement and collision with solid world blocks. It does not rotate island block states or render the island as a separate smooth rigid-body mesh. Island blocks, position, velocity, and movement state are saved with the world.

---

More Minecraft mod projects by EnderDevelopment: https://github.com/EnderDevelopment

Project page: https://dash.enderdevelopment.com/community/2afedb04-1667-4f7c-8284-ac51d04413ce?utm_source=github&utm_medium=readme&utm_campaign=auto_publish&utm_content=2afedb04-1667-4f7c-8284-ac51d04413ce
