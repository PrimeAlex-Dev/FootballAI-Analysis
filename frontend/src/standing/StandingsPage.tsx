import { useEffect, useState } from 'react'
import {
  standingApi,
  type Standing,
  type StandingInput,
  type StandingPreview,
  type StandingSummary,
  type TeamMapping,
} from './api'
import {
  Button,
  DeleteDialog,
  ErrorNotice,
  ListTable,
  Modal,
  PageHeading,
  Pagination,
} from '../shared/ui'
import { errorMessage } from '../shared/api'
import { useList } from '../shared/useList'
import MatchCatalogSelect from '../match/MatchCatalogSelect'
import { formatVietnamTime } from '../match/api'
import EntityLogo from '../shared/EntityLogo'

type Mode =
  | { kind: 'create' }
  | { kind: 'view' | 'edit' | 'delete'; item: StandingSummary }

function StandingTable({
  tables,
  mappings,
  onMap,
}: {
  tables: StandingPreview['tables']
  mappings?: TeamMapping[]
  onMap?: (mapping: TeamMapping) => void
}) {
  return (
    <div className="standing-tables">
      {tables.map((table, index) => (
        <section key={index}>
          <h3>Bảng {index + 1}</h3>
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  {[
                    'Hạng',
                    'Đội nguồn / Team',
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
                {table.map((row) => {
                  const mapped = mappings?.find(
                    (value) =>
                      value.tableIndex === row.tableIndex &&
                      value.rowIndex === row.rowIndex,
                  )
                  return (
                    <tr key={row.rowIndex}>
                      <td>{row.rank}</td>
                      <td>
                        <strong>{row.sourceTeam}</strong>
                        {row.teamId ? (
                          <span className="standing-team">
                            <EntityLogo
                              name={row.teamName || row.sourceTeam}
                              logo={row.teamLogo}
                            />
                            {row.teamName}
                          </span>
                        ) : onMap ? (
                          <MatchCatalogSelect
                            kind="team"
                            label="Chọn Team tương ứng"
                            value={mapped ? String(mapped.teamId) : ''}
                            onChange={(id) =>
                              id &&
                              onMap({
                                tableIndex: row.tableIndex,
                                rowIndex: row.rowIndex,
                                teamId: Number(id),
                              })
                            }
                          />
                        ) : (
                          <span>Chưa đối chiếu</span>
                        )}
                      </td>
                      <td>{row.matchesPlayed}</td>
                      <td>{row.wins}</td>
                      <td>{row.draws}</td>
                      <td>{row.losses}</td>
                      <td>{row.goalsFor}</td>
                      <td>{row.goalsAgainst}</td>
                      <td>{row.goalDifference}</td>
                      <td>{row.points}</td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        </section>
      ))}
    </div>
  )
}

function StandingDialog({
  mode,
  onClose,
  onSaved,
}: {
  mode: Exclude<Mode, { kind: 'delete' }>
  onClose: () => void
  onSaved: () => void
}) {
  const [competitionSeasonId, setCompetitionSeasonId] = useState(
    mode.kind === 'create' ? '' : String(mode.item.competitionSeasonId),
  )
  const [value, setValue] = useState<Standing>()
  const [raw, setRaw] = useState('')
  const [preview, setPreview] = useState<StandingPreview>()
  const [mappings, setMappings] = useState<TeamMapping[]>([])
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [confirm, setConfirm] = useState(false)
  useEffect(() => {
    if (mode.kind === 'create') return
    let active = true
    standingApi
      .get(mode.item.id)
      .then((data) => {
        if (active) setValue(data)
      })
      .catch((reason) => {
        if (active) setError(errorMessage(reason))
      })
    return () => {
      active = false
    }
  }, [mode])
  function input(): StandingInput | null {
    try {
      const parsed: unknown = JSON.parse(raw)
      if (
        !parsed ||
        typeof parsed !== 'object' ||
        Array.isArray(parsed) ||
        !('tables' in parsed)
      )
        throw new Error()
      return { ...(parsed as StandingInput), mappings }
    } catch {
      setError('JSON không hợp lệ hoặc thiếu trường tables.')
      return null
    }
  }
  async function examine() {
    const data = input()
    if (!data || !competitionSeasonId) return
    setBusy(true)
    setError('')
    try {
      setPreview(await standingApi.preview(Number(competitionSeasonId), data))
      setConfirm(false)
    } catch (reason) {
      setError(errorMessage(reason))
    } finally {
      setBusy(false)
    }
  }
  async function save() {
    const data = input()
    if (!data || !competitionSeasonId) return
    setBusy(true)
    setError('')
    try {
      if (mode.kind === 'edit')
        await standingApi.update(Number(competitionSeasonId), data)
      else await standingApi.create(Number(competitionSeasonId), data)
      onSaved()
    } catch (reason) {
      setError(errorMessage(reason))
    } finally {
      setBusy(false)
    }
  }
  const unresolved =
    preview?.tables
      .flat()
      .filter(
        (row) =>
          !row.teamId &&
          !mappings.some(
            (m) =>
              m.tableIndex === row.tableIndex && m.rowIndex === row.rowIndex,
          ),
      ).length ?? 0
  const duplicateTeam =
    preview?.tables.some((table) => {
      const ids = table
        .map(
          (row) =>
            row.teamId ??
            mappings.find(
              (mapping) =>
                mapping.tableIndex === row.tableIndex &&
                mapping.rowIndex === row.rowIndex,
            )?.teamId,
        )
        .filter((id): id is number => id !== undefined)
      return new Set(ids).size !== ids.length
    }) ?? false
  return (
    <Modal
      className="standing-modal"
      title={
        mode.kind === 'create'
          ? 'Thêm bảng xếp hạng'
          : mode.kind === 'edit'
            ? 'Cập nhật bảng xếp hạng'
            : 'Chi tiết bảng xếp hạng'
      }
      onClose={onClose}
      busy={busy}
      dirty={!!raw}
    >
      <div className="dialog-body standing-dialog">
        {mode.kind === 'create' ? (
          <MatchCatalogSelect
            kind="competition-season"
            label="Giải đấu theo mùa"
            value={competitionSeasonId}
            onChange={(id) => {
              setCompetitionSeasonId(id)
              setPreview(undefined)
            }}
          />
        ) : (
          <p>
            <strong>{mode.item.competitionSeasonName}</strong>
          </p>
        )}
        {mode.kind === 'view' ? (
          value ? (
            <>
              <p className="muted">
                Cập nhật: {formatVietnamTime(value.updatedAt)} (giờ Việt Nam)
              </p>
              <StandingTable tables={value.tables} />
            </>
          ) : (
            !error && <p>Đang tải dữ liệu…</p>
          )
        ) : (
          <>
            {mode.kind === 'edit' && value && (
              <StandingTable tables={value.tables} />
            )}
            <label className="admin-field">
              <span>Dán JSON Flashscore</span>
              <textarea
                rows={9}
                value={raw}
                onChange={(event) => {
                  setRaw(event.target.value)
                  setPreview(undefined)
                  setMappings([])
                  setConfirm(false)
                }}
              />
            </label>
            <Button
              secondary
              disabled={busy || !raw.trim() || !competitionSeasonId}
              onClick={examine}
            >
              Kiểm tra và xem trước
            </Button>
            {preview && (
              <>
                <p>
                  {preview.tables.length} bảng · {preview.tables.flat().length}{' '}
                  đội. Kiểm tra tên nguồn và Team đã liên kết trước khi lưu.
                </p>
                <StandingTable
                  tables={preview.tables}
                  mappings={mappings}
                  onMap={(mapping) => {
                    setMappings((current) => [
                      ...current.filter(
                        (item) =>
                          !(
                            item.tableIndex === mapping.tableIndex &&
                            item.rowIndex === mapping.rowIndex
                          ),
                      ),
                      mapping,
                    ])
                    setConfirm(false)
                  }}
                />
                {unresolved > 0 && (
                  <p className="muted">Còn {unresolved} đội cần đối chiếu.</p>
                )}
                {duplicateTeam && (
                  <ErrorNotice message="Một Team đang được chọn cho nhiều dòng trong cùng bảng." />
                )}
                {confirm && (
                  <p className="error-notice">
                    Toàn bộ bảng xếp hạng cũ sẽ được thay thế. Bạn xác nhận cập
                    nhật?
                  </p>
                )}
                <div className="dialog-actions">
                  <Button
                    disabled={busy || unresolved > 0 || duplicateTeam}
                    onClick={() => {
                      if (mode.kind === 'edit' && !confirm) setConfirm(true)
                      else void save()
                    }}
                  >
                    {mode.kind === 'edit'
                      ? confirm
                        ? 'Xác nhận thay thế'
                        : 'Cập nhật bằng JSON'
                      : 'Lưu bảng xếp hạng'}
                  </Button>
                </div>
              </>
            )}
          </>
        )}
        <ErrorNotice message={error} />
      </div>
    </Modal>
  )
}
export default function StandingsPage() {
  const list = useList(standingApi.list)
  const [mode, setMode] = useState<Mode | null>(null)
  const [visibilityItem, setVisibilityItem] = useState<StandingSummary | null>(
    null,
  )
  const [visibilityBusy, setVisibilityBusy] = useState(false)
  const [visibilityError, setVisibilityError] = useState('')
  const [notice, setNotice] = useState('')
  const saved = () => {
    setMode(null)
    setNotice('Đã lưu bảng xếp hạng.')
    list.refresh()
  }
  return (
    <>
      <PageHeading
        title="Bảng xếp hạng"
        description="Import bảng xếp hạng từ JSON Flashscore theo giải đấu và mùa giải."
        createLabel="Thêm Standing"
        onCreate={() => setMode({ kind: 'create' })}
      />
      {notice && (
        <p className="success-notice" role="status">
          {notice}
        </p>
      )}
      <section className="ui-card management-card">
        <div className="filter-bar">
          <MatchCatalogSelect
            kind="competition-season"
            label="Giải đấu theo mùa"
            value={list.params.get('competitionSeasonId') || ''}
            onChange={(id) => list.change({ competitionSeasonId: id })}
          />
          <label className="admin-field">
            <span>Hiển thị Customer</span>
            <select
              value={list.params.get('hidden') || ''}
              onChange={(event) => list.change({ hidden: event.target.value })}
            >
              <option value="">Tất cả</option>
              <option value="false">Đang hiển thị</option>
              <option value="true">Đã ẩn</option>
            </select>
          </label>
          <Button
            secondary
            onClick={() => list.change({ competitionSeasonId: '', hidden: '' })}
          >
            Đặt lại
          </Button>
        </div>
        <ListTable
          headers={[
            'Giải đấu theo mùa',
            'Số bảng',
            'Số đội',
            'Cập nhật (VN)',
            'Hiển thị',
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
              </td>
              <td>{item.tableCount}</td>
              <td>{item.teamCount}</td>
              <td>{formatVietnamTime(item.updatedAt)}</td>
              <td>
                <span className="type-badge">
                  {item.hidden ? 'Đã ẩn' : 'Đang hiển thị'}
                </span>
              </td>
              <td>
                <div className="row-actions">
                  <Button
                    secondary
                    onClick={() => {
                      setVisibilityItem(item)
                      setVisibilityError('')
                    }}
                  >
                    {item.hidden ? 'Hiện' : 'Ẩn'}
                  </Button>
                  <Button
                    secondary
                    onClick={() => setMode({ kind: 'view', item })}
                  >
                    Xem
                  </Button>
                  <Button
                    secondary
                    onClick={() => setMode({ kind: 'edit', item })}
                  >
                    Cập nhật
                  </Button>
                  <Button
                    secondary
                    onClick={() => setMode({ kind: 'delete', item })}
                  >
                    Xóa
                  </Button>
                </div>
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
          changeSize={(size) => list.change({ size: String(size) })}
          go={list.go}
        />
      </section>
      {mode && mode.kind !== 'delete' && (
        <StandingDialog
          mode={mode}
          onClose={() => setMode(null)}
          onSaved={saved}
        />
      )}
      {visibilityItem && (
        <Modal
          title={visibilityItem.hidden ? 'Hiện Standing' : 'Ẩn Standing'}
          onClose={() => setVisibilityItem(null)}
          busy={visibilityBusy}
        >
          <div className="dialog-body">
            <p>
              {visibilityItem.hidden
                ? 'Bảng xếp hạng sẽ hiển thị lại trên Customer UI.'
                : 'Customer UI sẽ hiện “Chưa có dữ liệu”. Bảng xếp hạng vẫn được lưu.'}
            </p>
            <ErrorNotice message={visibilityError} />
            <div className="dialog-actions">
              <Button
                secondary
                disabled={visibilityBusy}
                onClick={() => setVisibilityItem(null)}
              >
                Hủy
              </Button>
              <Button
                disabled={visibilityBusy}
                onClick={async () => {
                  setVisibilityBusy(true)
                  try {
                    await standingApi.visibility(
                      visibilityItem.id,
                      !visibilityItem.hidden,
                    )
                    setNotice(
                      visibilityItem.hidden
                        ? 'Đã hiện Standing.'
                        : 'Đã ẩn Standing.',
                    )
                    setVisibilityItem(null)
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
      {mode?.kind === 'delete' && (
        <DeleteDialog
          name={mode.item.competitionSeasonName}
          remove={() => standingApi.remove(mode.item.competitionSeasonId)}
          onClose={() => setMode(null)}
          onSuccess={() => {
            setMode(null)
            setNotice('Đã xóa bảng xếp hạng.')
            list.refresh()
          }}
        />
      )}
    </>
  )
}
