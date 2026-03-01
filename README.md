# Ignitron

Ignitron is a modern desktop game launcher built with JavaFX. It focuses on clean UI, fast startup, and a smooth experience for organizing and launching games across your system. The goal is to provide a unified, lightweight launcher that feels fast, polished, and easy to use.

---

## 🚧 Development Status

Ignitron is currently in active development. The launcher is functional, but many features are still being refined or expanded.

## ✨ Features

- **Clean, modern UI** — built with JavaFX for smooth animations and responsive layouts.
- **Automatic game detection** — scans your system for installed games and populates your library.
- **High‑quality icons** — uses local EXE icons or user‑provided artwork for a consistent visual experience.
- **Custom runtime** — ships with its own Java runtime (via jlink), so no Java installation is required.
- **Native Windows installer** — packaged with jpackage + WiX, including a custom glowing‑orange “I” icon.

---

## 🚀 Getting Started

### Requirements
- Windows 10 or later

### First Launch
Ignitron automatically scans for games and loads available icons.  

---

## 🛠️ Building Ignitron

Ignitron uses Maven for builds and JavaFX for UI.

### Build the JAR

```bash
mvn clean package
```
This produces:
target/Ignitron-1.0-SNAPSHOT.jar


Create the custom runtime (jlink)
```
  --module-path "%JAVA_HOME%\jmods;C:\javafx-sdk-21\lib" `
  --add-modules java.base,java.logging,java.xml,java.desktop,jdk.unsupported,jdk.management,javafx.controls,javafx.fxml,javafx.graphics `
  --output runtime
```

Package the Windows installer (jpackage)
```
jpackage `
  --name Ignitron `
  --input target `
  --main-jar Ignitron-1.0-SNAPSHOT.jar `
  --main-class org.example.ignitron.IgnitronApplication `
  --type exe `
  --icon src/main/resources/ignitron.ico `
  --runtime-image runtime
```
