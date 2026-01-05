import api from './index'
import type { ApiResponse } from '@/types/common'

export interface UploadResult {
  url: string
  filename: string
  originalFilename: string
}

export const uploadApi = {
  // 上传单个文件
  upload(file: File): Promise<ApiResponse<UploadResult>> {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 上传多个文件
  uploadMultiple(files: File[]): Promise<ApiResponse<UploadResult[]>> {
    const formData = new FormData()
    files.forEach(file => formData.append('files', file))
    return api.post('/upload/multiple', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}
