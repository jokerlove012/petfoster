import { describe, it, expect } from 'vitest'
import * as fc from 'fast-check'
import { serializeState, deserializeState } from '@/utils/serialization'

describe('State Serialization Properties', () => {
  /**
   * **Feature: pet-foster-platform, Property 11: State serialization round-trip**
   * **Validates: Requirements 11.1, 11.2**
   * 
   * For any valid application state, serializing to JSON and then 
   * deserializing should produce an equivalent state object.
   */
  it('serialization round-trip preserves state', () => {
    // Arbitrary for simple state objects
    const stateArbitrary = fc.record({
      id: fc.string({ minLength: 1, maxLength: 50 }),
      name: fc.string({ minLength: 0, maxLength: 100 }),
      count: fc.integer({ min: 0, max: 1000000 }),
      active: fc.boolean(),
      tags: fc.array(fc.string({ minLength: 1, maxLength: 20 }), { maxLength: 10 }),
      metadata: fc.option(fc.record({
        key: fc.string(),
        value: fc.string(),
      })),
    })

    fc.assert(
      fc.property(stateArbitrary, (state) => {
        const serialized = serializeState(state)
        const deserialized = deserializeState(serialized, {})
        
        // Check that all properties are preserved
        expect(deserialized).toEqual(state)
        return true
      }),
      { numRuns: 100 }
    )
  })

  /**
   * **Feature: pet-foster-platform, Property 11: State serialization round-trip**
   * **Validates: Requirements 11.1, 11.2**
   * 
   * For any valid user state with dates, serializing and deserializing
   * should preserve the date values.
   */
  it('serialization round-trip preserves dates', () => {
    interface UserState {
      id: string
      email: string
      createdAt: Date
      updatedAt: Date
    }
    
    const userStateArbitrary = fc.record({
      id: fc.string({ minLength: 1 }),
      email: fc.emailAddress(),
      createdAt: fc.date({ min: new Date('2020-01-01'), max: new Date('2030-12-31') }),
      updatedAt: fc.date({ min: new Date('2020-01-01'), max: new Date('2030-12-31') }),
    })

    fc.assert(
      fc.property(userStateArbitrary, (state) => {
        const serialized = serializeState(state)
        const deserialized = deserializeState<UserState>(serialized, state)
        
        // Check that dates are preserved (as Date objects)
        expect(deserialized.id).toBe(state.id)
        expect(deserialized.email).toBe(state.email)
        expect(deserialized.createdAt instanceof Date).toBe(true)
        expect(deserialized.updatedAt instanceof Date).toBe(true)
        expect(deserialized.createdAt.getTime()).toBe(state.createdAt.getTime())
        expect(deserialized.updatedAt.getTime()).toBe(state.updatedAt.getTime())
        return true
      }),
      { numRuns: 100 }
    )
  })

  /**
   * **Feature: pet-foster-platform, Property 11: State serialization round-trip**
   * **Validates: Requirements 11.1, 11.2**
   * 
   * For nested state objects, serialization should preserve the structure.
   */
  it('serialization round-trip preserves nested structures', () => {
    const nestedStateArbitrary = fc.record({
      user: fc.record({
        id: fc.string({ minLength: 1 }),
        profile: fc.record({
          name: fc.string(),
          age: fc.integer({ min: 0, max: 150 }),
        }),
      }),
      settings: fc.record({
        theme: fc.constantFrom('light', 'dark'),
        notifications: fc.boolean(),
      }),
      items: fc.array(
        fc.record({
          id: fc.string({ minLength: 1 }),
          value: fc.integer(),
        }),
        { maxLength: 5 }
      ),
    })

    fc.assert(
      fc.property(nestedStateArbitrary, (state) => {
        const serialized = serializeState(state)
        const deserialized = deserializeState(serialized, {})
        
        expect(deserialized).toEqual(state)
        return true
      }),
      { numRuns: 100 }
    )
  })

  /**
   * Serialized output should be valid JSON
   */
  it('serialization produces valid JSON', () => {
    const stateArbitrary = fc.record({
      key: fc.string(),
      value: fc.oneof(fc.string(), fc.integer(), fc.boolean()),
    })

    fc.assert(
      fc.property(stateArbitrary, (state) => {
        const serialized = serializeState(state)
        
        // Should not throw when parsing
        expect(() => JSON.parse(serialized)).not.toThrow()
        return true
      }),
      { numRuns: 100 }
    )
  })
})
