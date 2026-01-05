export interface ReviewRating {
  overall: number
  environment: number
  service: number
  hygiene: number
  communication: number
}

export interface ReviewReply {
  content: string
  repliedAt: string
}

export interface Review {
  id: string
  bookingId: string
  userId: string
  institutionId: string
  rating: ReviewRating
  content: string
  images: string[]
  reply?: ReviewReply
  isAnonymous: boolean
  createdAt: string
  updatedAt: string
}

export interface CreateReviewData {
  bookingId: string
  rating: ReviewRating
  content: string
  images?: string[]
  isAnonymous?: boolean
}

export interface ReviewListParams {
  institutionId?: string
  page?: number
  pageSize?: number
  sortBy?: 'newest' | 'highest' | 'lowest'
}
