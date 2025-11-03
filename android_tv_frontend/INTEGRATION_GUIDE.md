# Backend/Database Integration Guide

This guide explains how to replace the mock data implementation with a real backend API or database connection.

## Current Implementation

The app currently uses `MockRecipeRepository` which provides hardcoded sample recipes. This is defined in:
- `app/src/main/java/com/example/android_tv_frontend/data/repository/MockRecipeRepository.kt`

## Integration Steps

### Step 1: Identify Your Data Source

Determine which data source you'll be using:

**Option A**: REST API or GraphQL endpoint
**Option B**: Local Room database  
**Option C**: Direct database connection (recipe_database container)

### Step 2: Add Required Dependencies

#### For REST API (Retrofit already included):

```kotlin
// Already in build.gradle.kts:
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
```

#### For Room Database:

Add to `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
}

// Also add kapt plugin at top:
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}
```

#### For Direct Database Connection:

Add appropriate database driver (e.g., MySQL, PostgreSQL):

```kotlin
dependencies {
    implementation("mysql:mysql-connector-java:8.0.33")
    // or
    implementation("org.postgresql:postgresql:42.7.0")
}
```

### Step 3: Create Repository Implementation

#### Example: REST API Implementation

```kotlin
// 1. Define API service interface
interface RecipeApiService {
    @GET("api/recipes")
    suspend fun getAllRecipes(): List<RecipeDto>
    
    @GET("api/recipes/cuisine/{cuisine}")
    suspend fun getRecipesByCuisine(@Path("cuisine") cuisine: String): List<RecipeDto>
    
    @GET("api/recipes/{id}")
    suspend fun getRecipeById(@Path("id") id: String): RecipeDto
    
    @POST("api/recipes/{id}/favorite")
    suspend fun toggleFavorite(@Path("id") id: String): RecipeDto
}

// 2. Create DTO models
data class RecipeDto(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val cuisine: String,
    val prepTime: Int,
    val cookTime: Int,
    val difficulty: String,
    val servings: Int,
    val ingredients: List<IngredientDto>,
    val instructions: List<InstructionStepDto>,
    val isFavorite: Boolean
)

// 3. Create mapper extensions
fun RecipeDto.toRecipe(): Recipe {
    return Recipe(
        id = id,
        title = title,
        description = description,
        imageUrl = imageUrl,
        cuisine = cuisine,
        prepTime = prepTime,
        cookTime = cookTime,
        difficulty = Difficulty.valueOf(difficulty.uppercase()),
        servings = servings,
        ingredients = ingredients.map { it.toIngredient() },
        instructions = instructions.map { it.toInstructionStep() },
        isFavorite = isFavorite
    )
}

// 4. Implement repository
class ApiRecipeRepository(
    private val apiService: RecipeApiService
) : RecipeRepository {
    
    override fun getAllRecipes(): Flow<List<Recipe>> = flow {
        val recipes = apiService.getAllRecipes().map { it.toRecipe() }
        emit(recipes)
    }.flowOn(Dispatchers.IO)
    
    override fun getRecipesByCuisine(cuisine: String): Flow<List<Recipe>> = flow {
        val recipes = apiService.getRecipesByCuisine(cuisine).map { it.toRecipe() }
        emit(recipes)
    }.flowOn(Dispatchers.IO)
    
    override fun getFavoriteRecipes(): Flow<List<Recipe>> = flow {
        val recipes = apiService.getAllRecipes()
            .filter { it.isFavorite }
            .map { it.toRecipe() }
        emit(recipes)
    }.flowOn(Dispatchers.IO)
    
    override suspend fun getRecipeById(recipeId: String): Recipe? {
        return try {
            apiService.getRecipeById(recipeId).toRecipe()
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun toggleFavorite(recipeId: String) {
        apiService.toggleFavorite(recipeId)
    }
    
    override fun getAvailableCuisines(): List<String> {
        return listOf("Italian", "Mexican", "Asian", "American", "Mediterranean", "Indian")
    }
}

// 5. Create Retrofit instance
object RetrofitClient {
    private const val BASE_URL = "http://your-backend-url/" // TODO: Update with actual URL
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()
    
    val apiService: RecipeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RecipeApiService::class.java)
    }
}
```

