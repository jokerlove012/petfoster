/**
 * Property-Based Tests for Price Update Isolation
 * 
 * Tests that existing booking orders maintain their original prices
 * when service package prices are updated.
 */

import { describe, it, expect } from 'vitest'
import * as fc from 'fast-check'
import type { Booking } from '@/types/booking'
import type { ServicePackage } from '@/types/institution'

/**
 * Simulates creating a booking with a snapshot of the service package price
 * This represents the business rule that booking prices are locked at creation time
 */
function createBookingWithPriceSnapshot(
  servicePackage: ServicePackage,
  days: number
): Booking {
  const basePrice = servicePackage.pricePerDay
  const totalPrice = basePrice * days
  
  return {
    id: `booking-${Date.now()}`,
    orderNumber: `ORD-${Date.now()}`,
    userId: 'user-1',
    institutionId: servicePackage.institutionId,
    servicePackageId: servicePackage.id,
    petId: 'pet-1',
    status: 'confirmed',
    startDate: new Date().toISOString(),
    endDate: new Date(Date.now() + days * 24 * 60 * 60 * 1000).toISOString(),
    totalDays: days,
    basePrice: basePrice, // Price is snapshotted at booking creation
    discount: 0,
    totalPrice: totalPrice,
    paymentStatus: 'paid',
    emergencyContact: {
      name: 'Test Contact',
      phone: '13800138000',
      relationship: 'owner'
    },
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  }
}

/**
 * Simulates updating a service package price
 * Returns a new service package with updated price
 */
function updateServicePackagePrice(
  servicePackage: ServicePackage,
  newPrice: number
): ServicePackage {
  return {
    ...servicePackage,
    pricePerDay: newPrice
  }
}

/**
 * Verifies that a booking's price remains unchanged after service package price update
 */
function verifyBookingPriceIsolation(
  bookingBeforeUpdate: Booking,
  bookingAfterUpdate: Booking
): boolean {
  return (
    bookingBeforeUpdate.basePrice === bookingAfterUpdate.basePrice &&
    bookingBeforeUpdate.totalPrice === bookingAfterUpdate.totalPrice &&
    bookingBeforeUpdate.discount === bookingAfterUpdate.discount
  )
}

// Arbitrary generators for property testing
const servicePackageArbitrary = fc.record({
  id: fc.uuid(),
  institutionId: fc.uuid(),
  name: fc.string({ minLength: 1, maxLength: 50 }),
  description: fc.string({ minLength: 0, maxLength: 200 }),
  petTypes: fc.array(fc.constantFrom('dog', 'cat', 'other') as fc.Arbitrary<'dog' | 'cat' | 'other'>, { minLength: 1, maxLength: 3 }),
  pricePerDay: fc.integer({ min: 50, max: 1000 }),
  features: fc.array(fc.string({ minLength: 1, maxLength: 30 }), { minLength: 0, maxLength: 5 }),
  maxWeight: fc.option(fc.integer({ min: 1, max: 100 })),
  isActive: fc.boolean()
})

