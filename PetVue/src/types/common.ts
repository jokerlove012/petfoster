export type PetType = 'dog' | 'cat' | 'other'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface PaginatedData<T> {
  list: T[]
  pagination: Pagination
}

export interface Pagination {
  page: number
  pageSize: number
  total: number
  totalPages: number
}

export interface DateRange {
  start: string
  end: string
}
