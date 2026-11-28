import { useEffect, useState } from 'react'
import {
  Ban,
  CalendarClock,
  ChartNoAxesColumn,
  CircleCheck,
  Clock3,
  Eye,
  EyeOff,
  FileOutput,
  Sparkles,
  Pencil,
  Radio,
  Trash2,
} from 'lucide-react'
import {
  matchApi,
  statusLabels,
  formatScore,
  toVietnamInput,
  toVietnamIso,
  formatVietnamTime,
  type Match,
} from './api'
import MatchForm from './MatchForm'
import MatchCatalogSelect from './MatchCatalogSelect'
import MatchStatisticsDialog from './MatchStatisticsDialog'
import MatchPromptDialog from './MatchPromptDialog'
import MatchAnalysisDialog from './MatchAnalysisDialog'
import { errorMessage } from '../shared/api'
import EntityLogo from '../shared/EntityLogo'
import {
  Button,
  Field,
  ListTable,
  Modal,
  ErrorNotice,
  PageHeading,
  Pagination,
} from '../shared/ui'
import { useList } from '../shared/useList'
import { useRecordDialogs } from '../shared/useRecordDialogs'
const matchStatusIcons = {
  SCHEDULED: Clock3,
  LIVE: Radio,
  FINISHED: CircleCheck,
  POSTPONED: CalendarClock,
  CANCELLED: Ban,
}
export default function MatchesPage() {
  const [filterReset, setFilterReset] = useState(0)
  const [statisticsMatch, setStatisticsMatch] = useState<Match | null>(null)
  const [promptMatch, setPromptMatch] = useState<Match | null>(null)
  const [analysisMatch, setAnalysisMatch] = useState<Match | null>(null)
  const [visibilityMatch, setVisibilityMatch] = useState<Match | null>(null)
  const [visibilityBusy, setVisibilityBusy] = useState(false)
  const [visibilityError, setVisibilityError] = useState('')
  const [visibilityNotice, setVisibilityNotice] = useState('')
  const list = useList(matchApi.list, true)
  const { refresh } = list
  const records = useRecordDialogs(matchApi, list.refresh)
  // Poll the authoritative backend; never derive LIVE or scores from the browser clock.
  useEffect(() => {
    const timer = window.setInterval(() => {
      if (document.visibilityState === 'visible') refresh()
    }, 15000)
    return () => window.clearInterval(timer)
  }, [refresh])
  function label(match: Match) {
    return `${match.homeTeamName} – ${match.awayTeamName}`
  }
  return (
    <>
      <PageHeading
        title="Trận đấu"
        description="Quản lý lịch thi đấu, trạng thái và kết quả trận đấu."
        createLabel="Thêm trận đấu"
        onCreate={records.create}
      />
      {records.notice && (
        <p className="success-notice" role="status">
          {records.notice}
        </p>
      )}
      {visibilityNotice && (
        <p className="success-notice" role="status">
          {visibilityNotice}
        </p>
      )}
      {list.refreshError && (
        <div role="alert">
          <ErrorNotice
            message={`Không thể cập nhật danh sách: ${list.refreshError}`}
          />
          <Button secondary onClick={list.refresh}>
            Thử lại
          </Button>
        </div>
      )}
      <section className="ui-card management-card match-management">
        <form
          className="filter-bar"
          key={filterReset}
          onSubmit={(event) => {
            event.preventDefault()
            const form = new FormData(event.currentTarget)
            const from = String(form.get('from') || '')
            const to = String(form.get('to') || '')
            list.change({
              name: String(form.get('name') || '').trim(),
              from: toVietnamIso(from),
              to: toVietnamIso(to),
            })
          }}
        >
          <Field label="Tên đội bóng">
            <input
              key={list.params.get('name') || ''}
              name="name"
              defaultValue={list.params.get('name') || ''}
              placeholder="Tìm đội nhà hoặc đội khách…"
            />
          </Field>
          <MatchCatalogSelect
            kind="competition-season"
            label="Giải đấu theo mùa"
            value={list.params.get('competitionSeasonId') || ''}
            onChange={(id) => list.change({ competitionSeasonId: id })}
          />
          <MatchCatalogSelect
            kind="team"
            label="Đội bóng"
            value={list.params.get('teamId') || ''}
            onChange={(id) => list.change({ teamId: id })}
          />
          <Field label="Trạng thái">
            <select
              value={list.params.get('status') || ''}
              onChange={(event) => list.change({ status: event.target.value })}
            >
              <option value="">Tất cả trạng thái</option>
              {Object.entries(statusLabels).map(([key, label]) => (
                <option key={key} value={key}>
                  {label}
                </option>
              ))}
            </select>
          </Field>
          <Field label="Hiển thị Customer">
            <select
              value={list.params.get('hidden') || ''}
              onChange={(event) => list.change({ hidden: event.target.value })}
            >
              <option value="">Tất cả</option>
              <option value="false">Đang hiển thị</option>
              <option value="true">Đã ẩn</option>
            </select>
          </Field>
          <Field label="Từ thời gian (giờ Việt Nam)">
            <input
              type="datetime-local"
              step="1"
              key={list.params.get('from') || ''}
              name="from"
              defaultValue={
                list.params.get('from')
                  ? toVietnamInput(list.params.get('from')!)
                  : ''
              }
            />
          </Field>
          <Field label="Đến thời gian (giờ Việt Nam)">
            <input
              type="datetime-local"
              step="1"
              key={list.params.get('to') || ''}
              name="to"
              defaultValue={
                list.params.get('to')
                  ? toVietnamInput(list.params.get('to')!)
                  : ''
              }
            />
          </Field>
          <Field label="Sắp xếp thời gian">
            <select
              value={list.params.get('direction') || 'DESC'}
              onChange={(event) =>
                list.change({ direction: event.target.value })
              }
            >
              <option value="DESC">Mới nhất trước</option>
              <option value="ASC">Cũ nhất trước</option>
            </select>
          </Field>
          <Button type="submit">Tìm kiếm</Button>
          <Button
            secondary
            onClick={() => {
              setFilterReset((value) => value + 1)
              list.change({
                name: '',
                competitionSeasonId: '',
                teamId: '',
                homeTeamId: '',
                awayTeamId: '',
                status: '',
                hidden: '',
                from: '',
                to: '',
                direction: '',
              })
            }}
          >
            Đặt lại
          </Button>
        </form>
        <ListTable
          headers={[
            'Giải đấu / Vòng',
            'Đội nhà',
            'Tỷ số',
            'Đội khách',
            'Trạng thái',
            'Thống kê',
            'Hiển thị',
            'Thời gian (VN)',
            'Thao tác',
          ]}
          loading={list.loading}
          error={list.error}
          empty={!list.data?.content.length}
          retry={list.refresh}
        >
          {list.data?.content.map((item) => {
            const StatusIcon = matchStatusIcons[item.status]
            return (
              <tr key={item.id}>
                <td>
                  <strong>{item.competitionSeasonName}</strong>
                  <small className="match-subtext">
                    {item.round || '—'} · #{item.id}
                  </small>
                </td>
                <td>
                  <span className="competition-name">
                    <EntityLogo
                      name={item.homeTeamName}
                      logo={item.homeTeamLogo}
                    />
                    {item.homeTeamName}
                  </span>
                </td>
                <td>
                  <strong className="match-score">{formatScore(item)}</strong>
                </td>
                <td>
                  <span className="competition-name">
                    <EntityLogo
                      name={item.awayTeamName}
                      logo={item.awayTeamLogo}
                    />
                    {item.awayTeamName}
                  </span>
                </td>
                <td>
                  <span
                    className={`match-icon-badge match-status-${item.status.toLowerCase()}`}
                    title={statusLabels[item.status]}
                    aria-label={`Trạng thái: ${statusLabels[item.status]}`}
                  >
                    <StatusIcon size={18} aria-hidden="true" />
                  </span>
                </td>
                <td>
                  <span
                    className={`match-icon-badge ${item.hasStatistics ? 'match-icon-positive' : 'match-icon-muted'}`}
                    title={
                      item.hasStatistics
                        ? 'Đã lưu thống kê'
                        : 'Chưa có thống kê'
                    }
                    aria-label={
                      item.hasStatistics
                        ? 'Đã lưu thống kê'
                        : 'Chưa có thống kê'
                    }
                  >
                    <ChartNoAxesColumn size={18} aria-hidden="true" />
                  </span>
                </td>
                <td>
                  <span
                    className={`match-icon-badge ${item.hidden ? 'match-icon-muted' : 'match-icon-positive'}`}
                    title={item.hidden ? 'Đã ẩn' : 'Đang hiển thị'}
                    aria-label={item.hidden ? 'Đã ẩn' : 'Đang hiển thị'}
                  >
                    {item.hidden ? (
                      <EyeOff size={18} aria-hidden="true" />
                    ) : (
                      <Eye size={18} aria-hidden="true" />
                    )}
                  </span>
                </td>
                <td>
                  <time dateTime={item.kickoffTime}>
                    {formatVietnamTime(item.kickoffTime)}
                  </time>
                </td>
                <td>
                  <div className="row-actions match-row-actions">
                    <button
                      type="button"
                      title="Nhận định AI"
                      aria-label="Nhận định AI"
                      onClick={() => setAnalysisMatch(item)}
                    >
                      <Sparkles size={17} />
                    </button>
                    <button
                      type="button"
                      title="Export Prompt"
                      aria-label="Export Prompt"
                      onClick={() => setPromptMatch(item)}
                    >
                      <FileOutput size={17} />
                    </button>
                    <button
                      type="button"
                      title={item.hidden ? 'Hiện trận đấu' : 'Ẩn trận đấu'}
                      aria-label={item.hidden ? 'Hiện trận đấu' : 'Ẩn trận đấu'}
                      onClick={() => {
                        setVisibilityMatch(item)
                        setVisibilityError('')
                      }}
                    >
                      {item.hidden ? <Eye size={17} /> : <EyeOff size={17} />}
                    </button>
                    <button
                      type="button"
                      title="Thống kê"
                      aria-label="Thống kê"
                      onClick={() => setStatisticsMatch(item)}
                    >
                      <ChartNoAxesColumn size={17} />
                    </button>
                    <button
                      type="button"
                      title="Chi tiết"
                      aria-label="Chi tiết"
                      onClick={() => records.select('view', item)}
                    >
                      <Eye size={17} />
                    </button>
                    <button
                      type="button"
                      title="Chỉnh sửa"
                      aria-label="Chỉnh sửa"
                      onClick={() => records.select('edit', item)}
                    >
                      <Pencil size={17} />
                    </button>
                    <button
                      type="button"
                      className="danger-text"
                      title="Xóa"
                      aria-label="Xóa"
                      onClick={() => records.select('delete', item)}
                    >
                      <Trash2 size={17} />
                    </button>
                  </div>
                </td>
              </tr>
            )
          })}
        </ListTable>
        <Pagination
          page={list.page}
          size={list.size}
          total={list.data?.page.totalElements || 0}
          pages={list.data?.page.totalPages || 0}
          disabled={list.loading || !!list.error}
          changeSize={(size) => list.change({ size })}
          go={list.go}
        />
      </section>
      {statisticsMatch && (
        <MatchStatisticsDialog
          match={statisticsMatch}
          onClose={() => setStatisticsMatch(null)}
          onSaved={list.refresh}
        />
      )}
      {promptMatch && (
        <MatchPromptDialog
          match={promptMatch}
          onClose={() => setPromptMatch(null)}
        />
      )}
      {analysisMatch && (
        <MatchAnalysisDialog
          match={analysisMatch}
          onClose={() => setAnalysisMatch(null)}
        />
      )}
      {visibilityMatch && (
        <Modal
          title={visibilityMatch.hidden ? 'Hiện trận đấu' : 'Ẩn trận đấu'}
          onClose={() => setVisibilityMatch(null)}
          busy={visibilityBusy}
        >
          <div className="dialog-body">
            <p>
              {visibilityMatch.hidden
                ? 'Trận đấu sẽ hiển thị lại trên Customer UI.'
                : 'Trận đấu và lịch sử liên quan sẽ không hiển thị trên Customer UI. Dữ liệu vẫn được lưu.'}
            </p>
            <ErrorNotice message={visibilityError} />
            <div className="dialog-actions">
              <Button
                secondary
                disabled={visibilityBusy}
                onClick={() => setVisibilityMatch(null)}
              >
                Hủy
              </Button>
              <Button
                disabled={visibilityBusy}
                onClick={async () => {
                  setVisibilityBusy(true)
                  try {
                    await matchApi.visibility(
                      visibilityMatch.id,
                      !visibilityMatch.hidden,
                    )
                    setVisibilityNotice(
                      visibilityMatch.hidden
                        ? 'Đã hiện trận đấu.'
                        : 'Đã ẩn trận đấu.',
                    )
                    setVisibilityMatch(null)
                    list.refresh()
                  } catch (error) {
                    setVisibilityError(errorMessage(error))
                  } finally {
                    setVisibilityBusy(false)
                  }
                }}
              >
                Xác nhận
              </Button>
            </div>
          </div>
        </Modal>
      )}
      {(records.creating || records.selection?.mode === 'edit') && (
        <MatchForm
          value={records.selection?.value}
          onClose={records.close}
          onSuccess={records.success}
        />
      )}
      {records.dialogs(label, (item) => (
        <>
          <dt>ID</dt>
          <dd>#{item.id}</dd>
          <dt>Giải theo mùa</dt>
          <dd>{item.competitionSeasonName}</dd>
          <dt>Đội nhà</dt>
          <dd>{item.homeTeamName}</dd>
          <dt>Đội khách</dt>
          <dd>{item.awayTeamName}</dd>
          <dt>Tỷ số</dt>
          <dd>{formatScore(item)}</dd>
          <dt>Trạng thái</dt>
          <dd>{statusLabels[item.status]}</dd>
          <dt>Giờ Việt Nam (UTC+7)</dt>
          <dd>{formatVietnamTime(item.kickoffTime)}</dd>
          <dt>Vòng đấu</dt>
          <dd>{item.round || '—'}</dd>
        </>
      ))}
    </>
  )
}
