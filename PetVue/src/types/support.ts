export interface SupportTicket {
  id: string
  ticketNumber: string
  userId: string
  category: TicketCategory
  subject: string
  description: string
  attachments?: string[]
  status: TicketStatus
  priority: TicketPriority
  assignedTo?: string
  createdAt: Date
  updatedAt: Date
}

export type TicketCategory = 'booking' | 'payment' | 'service' | 'technical' | 'other'
export type TicketStatus = 'open' | 'in_progress' | 'resolved' | 'closed'
export type TicketPriority = 'low' | 'medium' | 'high' | 'urgent'

export interface Complaint {
  id: string
  bookingId: string
  petOwnerId: string
  institutionId: string
  category: ComplaintCategory
  description: string
  evidence: string[]
  status: ComplaintStatus
  institutionResponse?: string
  resolution?: ComplaintResolution
  createdAt: Date
}

export type ComplaintCategory = 'service_quality' | 'pet_safety' | 'communication' | 'facility' | 'other'
export type ComplaintStatus = 'pending' | 'awaiting_response' | 'under_review' | 'resolved'

export interface ComplaintResolution {
  decision: string
  remedies?: string
  resolvedBy: string
  resolvedAt: Date
}
