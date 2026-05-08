import { createRouter, createWebHistory } from 'vue-router'

import HomePage from '@/views/HomePage.vue'
import LoginPage from '@/views/LoginPage.vue'
import SignupPage from '@/views/SignupPage.vue'
import NewslettersPage from '@/views/NewslettersPage.vue'
import InboxPage from '@/views/InboxPage.vue'
import TodayPage from '@/views/TodayPage.vue'
import NewsletterIssuesPage from '@/views/NewsletterIssuesPage.vue'
import IssueDetailPage from '@/views/IssueDetailPage.vue'
import NotFoundPage from '@/views/NotFoundPage.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomePage,
  },
  {
    path: '/login',
    name: 'login',
    component: LoginPage,
  },
  {
    path: '/signup',
    name: 'signup',
    component: SignupPage,
  },
  {
    path: '/newsletters',
    name: 'newsletters',
    component: NewslettersPage,
  },
  {
    path: '/inbox',
    name: 'inbox',
    component: InboxPage,
  },
  {
    path: '/today',
    name: 'today',
    component: TodayPage,
  },
  {
    // 한 뉴스레터의 이슈 목록 (보관함 카드 클릭으로 진입).
    path: '/newsletters/:newsletterId/issues',
    name: 'newsletter-issues',
    component: NewsletterIssuesPage,
    // route param이 string으로 들어오므로 props로 변환할 때 number로 캐스팅.
    props: (route) => ({ newsletterId: Number(route.params.newsletterId) }),
  },
  {
    // 이슈 단건 상세 (투데이·뉴스레터별 목록의 항목 클릭으로 진입).
    path: '/issues/:issueId',
    name: 'issue-detail',
    component: IssueDetailPage,
    props: (route) => ({ issueId: Number(route.params.issueId) }),
  },
  // 어느 라우트와도 매칭되지 않는 모든 경로는 404로 처리한다.
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: NotFoundPage,
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