#### Example: Room Database Implementation

```kotlin
// 1. Define entities
@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val cuisine: String,
    val prepTime: Int,
    val cookTime: Int,
    val difficulty: String,
    val servings: Int,
    val isFavorite: Boolean
)

// 2. Create DAO
@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes")
    fun getAllRecipes(): Flow<List<RecipeEntity>>
    
    @Query("SELECT * FROM recipes WHERE cuisine = :cuisine")
    fun getRecipesByCuisine(cuisine: String): Flow<List<RecipeEntity>>
    
    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: String): RecipeEntity?
    
    @Query("UPDATE recipes SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: String)
}

// 3. Create database
@Database(entities = [RecipeEntity::class], version = 1)
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    
    companion object {
        @Volatile
        private var INSTANCE: RecipeDatabase? = null
        
        fun getInstance(context: Context): RecipeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecipeDatabase::class.java,
                    "recipe_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// 4. Implement repository
class RoomRecipeRepository(
    private val recipeDao: RecipeDao
) : RecipeRepository {
    
    override fun getAllRecipes(): Flow<List<Recipe>> {
        return recipeDao.getAllRecipes().map { entities ->
            entities.map { it.toRecipe() }
        }
    }
    
    override fun getRecipesByCuisine(cuisine: String): Flow<List<Recipe>> {
        return recipeDao.getRecipesByCuisine(cuisine).map { entities ->
            entities.map { it.toRecipe() }
        }
    }
    
    override suspend fun getRecipeById(recipeId: String): Recipe? {
        return recipeDao.getRecipeById(recipeId)?.toRecipe()
    }
    
    override suspend fun toggleFavorite(recipeId: String) {
        recipeDao.toggleFavorite(recipeId)
    }
    
    // ... implement other methods
}
```

### Step 4: Update Fragment Initialization

Replace the mock repository in your fragments:

#### BrowseFragment.kt

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // OLD (Mock):
    // val repository = MockRecipeRepository()
    
    // NEW (API):
    val repository = ApiRecipeRepository(RetrofitClient.apiService)
    
    // OR (Room):
    // val database = RecipeDatabase.getInstance(requireContext())
    // val repository = RoomRecipeRepository(database.recipeDao())
    
    viewModel = BrowseViewModel(repository)
}
```

#### DetailFragment.kt

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    recipeId = arguments?.getString(ARG_RECIPE_ID)
    
    // OLD (Mock):
    // val repository = MockRecipeRepository()
    
    // NEW (API):
    val repository = ApiRecipeRepository(RetrofitClient.apiService)
    
    viewModel = DetailViewModel(repository)
}
```

### Step 5: Environment Configuration

If using environment variables for API URLs or database connections:

1. Create a `local.properties` file (gitignored) or use environment variables
2. Read configuration at runtime:

```kotlin
object AppConfig {
    const val API_BASE_URL = BuildConfig.API_BASE_URL ?: "http://localhost:8000/"
}
```

Add to `app/build.gradle.kts`:

```kotlin
android {
    defaultConfig {
        // Read from environment or local.properties
        buildConfigField("String", "API_BASE_URL", "\"${System.getenv("API_BASE_URL") ?: "http://localhost:8000/"}\"")
    }
    
    buildFeatures {
        buildConfig = true
    }
}
```

### Step 6: Enable Image Loading

Uncomment Glide image loading code:

**In RecipeAdapter.kt** (around line 50):

```kotlin
// Uncomment:
Glide.with(itemView.context)
    .load(recipe.imageUrl)
    .placeholder(R.drawable.ic_launcher)
    .into(imageView)
```

**In DetailFragment.kt** (around line 125):

