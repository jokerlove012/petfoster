/**
 * 报表导出工具
 * 支持导出 CSV 和 PDF 格式的报表
 */

export interface ReportData {
  title: string
  generatedAt: string
  dateRange: string
  sections: ReportSection[]
}

export interface ReportSection {
  name: string
  data: Record<string, string | number>[]
  columns: { key: string; label: string }[]
}

/**
 * 将数据导出为 CSV 格式
 */
export function exportToCSV(data: ReportSection[], filename: string): void {
  const csvContent: string[] = []
  
  data.forEach((section, index) => {
    // 添加分节标题
    if (index > 0) {
      csvContent.push('') // 空行分隔
    }
    csvContent.push(`# ${section.name}`)
    
    // 添加表头
    const headers = section.columns.map(col => col.label)
    csvContent.push(headers.join(','))
    
    // 添加数据行
    section.data.forEach(row => {
      const values = section.columns.map(col => {
        const value = row[col.key]
        // 处理包含逗号或引号的值
        if (typeof value === 'string' && (value.includes(',') || value.includes('"'))) {
          return `"${value.replace(/"/g, '""')}"`
        }
        return String(value ?? '')
      })
      csvContent.push(values.join(','))
    })
  })
  
  // 添加 BOM 以支持中文
  const BOM = '\uFEFF'
  const blob = new Blob([BOM + csvContent.join('\n')], { type: 'text/csv;charset=utf-8' })
  downloadBlob(blob, `${filename}.csv`)
}

/**
 * 将数据导出为 PDF 格式（简化版，生成 HTML 打印）
 */
export function exportToPDF(report: ReportData, _filename: string): void {
  const htmlContent = generatePDFHTML(report)
  
  // 创建一个新窗口用于打印
  const printWindow = window.open('', '_blank')
  if (!printWindow) {
    console.error('无法打开打印窗口，请检查浏览器弹窗设置')
    return
  }
  
  printWindow.document.write(htmlContent)
  printWindow.document.close()
  
  // 等待内容加载完成后打印
  printWindow.onload = () => {
    printWindow.print()
  }
}

/**
 * 生成 PDF 打印用的 HTML
 */
function generatePDFHTML(report: ReportData): string {
  const sectionsHTML = report.sections.map(section => {
    const headerRow = section.columns.map(col => `<th>${col.label}</th>`).join('')
    const dataRows = section.data.map(row => {
      const cells = section.columns.map(col => `<td>${row[col.key] ?? ''}</td>`).join('')
      return `<tr>${cells}</tr>`
    }).join('')
    
    return `
      <div class="section">
        <h2>${section.name}</h2>
        <table>
          <thead>
            <tr>${headerRow}</tr>
          </thead>
          <tbody>
            ${dataRows}
          </tbody>
        </table>
      </div>
    `
  }).join('')
  
  return `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <title>${report.title}</title>
      <style>
        * {
          margin: 0;
          padding: 0;
          box-sizing: border-box;
        }
        body {
          font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
          padding: 40px;
          color: #333;
        }
        .header {
          text-align: center;
          margin-bottom: 30px;
          padding-bottom: 20px;
          border-bottom: 2px solid #FF6B35;
        }
        .header h1 {
          font-size: 24px;
          color: #FF6B35;
          margin-bottom: 10px;
        }
        .header .meta {
          font-size: 12px;
          color: #666;
        }
        .section {
          margin-bottom: 30px;
        }
        .section h2 {
          font-size: 16px;
          color: #333;
          margin-bottom: 15px;
          padding-bottom: 8px;
          border-bottom: 1px solid #eee;
        }
        table {
          width: 100%;
          border-collapse: collapse;
          font-size: 12px;
        }
        th, td {
          padding: 10px 12px;
          text-align: left;
          border: 1px solid #ddd;
        }
        th {
          background: #f5f5f5;
          font-weight: 600;
        }
        tr:nth-child(even) {
          background: #fafafa;
        }
        .footer {
          margin-top: 40px;
          text-align: center;
          font-size: 11px;
          color: #999;
        }
        @media print {
          body {
            padding: 20px;
          }
          .section {
            page-break-inside: avoid;
          }
        }
      </style>
    </head>
    <body>
      <div class="header">
        <h1>${report.title}</h1>
        <div class="meta">
          <p>统计周期: ${report.dateRange}</p>
          <p>生成时间: ${report.generatedAt}</p>
        </div>
      </div>
      ${sectionsHTML}
      <div class="footer">
        <p>宠物寄养平台 - 数据报表</p>
      </div>
    </body>
    </html>
  `
}

