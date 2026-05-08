<script>
import { newslettersMock, newsletterCategoriesMock } from '@/mocks/newsletters'
import NewsletterDetailModal from '@/components/NewsletterDetailModal.vue'
import { useAuthStore } from '@/stores/auth'

const PAGE_SIZE = 6

export default {
  name: 'NewslettersPage',
  components: {
    NewsletterDetailModal,
  },
  data() {
    return {
      // 추후 axios.get('/api/v1/newsletters', { params: { category, query, page, size } })
      // 응답으로 교체될 자리. mock 데이터 구조는 백엔드 API DRAFT와 동일.
      // 백엔드는 비로그인 호출 시 각 항목의 memberNewsletterStatus를 null로 응답하지만,
      // mock 데이터는 모든 항목에 status가 채워져 있으므로 store 기반으로 변환한다 (allNewsletters 참고).
      rawNewsletters: newslettersMock.items,
      // 카테고리 칩에 그릴 항목.
      // 백엔드 응답에는 도메인 enum만 (ALL 없음). 프론트가 '전체' 칩을 표시용으로 추가.
      categories: [
        { code: 'ALL', label: '전체' },
        ...newsletterCategoriesMock.categories,
      ],
      selectedCategory: 'ALL',
      searchQuery: '',
      // 무한 스크롤 상태. 현재까지 로드한 페이지 수.
      // 백엔드 붙일 때는 이 page 값으로 API 호출 (page, size).
      page: 0,
      // 상세 모달 상태
      detailOpen: false,
      selectedNewsletter: null,
    }
  },
  computed: {
    authStore() {
      return useAuthStore()
    },
    // 비로그인이면 모든 항목의 memberNewsletterStatus를 null로 강제.
    // 백엔드 응답을 흉내내는 것 — 비로그인 사용자에겐 회원-뉴스레터 관계 자체가 없음.
    // (백엔드 API DRAFT §1.1: 뉴스레터 목록은 비로그인 허용, 비로그인이면 status = null)
    allNewsletters() {
      if (this.authStore.isLoggedIn) {
        return this.rawNewsletters
      }
      return this.rawNewsletters.map((n) => ({
        ...n,
        memberNewsletterStatus: null,
      }))
    },
    filteredNewsletters() {
      let result = this.allNewsletters
      if (this.selectedCategory !== 'ALL') {
        result = result.filter((n) => n.category.code === this.selectedCategory)
      }
      const q = this.searchQuery.trim().toLowerCase()
      if (q) {
        result = result.filter(
          (n) =>
            n.name.toLowerCase().includes(q) ||
            n.description.toLowerCase().includes(q),
        )
      }
      return result
    },
    displayedItems() {
      return this.filteredNewsletters.slice(0, (this.page + 1) * PAGE_SIZE)
    },
    isEmpty() {
      return this.filteredNewsletters.length === 0
    },
  },
  watch: {
    // 카테고리·검색어 변경 시 무한 스크롤 페이지를 처음으로 되돌린다.
    selectedCategory() {
      this.page = 0
    },
    searchQuery() {
      this.page = 0
    },
  },
  methods: {
    // v-infinite-scroll의 @load 콜백.
    // 더 이상 로드할 항목이 없으면 done('empty'), 있으면 다음 페이지 로드 후 done('ok').
    onLoad({ done }) {
      if (this.displayedItems.length >= this.filteredNewsletters.length) {
        done('empty')
        return
      }
      this.page += 1
      done('ok')
    },
    openDetail(newsletter) {
      this.selectedNewsletter = newsletter
      this.detailOpen = true
    },
  },
}
</script>

<template>
  <v-container class="py-8" max-width="900">
    <header class="mb-6">
      <h1 class="text-h5 font-weight-bold mb-2">트렌디한 뉴스레터</h1>
      <p class="text-body-2 text-medium-emphasis">
        letterPick에 등록된 뉴스레터를 카테고리·키워드로 둘러보세요.
      </p>
    </header>

    <!-- 카테고리 칩 + 검색바 -->
    <div class="filters mb-6">
      <v-chip-group
        v-model="selectedCategory"
        mandatory
        column
        selected-class="bg-grey-darken-4 text-white"
      >
        <v-chip
          v-for="category in categories"
          :key="category.code"
          :value="category.code"
          variant="outlined"
          size="small"
        >
          {{ category.label }}
        </v-chip>
      </v-chip-group>

      <v-text-field
        v-model="searchQuery"
        prepend-inner-icon="mdi-magnify"
        placeholder="뉴스레터 이름·설명 검색"
        variant="outlined"
        density="compact"
        hide-details
        clearable
        class="mt-4"
        @click:clear="searchQuery = ''"
      />
    </div>

    <!-- 빈 상태 -->
    <v-sheet
      v-if="isEmpty"
      class="pa-12 text-center text-medium-emphasis"
      color="transparent"
    >
      <v-icon size="48" class="mb-2">mdi-magnify-close</v-icon>
      <div>검색 결과가 없습니다.</div>
    </v-sheet>

    <!-- 무한 스크롤 + 2열 리스트 (박스 내부 스크롤) -->
    <v-infinite-scroll
      v-else
      mode="intersect"
      max-height="600"
      empty-text=""
      class="newsletters-scroll"
      @load="onLoad"
    >
      <v-row dense>
        <v-col
          v-for="newsletter in displayedItems"
          :key="newsletter.newsletterId"
          cols="12"
          sm="6"
        >
          <v-list-item
            class="newsletter-item rounded-lg pa-3"
            :title="newsletter.name"
            :subtitle="newsletter.description"
            @click="openDetail(newsletter)"
          >
            <template #prepend>
              <v-avatar size="48" rounded="md">
                <v-img :src="newsletter.imageUrl" :alt="newsletter.name" />
              </v-avatar>
            </template>
          </v-list-item>
        </v-col>
      </v-row>
    </v-infinite-scroll>

    <!-- 뉴스레터 상세 모달 -->
    <NewsletterDetailModal
      v-model="detailOpen"
      :newsletter="selectedNewsletter"
    />
  </v-container>
</template>

<style scoped>
/* 뉴스레터 목록 스크롤 영역 — 박스 내부 스크롤 (페이지 전체 스크롤 X) */
.newsletters-scroll {
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  padding: 12px;
  background: #fff;
}

.newsletter-item {
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: #fff;
  transition: background-color 0.15s;
  cursor: pointer;
}

.newsletter-item:hover {
  background: #fafafa;
}

/* 부제(설명)가 한 줄로만 보이도록 ellipsis */
.newsletter-item :deep(.v-list-item-subtitle) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
