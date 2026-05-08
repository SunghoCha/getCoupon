// 보관함(내 뉴스레터) mock 데이터.
// 백엔드 GET /api/v1/newsletters/subscriptions 응답 모양을 그대로 따른다.
// - items: { newsletterId, name, imageUrl, category: { code, label }, latestIssueReceivedAt, unreadCount }
// 추후 axios.get('/api/v1/newsletters/subscriptions') 호출로 교체.

const palette = [
  '0f766e', '15803d', '2563eb', '1e40af', '0ea5e9',
  'ea580c', '171717', 'dc2626',
]

// 회원이 앱 내에서 구독 중인 뉴스레터들 — 카탈로그 mock의 ACTIVE 항목과 의미적으로 일치.
// 보관함은 ACTIVE만 보이므로 status 필드는 응답에 포함하지 않는다.
const baseItems = [
  ['어피티 모닝브리프', 'BIZ', 'IT·테크', '2026-04-30T08:00:00Z', 3],
  ['뉴닉', 'SOCIETY', '시사·사회', '2026-04-30T09:30:00Z', 5],
  ['Tech Weekly', 'TECH', 'IT·테크', '2026-04-29T18:00:00Z', 1],
  ['오렌지레터', 'TREND', '트렌드·라이프', '2026-04-28T12:00:00Z', 0],
  ['AI 위클리', 'AI', 'AI', '2026-04-27T07:00:00Z', 2],
  ['디에디트', 'CULTURE', '문화·예술', '2026-04-25T10:00:00Z', 0],
]

// 카테고리 코드 → 라벨 매핑 (API DRAFT 카테고리 enum에 맞춤).
const categoryLabels = {
  BIZ: '비즈·재테크',
  TECH: 'IT·테크',
  TREND: '트렌드·라이프',
  SOCIETY: '시사·사회',
  HOBBY: '취미·자기개발',
  TRAVEL: '지역·여행',
  CULTURE: '문화·예술',
  LIVING: '리빙·인테리어',
  AI: 'AI',
  STARTUP: '창업·스타트업',
}

const items = baseItems.map(([name, categoryCode, _, latestIssueReceivedAt, unreadCount], i) => {
  const id = i + 1
  const color = palette[i % palette.length]
  const initials = encodeURIComponent(name.slice(0, 2))
  return {
    newsletterId: id,
    name,
    imageUrl: `https://placehold.co/80x80/${color}/ffffff?text=${initials}`,
    category: { code: categoryCode, label: categoryLabels[categoryCode] },
    latestIssueReceivedAt,
    unreadCount,
  }
})

export const subscriptionsMock = {
  items,
}

// 앱 내 구독 해지 mock. subscriptionsMock에서 해당 newsletterId를 제거한다.
// 추후 axios.delete(`/api/v1/newsletters/${newsletterId}/subscription`)로 교체.
// 백엔드는 MemberNewsletter를 UNSUBSCRIBED 상태로 전이 — 보관함 응답에선 빠지지만
// 카탈로그 응답의 memberNewsletterStatus는 'UNSUBSCRIBED'로 남는다.
export function unsubscribe(newsletterId) {
  const idx = subscriptionsMock.items.findIndex((s) => s.newsletterId === newsletterId)
  if (idx !== -1) {
    subscriptionsMock.items.splice(idx, 1)
  }
}
