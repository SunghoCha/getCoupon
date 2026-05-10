// 뉴스레터 도메인 API 호출.
// fetchNewsletters: 카탈로그 페이지 단위 조회.
//   - GET /api/v1/newsletters?category=...&page=...&size=...
// fetchCategories: 카테고리 목록 조회.
//   - GET /api/v1/newsletters/categories
// 둘 다 백엔드 PUBLIC_GET이라 비로그인도 호출 가능.
import apiClient from './client'

export async function fetchNewsletters({ category, page = 0, size = 20 } = {}) {
  const params = { page, size }
  // 'ALL'은 프론트 표시용. 백엔드엔 보내지 않음.
  if (category && category !== 'ALL') {
    params.category = category
  }
  const { data } = await apiClient.get('/api/v1/newsletters', { params })
  return data
}

export async function fetchCategories() {
  const { data } = await apiClient.get('/api/v1/newsletters/categories')
  return data
}
