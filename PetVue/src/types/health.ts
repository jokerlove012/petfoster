export interface HealthRecord {
  id: string
  bookingId: string
  date: Date
  feedingStatus: FeedingStatus
  activityLevel: ActivityLevel
  healthObservations: string
  mood: PetMood
  weight?: number
  temperature?: number
  medications?: string[]
  photos?: string[]
  videos?: string[]
  isAbnormal: boolean
  abnormalDetails?: string
  createdBy: string
  createdAt: Date
}

export type FeedingStatus = 'normal' | 'reduced' | 'increased' | 'refused'
export type ActivityLevel = 'high' | 'normal' | 'low' | 'inactive'
export type PetMood = 'happy' | 'calm' | 'anxious' | 'stressed'
