export interface Notification {
  id: string
  userId: string
  type: NotificationType
  title: string
  content: string
  relatedId?: string
  isRead: boolean
  createdAt: Date
}

export type NotificationType =
  | 'booking_created'
  | 'booking_confirmed'
  | 'booking_cancelled'
  | 'health_update'
  | 'health_alert'
  | 'review_reminder'
  | 'message'
  | 'system'

export interface NotificationPreferences {
  email: NotificationChannelPrefs
  push: NotificationChannelPrefs
  inApp: NotificationChannelPrefs
}

export interface NotificationChannelPrefs {
  booking: boolean
  health: boolean
  message: boolean
  system: boolean
}
