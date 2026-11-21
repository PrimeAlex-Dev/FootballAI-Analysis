import { useEffect, useState } from 'react'
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
import EntityLogo from '../shared/EntityLogo'
import {
  Button,
  Field,
  ListTable,
  PageHeading,
  Pagination,
  RowActions,
} from '../shared/ui'
import { useList } from '../shared/useList'
import { useRecordDialogs } from '../shared/useRecordDialogs'
export default function MatchesPage() {
  const [filterReset, setFilterReset] = useState(0)
  const list = useList(matchApi.list)
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
            'Thời gian (VN)',
            'Thao tác',
          ]}
          loading={list.loading}
          error={list.error}
          empty={!list.data?.content.length}
          retry={list.refresh}
        >
          {list.data?.content.map((item) => (
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
                  className={`type-badge match-status-${item.status.toLowerCase()}`}
                >
                  {statusLabels[item.status]}
                </span>
              </td>
              <td>
                <time dateTime={item.kickoffTime}>
                  {formatVietnamTime(item.kickoffTime)}
                </time>
              </td>
              <td>
                <RowActions
                  view={() => records.select('view', item)}
                  edit={() => records.select('edit', item)}
                  remove={() => records.select('delete', item)}
                />
              </td>
            </tr>
          ))}
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
