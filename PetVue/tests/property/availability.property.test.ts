/**
 * Property-Based Tests for Availability Blocking
 * 
 * Tests that booking requests for unavailable dates are properly rejected.
 */

import { describe, it, expect } from 'vitest'
import * as fc from 'fast-check'

/**
 * Represents an institution's availability calendar
 */
interface AvailabilityCalendar {
  institutionId: string
  blockedDates: Set<string>
  bookedDates: Set<string>
}

/**
 * Represents a booking request
 */
interface BookingRequest {
  institutionId: string
  startDate: string
  endDate: string
  petId: string
}

/**
 * Result of a booking availability check
 */
interface AvailabilityCheckResult {
  isAvailable: boolean
  unavailableDates: string[]
  reason?: string
}

/**
 * Formats a Date object to YYYY-MM-DD string
 */
function formatDateStr(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * Generates all dates between start and end (inclusive)
 */
function getDateRange(startDate: string, endDate: string): string[] {
  const dates: string[] = []
  const start = new Date(startDate)
  const end = new Date(endDate)
  
  const current = new Date(start)
  while (current <= end) {
    dates.push(formatDateStr(current))
    current.setDate(current.getDate() + 1)
  }
  
  return dates
}

/**
 * Checks if a booking request can be made based on availability
 */
function checkAvailability(
  calendar: AvailabilityCalendar,
  request: BookingRequest
): AvailabilityCheckResult {
  const requestedDates = getDateRange(request.startDate, request.endDate)
  const unavailableDates: string[] = []
  
  for (const date of requestedDates) {
    if (calendar.blockedDates.has(date)) {
      unavailableDates.push(date)
    }
    if (calendar.bookedDates.has(date)) {
      unavailableDates.push(date)
    }
  }
  
  if (unavailableDates.length > 0) {
    return {
      isAvailable: false,
      unavailableDates,
      reason: `以下日期不可预约: ${unavailableDates.join(', ')}`
    }
  }
  
  return {
    isAvailable: true,
    unavailableDates: []
  }
}

/**
 * Simulates blocking a date in the calendar
 */
function blockDate(calendar: AvailabilityCalendar, date: string): AvailabilityCalendar {
  const newBlockedDates = new Set(calendar.blockedDates)
  newBlockedDates.add(date)
  return {
    ...calendar,
    blockedDates: newBlockedDates
  }
}

/**
 * Simulates unblocking a date in the calendar
 */
function unblockDate(calendar: AvailabilityCalendar, date: string): AvailabilityCalendar {
  const newBlockedDates = new Set(calendar.blockedDates)
  newBlockedDates.delete(date)
  return {
    ...calendar,
    blockedDates: newBlockedDates
  }
}

// Arbitrary generators
const dateArbitrary = fc.date({
  min: new Date('2025-01-01'),
  max: new Date('2025-12-31')
}).map(d => formatDateStr(d))

const dateRangeArbitrary = fc.tuple(
  fc.date({ min: new Date('2025-01-01'), max: new Date('2025-06-30') }),
  fc.integer({ min: 1, max: 14 })
).map(([startDate, days]) => {
  const endDate = new Date(startDate)
  endDate.setDate(endDate.getDate() + days)
  return {
    startDate: formatDateStr(startDate),
    endDate: formatDateStr(endDate)
  }
})

const calendarArbitrary = fc.record({
  institutionId: fc.uuid(),
  blockedDates: fc.array(dateArbitrary, { minLength: 0, maxLength: 30 }).map(dates => new Set(dates)),
  bookedDates: fc.array(dateArbitrary, { minLength: 0, maxLength: 10 }).map(dates => new Set(dates))
})

describe('Availability Blocking Properties', () => {
  /**
   * **Feature: pet-foster-platform, Property 9: Availability blocking**
   * **Validates: Requirements 6.3**
   * 
   * For any date marked as unavailable in the institution calendar,
   * booking requests for that date should be rejected.
   */
  it('booking requests for blocked dates should be rejected', () => {
    fc.assert(
      fc.property(
        calendarArbitrary,
        dateRangeArbitrary,
        (calendar, dateRange) => {
          const request: BookingRequest = {
            institutionId: calendar.institutionId,
            startDate: dateRange.startDate,
            endDate: dateRange.endDate,
            petId: 'pet-1'
          }
          
          const result = checkAvailability(calendar, request)
          const requestedDates = getDateRange(dateRange.startDate, dateRange.endDate)
          
          // Check if any requested date is blocked
          const hasBlockedDate = requestedDates.some(date => 
            calendar.blockedDates.has(date) || calendar.bookedDates.has(date)
          )
          
          // If any date is blocked, the request should be rejected
          if (hasBlockedDate) {
            expect(result.isAvailable).toBe(false)
            expect(result.unavailableDates.length).toBeGreaterThan(0)
          }
          
          // All unavailable dates in result should be in blocked or booked sets
          for (const date of result.unavailableDates) {
            expect(
              calendar.blockedDates.has(date) || calendar.bookedDates.has(date)
            ).toBe(true)
          }
          
          return true
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Blocking a date makes it unavailable for booking
   * Validates that the blocking operation correctly prevents bookings
   */
  it('blocking a date makes it unavailable for booking', () => {
    fc.assert(
      fc.property(
        fc.uuid(), // institutionId
        dateArbitrary, // date to block
        (institutionId, dateToBlock) => {
          // Start with empty calendar
          let calendar: AvailabilityCalendar = {
            institutionId,
            blockedDates: new Set(),
            bookedDates: new Set()
          }
          
          // Create a booking request for the date
          const request: BookingRequest = {
            institutionId,
            startDate: dateToBlock,
            endDate: dateToBlock,
            petId: 'pet-1'
          }
          
          // Before blocking, the date should be available
          const beforeBlock = checkAvailability(calendar, request)
          expect(beforeBlock.isAvailable).toBe(true)
          
          // Block the date
          calendar = blockDate(calendar, dateToBlock)
          
          // After blocking, the date should be unavailable
          const afterBlock = checkAvailability(calendar, request)
          expect(afterBlock.isAvailable).toBe(false)
          expect(afterBlock.unavailableDates).toContain(dateToBlock)
          
          return true
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Unblocking a date makes it available again
   * Validates that the unblocking operation correctly allows bookings
   */
  it('unblocking a date makes it available again', () => {
    fc.assert(
      fc.property(
        fc.uuid(),
        dateArbitrary,
        (institutionId, dateToToggle) => {
          // Start with the date blocked
          let calendar: AvailabilityCalendar = {
            institutionId,
            blockedDates: new Set([dateToToggle]),
            bookedDates: new Set()
          }
          
          const request: BookingRequest = {
            institutionId,
            startDate: dateToToggle,
            endDate: dateToToggle,
            petId: 'pet-1'
          }
          
          // Initially blocked
          const beforeUnblock = checkAvailability(calendar, request)
          expect(beforeUnblock.isAvailable).toBe(false)
          
          // Unblock the date
          calendar = unblockDate(calendar, dateToToggle)
          
          // After unblocking, should be available
          const afterUnblock = checkAvailability(calendar, request)
          expect(afterUnblock.isAvailable).toBe(true)
          
          return true
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Booking requests for fully available ranges should succeed
   * Validates that non-blocked dates allow bookings
   */
  it('booking requests for fully available ranges should succeed', () => {
    fc.assert(
      fc.property(
        fc.uuid(),
        dateRangeArbitrary,
        (institutionId, dateRange) => {
          // Calendar with no blocked dates
          const calendar: AvailabilityCalendar = {
            institutionId,
            blockedDates: new Set(),
            bookedDates: new Set()
          }
          
          const request: BookingRequest = {
            institutionId,
            startDate: dateRange.startDate,
            endDate: dateRange.endDate,
            petId: 'pet-1'
          }
          
          const result = checkAvailability(calendar, request)
          
          // Should be available
          expect(result.isAvailable).toBe(true)
          expect(result.unavailableDates).toHaveLength(0)
          
          return true
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Partial overlap with blocked dates should reject the entire request
   * Validates that even one blocked date in a range causes rejection
   */
  it('partial overlap with blocked dates rejects entire request', () => {
    fc.assert(
      fc.property(
        fc.uuid(),
        fc.date({ min: new Date('2025-01-01'), max: new Date('2025-06-01') }),
        fc.integer({ min: 3, max: 10 }), // range length
        fc.integer({ min: 0, max: 9 }), // which day to block (0-indexed)
        (institutionId, startDate, rangeLength, dayToBlock) => {
          const adjustedDayToBlock = dayToBlock % rangeLength
          
          // Calculate the date to block
          const blockDate = new Date(startDate)
          blockDate.setDate(blockDate.getDate() + adjustedDayToBlock)
          const blockDateStr = formatDateStr(blockDate)
          
          // Calculate end date
          const endDate = new Date(startDate)
          endDate.setDate(endDate.getDate() + rangeLength - 1)
          
          const calendar: AvailabilityCalendar = {
            institutionId,
            blockedDates: new Set([blockDateStr]),
            bookedDates: new Set()
          }
          
          const request: BookingRequest = {
            institutionId,
            startDate: formatDateStr(startDate),
            endDate: formatDateStr(endDate),
            petId: 'pet-1'
          }
          
          const result = checkAvailability(calendar, request)
          
          // Should be rejected due to the blocked date
          expect(result.isAvailable).toBe(false)
          expect(result.unavailableDates).toContain(blockDateStr)
          
          return true
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Already booked dates should also block new bookings
   * Validates that booked dates are treated as unavailable
   */
  it('already booked dates block new bookings', () => {
    fc.assert(
      fc.property(
        fc.uuid(),
        dateArbitrary,
        (institutionId, bookedDate) => {
          const calendar: AvailabilityCalendar = {
            institutionId,
            blockedDates: new Set(),
            bookedDates: new Set([bookedDate])
          }
          
          const request: BookingRequest = {
            institutionId,
            startDate: bookedDate,
            endDate: bookedDate,
            petId: 'pet-2'
          }
          
          const result = checkAvailability(calendar, request)
          
          // Should be rejected due to existing booking
          expect(result.isAvailable).toBe(false)
          expect(result.unavailableDates).toContain(bookedDate)
          
          return true
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: Unavailable dates list is complete
   * Validates that all blocked dates in the range are reported
   */
  it('all blocked dates in range are reported', () => {
    fc.assert(
      fc.property(
        calendarArbitrary,
        dateRangeArbitrary,
        (calendar, dateRange) => {
          const request: BookingRequest = {
            institutionId: calendar.institutionId,
            startDate: dateRange.startDate,
            endDate: dateRange.endDate,
            petId: 'pet-1'
          }
          
          const result = checkAvailability(calendar, request)
          const requestedDates = getDateRange(dateRange.startDate, dateRange.endDate)
          
          // All blocked dates in the range should be in unavailableDates
          for (const date of requestedDates) {
            if (calendar.blockedDates.has(date) || calendar.bookedDates.has(date)) {
              expect(result.unavailableDates).toContain(date)
            }
          }
          
          // No dates outside the blocked/booked sets should be in unavailableDates
          for (const date of result.unavailableDates) {
            expect(
              calendar.blockedDates.has(date) || calendar.bookedDates.has(date)
            ).toBe(true)
          }
          
          return true
        }
      ),
      { numRuns: 100 }
    )
  })
})
