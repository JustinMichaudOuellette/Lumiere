# Lumière

Lumière is a simple, interactive Android light app based on a single idea: mapping XYZ accelerometer data to RGB colors. The result is a dynamic light that you can use for fun or as a utility in the dark.

## Features

- **Accelerometer Color Mapping**: The device's orientation directly controls the light's color (Red, Green, Blue).
- **Multiple Modes**: Toggle between 6 different color mapping permutations (RGB, GRB, GBR, BGR, BRG, RBG) by tapping the screen.
- **Randomized Start**: Every time you open the app, it picks a random initial color mapping.
- **Ultra-Small APK**: Designed to minimize storage footprint using R8 full mode, resource shrinking, and native Android APIs without heavy dependencies.
- **Edge-to-Edge UI**: A modern, immersive experience that utilizes the entire screen, including the status and navigation bar areas.
- **Interactive About Screen**: Contains information about the project and a clickable, underlined link to the developer's website.

## Tech Stack

- **Language**: Kotlin
- **UI**: Native Android Views & Canvas (for performance and size)
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 37 (Android 15)
- **Build System**: Gradle with aggressive R8 optimizations

## Installation

You can build the project from source using Android Studio or the Gradle wrapper:

```bash
./gradlew assembleRelease
```

## License

This project is licensed under the **GNU General Public License v3.0 (GPL-3.0-only)** - see the [LICENSE](LICENSE) file for details.

## Author

**Justin Michaud-Ouellette** - [justinmo.ca](https://justinmo.ca)
