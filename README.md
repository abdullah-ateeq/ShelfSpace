# ShelfSpace 📐📦

**ShelfSpace** is a powerful, modern Android application built with **Jetpack Compose** and **Material 3**. It helps users, woodworkers, interior designers, and organizers accurately calculate cabinet dimensions, usable shelf surface area, linear frontage length, internal storage volume, item fitting capacity, and multi-unit comparisons.

---

## ✨ Features

### 📏 Cabinet & Usable Dimension Calculations
- **Precise Usable Space Math**: Accounts for side panel thickness, top/bottom panel thickness, and back panel thickness.
- **Surface Area & Linear Length**: Computes single shelf area, total surface area ($m^2$, $mm^2$), and total linear shelf length ($m$, $mm$).
- **Storage Volume**: Calculates usable internal volume in **Liters**, **Cubic Meters ($m^3$)**, and **Cubic Feet ($cu\ ft$)**.
- **Compartment Spacing**: Supports equal compartment height calculations and custom shelf spacing validation.
- **Maximum Shelf Finder**: Automatically determines the maximum number of shelves possible while maintaining a minimum desired vertical clearance.

### 📦 Item Fit & Capacity Engine
- **Item Fitting Analysis**: Computes total items across width, items deep, vertical layers, and total item capacity per compartment and cabinet.
- **Orientation Optimization**: Supports automatic orientation, fixed orientation, and rotation checking to fit the maximum possible items.
- **Clearance & Warnings**: Detects low clearance conditions and warns if items exceed compartment dimensions.
- **Storage Utilization**: Displays occupied vs. free volume ($L$) and surface area ($m^2$) utilization percentages.

### 🖼️ Interactive 2D Storage Visualizer
- **Multi-Angle Views**: Renders interactive 2D Canvas diagrams for **Front**, **Side**, and **Top** views.
- **Live Diagram Previews**: Visually displays cabinet framing, shelf positions, compartments, clearance margins, and placed item bounding boxes.

### 🏬 Multi-Unit & Custom Section Management
- **Multiple Units Comparison**: Manage and compare multiple storage cabinets in a single space.
- **Custom Sections**: Organize specific furniture types like Pantries, Wardrobes, and Bookcases.

### 💾 History, Storage & Export
- **Calculation History**: Automatically saves calculations (up to 10 items) with actions to reload, duplicate, or delete.
- **Export Formats**: Export full calculation reports to **CSV**, **JSON**, or **TXT** via Android Storage Access Framework (SAF).
- **Direct Sharing**: Easily share text summaries of calculations via Android System Share.

### ⚙️ Customizable Settings & Multi-Unit Support
- **Measurement Units**: Switch seamlessly between **Millimeters (mm)**, **Centimeters (cm)**, **Meters (m)**, **Inches (in)**, and **Feet (ft)**.
- **Theming**: Full support for System, Light, and Dark themes with Material 3 **Dynamic Color**.
- **Configurable Defaults**: Custom default panel thicknesses, shelf thickness, target clearance, diagram visibility, decimal precision, and haptics.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose (Material 3, Canvas 2D Graphics, Extended Material Icons)
- **Architecture**: MVVM (Model-View-ViewModel) + StateFlow
- **Navigation**: Jetpack Navigation Compose
- **Data Persistence**: SharedPreferences with JSON serialization
- **Target SDK**: Android 15 / API 37 (Minimum SDK 26)

---

## 📁 Project Structure

```
ShelfSpace/
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/com/example/shelfspace/
│       │   │   ├── calculator/          # Shelf & Capacity calculation engines
│       │   │   │   ├── ShelfCalculator.kt
│       │   │   │   └── StorageCapacityCalculator.kt
│       │   │   ├── data/                # Data persistence & export helpers
│       │   │   │   ├── ExportHelper.kt
│       │   │   │   └── PreferencesManager.kt
│       │   │   ├── model/               # Models (StorageUnit, StorageItem, ShelfResult, etc.)
│       │   │   │   └── Models.kt
│       │   │   ├── ui/                  # Compose UI Screens & Components
│       │   │   │   ├── components/      # Common UI components & StorageDiagramCanvas
│       │   │   │   ├── navigation/      # Navigation Graph
│       │   │   │   ├── screens/         # Home, History, StorageLayout, Settings, etc.
│       │   │   │   └── theme/           # Color, Theme, Typography
│       │   │   └── viewmodel/           # ShelfSpaceViewModel & UiState
│       │   └── AndroidManifest.xml
│       └── test/                        # Unit tests for calculation engines
└── build.gradle.kts
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Ladybug (2024.2.1) or newer
- **JDK**: JDK 11 or higher
- **Android SDK**: API level 37 (Target), API level 26 (Minimum)

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/ShelfSpace.git
   cd ShelfSpace
   ```
2. Open the project in **Android Studio**.
3. Build and run using `./gradlew`:
   ```bash
   # Build Debug APK
   ./gradlew assembleDebug

   # Run Unit Tests
   ./gradlew test
   ```

---

## 🧪 Testing

Unit tests for `ShelfCalculator` and core math logic are included under `app/src/test/java/com/example/shelfspace/ShelfCalculatorTest.kt`.

To run all unit tests:
```bash
./gradlew test
```

---

## 📜 License

This project is open-source and available under the [MIT License](LICENSE).
