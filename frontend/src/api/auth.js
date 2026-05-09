// 인증 도메인 API 호출.
// logout: 백엔드 세션 종료 + JSESSIONID 쿠키 삭제 응답.
//   - POST /api/v1/auth/logout (CSRF 토큰 필요)
//   - 비로그인 호출도 idempotent 204 (백엔드 permitAll)
// signup: 소셜 로그인 후 PENDING_SIGNUP 상태에서 닉네임 입력 마무리.
//   - POST /api/v1/auth/signup (CSRF 토큰 필요)
//   - ROLE_PENDING_SIGNUP 권한 필요. 성공 시 백엔드가 SecurityContext를 ROLE_USER로 갱신.
import apiClient, { ensureCsrfToken } from './client'

export async function logout() {
  await ensureCsrfToken()
  await apiClient.post('/api/v1/auth/logout')
}

export async function signup(nickname) {
  await ensureCsrfToken()
  await apiClient.post('/api/v1/auth/signup', { nickname })
}
