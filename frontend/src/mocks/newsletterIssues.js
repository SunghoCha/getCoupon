// 뉴스레터별 이슈 목록 mock 데이터.
// A 시나리오 — 한 뉴스레터의 시간순 이슈 흐름 (보관함 진입).
// 추후 백엔드 endpoint 확정 시 axios 호출로 교체.
//
// 응답 모양은 카탈로그 응답과 같은 패턴:
// {
//   items: [...],
//   page: { number, size, hasNext, nextPage }
// }
//
// page 메타는 백엔드 응답이 동일 형태일 것으로 가정 (카탈로그와 일관).
// 검색 endpoint(`/api/v1/newsletters/issues`)도 같은 page shape으로 통일하는 것이
// 자연스러우며, 추후 DRAFT 갱신 시 이 가정을 반영한다.
import { todayIssuesMock } from '@/mocks/issues'

// 보관함 mock과 newsletterId·name 일관성 유지.
const newsletters = [
  { id: 1, name: '어피티 모닝브리프', sender: 'morning@uppity.co.kr' },
  { id: 2, name: '뉴닉', sender: 'hello@newneek.co' },
  { id: 3, name: 'Tech Weekly', sender: 'weekly@tech.dev' },
  { id: 4, name: '오렌지레터', sender: 'editor@orangeletter.kr' },
  { id: 5, name: 'AI 위클리', sender: 'weekly@ai-newsletter.kr' },
  { id: 6, name: '디에디트', sender: 'edit@the-edit.co.kr' },
]

// 뉴스레터별 제목 템플릿 — 시간순 흐름이 보이도록 다양한 주제.
const subjectTemplates = {
  1: [
    '오늘의 경제 — {topic}', '오후 마켓 — {topic}', '주말 인사이트 — {topic}',
    '환율 동향 — {topic}', '코스피 마감 — {topic}',
  ],
  2: [
    '이번 주 시사 정리 — {topic}', '오늘의 이슈 — {topic}', '심층 해설 — {topic}',
    '해외 뉴스 — {topic}',
  ],
  3: [
    '{topic} 정식 출시 — 마이그레이션 가이드', '{topic} 베타 — 핵심 변경점', '주간 기술 — {topic}',
    'DevOps 팁 — {topic}', '오픈소스 동향 — {topic}',
  ],
  4: [
    '주말 여행 — {topic}', '계절 에세이 — {topic}', '큐레이션 — {topic}',
    '브랜드 이야기 — {topic}',
  ],
  5: [
    '이번 주 AI — {topic}', 'AI 페이퍼 리뷰 — {topic}', 'AI 산업 동향 — {topic}',
    '모델 비교 — {topic}',
  ],
  6: [
    '취향이 만든 {topic}', '5월의 리스트 — {topic}', '에디터 추천 — {topic}',
    '서울 가게 — {topic}',
  ],
}

const topicsByNewsletter = {
  1: ['환율 급등', '미국 금리 인상', '부동산 정책', '연말 전망', '국내 경기', '소비자 심리', '유가 변동', '내수 회복'],
  2: ['국회 본회의', '대통령 선거 동향', '교육 개혁안', '기후 정책', '복지 예산', '국제 분쟁', '청년 정책'],
  3: ['React 19', 'TypeScript 5.5', 'Next.js App Router', 'Docker 동향', 'Kubernetes 1.30', 'PostgreSQL 17', 'Bun 런타임'],
  4: ['강원도 봄', '제주 카페', '서울 골목', '5월의 일기', '독립서점', '봄 야경', '재즈 바'],
  5: ['멀티모달 모델', 'GPT 차세대', 'Claude 업데이트', 'Gemini 동향', '오픈소스 LLM', 'AI 규제', '추론 능력'],
  6: ['작은 가게', '독립 카페', '문구 큐레이션', '도시 산책', '책방 투어', '디자인 소품'],
}

// "오늘"부터 과거로 거슬러 올라가며 ISO 시각 생성.
function pastIsoDate(daysAgo, hour = 9) {
  const d = new Date()
  d.setDate(d.getDate() - daysAgo)
  d.setHours(hour, 0, 0, 0)
  return d.toISOString()
}

// 한 뉴스레터의 이슈 N개를 시간순(최신 → 과거)으로 생성.
function generateIssues(newsletter, count) {
  const subjects = subjectTemplates[newsletter.id] || ['주간 소식 — {topic}']
  const topics = topicsByNewsletter[newsletter.id] || ['일반 동향']
  const items = []
  for (let i = 0; i < count; i += 1) {
    const subjectTpl = subjects[i % subjects.length]
    const topic = topics[i % topics.length]
    items.push({
      issueId: newsletter.id * 1000 + i + 1,
      newsletterId: newsletter.id,
      newsletterName: newsletter.name,
      subject: subjectTpl.replace('{topic}', topic),
      sender: newsletter.sender,
      // 평균 1~2일 간격으로 발행한다고 가정.
      receivedAt: pastIsoDate(i + (i % 3 === 0 ? 1 : 0), 8 + (i % 12)),
      summary: `${topic}에 대한 핵심 정리와 배경 해설을 한 편의 글로 정리했습니다.`,
      // 최근 5개 이슈는 미열람으로 두면 검증이 자연스럽다.
      read: i >= 5,
    })
  }
  return items
}

