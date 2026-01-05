import { describe, it, expect } from 'vitest'
import * as fc from 'fast-check'

/**
 * Property 18: Form validation error display
 * Validates: Requirements 13.4
 */

// 验证函数
const validators = {
  email: (value: string): string | null => {
    if (!value) return '请输入邮箱地址'
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(value)) return '请输入有效的邮箱地址'
    return null
  },
  
  password: (value: string): string | null => {
    if (!value) return '请输入密码'
    if (value.length < 6) return '密码至少6个字符'
    if (value.length > 20) return '密码最多20个字符'
    return null
  },
  
  phone: (value: string): string | null => {
    if (!value) return '请输入手机号'
    const phoneRegex = /^1[3-9]\d{9}$/
    if (!phoneRegex.test(value)) return '请输入有效的手机号'
    return null
  },
  
  name: (value: string): string | null => {
    if (!value) return '请输入姓名'
    if (value.length < 2) return '姓名至少2个字符'
    if (value.length > 20) return '姓名最多20个字符'
    return null
  },
  
  confirmPassword: (password: string, confirmPassword: string): string | null => {
    if (!confirmPassword) return '请确认密码'
    if (password !== confirmPassword) return '两次输入的密码不一致'
    return null
  }
}

describe('Form Validation Properties', () => {
  it('Property 18.1: Empty fields always produce error messages', () => {
    fc.assert(
      fc.property(
        fc.constantFrom('email', 'password', 'phone', 'name'),
        (fieldType) => {
          const validator = validators[fieldType as keyof typeof validators]
          if (typeof validator === 'function') {
            const error = (validator as (v: string) => string | null)('')
            return error !== null && error.length > 0
          }
          return true
        }
      ),
      { numRuns: 10 }
    )
  })

  it('Property 18.2: Valid email passes validation', () => {
    fc.assert(
      fc.property(
        fc.emailAddress(),
        (email) => {
          const error = validators.email(email)
          return error === null
        }
      ),
      { numRuns: 30 }
    )
  })

  it('Property 18.3: Invalid email fails validation', () => {
    fc.assert(
      fc.property(
        fc.string().filter(s => !s.includes('@') || !s.includes('.')),
        (invalidEmail) => {
          if (!invalidEmail) return true // 空字符串有单独的错误
          const error = validators.email(invalidEmail)
          return error !== null
        }
      ),
      { numRuns: 30 }
    )
  })

  it('Property 18.4: Password length validation', () => {
    fc.assert(
      fc.property(
        fc.string({ minLength: 6, maxLength: 20 }),
        (password) => {
          const error = validators.password(password)
          return error === null
        }
      ),
      { numRuns: 30 }
    )
  })

  it('Property 18.5: Short password fails validation', () => {
    fc.assert(
      fc.property(
        fc.string({ minLength: 1, maxLength: 5 }),
        (shortPassword) => {
          const error = validators.password(shortPassword)
          return error !== null && error.includes('至少')
        }
      ),
      { numRuns: 20 }
    )
  })

  it('Property 18.6: Valid Chinese phone number passes validation', () => {
    fc.assert(
      fc.property(
        fc.constantFrom('3', '4', '5', '6', '7', '8', '9'), // 第二位只能是3-9
        fc.stringOf(fc.constantFrom('0', '1', '2', '3', '4', '5', '6', '7', '8', '9'), { minLength: 9, maxLength: 9 }),
        (secondDigit, rest) => {
          const phone = `1${secondDigit}${rest}`
          const error = validators.phone(phone)
          return error === null
        }
      ),
      { numRuns: 20 }
    )
  })

  it('Property 18.7: Invalid phone number fails validation', () => {
    fc.assert(
      fc.property(
        fc.oneof(
          fc.string({ minLength: 1, maxLength: 10 }), // 太短
          fc.string({ minLength: 12, maxLength: 15 }), // 太长
          fc.constant('2' + '1234567890'), // 不以1开头
          fc.constant('10123456789') // 第二位是0
        ),
        (invalidPhone) => {
          const error = validators.phone(invalidPhone)
          return error !== null
        }
      ),
      { numRuns: 20 }
    )
  })

  it('Property 18.8: Matching passwords pass confirmation validation', () => {
    fc.assert(
      fc.property(
        fc.string({ minLength: 1, maxLength: 50 }),
        (password) => {
          const error = validators.confirmPassword(password, password)
          return error === null
        }
      ),
      { numRuns: 30 }
    )
  })

  it('Property 18.9: Non-matching passwords fail confirmation validation', () => {
    fc.assert(
      fc.property(
        fc.string({ minLength: 1, maxLength: 50 }),
        fc.string({ minLength: 1, maxLength: 50 }),
        (password, confirmPassword) => {
          if (password === confirmPassword) return true // 跳过相同的情况
          const error = validators.confirmPassword(password, confirmPassword)
          return error !== null && error.includes('不一致')
        }
      ),
      { numRuns: 30 }
    )
  })

  it('Property 18.10: Name length validation', () => {
    fc.assert(
      fc.property(
        fc.string({ minLength: 2, maxLength: 20 }),
        (name) => {
          const error = validators.name(name)
          return error === null
        }
      ),
      { numRuns: 30 }
    )
  })
})
