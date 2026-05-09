import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vuetify from 'vite-plugin-vuetify'
import { fileURLToPath, URL } from 'node:url'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    // Vuetify 컴포넌트를 사용하는 곳에서 자동으로 import한다.
    // 명시적 import 없이 <v-btn> 같은 컴포넌트를 바로 쓸 수 있고,
    // 사용하지 않은 컴포넌트는 번들에 포함되지 않는다 (트리쉐이킹).
    vuetify({ autoImport: true }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    // 백엔드 CORS·redirect 설정이 5173 가정.
    // 점유 시 fallback하지 않고 명시 에러로 죽도록 strictPort 사용.
    port: 5173,
    strictPort: true,
  },
})
