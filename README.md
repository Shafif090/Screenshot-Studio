# Screenshot Studio

Screenshot Studio is a client-side Fabric photo mode mod for Minecraft `26.1.2`.

## Build

This target requires Java 25.

```powershell
.\gradlew.bat build
```

The built mod jar is written to `build/libs/`.

## Features

- Toggleable photo mode with default `F9` keybind.
- Singleplayer pause behavior through an in-game pause screen; multiplayer keeps the server running.
- Detached free camera with smooth interpolation.
- WASD horizontal camera movement, Space/Shift vertical movement, Q/E roll, mouse drag look, Alt+scroll speed control.
- Hidden vanilla HUD and first-person hand while photo mode is active.
- Animated cinematic letterbox and screenshot flash.
- Right-side photo panel with camera, color, effects, and preset tabs.
- Built-in presets: Cinematic, Vivid, Noir.
- Custom presets saved as JSON under `.minecraft/config/screenshotstudio/presets/`.
- Screenshot panel button using Minecraft's current screenshot API through a small abstraction.
- Shader resources for color grading, vignette, chromatic aberration, grain, and DoF fallback math.

## Keybinds

- `F9`: Toggle photo mode.
- `H`: Hide or show the photo panel while photo mode is open.
- `W/A/S/D`: Move the detached camera horizontally.
- `Space` / `Shift`: Move camera up / down.
- `Q` / `E`: Roll camera.
- `Alt + mouse wheel`: Adjust camera movement speed.
- Optional screenshot keybind is registered but disabled by default in `config/screenshotstudio.json`.

## Known Limitations

- Minecraft `26.1.2` moved GUI rendering to `GuiGraphicsExtractor`, so post effects are implemented with a safe overlay fallback by default. The GLSL resources are present for a future renderer-specific pipeline hook.
- Depth of field uses a screen-space center blur fallback because stable depth-buffer access is not exposed safely here.
- The time-of-day slider is disabled gracefully; visual-only sky time needs a version-specific renderer hook.
- Mouse look is drag-based while the UI screen is open, which keeps multiplayer input from being sent accidentally.