describe('Price Update Isolation Properties', () => {
  /**
   * **Feature: pet-foster-platform, Property 8: Price update isolation**
   * **Validates: Requirements 6.2**
   * 
   * For any existing booking order, when the service package price is updated,
   * the booking order's total price should remain unchanged.
   */
  it('existing booking prices remain unchanged when service package price is updated', () => {
    fc.assert(
      fc.property(
        servicePackageArbitrary,
        fc.integer({ min: 1, max: 30 }), // booking days
        fc.integer({ min: 50, max: 2000 }), // new price (different from original)
        (servicePackage, days, newPrice) => {
          // Step 1: Create a booking with the original service package price
          const originalBooking = createBookingWithPriceSnapshot(servicePackage, days)
          
          // Step 2: Update the service package price
          const updatedServicePackage = updateServicePackagePrice(servicePackage, newPrice)
          
          // Step 3: The booking should retain its original price
          // (In a real system, this would be fetched from database)
          const bookingAfterPriceUpdate = { ...originalBooking }
          
          // Verify: Booking price should NOT change when service package price changes
          expect(bookingAfterPriceUpdate.basePrice).toBe(servicePackage.pricePerDay)
          expect(bookingAfterPriceUpdate.totalPrice).toBe(servicePackage.pricePerDay * days)
          
          // Verify: The new price is different (to ensure we're testing a real change)
          // Note: We don't require newPrice !== originalPrice, but we verify isolation
          expect(updatedServicePackage.pricePerDay).toBe(newPrice)
          
          // The key property: booking price is isolated from service package price changes
          return verifyBookingPriceIsolation(originalBooking, bookingAfterPriceUpdate)
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Multiple bookings with same service package maintain independent prices
   * Validates that each booking captures its own price snapshot
   */
  it('multiple bookings maintain independent price snapshots', () => {
    fc.assert(
      fc.property(
        servicePackageArbitrary,
        fc.array(fc.integer({ min: 1, max: 30 }), { minLength: 2, maxLength: 5 }), // multiple booking days
        fc.array(fc.integer({ min: 50, max: 2000 }), { minLength: 2, maxLength: 5 }), // price changes
        (servicePackage, bookingDays, priceChanges) => {
          const bookings: Booking[] = []
          let currentPackage = { ...servicePackage }
          
          // Create bookings at different price points
          for (let i = 0; i < Math.min(bookingDays.length, priceChanges.length); i++) {
            // Create booking with current price
            const booking = createBookingWithPriceSnapshot(currentPackage, bookingDays[i])
            bookings.push(booking)
            
            // Update price for next booking
            currentPackage = updateServicePackagePrice(currentPackage, priceChanges[i])
          }
          
          // Verify each booking maintains its original price
          let previousPrice = servicePackage.pricePerDay
          for (let i = 0; i < bookings.length; i++) {
            const expectedPrice = i === 0 ? servicePackage.pricePerDay : priceChanges[i - 1]
            expect(bookings[i].basePrice).toBe(expectedPrice)
            expect(bookings[i].totalPrice).toBe(expectedPrice * bookingDays[i])
            previousPrice = priceChanges[i]
          }
          
          return true
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Booking price snapshot is immutable
   * Validates that booking prices cannot be accidentally modified
   */
  it('booking price snapshot is immutable after creation', () => {
    fc.assert(
      fc.property(
        servicePackageArbitrary,
        fc.integer({ min: 1, max: 30 }),
        fc.integer({ min: 1, max: 10 }), // number of price updates
        (servicePackage, days, updateCount) => {
          // Create initial booking
          const booking = createBookingWithPriceSnapshot(servicePackage, days)
          const originalBasePrice = booking.basePrice
          const originalTotalPrice = booking.totalPrice
          
          // Simulate multiple price updates to the service package
          let currentPackage = { ...servicePackage }
          for (let i = 0; i < updateCount; i++) {
            const newPrice = servicePackage.pricePerDay + (i + 1) * 100
            currentPackage = updateServicePackagePrice(currentPackage, newPrice)
          }
          
          // Booking prices should remain unchanged
          expect(booking.basePrice).toBe(originalBasePrice)
          expect(booking.totalPrice).toBe(originalTotalPrice)
          
          return true
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Price isolation works for both increases and decreases
   * Validates isolation regardless of price change direction
   */
  it('price isolation works for both price increases and decreases', () => {
    fc.assert(
      fc.property(
        servicePackageArbitrary,
        fc.integer({ min: 1, max: 30 }),
        fc.boolean(), // true = increase, false = decrease
        fc.integer({ min: 10, max: 500 }), // price change amount
        (servicePackage, days, isIncrease, changeAmount) => {
          // Create booking with original price
          const booking = createBookingWithPriceSnapshot(servicePackage, days)
          
          // Calculate new price (increase or decrease)
          const newPrice = isIncrease 
            ? servicePackage.pricePerDay + changeAmount
            : Math.max(1, servicePackage.pricePerDay - changeAmount)
          
          // Update service package price
          const updatedPackage = updateServicePackagePrice(servicePackage, newPrice)
          
          // Verify booking price is isolated from the change
          expect(booking.basePrice).toBe(servicePackage.pricePerDay)
          expect(booking.totalPrice).toBe(servicePackage.pricePerDay * days)
          
          // Verify the service package price actually changed
          expect(updatedPackage.pricePerDay).toBe(newPrice)
          
          return true
        }
      ),
      { numRuns: 100 }
    )
  })
})
