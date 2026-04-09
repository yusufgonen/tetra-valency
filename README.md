# Tetra Valency
<p align="center">
  <img src="assets/ui/game_logo.png" alt="Tetra Valency Logo" width="420" />
</p>
A LibGDX-based tower defense game centered around elemental-merge mechanics.
The player places Pillars on the map, powers them up with the Elemental Orbs, and keeps the Core alive
through economy and positioning decisions across enemy waves.

## Group Members
- Ümit Yusuf Gönen
- Oğuzhan Yılmaz
- Burhan Türk
- Ahmet Efe Canpolat
- Onur Yusuf Yılmaz

## Requirements
- Java JDK 11 or later
- Gradle Wrapper (`./gradlew` or `gradlew.bat`)

## Dependencies
- `com.badlogicgames.gdx:gdx:1.13.0`
- `com.badlogicgames.gdx:gdx-freetype:1.13.0`
- `com.badlogicgames.gdx-video:gdx-video:1.3.4`
- `com.badlogicgames.gdx:gdx-backend-lwjgl3:1.13.0`
- `com.badlogicgames.gdx:gdx-platform:1.13.0:natives-desktop`
- `com.badlogicgames.gdx:gdx-freetype-platform:1.13.0:natives-desktop`
- `com.badlogicgames.gdx-video:gdx-video-lwjgl3:1.3.4`

## Running the Game
1. Clone the repository:
   ```powershell
   git clone https://github.com/cosmovisi0n/tetra-valency.git
   cd tetra-valency
   ```
2. For Windows:
   1. Create a desktop shortcut with the game icon:
      ```powershell
      $projectDir = (Get-Location).Path
      $ws = New-Object -ComObject WScript.Shell
      $lnk = $ws.CreateShortcut("$env:USERPROFILE\\Desktop\\Tetra Valency.lnk")
      $lnk.TargetPath = "$projectDir\\run-lwjgl3.bat"
      $lnk.WorkingDirectory = "$projectDir"
      $lnk.IconLocation = "$projectDir\\assets\\icon\\tv.ico"
      $lnk.Save()
      ```
   2. Double-click `Tetra Valency.lnk` on your desktop.
3. For macOS:
   1. From the repo folder, run:
      ```bash
      ./gradlew :lwjgl3:run
      ```