```kotlin
// Uncomment:
Glide.with(this)
    .load(recipe.imageUrl)
    .placeholder(R.drawable.ic_launcher)
    .into(heroImage)
```

### Step 7: Error Handling

Add proper error handling for network/database failures:

```kotlin
override fun loadRecipes() {
    viewModelScope.launch {
        _uiState.value = BrowseUiState.Loading
        
        try {
            val flow = when (val filter = _selectedFilter.value) {
                is FilterType.All -> repository.getAllRecipes()
                is FilterType.Cuisine -> repository.getRecipesByCuisine(filter.cuisine)
                is FilterType.Favorites -> repository.getFavoriteRecipes()
            }
            
            flow.catch { error ->
                // Log error
                Log.e("BrowseViewModel", "Error loading recipes", error)
                _uiState.value = BrowseUiState.Error(
                    error.message ?: "Failed to load recipes"
                )
            }.collect { recipes ->
                if (recipes.isEmpty()) {
                    _uiState.value = BrowseUiState.Empty
                } else {
                    _uiState.value = BrowseUiState.Success(recipes)
                }
            }
        } catch (e: Exception) {
            Log.e("BrowseViewModel", "Error loading recipes", e)
            _uiState.value = BrowseUiState.Error(
                e.message ?: "Failed to load recipes"
            )
        }
    }
}
```

### Step 8: Testing

Test the integration:

1. **Unit tests** for repository implementation
2. **Integration tests** for API calls
3. **UI tests** for fragment behavior

```kotlin
class ApiRecipeRepositoryTest {
    @Test
    fun `getAllRecipes returns recipes from API`() = runTest {
        // Mock API service
        val mockService = mock<RecipeApiService>()
        whenever(mockService.getAllRecipes()).thenReturn(sampleRecipeDtos)
        
        val repository = ApiRecipeRepository(mockService)
        val recipes = repository.getAllRecipes().first()
        
        assertEquals(3, recipes.size)
    }
}
```

## Connecting to recipe_database Container

If the `recipe_database` container is available:

1. **Check environment variables** in `.env` file for database connection strings
2. **Create database client** using the provided credentials
3. **Query recipes** using SQL or ORM
4. **Map results** to Recipe model

Example with environment variables:

```kotlin
// Read from .env (use a library like dotenv-kotlin)
val dbHost = System.getenv("RECIPE_DB_HOST") ?: "localhost"
val dbPort = System.getenv("RECIPE_DB_PORT") ?: "5432"
val dbName = System.getenv("RECIPE_DB_NAME") ?: "recipes"
val dbUser = System.getenv("RECIPE_DB_USER") ?: "user"
val dbPass = System.getenv("RECIPE_DB_PASS") ?: "password"

// Create connection
val dataSource = PGSimpleDataSource().apply {
    serverNames = arrayOf(dbHost)
    portNumbers = intArrayOf(dbPort.toInt())
    databaseName = dbName
    user = dbUser
    password = dbPass
}
```

## Troubleshooting

### Network Issues

- Ensure `INTERNET` permission in AndroidManifest.xml (already added)
- Check network security config for cleartext traffic if using HTTP
- Verify API endpoint is accessible from emulator

### Database Issues

- Verify database schema matches model structure
- Check connection credentials
- Ensure database migrations are applied

### Image Loading Issues

- Verify image URLs are valid and accessible
- Check Glide logs for loading errors
- Ensure images are optimized for TV resolution

## Best Practices

1. **Use dependency injection** (Hilt/Koin) for managing repository instances
2. **Implement caching** for offline support
3. **Add retry logic** for failed network requests
4. **Use coroutines** for async operations (already implemented)
5. **Handle loading states** properly (already implemented)
6. **Log errors** for debugging
7. **Test on actual TV device** not just emulator

## Additional Resources

- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Room Documentation](https://developer.android.com/training/data-storage/room)
- [Android TV Development Guide](https://developer.android.com/training/tv)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