const ISSUES_PER_NEWSLETTER = 35

const newsletterIssuesByIdInternal = {}
newsletters.forEach((n) => {
  newsletterIssuesByIdInternal[n.id] = generateIssues(n, ISSUES_PER_NEWSLETTER)
})

const PAGE_SIZE = 10

// 뉴스레터별 이슈 페이지를 시뮬레이션해서 응답 모양으로 반환.
// 추후 axios.get('/api/v1/newsletters/{id}/issues', { params: { page, size } }) 응답으로 교체.
export function fetchNewsletterIssuesPage(newsletterId, page = 0) {
  const all = newsletterIssuesByIdInternal[newsletterId] || []
  const start = page * PAGE_SIZE
  const slice = all.slice(start, start + PAGE_SIZE)
  const hasNext = start + PAGE_SIZE < all.length
  return {
    items: slice,
    page: {
      number: page,
      size: PAGE_SIZE,
      hasNext,
      nextPage: hasNext ? page + 1 : null,
    },
  }
}

// 이슈 상세 mock 본문 HTML을 합성한다. 추후 백엔드 응답의 contents 필드로 교체.
// 실제 뉴스레터는 메일 원문 HTML이지만 mock에선 단순한 단락 구조로 시뮬레이션.
function generateContentsHtml(issue) {
  return `
    <h2>${issue.subject}</h2>
    <p>${issue.summary}</p>
    <p>이 글은 letterPick mock 본문입니다. 실제 백엔드 연동 시 <code>contents</code> 필드에 sanitize된 HTML 문자열이 들어옵니다.</p>
    <p>발행처는 <strong>${issue.newsletterName}</strong>이며, 본문은 메일 원문에서 추출됩니다.</p>
    <ul>
      <li>주요 트렌드와 변화의 배경 정리</li>
      <li>관련 인물·기업 인터뷰 요약</li>
      <li>다음 주 주목할 포인트</li>
    </ul>
    <p>본문 더 읽기는 발행처 사이트에서도 가능합니다.</p>
  `.trim()
}

// 이슈 삭제 mock. 두 mock(투데이·뉴스레터별)에서 해당 issueId를 제거한다.
// 추후 axios.delete(`/api/v1/newsletters/issues/${issueId}`)로 교체.
// 백엔드 시점엔 응답이 204 또는 200, 클라이언트 자체 데이터 갱신은 store/refetch로.
export function deleteIssue(issueId) {
  // 투데이 mock에서 제거.
  const todayIdx = todayIssuesMock.items.findIndex((i) => i.issueId === issueId)
  if (todayIdx !== -1) {
    todayIssuesMock.items.splice(todayIdx, 1)
  }
  // 뉴스레터별 mock에서 제거.
  for (const list of Object.values(newsletterIssuesByIdInternal)) {
    const idx = list.findIndex((i) => i.issueId === issueId)
    if (idx !== -1) {
      list.splice(idx, 1)
    }
  }
}

// 이슈 단건 조회. mock 단계에선 라우트 진입 시 read=true로 직접 mutate해서
// 같은 SPA 세션 안의 목록 화면(투데이·뉴스레터별 목록)에 반영되도록 한다.
// 추후 axios.get('/api/v1/newsletters/issues/{issueId}') 응답으로 교체.
// 백엔드의 GET 호출은 응답 시점에 read=true 처리를 함께 수행한다.
//
// 두 mock(투데이·뉴스레터별)을 모두 검색한다. 두 데이터 출처는 서로 다른 issueId 범위를
// 사용해 충돌 안 함 (today: 90001+, newsletter: 1001+).
export function fetchIssueDetail(issueId) {
  // 투데이 mock 우선 검색.
  const todayHit = todayIssuesMock.items.find((i) => i.issueId === issueId)
  if (todayHit) {
    todayHit.read = true
    return {
      ...todayHit,
      contents: generateContentsHtml(todayHit),
    }
  }
  // 뉴스레터별 mock 검색.
  for (const list of Object.values(newsletterIssuesByIdInternal)) {
    const issue = list.find((i) => i.issueId === issueId)
    if (issue) {
      issue.read = true
      return {
        ...issue,
        contents: generateContentsHtml(issue),
      }
    }
  }
  return null
}
