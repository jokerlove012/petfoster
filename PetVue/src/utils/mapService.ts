/**
 * 高德地图服务封装
 * Map Service - Amap (Gaode) API Integration
 * Requirements: 18.1
 */

// 高德地图 API Key (开发环境使用，生产环境应从环境变量获取)
const AMAP_KEY = import.meta.env.VITE_AMAP_KEY || 'demo_key'
const AMAP_VERSION = '2.0'

// 地图脚本加载状态
let mapScriptLoaded = false
let mapScriptLoading = false
let loadPromise: Promise<void> | null = null

/**
 * 坐标点类型
 */
export interface Coordinate {
  lng: number
  lat: number
}

/**
 * 地图标记点类型
 */
export interface MapMarker {
  id: string
  position: Coordinate
  title: string
  icon?: string
  data?: Record<string, unknown>
}

/**
 * 搜索结果类型
 */
export interface SearchResult {
  id: string
  name: string
  address: string
  location: Coordinate
  distance?: number
  tel?: string
}

/**
 * 地理编码结果
 */
export interface GeocodeResult {
  address: string
  location: Coordinate
  city: string
  district: string
}

/**
 * 加载高德地图脚本
 */
export function loadAmapScript(): Promise<void> {
  if (mapScriptLoaded) {
    return Promise.resolve()
  }

  if (mapScriptLoading && loadPromise) {
    return loadPromise
  }

  mapScriptLoading = true
  loadPromise = new Promise((resolve, reject) => {
    // 检查是否已存在脚本
    if (document.getElementById('amap-script')) {
      mapScriptLoaded = true
      mapScriptLoading = false
      resolve()
      return
    }

    const script = document.createElement('script')
    script.id = 'amap-script'
    // 添加 Geocoder 插件用于地址解析
    script.src = `https://webapi.amap.com/maps?v=${AMAP_VERSION}&key=${AMAP_KEY}&plugin=AMap.Geocoder`
    script.async = true

    script.onload = () => {
      mapScriptLoaded = true
      mapScriptLoading = false
      resolve()
    }

    script.onerror = () => {
      mapScriptLoading = false
      reject(new Error('Failed to load Amap script'))
    }

    document.head.appendChild(script)
  })

  return loadPromise
}

/**
 * 计算两点之间的距离（米）
 * 使用 Haversine 公式
 */
export function calculateDistance(point1: Coordinate, point2: Coordinate): number {
  const R = 6371000 // 地球半径（米）
  const dLat = toRad(point2.lat - point1.lat)
  const dLng = toRad(point2.lng - point1.lng)
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(point1.lat)) *
      Math.cos(toRad(point2.lat)) *
      Math.sin(dLng / 2) *
      Math.sin(dLng / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return Math.round(R * c)
}

function toRad(deg: number): number {
  return (deg * Math.PI) / 180
}

/**
 * 格式化距离显示
 */
export function formatDistance(meters: number): string {
  if (meters < 1000) {
    return `${meters}m`
  }
  return `${(meters / 1000).toFixed(1)}km`
}

/**
 * 获取当前位置
 */
export function getCurrentPosition(): Promise<Coordinate> {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('Geolocation is not supported'))
      return
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          lng: position.coords.longitude,
          lat: position.coords.latitude
        })
      },
      (error) => {
        reject(new Error(`Geolocation error: ${error.message}`))
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 60000
      }
    )
  })
}

/**
 * 打开导航应用
 * 支持高德地图、百度地图、腾讯地图
 */
export function openNavigation(
  destination: Coordinate,
  destinationName: string,
  origin?: Coordinate
): void {
  const { lng, lat } = destination
  const encodedName = encodeURIComponent(destinationName)

  // 检测设备类型
  const isIOS = /iPad|iPhone|iPod/.test(navigator.userAgent)
  const isAndroid = /Android/.test(navigator.userAgent)

  // 构建导航 URL
  let url: string

  if (isIOS || isAndroid) {
    // 移动端优先使用高德地图 App
    url = `https://uri.amap.com/navigation?to=${lng},${lat},${encodedName}&mode=car&coordinate=gaode`
  } else {
    // 桌面端使用高德地图网页版
    url = `https://www.amap.com/dir?to[name]=${encodedName}&to[lnglat]=${lng},${lat}`
    if (origin) {
      url += `&from[lnglat]=${origin.lng},${origin.lat}`
    }
  }

  window.open(url, '_blank')
}

