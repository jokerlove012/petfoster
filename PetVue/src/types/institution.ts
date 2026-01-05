export type PetType = 'dog' | 'cat' | 'other'
export type InstitutionStatus = 'pending' | 'active' | 'suspended' | 'rejected'

export interface BusinessHours {
  open: string
  close: string
}

export interface WeeklyBusinessHours {
  monday: BusinessHours
  tuesday: BusinessHours
  wednesday: BusinessHours
  thursday: BusinessHours
  friday: BusinessHours
  saturday: BusinessHours
  sunday: BusinessHours
}

export interface License {
  type: string
  number: string
  expiryDate: string
}

export interface Institution {
  id: string
  name: string
  description: string
  logo?: string
  images: string[]
  address: string
  latitude: number
  longitude: number
  phone: string
  email: string
  businessHours: WeeklyBusinessHours
  petTypes: PetType[]
  capacity: Record<string, number>
  currentOccupancy: Record<string, number>
  rating: number
  reviewCount: number
  verified: boolean
  status: InstitutionStatus
  createdAt: string
  updatedAt: string
  features: string[]
  licenses: License[]
}

export interface ServicePackage {
  id: string
  institutionId: string
  name: string
  description: string
  petTypes: PetType[]
  pricePerDay: number
  features: string[]
  maxWeight?: number
  isActive: boolean
}

export interface SearchFilters {
  keyword?: string
  petType?: PetType
  minRating?: number
  maxPrice?: number
  sortBy?: 'distance' | 'rating' | 'price' | 'reviewCount'
  lat?: number
  lng?: number
}

export interface InstitutionWithDistance extends Institution {
  distance?: number
  minPrice?: number
}
