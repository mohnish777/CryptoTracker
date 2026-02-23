# StateFlow & SharingStarted Quick Notes

## Code Pattern
```kotlin
val _state = MutableStateFlow(CoinListState()) // hot
val state = _state
    .onStart { // cold operation
        loadCoin()
    }
    .stateIn( // converts to hot
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CoinListState()
    )
```

## Hot vs Cold Flows

### 🔥 **Hot Flow** (MutableStateFlow)
- **Always active** - Emits values regardless of collectors
- **Shared** - Multiple collectors get the same values
- **Has state** - Always holds a current value
- **Example**: `MutableStateFlow(CoinListState())`

### ❄️ **Cold Flow** (Regular Flow)
- **Lazy** - Only activates when collected
- **Independent** - Each collector gets its own execution
- **No state** - Doesn't hold values
- **Example**: `.onStart { }` creates a cold flow

## Step-by-Step Breakdown

### 1. **`_state = MutableStateFlow(CoinListState())`**
```
✅ Creates hot flow
✅ Initial value: CoinListState()
✅ Private (internal use only)
✅ Immediately active
```

### 2. **`.onStart { loadCoin() }`**
```
❄️ Converts to cold flow temporarily
❄️ Executes loadCoin() when first collector subscribes
❄️ Runs BEFORE emitting any values
❄️ One-time initialization per collection
```

### 3. **`.stateIn(...)`**
```
🔥 Converts back to hot StateFlow
🔥 Shared among all collectors
🔥 Lifecycle-aware collection
🔥 Optimized for UI consumption
```

## SharingStarted.WhileSubscribed(5000)

### What It Does
- **Starts** collecting when first subscriber appears
- **Stops** collecting 5000ms (5 seconds) after last subscriber leaves
- **Restarts** if new subscriber appears within timeout

### The 5000ms Timeout Explained

```
Subscriber 1 arrives → Start collecting
Subscriber 1 leaves  → Wait 5 seconds...
    ↓
    If no new subscriber → Stop collecting (save resources)
    If new subscriber   → Keep collecting (avoid restart)
```

### Why 5 seconds?
- **Configuration changes** (rotation) typically complete within 5s
- Avoids unnecessary **stop/restart** during quick UI changes
- **Saves resources** if user truly leaves the screen

## What Happens When...

### ✅ **First UI Collector (Composable) Subscribes**
1. `onStart { loadCoin() }` executes
2. Data loading begins
3. StateFlow starts emitting updates
4. UI receives initial state + updates

### ✅ **User Rotates Screen**
1. Old composable unsubscribes
2. 5-second timer starts
3. New composable subscribes (within 5s)
4. Timer cancelled → **No restart needed**
5. `loadCoin()` **NOT called again** ✅
6. UI gets current state immediately

### ✅ **User Navigates Away**
1. Composable unsubscribes
2. 5-second timer starts
3. No new subscriber within 5s
4. StateFlow **stops collecting**
5. Resources freed (battery saved)

### ✅ **User Returns Quickly (<5s)**
1. New composable subscribes
2. Timer cancelled
3. StateFlow **still active**
4. `loadCoin()` **NOT called again** ✅
5. UI gets current state

### ✅ **User Returns Later (>5s)**
1. StateFlow was stopped
2. New composable subscribes
3. `onStart { loadCoin() }` executes **again**
4. Fresh data loaded
5. UI updated

## Alternative SharingStarted Options

### **SharingStarted.Eagerly**
```kotlin
started = SharingStarted.Eagerly
```
- Starts **immediately** when ViewModel created
- **Never stops** (even with no subscribers)
- ⚠️ Can waste resources
- Use for: Critical background tasks

### **SharingStarted.Lazily**
```kotlin
started = SharingStarted.Lazily
```
- Starts when **first subscriber** appears
- **Never stops** (keeps running forever)
- ⚠️ Can waste resources after last subscriber leaves
- Use for: Data that should persist

### **SharingStarted.WhileSubscribed(0)**
```kotlin
started = SharingStarted.WhileSubscribed(0)
```
- Stops **immediately** when last subscriber leaves
- No timeout grace period
- ⚠️ Restarts on every config change
- Use for: One-time operations

## Key Benefits of This Pattern

✅ **Efficient** - Stops when not needed (after 5s timeout)
✅ **Smart** - Survives config changes (rotation)
✅ **Clean** - Auto-initialization with `onStart`
✅ **Safe** - Scoped to viewModelScope (auto-cleanup)
✅ **Optimized** - Single shared flow for all collectors

## Common Timeout Values

| Timeout | Use Case |
|---------|----------|
| `0ms` | Stop immediately (aggressive cleanup) |
| `5000ms` | **Default** - Survives config changes |
| `Long.MAX_VALUE` | Essentially never stops |

## Memory & Performance

```
No Subscribers → Wait 5s → Stop Collecting
    ↓
Saves CPU, Battery, Memory
    ↓
Automatic Resource Management
```

## Quick Comparison

| Aspect | MutableStateFlow | stateIn() |
|--------|------------------|-----------|
| Type | Hot | Hot |
| Initialization | Immediate | On first subscriber |
| Lifecycle | Manual | Automatic (with WhileSubscribed) |
| Sharing | Always | Configurable |
| Best for | Internal state | Exposed state to UI |