/**
 * 地址解析（地址转坐标）
 * 使用高德地图 JS API 的 Geocoder 插件
 */
export async function geocode(address: string): Promise<GeocodeResult | null> {
  if (!address || address.trim() === '') {
    return null
  }

  try {
    // 确保地图脚本已加载
    await loadAmapScript()
    
    const AMapObj = (window as unknown as { AMap: Record<string, unknown> }).AMap

    // 检查 Geocoder 是否已加载
    if (!AMapObj.Geocoder) {
      console.warn('AMap.Geocoder not available')
      return null
    }

    const GeocoderClass = AMapObj.Geocoder as new (options?: { city?: string }) => {
      getLocation: (address: string, callback: (status: string, result: unknown) => void) => void
    }

    // 添加超时处理
    return new Promise((resolve) => {
      const timeout = setTimeout(() => {
        console.warn('Geocode timeout - callback not called')
        resolve(null)
      }, 10000)

      try {
        console.log('Creating Geocoder instance...')
        const geocoder = new GeocoderClass({ city: '广州' })
        console.log('Geocoder created, calling getLocation...')
        
        geocoder.getLocation(address, (status: string, result: unknown) => {
          clearTimeout(timeout)
          console.log('Geocode callback received, status:', status)
          console.log('Geocode result:', result)
          
          const res = result as {
            geocodes?: Array<{
              location?: { getLng?: () => number; getLat?: () => number; lng?: number; lat?: number }
              formattedAddress?: string
              city?: string
              district?: string
            }>
          }
          
          if (status === 'complete' && res.geocodes && res.geocodes.length > 0) {
            const geo = res.geocodes[0]
            // 兼容两种获取坐标的方式
            let lng: number, lat: number
            if (geo.location?.getLng) {
              lng = geo.location.getLng()
              lat = geo.location.getLat!()
            } else if (geo.location?.lng !== undefined) {
              lng = geo.location.lng
              lat = geo.location.lat!
            } else {
              console.warn('Cannot get coordinates from result')
              resolve(null)
              return
            }
            
            console.log('Geocode success:', { lng, lat })
            resolve({
              address: geo.formattedAddress || address,
              location: { lng, lat },
              city: geo.city || '',
              district: geo.district || ''
            })
          } else {
            console.warn('Geocode no result, status:', status)
            resolve(null)
          }
        })
        console.log('getLocation called, waiting for callback...')
      } catch (e) {
        clearTimeout(timeout)
        console.error('Geocoder error:', e)
        resolve(null)
      }
    })
  } catch (error) {
    console.error('Geocode error:', error)
    return null
  }
}

/**
 * 逆地址解析（坐标转地址）
 * 使用高德地图 Web 服务 API
 */
export async function reverseGeocode(location: Coordinate): Promise<string> {
  if (!location || isNaN(location.lng) || isNaN(location.lat)) {
    return ''
  }

  try {
    const url = `https://restapi.amap.com/v3/geocode/regeo?key=${AMAP_KEY}&location=${location.lng},${location.lat}`
    const response = await fetch(url)
    const data = await response.json()

    if (data.status === '1' && data.regeocode) {
      return data.regeocode.formatted_address || ''
    }
    
    return `位置 (${location.lng.toFixed(4)}, ${location.lat.toFixed(4)})`
  } catch (error) {
    console.error('Reverse geocode error:', error)
    return `位置 (${location.lng.toFixed(4)}, ${location.lat.toFixed(4)})`
  }
}

/**
 * 周边搜索
 * TODO: 对接高德地图 API
 */
export async function searchNearby(
  center: Coordinate,
  keyword: string,
  radius: number = 5000
): Promise<SearchResult[]> {
  console.log('Searching nearby:', { center, keyword, radius })

  // TODO: 调用高德地图 POI 搜索 API
  return []
}

/**
 * 地图服务类
 */
export class MapService {
  private map: unknown = null
  private markers: Map<string, unknown> = new Map()
  // infoWindow reserved for future use with marker popups

  /**
   * 初始化地图
   */
  async init(containerId: string, options?: { center?: Coordinate; zoom?: number }): Promise<void> {
    await loadAmapScript()

    const AMap = (window as unknown as { AMap: unknown }).AMap as {
      Map: new (
        container: string,
        options: { center: [number, number]; zoom: number; resizeEnable: boolean }
      ) => unknown
    }

    // 默认使用广州天河区坐标，确保坐标有效
    let center = { lng: 113.3245, lat: 23.1065 }
    if (options?.center && !isNaN(options.center.lng) && !isNaN(options.center.lat)) {
      center = options.center
    }
    const zoom = options?.zoom || 13

    this.map = new AMap.Map(containerId, {
      center: [center.lng, center.lat],
      zoom,
      resizeEnable: true
    })
  }

