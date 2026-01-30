# KillLevelPvP

A Minecraft Paper plugin (1.21.1) that implements a PvP kill-based leveling system with 8 levels. Players earn kills in PvP combat to level up and gain permanent abilities.

## Features

- **8 Level Progression System**: Progress through levels by getting PvP kills
- **Permanent Abilities**: Each level grants new permanent buffs
- **GUI Menu**: Clean interface showing level, kills, and progress
- **Tab List Integration**: Shows player level prefix
- **Anti-Farm Protection**: Prevents kill farming
- **Full Configurability**: Customize thresholds, messages, and sounds
- **Data Persistence**: Player data saved to YAML file

## Level Abilities

| Level | Kills Required | Ability |
|-------|----------------|---------|
| 1 | 0 | None |
| 2 | 1 | Speed I (permanent) |
| 3 | 3 | +1 Max Heart |
| 4 | 5 | Strength I (permanent) |
| 5 | 8 | Double Jump (5s cooldown) |
| 6 | 12 | +1 Max Heart (total +2) |
| 7 | 16 | Speed II (replaces Speed I) |
| 8 | 20 | +1 Max Heart (total +3) |

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/level` or `/lvl` | Opens the level GUI | `killlevelpvp.use` (default: true) |
| `/killlevelpvp reload` | Reload configuration | `killlevelpvp.admin` (default: op) |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `killlevelpvp.use` | Access to /level command | true |
| `killlevelpvp.admin` | Access to admin commands | op |

## Installation

1. Download the plugin JAR file
2. Place it in your server's `plugins` folder
3. Restart or reload your server
4. Configure as needed in `plugins/KillLevelPvP/config.yml`

## Building

### Maven
```bash
mvn clean package
```
The JAR will be in `target/KillLevelPvP-1.0.0.jar`

### Gradle
```bash
gradle build
```
The JAR will be in `build/libs/KillLevelPvP-1.0.0.jar`

## Configuration

### config.yml

```yaml
# Level kill thresholds
levels:
  1:
    kills: 0
  2:
    kills: 1
  # ... etc

# Sound on level up
sounds:
  level-up:
    sound: ENTITY_PLAYER_LEVELUP
    volume: 1.0
    pitch: 1.0

# Double jump cooldown in seconds
double-jump:
  cooldown: 5

# Tab list format ({level} and {name} placeholders)
tab-list:
  format: "&7[&eLv {level}&7] &f{name}"

# Anti-farm cooldown in seconds
anti-farm:
  cooldown: 60
```

### messages.yml

Customize all plugin messages including:
- Level up/down notifications
- Max level broadcast
- GUI text

## Mechanics

### Kill/Death System
- **Kill a player**: +1 kill
- **Die to a player**: -1 kill (minimum 0)
- Level is recalculated immediately
- Abilities update instantly on level change

### Double Jump Rules
- Only works in Survival/Adventure mode
- Disabled in water, lava, or while gliding
- Has a configurable cooldown (default 5s)
- Cannot be abused for creative flight

### Anti-Farm Protection
- Same killer vs victim has a cooldown
- Only first kill counts within the cooldown period
- Default: 60 seconds

## Technical Details

- **API**: Paper API 1.21.1
- **Java**: 21
- **Data Storage**: UUID-based YAML file
- **No External Dependencies**

## File Structure

```
KillLevelPvP/
├── src/main/java/com/killlevelpvp/
│   ├── KillLevelPvP.java          # Main plugin class
│   ├── commands/
│   │   ├── LevelCommand.java      # /level command
│   │   └── ReloadCommand.java     # /killlevelpvp reload
│   ├── gui/
│   │   └── LevelGUI.java          # Level menu GUI
│   ├── listeners/
│   │   ├── CombatListener.java    # Kill/death handling
│   │   ├── DoubleJumpListener.java# Double jump ability
│   │   ├── GUIListener.java       # GUI click handling
│   │   └── PlayerJoinListener.java# Join/quit/respawn
│   ├── managers/
│   │   ├── ConfigManager.java     # Configuration handling
│   │   ├── LevelManager.java      # Ability management
│   │   └── PlayerDataManager.java # Data persistence
│   └── utils/
│       └── ColorUtils.java        # Color code utilities
├── src/main/resources/
│   ├── plugin.yml
│   ├── config.yml
│   └── messages.yml
├── pom.xml                        # Maven build
└── build.gradle                   # Gradle build
```

## Support

If you encounter issues:
1. Check the console for error messages
2. Verify your Paper version (1.21.1+)
3. Ensure Java 21 is installed
4. Check configuration syntax

## License

This plugin is provided as-is for educational and server use.
