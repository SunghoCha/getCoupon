// 뉴스레터 도메인 API 호출.
//
// 카탈로그 (비로그인 OK):
//   - fetchNewsletters: GET /api/v1/newsletters?category=...&page=...&size=...
//   - fetchCategories:  GET /api/v1/newsletters/categories
//
// 회원-뉴스레터 구독 (로그인 필수, /api/v1/me/newsletter-subscriptions/{id}):
//   - fetchSubscriptionInfo: GET — { status, externalSubscribeUrl }
//       status=NONE이면 externalSubscribeUrl 채워짐, 그 외엔 null
//   - resubscribe:           PATCH — UNSUBSCRIBED → ACTIVE, 204
//   - 처음 구독은 별도 API 없음 — fetchSubscriptionInfo에서 받은 externalSubscribeUrl로 외부 이동
//   - 구독 해지(DELETE)는 다음 사이클 (티켓정제 03)
import apiClient, { ensureCsrfToken } from './client'

const SUBSCRIPTION_BASE = '/api/v1/me/newsletter-subscriptions'

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

export async function fetchSubscriptionInfo(newsletterId) {
  const { data } = await apiClient.get(`${SUBSCRIPTION_BASE}/${newsletterId}`)
  return data
}

export async function resubscribe(newsletterId) {
  await ensureCsrfToken()
  await apiClient.patch(`${SUBSCRIPTION_BASE}/${newsletterId}`)
}