  /**
   * 添加标记点
   */
  addMarker(marker: MapMarker, onClick?: (marker: MapMarker) => void): void {
    if (!this.map) return
    // 验证坐标有效性
    if (!marker.position || isNaN(marker.position.lng) || isNaN(marker.position.lat)) return

    const AMap = (window as unknown as { AMap: unknown }).AMap as {
      Marker: new (options: {
        position: [number, number]
        title: string
        map: unknown
        icon?: unknown
        offset?: unknown
        content?: string
      }) => { on: (event: string, handler: () => void) => void }
      Icon: new (options: {
        size: unknown
        image: string
        imageSize: unknown
      }) => unknown
      Size: new (width: number, height: number) => unknown
      Pixel: new (x: number, y: number) => unknown
    }

    // 创建自定义标记内容 - 宠物爪印图标
    const markerContent = `
      <div style="
        width: 40px;
        height: 48px;
        position: relative;
        cursor: pointer;
      ">
        <svg viewBox="0 0 40 48" width="40" height="48">
          <path d="M20 0C12 0 5 7 5 15c0 10 15 33 15 33s15-23 15-33c0-8-7-15-15-15z" fill="#667eea"/>
          <circle cx="20" cy="15" r="10" fill="#fff"/>
          <g transform="translate(12, 7)" fill="#667eea">
            <ellipse cx="4" cy="10" rx="3" ry="4"/>
            <ellipse cx="12" cy="10" rx="3" ry="4"/>
            <circle cx="2" cy="4" r="2"/>
            <circle cx="6" cy="2" r="2"/>
            <circle cx="10" cy="2" r="2"/>
            <circle cx="14" cy="4" r="2"/>
          </g>
        </svg>
      </div>
    `

    const amapMarker = new AMap.Marker({
      position: [marker.position.lng, marker.position.lat],
      title: marker.title,
      map: this.map,
      content: markerContent,
      offset: new AMap.Pixel(-20, -48)
    })

    if (onClick) {
      amapMarker.on('click', () => onClick(marker))
    }

    this.markers.set(marker.id, amapMarker)
  }

  /**
   * 移除标记点
   */
  removeMarker(markerId: string): void {
    const marker = this.markers.get(markerId) as { setMap: (map: null) => void } | undefined
    if (marker) {
      marker.setMap(null)
      this.markers.delete(markerId)
    }
  }

  /**
   * 清除所有标记点
   */
  clearMarkers(): void {
    this.markers.forEach((marker) => {
      ;(marker as { setMap: (map: null) => void }).setMap(null)
    })
    this.markers.clear()
  }

  /**
   * 设置地图中心
   */
  setCenter(center: Coordinate, zoom?: number): void {
    if (!this.map) return
    // 验证坐标有效性
    if (!center || isNaN(center.lng) || isNaN(center.lat)) return
    const map = this.map as { setCenter: (center: [number, number]) => void; setZoom: (zoom: number) => void }
    map.setCenter([center.lng, center.lat])
    if (zoom) {
      map.setZoom(zoom)
    }
  }

  /**
   * 自适应显示所有标记点
   */
  fitView(): void {
    if (!this.map || this.markers.size === 0) return
    const map = this.map as { setFitView: (markers: unknown[]) => void }
    map.setFitView(Array.from(this.markers.values()))
  }

  /**
   * 放大地图
   */
  zoomIn(): void {
    if (!this.map) return
    const map = this.map as { zoomIn: () => void }
    map.zoomIn()
  }

  /**
   * 缩小地图
   */
  zoomOut(): void {
    if (!this.map) return
    const map = this.map as { zoomOut: () => void }
    map.zoomOut()
  }

  /**
   * 销毁地图
   */
  destroy(): void {
    if (this.map) {
      ;(this.map as { destroy: () => void }).destroy()
      this.map = null
    }
    this.markers.clear()
  }
}

// 导出单例
export const mapService = new MapService()

export default {
  loadAmapScript,
  calculateDistance,
  formatDistance,
  getCurrentPosition,
  openNavigation,
  geocode,
  reverseGeocode,
  searchNearby,
  MapService,
  mapService
}
