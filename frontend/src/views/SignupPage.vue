<script>
import * as authApi from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

// 백엔드 MemberSignupRequest 제약과 동일.
// @NotBlank, @Size(min=2,max=20), @Pattern(^[가-힣a-zA-Z0-9]+$)
const NICKNAME_PATTERN = /^[가-힣a-zA-Z0-9]+$/

export default {
  name: 'SignupPage',
  data() {
    return {
      nickname: '',
      submitting: false,
      errorMessage: '',
      nicknameMin: 2,
      nicknameMax: 20,
    }
  },
  computed: {
    isValid() {
      const v = this.nickname.trim()
      return (
        v.length >= this.nicknameMin
        && v.length <= this.nicknameMax
        && NICKNAME_PATTERN.test(v)
      )
    },
  },
  methods: {
    async submit() {
      if (!this.isValid || this.submitting) return
      this.submitting = true
      this.errorMessage = ''
      try {
        await authApi.signup(this.nickname.trim())
        // 가입 성공 시 백엔드가 SecurityContext를 ROLE_USER로 갱신함.
        // 클라이언트 store는 여전히 비로그인 상태이므로 fetchMe로 채운 뒤 이동한다.
        // (안 하면 헤더가 새로고침 전까지 비로그인으로 보임 — main.js fetchMe만 부팅 시 1회 돌므로)
        const authStore = useAuthStore()
        await authStore.fetchMe()
        this.$router.push('/')
      } catch (err) {
        // 401: 세션 없음. /login으로 보내기.
        if (err.response?.status === 401) {
          this.$router.push('/login')
          return
        }
        const message = err.response?.data?.message
        this.errorMessage = message ?? '가입 처리 중 오류가 발생했습니다.'
      } finally {
        this.submitting = false
      }
    },
  },
}
</script>

<template>
  <v-container class="signup-wrap" max-width="420">
    <div class="text-center mb-10">
      <h1 class="text-h4 font-weight-bold mb-2">letterPick에 오신 걸 환영해요</h1>
      <p class="text-body-2 text-medium-emphasis">
        앱에서 사용할 닉네임을 정해주세요.
      </p>
    </div>

    <v-card class="pa-6" variant="outlined">
      <v-form @submit.prevent="submit">
        <v-text-field
          v-model="nickname"
          label="닉네임"
          :counter="nicknameMax"
          :hint="`${nicknameMin}~${nicknameMax}자, 한글·영문·숫자만`"
          persistent-hint
          autofocus
          required
        />

        <v-alert
          v-if="errorMessage"
          type="error"
          variant="tonal"
          class="mt-4"
        >
          {{ errorMessage }}
        </v-alert>

        <v-btn
          type="submit"
          :disabled="!isValid || submitting"
          :loading="submitting"
          color="primary"
          size="large"
          block
          class="mt-6"
        >
          가입 완료
        </v-btn>
      </v-form>
    </v-card>
  </v-container>
</template>

<style scoped>
.signup-wrap {
  padding-top: 64px;
  padding-bottom: 64px;
}
</style>
