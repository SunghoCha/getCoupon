<script>
import * as newsletterApi from '@/api/newsletter'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'

export default {
  name: 'NewsletterDetailModal',
  props: {
    // v-dialog의 v-model에 바인딩 — 모달 열림/닫힘 상태
    modelValue: {
      type: Boolean,
      required: true,
    },
    // 표시할 뉴스레터. 모달이 닫힌 상태에서는 null일 수 있다.
    newsletter: {
      type: Object,
      default: null,
    },
  },
  emits: ['update:modelValue'],
  data() {
    return {
      // 백엔드 GET /api/v1/me/newsletter-subscriptions/{id} 응답:
      //   { status: 'NONE'|'ACTIVE'|'UNSUBSCRIBED', externalSubscribeUrl: string|null }
      // 비로그인이면 호출 안 함 → null로 둠.
      // 모달 열림·뉴스레터 변경마다 갱신.
      subscriptionInfo: null,
      // 구독 정보 조회 중 여부. 액션 버튼 로딩 표시에 사용.
      loadingSubscription: false,
      // 재구독 PATCH 진행 중 여부. 중복 호출 차단.
      submitting: false,
    }
  },
  computed: {
    authStore() {
      return useAuthStore()
    },
    toastStore() {
      return useToastStore()
    },
    isOpen: {
      get() {
        return this.modelValue
      },
      set(value) {
        this.$emit('update:modelValue', value)
      },
    },
    categoryLabel() {
      return this.newsletter?.category?.label ?? ''
    },
    // 회원의 앱 내 관계 상태에 따른 액션 버튼 분기.
    // 비로그인이면 백엔드 호출 없이 '로그인 후 구독' 분기 (티켓정제 02 결정).
    // 로그인+조회 전이면 비활성 + 로딩, 조회 후 status별 분기.
    primaryAction() {
      if (!this.newsletter) return null
      if (!this.authStore.isLoggedIn) {
        return { label: '로그인 후 구독', color: 'default', variant: 'tonal', disabled: false, action: 'login' }
      }
      if (!this.subscriptionInfo) {
        return { label: '구독 상태 확인 중', color: 'default', variant: 'tonal', disabled: true, action: 'none' }
      }
      switch (this.subscriptionInfo.status) {
        case 'NONE':
          return { label: '구독 시작', color: 'primary', variant: 'flat', disabled: false, action: 'subscribe-external' }
        case 'UNSUBSCRIBED':
          return { label: '다시 구독', color: 'primary', variant: 'flat', disabled: false, action: 'resubscribe' }
        case 'ACTIVE':
          return { label: '구독 중', color: 'default', variant: 'tonal', disabled: true, action: 'none' }
        default:
          return { label: '구독 시작', color: 'primary', variant: 'flat', disabled: false, action: 'subscribe-external' }
      }
    },
  },
  watch: {
    // 모달이 열리거나 표시 뉴스레터가 바뀌면 구독 상태 다시 조회.
    // 닫힐 때는 다음 열림에서 stale 데이터를 잠깐이라도 안 보이게 즉시 reset.
    isOpen: {
      handler(open) {
        if (open) {
          this.loadSubscriptionInfo()
        } else {
          this.subscriptionInfo = null
        }
      },
      immediate: true,
    },
    'newsletter.newsletterId'() {
      if (this.isOpen) this.loadSubscriptionInfo()
    },
  },
  methods: {
    async loadSubscriptionInfo() {
      // 비로그인이면 호출 안 함 (백엔드 401 회피 + 의미 없음).
      // 로그인 사용자만 자신의 앱 내 구독 상태를 조회한다 — 티켓정제 02 L240.
      if (!this.authStore.isLoggedIn || !this.newsletter) {
        this.subscriptionInfo = null
        return
      }
      this.loadingSubscription = true
      try {
        this.subscriptionInfo = await newsletterApi.fetchSubscriptionInfo(this.newsletter.newsletterId)
      } catch {
        // 401은 인터셉터가 store clear. 그 외 에러는 토스트만.
        // 액션 영역은 '구독 상태 확인 중' 비활성으로 남음.
        this.subscriptionInfo = null
        this.toastStore.error('구독 상태를 불러오지 못했습니다.')
      } finally {
        this.loadingSubscription = false
      }
    },
    onPrimaryClick() {
      const action = this.primaryAction?.action
      if (action === 'login') {
        this.close()
        this.$router.push({ name: 'login' })
        return
      }
      if (action === 'subscribe-external') {
        this.openExternalSubscribePage()
        return
      }
      if (action === 'resubscribe') {
        this.resubscribe()
        return
      }
    },
    openExternalSubscribePage() {
      // status === 'NONE'에서만 호출됨 — 도메인 invariant상 externalSubscribeUrl 있음.
      const url = this.subscriptionInfo?.externalSubscribeUrl
      if (!url) {
        this.toastStore.error('외부 구독 URL이 응답에 없습니다.')
        return
      }
      // 회원 수신주소를 토스트로 안내 — 사용자가 외부 페이지에 직접 입력.
      // (자동 클립보드 복사는 권한·HTTPS 의존성 있음. 다음 사이클 검토)
      const inboxAddress = this.authStore.member?.newsletterInboxAddress
      if (inboxAddress) {
        this.toastStore.info(`수신주소: ${inboxAddress} — 외부 페이지에서 이 주소로 구독해주세요.`)
      }
      // noopener: 외부 페이지가 window.opener를 통해 우리 탭을 조작하지 못하게 차단.
      window.open(url, '_blank', 'noopener,noreferrer')
    },
    async resubscribe() {
      if (this.submitting) return
      this.submitting = true
      try {
        await newsletterApi.resubscribe(this.newsletter.newsletterId)
        // 낙관적 갱신 — 백엔드 PATCH가 멱등이고 응답 바디 없음(204).
        // 다시 GET 호출하지 않고 즉시 ACTIVE로 반영.
        this.subscriptionInfo = { status: 'ACTIVE', externalSubscribeUrl: null }
        this.toastStore.info('다시 구독했습니다.')
      } catch {
        this.toastStore.error('재구독에 실패했습니다.')
      } finally {
        this.submitting = false
      }
    },
    close() {
      this.isOpen = false
    },
  },
}
</script>

