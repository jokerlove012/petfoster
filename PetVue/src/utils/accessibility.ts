/**
 * 可访问性工具函数
 */

/**
 * 焦点陷阱 - 将焦点限制在指定容器内
 */
export function createFocusTrap(container: HTMLElement) {
  const focusableSelectors = [
    'a[href]',
    'button:not([disabled])',
    'input:not([disabled])',
    'select:not([disabled])',
    'textarea:not([disabled])',
    '[tabindex]:not([tabindex="-1"])'
  ].join(', ')

  let firstFocusable: HTMLElement | null = null
  let lastFocusable: HTMLElement | null = null

  function updateFocusableElements() {
    const focusables = container.querySelectorAll<HTMLElement>(focusableSelectors)
    firstFocusable = focusables[0] || null
    lastFocusable = focusables[focusables.length - 1] || null
  }

  function handleKeyDown(e: KeyboardEvent) {
    if (e.key !== 'Tab') return

    updateFocusableElements()

    if (!firstFocusable || !lastFocusable) return

    if (e.shiftKey) {
      // Shift + Tab
      if (document.activeElement === firstFocusable) {
        e.preventDefault()
        lastFocusable.focus()
      }
    } else {
      // Tab
      if (document.activeElement === lastFocusable) {
        e.preventDefault()
        firstFocusable.focus()
      }
    }
  }

  function activate() {
    updateFocusableElements()
    container.addEventListener('keydown', handleKeyDown)
    firstFocusable?.focus()
  }

  function deactivate() {
    container.removeEventListener('keydown', handleKeyDown)
  }

  return { activate, deactivate }
}

/**
 * 屏幕阅读器公告
 */
export function announce(message: string, priority: 'polite' | 'assertive' = 'polite') {
  const announcer = document.createElement('div')
  announcer.setAttribute('role', 'status')
  announcer.setAttribute('aria-live', priority)
  announcer.setAttribute('aria-atomic', 'true')
  announcer.className = 'sr-only'
  announcer.textContent = message

  document.body.appendChild(announcer)

  setTimeout(() => {
    document.body.removeChild(announcer)
  }, 1000)
}

/**
 * 键盘导航支持
 */
export function handleArrowNavigation(
  e: KeyboardEvent,
  items: HTMLElement[],
  currentIndex: number,
  options: { wrap?: boolean; orientation?: 'horizontal' | 'vertical' } = {}
): number {
  const { wrap = true, orientation = 'vertical' } = options

  const prevKey = orientation === 'vertical' ? 'ArrowUp' : 'ArrowLeft'
  const nextKey = orientation === 'vertical' ? 'ArrowDown' : 'ArrowRight'

  let newIndex = currentIndex

  if (e.key === prevKey) {
    e.preventDefault()
    newIndex = currentIndex - 1
    if (newIndex < 0) {
      newIndex = wrap ? items.length - 1 : 0
    }
  } else if (e.key === nextKey) {
    e.preventDefault()
    newIndex = currentIndex + 1
    if (newIndex >= items.length) {
      newIndex = wrap ? 0 : items.length - 1
    }
  } else if (e.key === 'Home') {
    e.preventDefault()
    newIndex = 0
  } else if (e.key === 'End') {
    e.preventDefault()
    newIndex = items.length - 1
  }

  if (newIndex !== currentIndex && items[newIndex]) {
    items[newIndex].focus()
  }

  return newIndex
}

/**
 * 生成唯一 ID
 */
let idCounter = 0
export function generateId(prefix = 'id'): string {
  return `${prefix}-${++idCounter}`
}

/**
 * 检查是否使用键盘导航
 */
export function useKeyboardNavigation() {
  let isUsingKeyboard = false

  if (typeof window !== 'undefined') {
    window.addEventListener('keydown', (e) => {
      if (e.key === 'Tab') {
        isUsingKeyboard = true
        document.body.classList.add('using-keyboard')
      }
    })

    window.addEventListener('mousedown', () => {
      isUsingKeyboard = false
      document.body.classList.remove('using-keyboard')
    })
  }

  return {
    get isUsingKeyboard() {
      return isUsingKeyboard
    }
  }
}
