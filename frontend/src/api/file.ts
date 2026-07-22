import request from './request'

export function getFileList(params: any) {
  return request.get('/files/list', { params })
}

export function uploadFile(data: FormData) {
  return request.post('/files/upload', data, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
