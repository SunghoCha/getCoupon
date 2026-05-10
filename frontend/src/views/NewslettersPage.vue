<script>
import * as newsletterApi from '@/api/newsletter'
import NewsletterDetailModal from '@/components/NewsletterDetailModal.vue'
import { useToastStore } from '@/stores/toast'

const PAGE_SIZE = 20

export default {
  name: 'NewslettersPage',
  components: {
    NewsletterDetailModal,
  },
  data() {
    return {
      // 백엔드 GET /api/v1/newsletters 응답 누적.
      // memberNewsletterStatus는 비로그인이면 백엔드가 null로 응답하므로 클라 사이드 변환 불필요.
      items: [],
      // 다음에 호출할 페이지 번호. 0부터 시작.
      nextPage: 0,
      hasNext: false,
      loading: false,

      // 카테고리 칩에 그릴 항목. 'ALL'은 백엔드 응답 없는 프론트 표시용.
      categories: [{ code: 'ALL', label: '전체' }],
      selectedCategory: 'ALL',

      // 키워드 검색은 백엔드 미지원이라 현재 로드된 items 내 클라 사이드 필터.
      // 백엔드에 검색 endpoint 추가되면 query를 axios params로 옮김.
      searchQuery: '',

      // 상세 모달
      detailOpen: false,
      selectedNewsletter: null,
    }
  },
  computed: {
    toastStore() {
      return useToastStore()
    },
    filteredItems() {
      const q = this.searchQuery.trim().toLowerCase()
      if (!q) return this.items
      return this.items.filter(
        (n) =>
          n.name.toLowerCase().includes(q)
          || n.description.toLowerCase().includes(q),
      )
    },
    isEmpty() {
      // 최소 한 번이라도 fetchPage가 끝난 뒤에만 빈 상태로 본다 (nextPage > 0).
      // mount 직후 items=[]·loading=false 시점엔 v-infinite-scroll을 표시해서
      // sentinel이 mount되어 첫 onLoad가 자동 트리거되도록 한다.
      return this.nextPage > 0 && !this.loading && this.filteredItems.length === 0
    },
  },
  watch: {
    // 카테고리 변경 시 reset.
    // fetchPage(0)을 직접 부르지 않고, items=[]·nextPage=0·hasNext=true로 두면
    // v-infinite-scroll의 sentinel이 빈 박스 viewport에 visible해져 onLoad 자동 트리거 → fetchPage(0).
    // 첫 페이지 호출 경로를 onLoad 한 곳으로 통일해서 자동 누적 로딩 회피.
    selectedCategory() {
      this.items = []
      this.nextPage = 0
      this.hasNext = true
    },
  },
  async created() {
    // 첫 페이지는 v-infinite-scroll의 onLoad가 mount 시 자동 트리거하므로 여기서 호출하지 않는다.
    // (created에서 직접 호출하면 mode="intersect" sentinel이 mount 직후 visible 상태에서 추가 트리거되어
    //  사용자 스크롤 없이 모든 페이지가 자동 누적 호출되는 현상이 발생함)
    await this.loadCategories()
  },
  methods: {
    async loadCategories() {
      try {
        const data = await newsletterApi.fetchCategories()
        this.categories = [
          { code: 'ALL', label: '전체' },
          ...data.categories,
        ]
      } catch {
        this.toastStore.error('카테고리를 불러오지 못했습니다.')
      }
    },
    async fetchPage(pageNum) {
      if (this.loading) return
      this.loading = true
      try {
        const data = await newsletterApi.fetchNewsletters({
          category: this.selectedCategory,
          page: pageNum,
          size: PAGE_SIZE,
        })
        if (pageNum === 0) {
          this.items = data.items
        } else {
          this.items.push(...data.items)
        }
        this.hasNext = data.page.hasNext
        this.nextPage = data.page.number + 1
      } catch {
        this.toastStore.error('뉴스레터 목록을 불러오지 못했습니다.')
      } finally {
        this.loading = false
      }
    },
    // v-infinite-scroll @load. 첫 진입(items=[])·카테고리 변경 후·사용자 스크롤 끝 도달 모두 이 경로로 들어옴.
    onLoad({ done }) {
      if (!this.hasNext && this.items.length > 0) {
        // 끝까지 다 받음. 다음 트리거 차단.
        done('empty')
        return
      }
      this.fetchPage(this.nextPage).then(() => {
        done(this.hasNext ? 'ok' : 'empty')
      })
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
        placeholder="뉴스레터 이름·설명 검색 (현재 로드된 항목 내)"
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

    <!--
      무한 스크롤 + 2열 리스트.
      :key="selectedCategory" — 카테고리 변경 시 v-infinite-scroll을 강제 재생성.
      이유: v-infinite-scroll은 한 번 done('empty')를 받으면 sentinel observer가 비활성되어
      items를 비우고 hasNext=true로 reset해도 다시 트리거되지 않는다. 새 인스턴스로 강제 mount.
    -->
    <v-infinite-scroll
      v-else
      :key="selectedCategory"
      mode="intersect"
      max-height="600"
      empty-text=""
      class="newsletters-scroll"
      @load="onLoad"
    >
      <v-row dense>
        <v-col
          v-for="newsletter in filteredItems"
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
                <v-img :src="newsletter.imageUrl" :alt="newsletter.name">
                  <template #error>
                    <div class="image-fallback">
                      {{ newsletter.name.slice(0, 2) }}
                    </div>
                  </template>
                </v-img>
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
  font-size: 14px;
}
</style>