/**
 * 下载 Blob 文件
 */
function downloadBlob(blob: Blob, filename: string): void {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

/**
 * 格式化日期范围显示
 */
export function formatDateRange(range: string): string {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  
  switch (range) {
    case 'week':
      return `${year}年第${getWeekNumber(now)}周`
    case 'month':
      return `${year}年${month}月`
    case 'quarter':
      return `${year}年第${Math.ceil(month / 3)}季度`
    case 'year':
      return `${year}年`
    default:
      return `${year}年${month}月`
  }
}

/**
 * 获取周数
 */
function getWeekNumber(date: Date): number {
  const firstDayOfYear = new Date(date.getFullYear(), 0, 1)
  const pastDaysOfYear = (date.getTime() - firstDayOfYear.getTime()) / 86400000
  return Math.ceil((pastDaysOfYear + firstDayOfYear.getDay() + 1) / 7)
}

/**
 * 格式化当前时间
 */
export function formatCurrentTime(): string {
  const now = new Date()
  return now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

/**
 * 创建平台数据报表
 */
export function createPlatformReport(
  dateRange: string,
  userStats: { total: number; newThisMonth: number; activeUsers: number; growth: number },
  orderStats: { total: number; thisMonth: number; revenue: number; avgOrderValue: number },
  institutionStats: { total: number; active: number; pending: number; suspended: number },
  trendData: { month: string; users: number; orders: number; revenue: number }[],
  topInstitutions: { name: string; orders: number; revenue: number; rating: number }[]
): ReportData {
  return {
    title: '宠物寄养平台数据报表',
    generatedAt: formatCurrentTime(),
    dateRange: formatDateRange(dateRange),
    sections: [
      {
        name: '用户统计',
        columns: [
          { key: 'metric', label: '指标' },
          { key: 'value', label: '数值' }
        ],
        data: [
          { metric: '总用户数', value: userStats.total },
          { metric: '本月新增', value: userStats.newThisMonth },
          { metric: '活跃用户', value: userStats.activeUsers },
          { metric: '增长率', value: `${userStats.growth}%` }
        ]
      },
      {
        name: '订单统计',
        columns: [
          { key: 'metric', label: '指标' },
          { key: 'value', label: '数值' }
        ],
        data: [
          { metric: '总订单数', value: orderStats.total },
          { metric: '本月订单', value: orderStats.thisMonth },
          { metric: '本月收入', value: `¥${orderStats.revenue.toLocaleString()}` },
          { metric: '平均订单金额', value: `¥${orderStats.avgOrderValue}` }
        ]
      },
      {
        name: '机构统计',
        columns: [
          { key: 'metric', label: '指标' },
          { key: 'value', label: '数值' }
        ],
        data: [
          { metric: '总机构数', value: institutionStats.total },
          { metric: '活跃机构', value: institutionStats.active },
          { metric: '待审核', value: institutionStats.pending },
          { metric: '已暂停', value: institutionStats.suspended }
        ]
      },
      {
        name: '月度趋势',
        columns: [
          { key: 'month', label: '月份' },
          { key: 'users', label: '用户数' },
          { key: 'orders', label: '订单数' },
          { key: 'revenue', label: '收入' }
        ],
        data: trendData.map(item => ({
          month: item.month,
          users: item.users,
          orders: item.orders,
          revenue: `¥${item.revenue.toLocaleString()}`
        }))
      },
      {
        name: '机构排行榜',
        columns: [
          { key: 'rank', label: '排名' },
          { key: 'name', label: '机构名称' },
          { key: 'orders', label: '订单数' },
          { key: 'revenue', label: '收入' },
          { key: 'rating', label: '评分' }
        ],
        data: topInstitutions.map((inst, index) => ({
          rank: index + 1,
          name: inst.name,
          orders: inst.orders,
          revenue: `¥${inst.revenue.toLocaleString()}`,
          rating: inst.rating
        }))
      }
    ]
  }
}
