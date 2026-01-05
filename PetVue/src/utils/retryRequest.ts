/**
 * 网络请求重试工具
 */

export interface RetryOptions {
  maxRetries?: number
  retryDelay?: number
  shouldRetry?: (error: unknown) => boolean
  onRetry?: (attempt: number, error: unknown) => void
}

const defaultOptions: Required<RetryOptions> = {
  maxRetries: 3,
  retryDelay: 1000,
  shouldRetry: (error: unknown) => {
    // 默认只重试网络错误
    if (error instanceof Error) {
      return (
        error.message.includes('Network') ||
        error.message.includes('timeout') ||
        error.message.includes('ECONNREFUSED')
      )
    }
    return false
  },
  onRetry: () => {}
}

/**
 * 延迟函数
 */
function delay(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

/**
 * 带重试的异步函数执行器
 */
export async function withRetry<T>(
  fn: () => Promise<T>,
  options: RetryOptions = {}
): Promise<T> {
  const opts = { ...defaultOptions, ...options }
  let lastError: unknown

  for (let attempt = 1; attempt <= opts.maxRetries + 1; attempt++) {
    try {
      return await fn()
    } catch (error) {
      lastError = error

      if (attempt <= opts.maxRetries && opts.shouldRetry(error)) {
        opts.onRetry(attempt, error)
        await delay(opts.retryDelay * attempt) // 指数退避
      } else {
        throw error
      }
    }
  }

  throw lastError
}

/**
 * 创建带重试的 fetch 函数
 */
export function createRetryFetch(options: RetryOptions = {}) {
  return async function retryFetch(
    input: RequestInfo | URL,
    init?: RequestInit
  ): Promise<Response> {
    return withRetry(() => fetch(input, init), {
      ...options,
      shouldRetry: (error) => {
        // 网络错误或 5xx 错误重试
        if (error instanceof TypeError) {
          return true // 网络错误
        }
        if (error instanceof Response && error.status >= 500) {
          return true
        }
        return options.shouldRetry?.(error) ?? false
      }
    })
  }
}

/**
 * 网络状态监听
 */
export function useNetworkStatus() {
  const isOnline = typeof navigator !== 'undefined' ? navigator.onLine : true

  const listeners: Array<(online: boolean) => void> = []

  if (typeof window !== 'undefined') {
    window.addEventListener('online', () => {
      listeners.forEach((fn) => fn(true))
    })

    window.addEventListener('offline', () => {
      listeners.forEach((fn) => fn(false))
    })
  }

  return {
    isOnline,
    onStatusChange(callback: (online: boolean) => void) {
      listeners.push(callback)
      return () => {
        const index = listeners.indexOf(callback)
        if (index > -1) listeners.splice(index, 1)
      }
    }
  }
}
