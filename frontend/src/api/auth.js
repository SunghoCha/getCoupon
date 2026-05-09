// 인증 도메인 API 호출.
// logout: 백엔드 세션 종료 + JSESSIONID 쿠키 삭제 응답.
//   - POST /api/v1/auth/logout (CSRF 토큰 필요)
//   - 비로그인 호출도 idempotent 204 (백엔드 permitAll)
import apiClient, { ensureCsrfToken } from './client'

export async function logout() {
  await ensureCsrfToken()
  await apiClient.post('/api/v1/auth/logout')
}
