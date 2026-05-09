import axios from 'axios'

// 프로젝트 전역 axios 인스턴스.
// baseURL은 환경변수에서 받는다. 개발 환경에서는 비워서 Vite 프록시가 처리한다.
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  withCredentials: true,
  withXSRFToken: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
})

export async function ensureCsrfToken() {
  await apiClient.get('/api/v1/csrf')
}

// 요청 인터셉터 골격 (인증 헤더 등 후속 작업에서 추가)
apiClient.interceptors.request.use(
  (config) => config,
  (error) => Promise.reject(error),
)

// 응답 인터셉터 골격 (공통 에러 처리는 후속 작업에서 추가)
apiClient.interceptors.response.use(
  (response) => response,
  (error) => Promise.reject(error),
)

export default apiClient
