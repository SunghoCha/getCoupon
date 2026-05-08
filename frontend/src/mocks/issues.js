// 이슈(메일) mock 데이터.
// 백엔드 GET /api/v1/newsletters/issues/today 응답 모양을 그대로 따른다.
// - items: { issueId, newsletterId, newsletterName, subject, sender, receivedAt, summary, read }
// 추후 axios.get('/api/v1/newsletters/issues/today') 응답으로 교체.

// 기준 시각은 "오늘 임의 시간". 실제 백엔드는 timezone 정책에 따라 결정 (DRAFT 미결).
// mock은 빌드 시점의 오늘 날짜를 사용해 "방금/N분 전" 같은 상대 시각이 자연스럽도록 한다.
function todayAt(hour, minute = 0) {
  const d = new Date()
  d.setHours(hour, minute, 0, 0)
  return d.toISOString()
}

// 보관함 mock과 newsletterId·name 일관성 유지 (회원이 구독 중인 뉴스레터에서 도착).
const todayItems = [
  {
    issueId: 90001,
    newsletterId: 1,
    newsletterName: '어피티 모닝브리프',
    subject: '오늘의 경제 — 환율 급등과 수출 영향',
    sender: 'morning@uppity.co.kr',
    receivedAt: todayAt(7, 30),
    summary: '원달러 환율이 1450원을 돌파했습니다. 수출 비중이 높은 기업들의 영향과 정부의 대응 방향을 정리합니다.',
    read: false,
  },
  {
    issueId: 90002,
    newsletterId: 2,
    newsletterName: '뉴닉',
    subject: '이번 주 시사 정리 — 국회 본회의 주요 안건',
    sender: 'hello@newneek.co',
    receivedAt: todayAt(8, 0),
    summary: '이번 주 국회 본회의에서 다뤄질 주요 안건 5가지를 핵심만 모아드립니다.',
    read: false,
  },
  {
    issueId: 90003,
    newsletterId: 3,
    newsletterName: 'Tech Weekly',
    subject: 'React 19 정식 출시, Server Components 마이그레이션 가이드',
    sender: 'weekly@tech.dev',
    receivedAt: todayAt(9, 15),
    summary: 'React 19가 정식 출시되며 Server Components가 안정화되었습니다. 기존 프로젝트 마이그레이션 시 고려할 점.',
    read: true,
  },
  {
    issueId: 90004,
    newsletterId: 4,
    newsletterName: '오렌지레터',
    subject: '주말 여행 — 봄 끝자락의 강원도',
    sender: 'editor@orangeletter.kr',
    receivedAt: todayAt(10, 45),
    summary: '봄이 떠나기 전 마지막 주말을 위한 강원도 소도시 여행 추천 코스 3가지.',
    read: false,
  },
  {
    issueId: 90005,
    newsletterId: 5,
    newsletterName: 'AI 위클리',
    subject: '이번 주 AI — 멀티모달 모델 경쟁 격화',
    sender: 'weekly@ai-newsletter.kr',
    receivedAt: todayAt(11, 30),
    summary: 'OpenAI, Anthropic, Google의 신규 멀티모달 모델 발표가 같은 주에 몰렸습니다. 각 모델의 차별점.',
    read: true,
  },
  {
    issueId: 90006,
    newsletterId: 1,
    newsletterName: '어피티 모닝브리프',
    subject: '오후 마켓 — 코스피 마감 동향',
    sender: 'morning@uppity.co.kr',
    receivedAt: todayAt(15, 30),
    summary: '오늘 코스피 마감 동향과 외국인 매도세의 배경을 짧게 정리합니다.',
    read: false,
  },
  {
    issueId: 90007,
    newsletterId: 6,
    newsletterName: '디에디트',
    subject: '취향이 만든 작은 가게 — 5월의 리스트',
    sender: 'edit@the-edit.co.kr',
    receivedAt: todayAt(16, 0),
    summary: '5월에 새롭게 발견한 작은 가게들. 카페·서점·소품샵을 모았습니다.',
    read: false,
  },
]

export const todayIssuesMock = {
  items: todayItems,
}
