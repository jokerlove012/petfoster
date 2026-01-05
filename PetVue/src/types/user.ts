export type UserRole = 'pet_owner' | 'institution_staff' | 'admin'

export interface User {
  id: string
  email: string
  phone?: string
  name: string
  avatar?: string
  role: UserRole
  createdAt: string
  updatedAt: string
}

export interface Pet {
  id: string
  name: string
  species: 'dog' | 'cat' | 'other'
  breed: string
  age: number
  weight: number
  gender: 'male' | 'female'
  avatar?: string
  healthNotes?: string
  vaccinated: boolean
  neutered: boolean
}

export interface Address {
  id: string
  label: string
  address: string
  latitude: number
  longitude: number
  isDefault: boolean
}

export interface PetOwner extends User {
  role: 'pet_owner'
  pets: Pet[]
  addresses: Address[]
  favoriteInstitutions: string[]
}

export interface InstitutionStaff extends User {
  role: 'institution_staff'
  institutionId: string
  position: 'manager' | 'caretaker' | 'receptionist'
  permissions: string[]
}

export interface Admin extends User {
  role: 'admin'
  adminLevel: 'super' | 'normal'
  permissions: string[]
}

export interface RegisterData {
  phone: string
  password: string
  role: UserRole
  name?: string
}

export interface AuthResult {
  user: User
  token: string
}
