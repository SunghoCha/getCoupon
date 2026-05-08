<script>
import { fetchNewsletterIssuesPage } from '@/mocks/newsletterIssues'
import { useAuthStore } from '@/stores/auth'

export default {
  name: 'NewsletterIssuesPage',
  props: {
    // route param. router에서 number로 캐스팅된 값.
    newsletterId: {
      type: Number,
      required: true,
    },
  },
  data() {
    return {
      // 로드된 이슈 누적.
      items: [],
      // 다음 호출에 보낼 page 값.
      nextPage: 0,
      hasNext: true,
      // 첫 응답에서 추출한 뉴스레터 이름 (응답에 포함되어 있음).
      // 보관함에서 router state로 미리 받을 수도 있지만 첫 응답이 진실 원천.
      newsletterName: '',
    }
  },
  computed: {
    authStore() {
      return useAuthStore()
    },
    isLoggedIn() {
      return this.authStore.isLoggedIn
    },
    // 첫 페이지를 받기 전 + 더 받을 게 없는 상태에서 items가 비었으면 빈 보관함.
    isEmpty() {
      return !this.hasNext && this.items.length === 0
    },
  },
  methods: {
    // v-infinite-scroll의 @load 콜백.
    // 추후 axios.get(`/api/v1/newsletters/${this.newsletterId}/issues`, { params: { page: this.nextPage, size: 10 } })
    // 응답으로 교체.
    onLoad({ done }) {
      if (!this.hasNext) {
        done('empty')
        return
      }
      const { items, page } = fetchNewsletterIssuesPage(this.newsletterId, this.nextPage)
      this.items.push(...items)
      this.nextPage = page.nextPage ?? this.nextPage
      this.hasNext = page.hasNext
      // 첫 응답에서 뉴스레터 이름 추출.
      if (!this.newsletterName && items.length > 0) {
        this.newsletterName = items[0].newsletterName
      }
      done(this.hasNext ? 'ok' : 'empty')
    },
    onItemClick(issue) {
      this.$router.push({
        name: 'issue-detail',
        params: { issueId: issue.issueId },
      })
    },
    formatDateTime(isoDate) {
      const d = new Date(isoDate)
      const mm = String(d.getMonth() + 1).padStart(2, '0')
      const dd = String(d.getDate()).padStart(2, '0')
      const hh = String(d.getHours()).padStart(2, '0')
      const mi = String(d.getMinutes()).padStart(2, '0')
      return `${mm}/${dd} ${hh}:${mi}`
    },
    goBack() {
      // 보관함에서 들어온 게 일반적이지만 직접 URL 진입도 가능 → 보관함 fallback.
      if (window.history.length > 1) {
        this.$router.back()
      } else {
        this.$router.push({ name: 'inbox' })
      }
    },
    goToLogin() {
      this.$router.push({ name: 'login' })
    },
  },
}
</script>

<template>
  <v-container class="py-8" max-width="900">
    <!-- 상단: 뒤로가기 + 뉴스레터 이름 -->
    <header class="mb-6 d-flex align-center">
      <v-btn
        icon="mdi-arrow-left"
        variant="text"
        size="small"
        class="mr-2"
        @click="goBack"
      />
      <div>
        <div class="text-caption text-medium-emphasis">뉴스레터 이슈</div>
        <h1 class="text-h6 font-weight-bold mb-0">
          {{ newsletterName || '...' }}
        </h1>
      </div>
    </header>

    <!-- 비로그인: 로그인 안내 -->
    <v-sheet
      v-if="!isLoggedIn"
      class="pa-12 text-center"
      color="transparent"
    >
      <v-icon size="48" class="mb-3 text-medium-emphasis">mdi-lock-outline</v-icon>
      <div class="text-body-1 font-weight-medium mb-2">로그인이 필요합니다</div>
      <div class="text-body-2 text-medium-emphasis mb-4">
        이슈 목록은 회원 본인의 자료라 로그인 후 확인할 수 있어요.
      </div>
      <v-btn color="primary" @click="goToLogin">로그인하기</v-btn>
    </v-sheet>

    <!-- 로그인 + 빈 상태 -->
    <v-sheet
      v-else-if="isEmpty"
      class="pa-12 text-center"
      color="transparent"
    >
      <v-icon size="48" class="mb-3 text-medium-emphasis">mdi-email-outline</v-icon>
      <div class="text-body-1 font-weight-medium mb-2">아직 도착한 이슈가 없어요</div>
      <div class="text-body-2 text-medium-emphasis">
        구독을 시작한 직후라면 다음 발송을 잠시 기다려주세요.
      </div>
    </v-sheet>

    <!-- 로그인 + 무한 스크롤 이슈 리스트 -->
    <v-infinite-scroll
      v-else
      mode="intersect"
      empty-text=""
      class="issue-scroll"
      @load="onLoad"
    >
      <v-list class="issue-list pa-0" lines="three">
        <v-list-item
          v-for="(issue, index) in items"
          :key="issue.issueId"
          :class="['issue-item', { 'issue-item--read': issue.read }]"
          :border="index < items.length - 1 ? 'b' : ''"
          @click="onItemClick(issue)"
        >
          <template #prepend>
            <div class="unread-dot-wrap">
              <span v-if="!issue.read" class="unread-dot" />
            </div>
          </template>

          <div class="d-flex align-center mb-1">
            <span class="text-caption text-medium-emphasis">
              {{ formatDateTime(issue.receivedAt) }}
            </span>
          </div>
          <div :class="['text-subtitle-2', issue.read ? 'font-weight-regular' : 'font-weight-bold']">
            {{ issue.subject }}
          </div>
          <div class="text-body-2 text-medium-emphasis issue-summary">
            {{ issue.summary }}
          </div>
        </v-list-item>
      </v-list>
    </v-infinite-scroll>
  </v-container>
</template>

<style scoped>
.issue-scroll {
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  overflow: hidden;
}

.issue-list {
  background: transparent;
}

.issue-item {
  cursor: pointer;
  transition: background-color 0.15s;
  padding: 12px 16px;
}

.issue-item:hover {
  background: #fafafa;
}

.issue-item--read :deep(.text-subtitle-2),
.issue-item--read .issue-summary {
  color: rgba(0, 0, 0, 0.55);
}

.unread-dot-wrap {
  width: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgb(var(--v-theme-primary));
}

.issue-summary {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 2px;
}
</style>
