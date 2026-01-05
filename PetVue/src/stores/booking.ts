import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Booking, BookingStatus } from '@/types/booking'

export const useBookingStore = defineStore('booking', () => {
  // State
  const bookings = ref<Booking[]>([])
  const currentBooking = ref<Booking | null>(null)
  const loading = ref(false)

  // Getters
  const pendingBookings = computed(() => 
    bookings.value.filter(b => b.status === 'pending')
  )

  const confirmedBookings = computed(() => 
    bookings.value.filter(b => b.status === 'confirmed')
  )

  const inProgressBookings = computed(() => 
    bookings.value.filter(b => b.status === 'in_progress')
  )

  const completedBookings = computed(() => 
    bookings.value.filter(b => b.status === 'completed')
  )

  const cancelledBookings = computed(() => 
    bookings.value.filter(b => b.status === 'cancelled')
  )

  const activeBookings = computed(() => 
    bookings.value.filter(b => ['pending', 'confirmed', 'in_progress'].includes(b.status))
  )

  const historyBookings = computed(() => 
    bookings.value.filter(b => ['completed', 'cancelled'].includes(b.status))
  )

  const bookingsByStatus = computed(() => (status: BookingStatus) => 
    bookings.value.filter(b => b.status === status)
  )

  // Actions
  function setBookings(list: Booking[]) {
    bookings.value = list
  }

  function addBooking(booking: Booking) {
    bookings.value.unshift(booking)
  }

  function updateBooking(id: string, updates: Partial<Booking>) {
    const index = bookings.value.findIndex(b => b.id === id)
    if (index > -1) {
      bookings.value[index] = { ...bookings.value[index], ...updates }
    }
    if (currentBooking.value?.id === id) {
      currentBooking.value = { ...currentBooking.value, ...updates }
    }
  }

  function setCurrentBooking(booking: Booking | null) {
    currentBooking.value = booking
  }

  function setLoading(value: boolean) {
    loading.value = value
  }

  function getBookingById(id: string): Booking | undefined {
    return bookings.value.find(b => b.id === id)
  }

  function clearBookings() {
    bookings.value = []
    currentBooking.value = null
  }

  return {
    // State
    bookings,
    currentBooking,
    loading,
    // Getters
    pendingBookings,
    confirmedBookings,
    inProgressBookings,
    completedBookings,
    cancelledBookings,
    activeBookings,
    historyBookings,
    bookingsByStatus,
    // Actions
    setBookings,
    addBooking,
    updateBooking,
    setCurrentBooking,
    setLoading,
    getBookingById,
    clearBookings
  }
})
