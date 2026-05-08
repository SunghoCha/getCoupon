<script>
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
  computed: {
    toastStore() {
      return useToastStore()
    },
    // v-model을 부모와 양방향 바인딩하기 위한 computed.
    // 자식이 modelValue를 직접 변경할 수 없으므로 emit으로 부모에 알린다.
    isOpen: {
      get() {
        return this.modelValue
      },
      set(value) {
        this.$emit('update:modelValue', value)
      },
    },
    // 백엔드 응답의 category는 { code, label } 객체. 라벨을 그대로 표시한다.
    categoryLabel() {
      return this.newsletter?.category?.label ?? ''
    },
    // 회원의 앱 내 관계 상태에 따른 액션 버튼 분기.
    // 백엔드 응답의 memberNewsletterStatus를 그대로 사용.
    // null/undefined면 비로그인 사용자 — 로그인 페이지로 유도.
    primaryAction() {
      if (!this.newsletter) return null
      const status = this.newsletter.memberNewsletterStatus
      if (status === null || status === undefined) {
        return { label: '로그인 후 구독', color: 'default', variant: 'tonal', disabled: false, action: 'login' }
      }
      switch (status) {
        case 'NONE':
          return { label: '구독 시작', color: 'primary', variant: 'flat', disabled: false, action: 'subscribe' }
        case 'UNSUBSCRIBED':
          return { label: '다시 구독', color: 'primary', variant: 'flat', disabled: false, action: 'subscribe' }
        case 'ACTIVE':
          return { label: '구독 중', color: 'default', variant: 'tonal', disabled: true, action: 'none' }
        default:
          return { label: '구독 시작', color: 'primary', variant: 'flat', disabled: false, action: 'subscribe' }
      }
    },
  },
  methods: {
    onPrimaryClick() {
      const action = this.primaryAction?.action
      if (action === 'login') {
        // 비로그인 사용자 — 로그인 페이지로 이동.
        this.close()
        this.$router.push({ name: 'login' })
        return
      }
      if (action === 'subscribe') {
        // 추후 axios.post(`/api/v1/newsletters/${id}/subscription`)로 교체.
        // 응답 result에 따라 외부 구독 페이지 이동 + 수신 주소 클립보드 복사 + 토스트로 분기.
        this.toastStore.info('구독 요청은 백엔드 연동 후 활성화됩니다.')
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
          <v-img :src="newsletter.imageUrl" :alt="newsletter.name" />
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

      <!-- 주요 액션: 구독하기 버튼 -->
      <div class="px-6 mb-4">
        <v-btn
          :color="primaryAction.color"
          :variant="primaryAction.variant"
          :disabled="primaryAction.disabled"
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
</style>
