// 토스트(스낵바) 알림 store.
// 컴포넌트 어디서든 useToastStore().success/info/error()로 호출 가능.
// 실제 표시는 App.vue의 <v-snackbar>가 store 상태를 v-model로 바인딩하여 담당.
import { defineStore } from 'pinia'

const DEFAULT_TIMEOUT = 3000

export const useToastStore = defineStore('toast', {
  state: () => ({
    show: false,
    text: '',
    // Vuetify color: success / error / info / warning 등.
    color: 'info',
    timeout: DEFAULT_TIMEOUT,
  }),
  actions: {
    info(text, opts) {
      this.notify(text, 'info', opts)
    },
    success(text, opts) {
      this.notify(text, 'success', opts)
    },
    error(text, opts) {
      this.notify(text, 'error', opts)
    },
    // 공통 진입점. timeout은 ms 단위, 0이면 사용자가 닫을 때까지 유지.
    notify(text, color = 'info', { timeout = DEFAULT_TIMEOUT } = {}) {
      this.text = text
      this.color = color
      this.timeout = timeout
      // 이미 떠 있는 토스트가 있으면 새 내용으로 교체될 수 있도록
      // show를 false → true로 토글한다 (같은 true 유지 시 v-snackbar의 timeout이 갱신 안 됨).
      this.show = false
      setTimeout(() => {
        this.show = true
      }, 0)
    },
    dismiss() {
      this.show = false
    },
  },
})
