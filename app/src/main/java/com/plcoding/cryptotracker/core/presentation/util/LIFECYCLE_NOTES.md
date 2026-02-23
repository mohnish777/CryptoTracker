# ObserveAsEvents Lifecycle Quick Notes

## Code Flow
```kotlin
val lifecycleOwner = LocalLifecycleOwner.current
LaunchedEffect(lifecycleOwner.lifecycle, key1, key2) {
    lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        withContext(Dispatchers.Main.immediate) {
            events.collect { event ->
                onEvent(event)
            }
        }
    }
}
```

## Lifecycle Steps

### 1. **Composition Phase**
- `LocalLifecycleOwner.current` → Gets the current lifecycle owner (Activity/Fragment)
- Composable enters composition

### 2. **LaunchedEffect Triggered**
- Launches when: Composable enters composition OR `key1`/`key2` changes
- Creates a coroutine tied to the composable's lifecycle
- **Cancels & restarts** if keys change

### 3. **repeatOnLifecycle(STARTED)**
- **STARTED** → Lifecycle is at least STARTED (visible to user)
- Suspends when lifecycle goes below STARTED (app backgrounded)
- Resumes when lifecycle returns to STARTED (app foregrounded)
- **Automatically cancels** collection when lifecycle drops below STARTED

### 4. **withContext(Dispatchers.Main.immediate)**
- Ensures collection happens on Main thread
- `.immediate` → Doesn't redispatch if already on Main thread (performance optimization)

### 5. **events.collect()**
- Starts collecting events from the Flow
- Each event triggers `onEvent(event)`
- Collection is **lifecycle-aware** and **safe**

## What Happens When...

### ✅ **App Goes to Background**
1. Lifecycle → STOPPED
2. `repeatOnLifecycle` suspends
3. Flow collection **pauses** (no events processed)
4. Coroutine stays alive but suspended

### ✅ **App Returns to Foreground**
1. Lifecycle → STARTED
2. `repeatOnLifecycle` resumes
3. Flow collection **resumes**
4. Events start processing again

### ✅ **Composable Leaves Composition**
1. LaunchedEffect cancelled
2. Coroutine cancelled
3. Flow collection stopped
4. Resources cleaned up

### ✅ **key1 or key2 Changes**
1. Previous LaunchedEffect cancelled
2. New LaunchedEffect launched
3. Flow collection restarted with new keys

### ✅ **Configuration Change (Rotation)**
1. Composable recomposes
2. If keys unchanged → LaunchedEffect continues
3. If keys changed → Restarts

## Key Benefits

🔒 **No Memory Leaks** - Automatically cancels when composable removed
🔋 **Battery Efficient** - Stops processing when app backgrounded
🎯 **Main Thread Safe** - Events always handled on UI thread
♻️ **Lifecycle Aware** - Respects Android lifecycle states

## Lifecycle States (Quick Reference)

```
DESTROYED → INITIALIZED → CREATED → STARTED → RESUMED
                                      ↑
                                   (Visible)
```

- **RESUMED** = Foreground, interactive
- **STARTED** = Visible but may not have focus
- **CREATED** = Created but not visible
- **DESTROYED** = Cleaned up

