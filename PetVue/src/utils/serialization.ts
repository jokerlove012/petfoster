/**
 * Serialize state to JSON string
 */
export function serializeState<T>(state: T): string {
  // Pre-process to convert Date objects before JSON.stringify
  const processed = convertDatesToMarkers(state)
  return JSON.stringify(processed, null, 2)
}

/**
 * Recursively convert Date objects to marker format
 */
function convertDatesToMarkers(obj: unknown): unknown {
  if (obj === null || obj === undefined) {
    return obj
  }
  
  if (obj instanceof Date) {
    return { __type: 'Date', value: obj.toISOString() }
  }
  
  if (Array.isArray(obj)) {
    return obj.map(convertDatesToMarkers)
  }
  
  if (typeof obj === 'object') {
    const result: Record<string, unknown> = {}
    for (const key of Object.keys(obj as Record<string, unknown>)) {
      result[key] = convertDatesToMarkers((obj as Record<string, unknown>)[key])
    }
    return result
  }
  
  return obj
}

/**
 * Deserialize JSON string to state
 */
export function deserializeState<T>(json: string, defaultState: T): T {
  try {
    const parsed = JSON.parse(json)
    
    // Validate that parsed result is an object
    if (typeof parsed !== 'object' || parsed === null) {
      console.warn('Invalid state structure, using default')
      return defaultState
    }
    
    // Recursively restore Date objects in the parsed result
    return restoreDates(parsed) as T
  } catch (error) {
    console.error('Failed to deserialize state:', error)
    return defaultState
  }
}

/**
 * Recursively restore Date objects from serialized format
 */
function restoreDates(obj: unknown): unknown {
  if (obj === null || obj === undefined) {
    return obj
  }
  
  if (Array.isArray(obj)) {
    return obj.map(restoreDates)
  }
  
  if (typeof obj === 'object') {
    const record = obj as Record<string, unknown>
    
    // Check if this is our Date marker
    if (record.__type === 'Date' && typeof record.value === 'string') {
      return new Date(record.value)
    }
    
    // Recursively process all properties
    const result: Record<string, unknown> = {}
    for (const key of Object.keys(record)) {
      result[key] = restoreDates(record[key])
    }
    return result
  }
  
  return obj
}

/**
 * Check if a value is a valid state object
 */
export function isValidState(value: unknown): boolean {
  return typeof value === 'object' && value !== null
}
