import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

// 프로젝트 전역 axios 인스턴스.
// baseURL은 환경변수에서 받는다 (개발: http://localhost:8080, 운영: 운영 백엔드 도메인).
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  withCredentials: true,
  withXSRFToken: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
})

// CSRF 토큰을 백엔드로부터 받아 쿠키에 심는다.
// mutating 요청 직전에 호출해야 axios가 X-XSRF-TOKEN 헤더를 자동 첨부한다.
export async function ensureCsrfToken() {
  await apiClient.get('/api/v1/csrf')
}

// 응답 인터셉터: 401이면 인증 store를 비운다.
// 헤더 정합·라우터 가드가 즉시 비로그인 상태를 보도록 하기 위함.
// 페이지 redirect는 라우터 가드의 책임이라 여기선 store 정리만 한다.
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      authStore.clear()
    }
    return Promise.reject(error)
  },
)

export default apiClient
