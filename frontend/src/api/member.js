// 회원 도메인 API 호출.
// fetchMe: 부팅 시 세션 상태 확인 + 마이페이지 진입 시 회원 정보 조회.
// changeNickname / withdraw: 마이페이지의 mutating 액션.
import apiClient, { ensureCsrfToken } from './client'

const ENDPOINT = '/api/v1/members/me'

export async function fetchMe() {
  const { data } = await apiClient.get(ENDPOINT)
  return data
}

export async function changeNickname(nickname) {
  await ensureCsrfToken()
  await apiClient.patch(ENDPOINT, { nickname })
}

export async function withdraw() {
  await ensureCsrfToken()
  await apiClient.delete(ENDPOINT)
}
