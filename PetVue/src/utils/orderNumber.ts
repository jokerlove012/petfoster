/**
 * 订单号生成工具
 * 生成唯一的订单号，格式: PF + 年月日 + 6位随机数
 * 例如: PF20241222123456
 */

// 用于追踪已生成的订单号，确保唯一性
const generatedOrderNumbers = new Set<string>()

/**
 * 生成随机数字字符串
 */
function generateRandomDigits(length: number): string {
  let result = ''
  for (let i = 0; i < length; i++) {
    result += Math.floor(Math.random() * 10).toString()
  }
  return result
}

/**
 * 格式化日期为 YYYYMMDD 格式
 */
function formatDateForOrderNumber(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}${month}${day}`
}

/**
 * 生成唯一订单号
 * @param prefix 订单号前缀，默认为 'PF'
 * @param date 日期，默认为当前日期
 * @returns 唯一订单号
 */
export function generateOrderNumber(prefix: string = 'PF', date: Date = new Date()): string {
  const dateStr = formatDateForOrderNumber(date)
  let orderNumber: string
  let attempts = 0
  const maxAttempts = 100
  
  // 尝试生成唯一订单号
  do {
    const randomPart = generateRandomDigits(6)
    orderNumber = `${prefix}${dateStr}${randomPart}`
    attempts++
    
    if (attempts >= maxAttempts) {
      // 如果多次尝试仍然重复，增加随机位数
      const extendedRandom = generateRandomDigits(8)
      orderNumber = `${prefix}${dateStr}${extendedRandom}`
      break
    }
  } while (generatedOrderNumbers.has(orderNumber))
  
  // 记录已生成的订单号
  generatedOrderNumbers.add(orderNumber)
  
  return orderNumber
}

/**
 * 批量生成唯一订单号
 * @param count 需要生成的数量
 * @param prefix 订单号前缀
 * @param date 日期
 * @returns 订单号数组
 */
export function generateOrderNumbers(
  count: number,
  prefix: string = 'PF',
  date: Date = new Date()
): string[] {
  const orderNumbers: string[] = []
  
  for (let i = 0; i < count; i++) {
    orderNumbers.push(generateOrderNumber(prefix, date))
  }
  
  return orderNumbers
}

/**
 * 验证订单号格式
 * @param orderNumber 订单号
 * @returns 是否有效
 */
export function isValidOrderNumber(orderNumber: string): boolean {
  // 格式: 前缀(2-4字符) + 日期(8位) + 随机数(6-8位)
  const pattern = /^[A-Z]{2,4}\d{14,16}$/
  return pattern.test(orderNumber)
}

/**
 * 解析订单号
 * @param orderNumber 订单号
 * @returns 解析结果
 */
export function parseOrderNumber(orderNumber: string): {
  prefix: string
  date: Date
  randomPart: string
} | null {
  if (!isValidOrderNumber(orderNumber)) {
    return null
  }
  
  // 提取前缀（字母部分）
  const prefixMatch = orderNumber.match(/^[A-Z]+/)
  if (!prefixMatch) return null
  
  const prefix = prefixMatch[0]
  const rest = orderNumber.slice(prefix.length)
  
  // 提取日期（8位数字）
  const dateStr = rest.slice(0, 8)
  const year = parseInt(dateStr.slice(0, 4))
  const month = parseInt(dateStr.slice(4, 6)) - 1
  const day = parseInt(dateStr.slice(6, 8))
  const date = new Date(year, month, day)
  
  // 提取随机部分
  const randomPart = rest.slice(8)
  
  return { prefix, date, randomPart }
}

/**
 * 清除已生成订单号记录（仅用于测试）
 */
export function clearGeneratedOrderNumbers(): void {
  generatedOrderNumbers.clear()
}

/**
 * 获取已生成订单号数量（仅用于测试）
 */
export function getGeneratedOrderNumbersCount(): number {
  return generatedOrderNumbers.size
}
