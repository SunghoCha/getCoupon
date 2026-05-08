// Vuetify 인스턴스 설정.
// 테마, 기본 색상, 아이콘 등을 한곳에 모아 둔다.

import 'vuetify/styles' // Vuetify 기본 스타일
import '@mdi/font/css/materialdesignicons.css' // Material Design Icons
import { createVuetify } from 'vuetify'

const vuetify = createVuetify({
  theme: {
    defaultTheme: 'light',
    themes: {
      light: {
        colors: {
          // letterPick 브랜드 색상. 추후 디자인 결정 시 조정한다.
          primary: '#2563eb',
          secondary: '#64748b',
          background: '#ffffff',
          surface: '#ffffff',
        },
      },
    },
  },
  defaults: {
    // 컴포넌트별 기본 옵션. 일관성을 위해 자주 쓰는 옵션을 미리 고정한다.
    VBtn: {
      variant: 'flat',
      rounded: 'md',
    },
    VCard: {
      rounded: 'lg',
    },
  },
})

export default vuetify
