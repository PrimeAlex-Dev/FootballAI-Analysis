import { useEffect, useState } from 'react'
import {
  Activity,
  ArrowRight,
  CalendarDays,
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  CircleDot,
  Moon,
  Search,
  Sparkles,
  Sun,
  Trophy,
  X,
} from 'lucide-react'
import {
  Link,
  NavLink,
  Navigate,
  Outlet,
  useOutletContext,
  useParams,
} from 'react-router-dom'
import { todayKey } from './mock'
import {
  competitionSeason,
  getCompetitionSeasons,
  dateLabel,
  getHeadToHead,
  getMatchById,
  getMatchPrediction,
  getMatches,
  getMatchStatistics,
  getRecentForm,
  getStandings,
  score,
  statusLabel,
  team,
  timeLabel,
} from './service'
import type { FormResult, MatchFilters } from './service'
import type { Match, Prediction, Standing, Statistic, Status } from './types'
import './customer.css'

function useAsync<T>(load: () => Promise<T>, dependencies: unknown[]) {
  const key = JSON.stringify(dependencies)
  const [state, setState] = useState<{
    data: T | null
    loading: boolean
    error: boolean
    key: string
  }>({ data: null, loading: true, error: false, key })
  // Data adapter is asynchronous so the component boundary stays usable with a future API.
  useEffect(() => {
    let active = true
    load()
      .then((data) => {
        if (active) setState({ data, loading: false, error: false, key })
      })
      .catch(() => {
        if (active) setState({ data: null, loading: false, error: true, key })
      })
    return () => {
      active = false
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, dependencies)
  return state.key === key ? state : { data: null, loading: true, error: false }
}
function dayOffset(key: string, offset: number) {
  const [year, month, day] = key.split('-').map(Number)
  return new Date(Date.UTC(year, month - 1, day + offset))
    .toISOString()
    .slice(0, 10)
}
function TeamMark({
  id,
  size = 'normal',
}: {
  id: number
  size?: 'normal' | 'large'
}) {
  const item = team(id)
  return (
    <span className={`team-mark ${size}`} aria-label={item.name}>
      {item.logo ? (
        <img
          src={item.logo}
          alt=""
          onError={(event) => {
            event.currentTarget.style.display = 'none'
          }}
        />
      ) : (
        item.name
          .split(' ')
          .map((word) => word[0])
          .slice(0, 2)
          .join('')
      )}
    </span>
  )
}
function MatchLine({
  match,
  compact = false,
}: {
  match: Match
  compact?: boolean
}) {
  return (
    <Link
      className={`match-line ${compact ? 'compact' : ''}`}
      to={`/matches/${match.id}/ai`}
    >
      <span className="match-time">
        <strong>{timeLabel(match.kickoffTime)}</strong>
        <span className="match-date">{dateLabel(match.kickoffTime)}</span>
        <small>{match.round ?? '—'}</small>
      </span>
      <span className="match-team home">
        <span>{team(match.homeTeamId).name}</span>
        <TeamMark id={match.homeTeamId} />
      </span>
      <strong className="match-score">{score(match)}</strong>
      <span className="match-team away">
        <TeamMark id={match.awayTeamId} />
        <span>{team(match.awayTeamId).name}</span>
      </span>
      <span className={`status ${match.status.toLowerCase()}`}>
        {match.status === 'LIVE' && <i />}
        {statusLabel[match.status]}
      </span>
      <ChevronRight className="match-arrow" size={16} aria-hidden="true" />
    </Link>
  )
}
function Empty({ title, text }: { title: string; text: string }) {
  return (
    <div className="customer-empty">
      <CircleDot size={25} />
      <strong>{title}</strong>
      <p>{text}</p>
    </div>
  )
}
export function CustomerLayout() {
  const [dark, setDark] = useState(() => {
    try {
      return localStorage.getItem('football-customer-theme') !== 'light'
    } catch {
      return true
    }
  })
  const toggle = () => {
    setDark(!dark)
    try {
      localStorage.setItem('football-customer-theme', dark ? 'light' : 'dark')
    } catch {
      /* private browsing */
    }
  }
  return (
    <div className="customer" data-theme={dark ? 'dark' : 'light'}>
      <header className="customer-header">
        <div className="customer-header-inner">
          <Link className="customer-brand" to="/matches">
            <span className="brand-ball">
              <CircleDot size={24} />
            </span>
            <span>
              Football <b>AI</b>
            </span>
          </Link>
          <div className="customer-header-right">
            <span className="header-note">
              Dữ liệu bóng đá · Góc nhìn rõ hơn
            </span>
            <button
              className="icon-button"
              onClick={toggle}
              aria-label={dark ? 'Bật giao diện sáng' : 'Bật giao diện tối'}
            >
              {dark ? <Sun size={20} /> : <Moon size={20} />}
            </button>
          </div>
        </div>
      </header>
      <main className="customer-main">
        <Outlet />
      </main>
      <footer className="customer-footer">
        <span>Football AI</span>
        <span>
          Trận đấu từ hệ thống · Nội dung phân tích đang dùng dữ liệu mẫu
        </span>
      </footer>
    </div>
  )
}
export function MatchOverviewPage() {
  const [date, setDate] = useState(todayKey)
  const [searchInput, setSearchInput] = useState('')
  const [search, setSearch] = useState('')
  const [competitionId, setCompetitionId] = useState<number | null>(null)
  const [status, setStatus] = useState<Status | null>(null)
  const [shown, setShown] = useState(4)
  const [collapsed, setCollapsed] = useState<number[]>([])
  const catalog = useAsync(getCompetitionSeasons, [])
  useEffect(() => {
    const timeout = setTimeout(() => setSearch(searchInput), 280)
    return () => clearTimeout(timeout)
  }, [searchInput])
  const changeFilters = (change: () => void) => {
    change()
    setShown(4)
    setCollapsed([])
  }
  const filters: MatchFilters = {
    date,
    search,
    competitionSeasonId: competitionId,
    status,
  }
  const result = useAsync(
    () => getMatches(filters),
    [date, search, competitionId, status],
  )
  const visible = result.data?.slice(0, shown) ?? []
  const groups = Array.from(
    new Set(visible.map((match) => match.competitionSeasonId)),
  ).map((id) => ({
    id,
    items: visible.filter((match) => match.competitionSeasonId === id),
  }))
  const dates = [-2, -1, 0, 1, 2].map((offset) => dayOffset(date, offset))
  const title =
    date === todayKey
      ? 'Trận đấu hôm nay'
      : `Trận đấu ngày ${dateLabel(`${date}T12:00:00+07:00`)}`
  return (
    <div className="customer-overview">
      <section className="customer-hero">
        <div>
          <span className="eyebrow">
            <CircleDot size={14} /> MATCH CENTER
          </span>
          <h1>{title}</h1>
          <p>Khám phá lịch thi đấu, kết quả và phân tích bóng đá.</p>
        </div>
        <div className="hero-side">
          <span>{result.data?.length ?? 0}</span>
          <small>
            trận từ hệ thống
            <br />
            cho ngày đã chọn
          </small>
        </div>
      </section>
      <section className="customer-panel filters" aria-label="Bộ lọc trận đấu">
        <div className="search-wrap">
          <Search size={19} />
          <input
            value={searchInput}
            onChange={(event) =>
              changeFilters(() => setSearchInput(event.target.value))
            }
            placeholder="Tìm trận đấu, đội bóng..."
            aria-label="Tìm đội bóng"
          />
          {searchInput && (
            <button
              className="clear-search"
              onClick={() => changeFilters(() => setSearchInput(''))}
              aria-label="Xóa tìm kiếm"
            >
              <X size={18} />
            </button>
          )}
        </div>
        <div className="date-bar">
          <button
            className="square-button"
            onClick={() => changeFilters(() => setDate(dayOffset(date, -1)))}
            aria-label="Ngày trước"
          >
            <ChevronLeft size={18} />
          </button>
          <div className="date-items">
            {dates.map((key) => (
              <button
                key={key}
                className={`date-item ${key === date ? 'active' : ''}`}
                onClick={() => changeFilters(() => setDate(key))}
              >
                <span>
                  {key === todayKey
                    ? 'Hôm nay'
                    : new Intl.DateTimeFormat('vi-VN', {
                        weekday: 'short',
                        timeZone: 'Asia/Ho_Chi_Minh',
                      }).format(new Date(`${key}T12:00:00+07:00`))}
                </span>
                <strong>
                  {key.slice(8)}/{key.slice(5, 7)}
                </strong>
              </button>
            ))}
          </div>
          <button
            className="square-button"
            onClick={() => changeFilters(() => setDate(dayOffset(date, 1)))}
            aria-label="Ngày sau"
          >
            <ChevronRight size={18} />
          </button>
          <label className="date-picker">
            <CalendarDays size={18} />
            <input
              type="date"
              value={date}
              onChange={(event) =>
                event.target.value &&
                changeFilters(() => setDate(event.target.value))
              }
              aria-label="Chọn ngày"
            />
          </label>
          <button
            className="today-button"
            onClick={() => changeFilters(() => setDate(todayKey))}
          >
            Hôm nay
          </button>
        </div>
        <div className="filter-section">
          <span className="filter-label">GIẢI ĐẤU</span>
          <div className="chip-scroll">
            <button
              className={`filter-chip ${competitionId === null ? 'active' : ''}`}
              onClick={() => changeFilters(() => setCompetitionId(null))}
            >
              Tất cả giải đấu
            </button>
            {catalog.data?.map((link) => {
              return (
                <button
                  key={link.id}
                  className={`filter-chip ${competitionId === link.id ? 'active' : ''}`}
                  onClick={() => changeFilters(() => setCompetitionId(link.id))}
                >
                  {link.displayName}
                </button>
              )
            })}
          </div>
        </div>
        <div className="filter-section">
          <span className="filter-label">TRẠNG THÁI</span>
          <div className="chip-scroll">
            <button
              className={`filter-chip ${status === null ? 'active' : ''}`}
              onClick={() => changeFilters(() => setStatus(null))}
            >
              Tất cả
            </button>
            {(
              [
                'SCHEDULED',
                'LIVE',
                'FINISHED',
                'POSTPONED',
                'CANCELLED',
              ] as Status[]
            ).map((item) => (
              <button
                key={item}
                className={`filter-chip ${status === item ? 'active' : ''}`}
                onClick={() => changeFilters(() => setStatus(item))}
              >
                {statusLabel[item]}
              </button>
            ))}
          </div>
        </div>
        {(searchInput || competitionId || status || date !== todayKey) && (
          <button
            className="reset-filters"
            onClick={() => {
              changeFilters(() => {
                setSearchInput('')
                setCompetitionId(null)
                setStatus(null)
                setDate(todayKey)
              })
            }}
          >
            Xóa bộ lọc <X size={15} />
          </button>
        )}
      </section>
      <div className="section-heading">
        <div>
          <span className="eyebrow">LỊCH THI ĐẤU</span>
          <h2>Danh sách trận đấu</h2>
        </div>
        <span>{result.data?.length ?? 0} trận</span>
      </div>
      {result.loading ? (
        <div
          className="customer-panel skeleton"
          aria-label="Đang tải trận đấu"
        />
      ) : result.error ? (
        <Empty title="Không tải được trận đấu" text="Vui lòng thử lại sau." />
      ) : !visible.length ? (
        <Empty
          title="Không tìm thấy trận đấu"
          text="Thử ngày khác hoặc điều chỉnh bộ lọc."
        />
      ) : (
        groups.map(({ id, items }) => {
          const data = competitionSeason(id)
          const isCollapsed = collapsed.includes(id)
          return (
            <section className="customer-panel match-group" key={id}>
              <button
                className="group-heading"
                aria-expanded={!isCollapsed}
                onClick={() =>
                  setCollapsed(
                    isCollapsed
                      ? collapsed.filter((value) => value !== id)
                      : [...collapsed, id],
                  )
                }
              >
                <span className="competition-icon">
                  <Trophy size={19} />
                </span>
                <span>
                  <strong>{data.competition.name}</strong>
                  <small>{data.season.name}</small>
                </span>
                <em>{items.length} trận</em>
                <ChevronDown
                  className={isCollapsed ? 'rotated' : ''}
                  size={19}
                />
              </button>
              {!isCollapsed && (
                <div>
                  {items.map((match) => (
                    <MatchLine key={match.id} match={match} />
                  ))}
                </div>
              )}
            </section>
          )
        })
      )}
      {result.data && shown < result.data.length && (
        <button className="load-more" onClick={() => setShown(shown + 4)}>
          Xem thêm trận đấu <ArrowRight size={16} />
        </button>
      )}
    </div>
  )
}
export function MatchDetailLayout() {
  const { matchId } = useParams()
  const result = useAsync(() => getMatchById(Number(matchId)), [matchId])
  if (result.loading)
    return (
      <div className="customer-panel skeleton" aria-label="Đang tải chi tiết" />
    )
  if (result.error || !result.data)
    return (
      <Empty
        title="Không tìm thấy trận đấu"
        text="Trận đấu này chưa có dữ liệu."
      />
    )
  const match = result.data
  const cs = competitionSeason(match.competitionSeasonId)
  const tabs = [
    { to: 'ai', label: 'Nhận định AI', icon: Sparkles },
    { to: 'form', label: 'Phong độ & Đối đầu', icon: Activity },
    { to: 'statistics', label: 'Thống kê', icon: CircleDot },
    { to: 'standings', label: 'Bảng xếp hạng', icon: Trophy },
  ]
  return (
    <div className="detail-page">
      <div className="breadcrumb">
        <Link to="/matches">Trận đấu</Link>
        <ChevronRight size={15} />
        <span>
          {cs.competition.name} {cs.season.name}
        </span>
        <ChevronRight size={15} />
        <strong>
          {team(match.homeTeamId).name} vs {team(match.awayTeamId).name}
        </strong>
      </div>
      <section className="customer-panel detail-head">
        <div className="detail-meta">
          <span className="eyebrow">
            {cs.competition.name} · {cs.season.name}
          </span>
          <span>{match.round ?? 'Trận đấu'}</span>
        </div>
        <div className="detail-scoreline">
          <div className="detail-team">
            <TeamMark id={match.homeTeamId} size="large" />
            <strong>{team(match.homeTeamId).name}</strong>
            <small>ĐỘI NHÀ</small>
          </div>
          <div className="detail-center">
            <span className="kickoff">
              {dateLabel(match.kickoffTime)} · {timeLabel(match.kickoffTime)}{' '}
              (GMT+7)
            </span>
            <strong>{score(match)}</strong>
            <span className={`status ${match.status.toLowerCase()}`}>
              {match.status === 'LIVE' && <i />}
              {statusLabel[match.status]}
            </span>
          </div>
          <div className="detail-team">
            <TeamMark id={match.awayTeamId} size="large" />
            <strong>{team(match.awayTeamId).name}</strong>
            <small>ĐỘI KHÁCH</small>
          </div>
        </div>
      </section>
      <nav className="detail-tabs" aria-label="Nội dung trận đấu">
        {tabs.map(({ to, label, icon: Icon }) => (
          <NavLink key={to} to={to}>
            <Icon size={18} />
            {label}
          </NavLink>
        ))}
      </nav>
      <Outlet context={match} />
    </div>
  )
}
function useMatch(): Match {
  return useOutletContext<Match>()
}
export function AiPage() {
  const match = useMatch()
  const result = useAsync(() => getMatchPrediction(match.id), [match.id])
  if (result.loading) return <div className="customer-panel skeleton" />
  if (!result.data)
    return (
      <Empty
        title="Chưa có nhận định AI"
        text="Nhận định cho trận đấu này hiện chưa có dữ liệu."
      />
    )
  const prediction: Prediction = result.data
  const values = [
    { label: `${team(match.homeTeamId).name} thắng`, value: prediction.home },
    { label: 'Hòa', value: prediction.draw },
    { label: `${team(match.awayTeamId).name} thắng`, value: prediction.away },
  ]
  return (
    <div className="detail-grid ai-grid">
      <section className="customer-panel content-card">
        <span className="eyebrow">
          <Sparkles size={16} /> DỮ LIỆU MẪU
        </span>
        <h2>Nhận định AI</h2>
        <p className="muted">
          Xác suất minh họa, chưa dựa trên mô hình dự đoán thực tế.
        </p>
        <div className="probabilities">
          {values.map((item) => (
            <div
              className={`probability ${item.value === Math.max(...values.map((v) => v.value)) ? 'leading' : ''}`}
              key={item.label}
            >
              <span>{item.label}</span>
              <strong>{item.value}%</strong>
            </div>
          ))}
        </div>
        <div className="prob-bar" aria-label="Tỷ lệ dự đoán">
          <span style={{ width: `${prediction.home}%` }} />
          <span style={{ width: `${prediction.draw}%` }} />
          <span style={{ width: `${prediction.away}%` }} />
        </div>
        {prediction.confidence !== undefined && (
          <p className="confidence">
            Độ tin cậy minh họa <strong>{prediction.confidence}%</strong>
          </p>
        )}
      </section>
      <section className="customer-panel content-card">
        <span className="eyebrow">GÓC NHÌN TRẬN ĐẤU</span>
        <h2>Phân tích trận đấu</h2>
        <p className="analysis-text">{prediction.summary}</p>
        <h3>Yếu tố được nhắc đến</h3>
        <ul className="factor-list">
          {prediction.factors.map((factor) => (
            <li key={factor}>
              <span />
              {factor}
            </li>
          ))}
        </ul>
      </section>
    </div>
  )
}
function HistoryList({ items }: { items: FormResult[] }) {
  return (
    <div className="history-list">
      {items.map(({ match, outcome }) => (
        <Link to={`/matches/${match.id}/ai`} key={match.id}>
          <span className={`outcome ${outcome.toLowerCase()}`}>{outcome}</span>
          <span>{dateLabel(match.kickoffTime)}</span>
          <span>
            {team(match.homeTeamId).name} · {team(match.awayTeamId).name}
          </span>
          <strong>{score(match)}</strong>
        </Link>
      ))}
    </div>
  )
}
export function FormPage() {
  const match = useMatch()
  const home = useAsync(
    () => getRecentForm(match.homeTeamId, match.kickoffTime),
    [match.id],
  )
  const away = useAsync(
    () => getRecentForm(match.awayTeamId, match.kickoffTime),
    [match.id],
  )
  const h2h = useAsync(
    () => getHeadToHead(match.homeTeamId, match.awayTeamId, match.kickoffTime),
    [match.id],
  )
  const [pageState, setPageState] = useState({ matchId: match.id, shown: 5 })
  const shown = pageState.matchId === match.id ? pageState.shown : 5
  return (
    <>
      <div className="detail-grid">
        {[
          { id: match.homeTeamId, data: home.data },
          { id: match.awayTeamId, data: away.data },
        ].map(({ id, data }) => (
          <section className="customer-panel content-card" key={id}>
            <div className="card-title">
              <TeamMark id={id} />
              <div>
                <span className="eyebrow">PHONG ĐỘ GẦN ĐÂY</span>
                <h2>{team(id).name}</h2>
              </div>
            </div>
            {data?.length ? (
              <>
                <div className="form-pills">
                  {data.map(({ match: item, outcome }) => (
                    <span
                      className={`outcome ${outcome.toLowerCase()}`}
                      key={item.id}
                    >
                      {outcome}
                    </span>
                  ))}
                </div>
                <p className="muted">
                  {data.filter((item) => item.outcome === 'W').length} thắng ·{' '}
                  {data.filter((item) => item.outcome === 'D').length} hòa ·{' '}
                  {data.filter((item) => item.outcome === 'L').length} thua
                </p>
                <HistoryList items={data} />
              </>
            ) : (
              <Empty
                title="Chưa có phong độ"
                text="Không có trận đã kết thúc trước thời điểm này."
              />
            )}
          </section>
        ))}
      </div>
      <section className="customer-panel content-card h2h">
        <span className="eyebrow">LỊCH SỬ CHẠM TRÁN</span>
        <h2>Đối đầu trực tiếp</h2>
        {h2h.data?.length ? (
          <>
            <div className="h2h-summary">
              <span>{h2h.data.length} trận</span>
              <span>
                {team(match.homeTeamId).name}:{' '}
                {
                  h2h.data.filter((item) =>
                    item.homeTeamId === match.homeTeamId
                      ? item.homeScore! > item.awayScore!
                      : item.awayScore! > item.homeScore!,
                  ).length
                }{' '}
                thắng
              </span>
              <span>
                Hòa:{' '}
                {
                  h2h.data.filter((item) => item.homeScore === item.awayScore)
                    .length
                }
              </span>
              <span>
                {team(match.awayTeamId).name}:{' '}
                {
                  h2h.data.filter((item) =>
                    item.homeTeamId === match.awayTeamId
                      ? item.homeScore! > item.awayScore!
                      : item.awayScore! > item.homeScore!,
                  ).length
                }{' '}
                thắng
              </span>
            </div>
            <div className="history-list">
              {h2h.data.slice(0, shown).map((item) => (
                <Link key={item.id} to={`/matches/${item.id}/ai`}>
                  <span>{dateLabel(item.kickoffTime)}</span>
                  <span>
                    {
                      competitionSeason(item.competitionSeasonId).competition
                        .name
                    }
                  </span>
                  <span>
                    {team(item.homeTeamId).name} · {team(item.awayTeamId).name}
                  </span>
                  <strong>{score(item)}</strong>
                </Link>
              ))}
            </div>
            {shown < h2h.data.length && (
              <button
                className="load-more"
                onClick={() =>
                  setPageState({ matchId: match.id, shown: shown + 5 })
                }
              >
                Xem thêm đối đầu <ArrowRight size={16} />
              </button>
            )}
          </>
        ) : (
          <Empty
            title="Chưa có dữ liệu đối đầu"
            text="Chưa có dữ liệu đối đầu giữa hai đội."
          />
        )}
      </section>
    </>
  )
}
export function StatisticsPage() {
  const match = useMatch()
  const result = useAsync(() => getMatchStatistics(match.id), [match.id])
  if (!result.data)
    return (
      <Empty
        title="Chưa có thống kê"
        text="Trận đấu này hiện chưa có thống kê mẫu."
      />
    )
  return (
    <section className="customer-panel content-card stats-card">
      <span className="eyebrow">THỐNG KÊ TRẬN ĐẤU · DỮ LIỆU MẪU</span>
      <h2>So sánh hai đội</h2>
      <div className="stats-teams">
        <span>
          <TeamMark id={match.homeTeamId} />
          {team(match.homeTeamId).name}
        </span>
        <b>VS</b>
        <span>
          {team(match.awayTeamId).name}
          <TeamMark id={match.awayTeamId} />
        </span>
      </div>
      {result.data.map((stat: Statistic) => {
        const total = stat.home + stat.away
        return (
          <div className="stat-row" key={stat.label}>
            <div>
              <strong>
                {stat.home}
                {stat.unit}
              </strong>
              <span>{stat.label}</span>
              <strong>
                {stat.away}
                {stat.unit}
              </strong>
            </div>
            <div className="stat-bars">
              <span
                style={{ width: `${total ? (stat.home / total) * 100 : 0}%` }}
              />
              <span
                style={{ width: `${total ? (stat.away / total) * 100 : 0}%` }}
              />
            </div>
          </div>
        )
      })}
    </section>
  )
}
export function StandingsPage() {
  const match = useMatch()
  const result = useAsync(
    () => getStandings(match.competitionSeasonId),
    [match.competitionSeasonId],
  )
  if (!result.data)
    return (
      <Empty
        title="Chưa có bảng xếp hạng"
        text="Giải đấu theo mùa này hiện chưa có bảng điểm mẫu."
      />
    )
  return (
    <section className="customer-panel content-card">
      <span className="eyebrow">
        {competitionSeason(match.competitionSeasonId).competition.name} ·{' '}
        {competitionSeason(match.competitionSeasonId).season.name}
      </span>
      <h2>Bảng xếp hạng</h2>
      <div className="standing-scroll">
        <table className="standing-table">
          <thead>
            <tr>
              {[
                '#',
                'Đội bóng',
                'Tr',
                'T',
                'H',
                'B',
                'BT',
                'BB',
                'HS',
                'Điểm',
              ].map((label) => (
                <th key={label}>{label}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {result.data.map((row: Standing, index) => (
              <tr
                key={row.teamId}
                className={
                  row.teamId === match.homeTeamId ||
                  row.teamId === match.awayTeamId
                    ? 'highlight'
                    : ''
                }
              >
                <td>{index + 1}</td>
                <td>
                  <TeamMark id={row.teamId} />
                  {team(row.teamId).name}
                </td>
                <td>{row.played}</td>
                <td>{row.won}</td>
                <td>{row.drawn}</td>
                <td>{row.lost}</td>
                <td>{row.goalsFor}</td>
                <td>{row.goalsAgainst}</td>
                <td>
                  {row.goalsFor - row.goalsAgainst > 0 ? '+' : ''}
                  {row.goalsFor - row.goalsAgainst}
                </td>
                <td>
                  <strong>{row.points}</strong>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  )
}
export function DetailIndex() {
  return <Navigate to="ai" replace />
}
