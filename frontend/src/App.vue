<script>
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'

export default {
  name: 'App',
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
    memberName() {
      return this.authStore.member?.name ?? ''
    },
    // v-model로 store 상태와 양방향 바인딩.
    // 사용자가 X 버튼·바깥 클릭으로 닫을 때 store의 show가 false로 갱신됨.
    toastShow: {
      get() {
        return this.toastStore.show
      },
      set(value) {
        this.toastStore.show = value
      },
    },
  },
  methods: {
    onMockToggle() {
      if (this.isLoggedIn) {
        this.authStore.mockLogout()
      } else {
        this.authStore.mockLogin()
      }
    },
  },
}
</script>

<template>
  <v-app>
    <v-app-bar color="white" elevation="1" density="compact">
      <v-app-bar-title>
        <router-link to="/" class="brand">letterPick</router-link>
      </v-app-bar-title>

      <template #append>
        <v-btn :to="{ name: 'home' }" variant="text">홈</v-btn>
        <v-btn :to="{ name: 'newsletters' }" variant="text">뉴스레터</v-btn>
        <v-btn :to="{ name: 'today' }" variant="text">투데이</v-btn>
        <v-btn :to="{ name: 'inbox' }" variant="text">보관함</v-btn>
        <v-btn v-if="!isLoggedIn" :to="{ name: 'login' }" variant="text">
          로그인
        </v-btn>
        <span v-else class="text-body-2 text-medium-emphasis ml-2 mr-1">
          {{ memberName }}
        </span>

        <!-- 개발용 mock 토글. 비로그인 분기 검증용. 추후 OAuth2 붙이면 제거. -->
        <v-btn
          variant="tonal"
          size="small"
          color="grey-darken-2"
          class="ml-2"
          @click="onMockToggle"
        >
          {{ isLoggedIn ? '로그아웃(mock)' : '로그인(mock)' }}
        </v-btn>
      </template>
    </v-app-bar>

    <v-main>
      <router-view />
    </v-main>

    <!-- 전역 토스트(스낵바). useToastStore().info/success/error()로 트리거. -->
    <v-snackbar
      v-model="toastShow"
      :color="toastStore.color"
      :timeout="toastStore.timeout"
      location="bottom"
    >
      {{ toastStore.text }}
      <template #actions>
        <v-btn variant="text" @click="toastStore.dismiss">닫기</v-btn>
      </template>
    </v-snackbar>
  </v-app>
</template>

<style scoped>
.brand {
  font-weight: 600;
  color: inherit;
  text-decoration: none;
}
</style>
