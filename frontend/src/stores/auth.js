// 회원 인증 상태 store.
// 추후 OAuth2 로그인 흐름이 붙으면 mockLogin/mockLogout 자리는
// 실제 세션 확인 호출(GET /api/v1/members/me 등)로 교체된다.
//
// 지금은 비로그인 분기(NewsletterDetailModal, NewslettersPage status 매핑)를
// 검증하기 위한 토글 용도.
import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    // 로그인한 회원 정보. 비로그인이면 null.
    // 실제 응답 모양은 추후 백엔드 회원 조회 API와 맞춘다.
    member: null,
  }),
  getters: {
    isLoggedIn: (state) => state.member !== null,
  },
  actions: {
    // 개발용 mock 로그인. 헤더 토글 버튼에서 호출.
    mockLogin() {
      this.member = {
        email: 'test@letterpick.dev',
        name: '테스트',
      }
    },
    mockLogout() {
      this.member = null
    },
  },
})
