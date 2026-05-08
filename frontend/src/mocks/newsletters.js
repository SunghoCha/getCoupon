// 뉴스레터 목록 mock 데이터.
// 백엔드 GET /api/v1/newsletters 응답 모양을 그대로 따른다.
// 추후 백엔드 붙일 때 이 import를 axios 호출로 교체하면 된다.

// 카테고리 코드 ↔ 한국어 라벨 매핑 (mock 내부 정의용).
// 도메인 enum에 'ALL'은 두지 않는다 — 'ALL'은 프론트의 필터 표현 용도.
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

// 카테고리 리스트 응답 mock — 백엔드 GET /api/v1/newsletters/categories 응답 모양.
// 추후 axios.get('/api/v1/newsletters/categories') 호출로 교체.
export const newsletterCategoriesMock = {
  categories: Object.entries(categoryLabels).map(([code, label]) => ({
    code,
    label,
  })),
}

const palette = [
  '0f766e', '15803d', '2563eb', '1e40af', '0ea5e9',
  'ea580c', '171717', 'dc2626', '991b1b', '7c3aed',
  '0891b2', 'be185d', 'db2777', 'd97706', '4338ca',
]

const baseItems = [
  // 비즈·재테크
  ['머니레터', '하루 5분, 돈이 되는 경제 흐름을 짧게 정리해 드립니다.', 'BIZ', 'NONE'],
  ['어피티 모닝브리프', '20·30 직장인을 위한 친근한 경제 뉴스레터.', 'BIZ', 'ACTIVE'],
  // IT·테크
  ['Tech Weekly', '매주 화요일에 도착하는 개발자용 기술 뉴스 큐레이션.', 'TECH', 'NONE'],
  ['GeekNews 다이제스트', '지난 일주일간 GeekNews에서 인기 있었던 글을 모아 보내드려요.', 'TECH', 'NONE'],
  ['FE News', '프론트엔드 개발자를 위한 양질의 기술 소식 큐레이션.', 'TECH', 'UNSUBSCRIBED'],
  // 트렌드·라이프
  ['오렌지레터', '한 주의 트렌드와 라이프 인사이트를 부드럽게 풀어드려요.', 'TREND', 'NONE'],
  ['롱블랙', '하루 하나, 깊이 있는 사람과 브랜드 이야기.', 'TREND', 'NONE'],
  // 시사·사회
  ['뉴닉', '오늘의 시사 이슈를 친구처럼 풀어주는 뉴스레터.', 'SOCIETY', 'ACTIVE'],
  ['더 뉴 빈', '주요 뉴스의 맥락과 배경을 깊이 있게 정리합니다.', 'SOCIETY', 'NONE'],
  // 취미·자기개발
  ['월간 서른', '서른의 일상과 자기 성장을 위한 월간 에세이.', 'HOBBY', 'NONE'],
  // 지역·여행
  ['트래블 모먼트', '국내외 여행지에서 발견한 작은 순간들.', 'TRAVEL', 'NONE'],
  // 문화·예술
  ['디에디트', '취향이 만든 라이프스타일 콘텐츠.', 'CULTURE', 'NONE'],
  ['캐릿', 'Z세대 트렌드와 문화 이슈를 빠르게 캐치.', 'CULTURE', 'UNSUBSCRIBED'],
  // 리빙·인테리어
  ['오늘의집 매거진', '인테리어 인사이트와 라이프스타일 큐레이션.', 'LIVING', 'NONE'],
  // AI
  ['AI 위클리', '한 주의 AI 기술·산업 동향을 정리해 보내드립니다.', 'AI', 'NONE'],
]

// 무한 스크롤 검증용 추가 mock — 진짜 이름과 패턴이 다른 generic 항목들.
const additionalTemplates = {
  BIZ: ['금융 모먼트', '주식 다이제스트', '부동산 인사이트', '재테크 노트', '글로벌 마켓 위클리'],
  TECH: ['DevOps 위클리', '클라우드 다이제스트', '백엔드 노트', '데이터 위클리', '오픈소스 트렌드', '보안 인사이트'],
  TREND: ['주말 모먼트', '브랜드 위클리', '컬처 트렌드'],
  SOCIETY: ['시사 다이제스트', '오늘의 이슈', '주간 인사이트'],
  HOBBY: ['자기개발 노트', '러닝 라이프', '독서 모먼트'],
  TRAVEL: ['로컬 트래블', '국내 여행기'],
  CULTURE: ['아트 다이제스트', '주말 큐레이션', '책 한 권'],
  LIVING: ['홈 인사이트', '리빙 노트', '플랜트 라이프'],
  AI: ['GPT 트렌드', '머신러닝 위클리'],
  STARTUP: ['스타트업 다이제스트', '창업 인사이트', '벤처 위클리', '투자 라운드'],
}

const additionalItems = []
Object.entries(additionalTemplates).forEach(([category, names]) => {
  names.forEach((name) => {
    additionalItems.push([
      name,
      `${categoryLabels[category]} 분야의 다양한 소식과 인사이트를 한 주에 한 번 전합니다.`,
      category,
      'NONE',
    ])
  })
})

const allRaw = [...baseItems, ...additionalItems]

// 뉴스레터 목록 응답 모양은 백엔드 API DRAFT를 그대로 따른다.
// - items: 뉴스레터 항목 배열 (externalSubscribeUrl은 목록 응답에 포함하지 않는다)
// - page: 무한 스크롤·페이지네이션용 메타 (hasNext, nextPage)
const items = allRaw.map((row, i) => {
  const [name, description, category, status] = row
  const id = i + 1
  const color = palette[i % palette.length]
  const initials = encodeURIComponent(name.slice(0, 2))
  return {
    newsletterId: id,
    name,
    description,
    imageUrl: `https://placehold.co/80x80/${color}/ffffff?text=${initials}`,
    category: { code: category, label: categoryLabels[category] },
    memberNewsletterStatus: status,
  }
})

export const newslettersMock = {
  items,
  // mock은 페이지네이션 없이 한 번에 모두 응답한다고 가정.
  // 백엔드 붙일 때는 axios.get('/api/v1/newsletters', { params: { page, size } }) 응답으로 교체.
  page: {
    number: 0,
    size: items.length,
    hasNext: false,
    nextPage: null,
  },
}
