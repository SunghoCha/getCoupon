<script>
import { subscriptionsMock, unsubscribe } from '@/mocks/subscriptions'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'

export default {
  name: 'InboxPage',
  data() {
    return {
      // 추후 axios.get('/api/v1/newsletters/subscriptions') 응답으로 교체.
      subscriptions: subscriptionsMock.items,
      // 구독 해지 확인 다이얼로그.
      unsubscribeDialogOpen: false,
      // 다이얼로그가 어떤 카드의 액션인지 기억 (확인 시 newsletterId·name 사용).
      unsubscribeTarget: null,
    }
  },
  computed: {
    authStore() {
      return useAuthStore()
    },
    toastStore() {
      return useToastStore()
    },
    isLoggedIn() {
      return this.authStore.isLoggedIn
    },
    isEmpty() {
      return this.subscriptions.length === 0
    },
  },
  methods: {
    onItemClick(subscription) {
      this.$router.push({
        name: 'newsletter-issues',
        params: { newsletterId: subscription.newsletterId },
      })
    },
    onUnsubscribeClick(subscription) {
      this.unsubscribeTarget = subscription
      this.unsubscribeDialogOpen = true
    },
    confirmUnsubscribe() {
      // 추후 axios.delete(`/api/v1/newsletters/${this.unsubscribeTarget.newsletterId}/subscription`)로 교체.
      const target = this.unsubscribeTarget
      unsubscribe(target.newsletterId)
      this.unsubscribeDialogOpen = false
      this.unsubscribeTarget = null
      this.toastStore.success(`${target.name} 구독을 해지했어요.`)
    },
    // 보관함 항목의 마지막 발행일을 사용자 친화적인 표현으로 변환.
    // 추후 dayjs 같은 라이브러리 도입 가능 — 지금은 단순 차이 계산으로 충분.
    formatRelativeDate(isoDate) {
      const now = Date.now()
      const then = new Date(isoDate).getTime()
      const diffMs = now - then
      const diffMin = Math.floor(diffMs / 60000)
      const diffHour = Math.floor(diffMs / 3600000)
      const diffDay = Math.floor(diffMs / 86400000)
      if (diffMin < 1) return '방금 전'
      if (diffMin < 60) return `${diffMin}분 전`
      if (diffHour < 24) return `${diffHour}시간 전`
      if (diffDay < 7) return `${diffDay}일 전`
      // 7일 이상은 YYYY-MM-DD 형식.
      return new Date(isoDate).toISOString().slice(0, 10)
    },
    goToLogin() {
      this.$router.push({ name: 'login' })
    },
    goToNewsletters() {
      this.$router.push({ name: 'newsletters' })
    },
  },
}
</script>

<template>
  <v-container class="py-8" max-width="900">
    <header class="mb-6">
      <h1 class="text-h5 font-weight-bold mb-2">보관함</h1>
      <p class="text-body-2 text-medium-emphasis">
        letterPick에서 구독 중인 뉴스레터를 모아 봅니다.
      </p>
    </header>

    <!-- 비로그인: 로그인 안내 카드 -->
    <v-sheet
      v-if="!isLoggedIn"
      class="pa-12 text-center"
      color="transparent"
    >
      <v-icon size="48" class="mb-3 text-medium-emphasis">mdi-lock-outline</v-icon>
      <div class="text-body-1 font-weight-medium mb-2">로그인이 필요합니다</div>
      <div class="text-body-2 text-medium-emphasis mb-4">
        보관함은 회원 본인의 구독 정보라 로그인 후 확인할 수 있어요.
      </div>
      <v-btn color="primary" @click="goToLogin">로그인하기</v-btn>
    </v-sheet>

    <!-- 로그인 + 빈 보관함 -->
    <v-sheet
      v-else-if="isEmpty"
      class="pa-12 text-center"
      color="transparent"
    >
      <v-icon size="48" class="mb-3 text-medium-emphasis">mdi-inbox-outline</v-icon>
      <div class="text-body-1 font-weight-medium mb-2">아직 구독한 뉴스레터가 없어요</div>
      <div class="text-body-2 text-medium-emphasis mb-4">
        뉴스레터 목록에서 마음에 드는 뉴스레터를 찾아 구독을 시작해보세요.
      </div>
      <v-btn color="primary" @click="goToNewsletters">뉴스레터 둘러보기</v-btn>
    </v-sheet>

    <!-- 로그인 + 구독 목록 -->
    <v-row v-else dense>
      <v-col
        v-for="subscription in subscriptions"
        :key="subscription.newsletterId"
        cols="12"
        sm="6"
      >
        <v-card
          class="subscription-card pa-4"
          variant="outlined"
          rounded="lg"
          @click="onItemClick(subscription)"
        >
          <div class="d-flex align-start">
            <v-avatar size="48" rounded="md" class="mr-3">
              <v-img :src="subscription.imageUrl" :alt="subscription.name" />
            </v-avatar>
            <div class="flex-grow-1">
              <div class="d-flex align-center">
                <h3 class="text-subtitle-1 font-weight-bold mb-0 flex-grow-1">
                  {{ subscription.name }}
                </h3>
                <v-badge
                  v-if="subscription.unreadCount > 0"
                  :content="subscription.unreadCount"
                  color="error"
                  inline
                  class="mr-1"
                />
                <!-- "..." 메뉴. 카드의 click 이벤트와 분리되도록 .stop 사용. -->
                <v-menu>
                  <template #activator="{ props: menuProps }">
                    <v-btn
                      v-bind="menuProps"
                      icon="mdi-dots-vertical"
                      variant="text"
                      size="x-small"
                      @click.stop
                    />
                  </template>
                  <v-list density="compact">
                    <v-list-item
                      base-color="error"
                      @click.stop="onUnsubscribeClick(subscription)"
                    >
                      <template #prepend>
                        <v-icon size="small">mdi-close-circle-outline</v-icon>
                      </template>
                      <v-list-item-title>구독 해지</v-list-item-title>
                    </v-list-item>
                  </v-list>
                </v-menu>
              </div>
              <div class="mt-1">
                <v-chip size="x-small" variant="tonal">
                  {{ subscription.category.label }}
                </v-chip>
              </div>
              <div class="text-caption text-medium-emphasis mt-2">
                마지막 이슈 · {{ formatRelativeDate(subscription.latestIssueReceivedAt) }}
              </div>
            </div>
          </div>
        </v-card>
      </v-col>
    </v-row>

    <!-- 구독 해지 확인 다이얼로그 -->
    <v-dialog v-model="unsubscribeDialogOpen" max-width="420">
      <v-card rounded="lg">
        <v-card-title class="text-subtitle-1 font-weight-bold">
          구독을 해지할까요?
        </v-card-title>
        <v-card-text class="text-body-2">
          <strong>{{ unsubscribeTarget?.name }}</strong> 구독을 해지하면 보관함에서 제거되고,
          앞으로 도착하는 이슈는 letterPick에 저장되지 않아요.
          외부 사이트의 실제 구독은 별도로 해지해야 합니다.
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="unsubscribeDialogOpen = false">취소</v-btn>
          <v-btn color="error" variant="flat" @click="confirmUnsubscribe">구독 해지</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<style scoped>
.subscription-card {
  cursor: pointer;
  background: #fff;
  transition: background-color 0.15s, border-color 0.15s;
}

.subscription-card:hover {
  background: #fafafa;
  border-color: rgba(0, 0, 0, 0.2);
}
</style>
