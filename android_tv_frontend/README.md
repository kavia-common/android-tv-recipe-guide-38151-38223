# Android TV Recipe Guide

A Recipe browsing application for Android TV that displays recipes in a card-based grid layout with side navigation and full-screen detail views.

## Features

- **Browse Screen**: Grid layout (3 columns) optimized for TV D-pad navigation showing recipe cards
- **Side Navigation**: Filter recipes by cuisine type or view favorites
- **Detail Screen**: Full recipe information with hero image, metadata, ingredients, and step-by-step instructions
- **Ocean Professional Theme**: Modern blue (#2563EB) and amber (#F59E0B) color scheme
- **TV-Optimized**: D-pad navigation, focus states, overscan-safe margins, and large text for viewing distance
- **Mock Data**: Currently uses in-memory mock repository with sample recipes

## Architecture

### Data Layer

- **Models**: `Recipe`, `Ingredient`, `InstructionStep`, `Difficulty` enum
- **Repository Interface**: `RecipeRepository` - abstraction for data access
- **Mock Implementation**: `MockRecipeRepository` - provides sample data

### UI Layer

- **MainActivity**: Hosts fragments and handles navigation
- **BrowseFragment**: Displays recipe grid with filters
- **DetailFragment**: Shows full recipe details
- **ViewModels**: Manage UI state and data loading
- **Adapters**: RecyclerView adapters for recipe cards and filters

## Project Structure

```
app/src/main/
├── java/com/example/android_tv_frontend/
│   ├── MainActivity.kt
│   ├── data/
│   │   ├── model/
│   │   │   └── Recipe.kt
│   │   └── repository/
│   │       ├── RecipeRepository.kt
│   │       └── MockRecipeRepository.kt
│   └── ui/
│       ├── browse/
│       │   ├── BrowseFragment.kt
│       │   ├── BrowseViewModel.kt
│       │   ├── RecipeAdapter.kt
│       │   └── FilterAdapter.kt
│       └── detail/
│           ├── DetailFragment.kt
│           └── DetailViewModel.kt
└── res/
    ├── layout/
    │   ├── activity_main.xml
    │   ├── fragment_browse.xml
    │   ├── fragment_detail.xml
    │   ├── item_recipe_card.xml
    │   └── item_filter.xml
    ├── values/
    │   ├── colors.xml (Ocean Professional theme)
    │   ├── dimens.xml (TV-optimized sizes)
    │   ├── strings.xml
    │   └── styles.xml
    └── drawable/
        ├── card_background.xml (focus states)
        └── button_background.xml (focus states)
```

## Future Backend/Database Integration

The app is designed with data abstraction to easily swap mock data for real backend or database integration.

### Option 1: Backend API Integration

1. Create a new implementation of `RecipeRepository`:

```kotlin
class ApiRecipeRepository(
    private val apiService: RecipeApiService
) : RecipeRepository {
    override fun getAllRecipes(): Flow<List<Recipe>> {
        // Call API and convert to Flow
    }
    // ... implement other methods
}
```

2. Define Retrofit API interface:

```kotlin
interface RecipeApiService {
    @GET("recipes")
    suspend fun getRecipes(): List<RecipeDto>
    
    @GET("recipes/{id}")
    suspend fun getRecipeById(@Path("id") id: String): RecipeDto
}
```

3. Update ViewModels to use the new repository implementation

### Option 2: Local Database Integration

1. Add Room dependencies to `build.gradle.kts`
2. Create Room entities, DAOs, and database
3. Implement `RoomRecipeRepository`:

```kotlin
class RoomRecipeRepository(
    private val recipeDao: RecipeDao
) : RecipeRepository {
    override fun getAllRecipes(): Flow<List<Recipe>> {
        return recipeDao.getAllRecipes().map { entities ->
            entities.map { it.toRecipe() }
        }
    }
    // ... implement other methods
}
```

### Option 3: Connect to recipe_database Container

If integrating with the `recipe_database` container mentioned in the work item:

1. Check the database connection environment variables in `.env` file
2. Use the provided database credentials to connect
3. Implement repository using the database connection:

```kotlin
class DatabaseRecipeRepository(
    private val databaseClient: DatabaseClient
) : RecipeRepository {
    // Implement using database queries
}
```

### Switching Repository Implementation

Update the ViewModels' initialization in Fragments:

```kotlin
// Current (Mock):
val repository = MockRecipeRepository()

// Future (API):
val apiService = retrofit.create(RecipeApiService::class.java)
val repository = ApiRecipeRepository(apiService)

// Future (Room):
val database = RecipeDatabase.getInstance(requireContext())
val repository = RoomRecipeRepository(database.recipeDao())
```

For production, use dependency injection (Hilt/Koin) to manage repository instances.

## Image Loading

Currently using placeholder images. To load real images from URLs:

1. Uncomment Glide code in adapters:
   - `RecipeAdapter.kt` line ~50
   - `DetailFragment.kt` line ~125

2. Ensure image URLs are included in Recipe data

3. Glide is already included in dependencies

## Building and Running

### Requirements

- Android Studio Arctic Fox or later
- Android SDK 34
- Min SDK 21 (Android 5.0)
- Target SDK 34
- Gradle 8.7

### Build Commands

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test

# Lint check
./gradlew lintDebug
```

### Running on TV Emulator

1. Create Android TV emulator in Android Studio (API 21+)
2. Run the app from Android Studio or:
   ```bash
   ./gradlew installDebug
   ```

## TV Navigation

- **D-PAD**: Navigate between UI elements
- **CENTER/OK**: Select items
- **BACK**: Go back to previous screen
- All UI elements have proper focus states and are navigable via D-pad

## Theme Customization

Colors are defined in `res/values/colors.xml`:
- Primary: `#2563EB` (Blue)
- Secondary: `#F59E0B` (Amber)
- Background: `#0F172A` (Dark blue-gray)
- Text: `#F9FAFB` (Light)

## Notes

- All layouts use overscan-safe margins (96dp horizontal, 54dp vertical)
- Text sizes are optimized for TV viewing distance (18sp minimum)
- Focus states use 3dp stroke with primary color
- Grid uses 3 columns with 280dp card width
- Smooth transitions between screens
- Empty, loading, and error states are handled

## License

[Add your license here]
