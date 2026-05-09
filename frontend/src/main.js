import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import vuetify from './plugins/vuetify'
import './style.css'
import { useAuthStore } from './stores/auth'

const app = createApp(App)
app.use(createPinia())

// 라우터 가드가 정확한 인증 상태를 보도록 부팅 시 fetchMe를 먼저 끝낸 뒤 mount 한다.
// 네트워크 에러 등으로 fetchMe가 실패해도 앱은 mount되어야 하므로 finally에서 mount.
const authStore = useAuthStore()
authStore.fetchMe().finally(() => {
  app.use(router)
  app.use(vuetify)
  app.mount('#app')
})