<template>
  <v-dialog v-model="isOpen" max-width="560">
    <v-card v-if="newsletter" rounded="lg">
      <!-- 상단: 닫기 버튼 -->
      <div class="d-flex justify-end pa-2">
        <v-btn icon="mdi-close" variant="text" size="small" @click="close" />
      </div>

      <!-- 헤더: 로고 + 이름 + 카테고리 -->
      <div class="d-flex align-center px-6 mb-4">
        <v-avatar size="56" rounded="md" class="mr-4">
          <v-img :src="newsletter.imageUrl" :alt="newsletter.name">
            <template #error>
              <div class="image-fallback">
                {{ newsletter.name.slice(0, 2) }}
              </div>
            </template>
          </v-img>
        </v-avatar>
        <div class="flex-grow-1">
          <h2 class="text-h6 font-weight-bold mb-0">{{ newsletter.name }}</h2>
          <div class="mt-1">
            <v-chip size="x-small" variant="tonal">
              {{ categoryLabel }}
            </v-chip>
          </div>
        </div>
      </div>

      <!-- 주요 액션: 구독 버튼 (status별 분기 — primaryAction computed 참고) -->
      <div class="px-6 mb-4">
        <v-btn
          v-if="primaryAction"
          :color="primaryAction.color"
          :variant="primaryAction.variant"
          :disabled="primaryAction.disabled || submitting"
          :loading="submitting"
          block
          size="large"
          @click="onPrimaryClick"
        >
          {{ primaryAction.label }}
        </v-btn>
      </div>

      <!-- 본문: 뉴스레터 소개 -->
      <v-card-text class="px-6 pb-4">
        <h3 class="text-subtitle-2 font-weight-bold mb-2">뉴스레터 소개</h3>
        <p class="text-body-2">{{ newsletter.description }}</p>
      </v-card-text>

      <!-- 구독 방법 안내 카드 -->
      <v-card-text class="px-6 pb-6">
        <v-card variant="flat" class="px-4 pt-3 pb-4 bg-grey-lighten-4" rounded="md">
          <h3 class="text-subtitle-2 font-weight-bold mb-6 text-grey-darken-4">구독 방법</h3>
          <ol class="steps">
            <li class="step">
              <span class="step-num">1</span>
              <div>
                <div class="font-weight-medium">구독 시작 버튼 클릭</div>
                <div class="text-caption text-medium-emphasis">
                  위의 "구독 시작" 버튼을 눌러주세요.
                </div>
              </div>
            </li>
            <li class="step">
              <span class="step-num">2</span>
              <div>
                <div class="font-weight-medium">외부 구독 페이지 이동</div>
                <div class="text-caption text-medium-emphasis">
                  발행처의 공식 구독 페이지가 새 창으로 열립니다.
                </div>
              </div>
            </li>
            <li class="step">
              <span class="step-num">3</span>
              <div>
                <div class="font-weight-medium">letterPick 수신 주소 입력</div>
                <div class="text-caption text-medium-emphasis">
                  발행처 폼에 letterPick이 발급한 수신 주소를 입력합니다.
                </div>
              </div>
            </li>
            <li class="step">
              <span class="step-num">4</span>
              <div>
                <div class="font-weight-medium">구독 완료</div>
                <div class="text-caption text-medium-emphasis">
                  뉴스레터가 도착하면 letterPick이 자동으로 인식해 보관함에 보여줍니다.
                </div>
              </div>
            </li>
          </ol>
        </v-card>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<style scoped>
.steps {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.step {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.step-num {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgb(var(--v-theme-primary));
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

/* 이미지 로드 실패 시 fallback (외부 호스트 hotlink·dead URL 대응) */
.image-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  color: #757575;
  font-weight: 600;
  font-size: 16px;
}
</style>
