import { useEffect, useState } from 'react'
import { Button, ErrorNotice, Modal } from '../shared/ui'
import { errorMessage } from '../shared/api'
import { formatVietnamTime, type Match } from './api'
import {
  statisticFields,
  statisticsApi,
  type StatisticsInput,
  type StatisticsResponse,
} from './statisticsApi'

export default function MatchStatisticsDialog({
  match,
  onClose,
  onSaved,
}: {
  match: Match
  onClose: () => void
  onSaved: () => void
}) {
  const [saved, setSaved] = useState<StatisticsResponse | null>(null)
  const [preview, setPreview] = useState<StatisticsResponse | null>(null)
  const [input, setInput] = useState<StatisticsInput | null>(null)
  const [raw, setRaw] = useState('')
  const [busy, setBusy] = useState(false)
  const [loading, setLoading] = useState(match.hasStatistics)
  const [error, setError] = useState('')
  const [confirm, setConfirm] = useState(false)
  useEffect(() => {
    if (!match.hasStatistics) return
    let active = true
    statisticsApi
      .get(match.id)
      .then((data) => {
        if (active) setSaved(data)
      })
      .catch((reason: unknown) => {
        if (active) setError(errorMessage(reason))
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [match.id, match.hasStatistics])
  async function examine() {
    setError('')
    setPreview(null)
    setInput(null)
    setConfirm(false)
    let parsed: StatisticsInput
    try {
      parsed = JSON.parse(raw) as StatisticsInput
    } catch {
      setError('JSON không hợp lệ. Vui lòng kiểm tra cú pháp.')
      return
    }
    setBusy(true)
    try {
      const data = await statisticsApi.preview(match.id, parsed)
      setInput(parsed)
      setPreview(data)
    } catch (reason) {
      setError(errorMessage(reason))
    } finally {
      setBusy(false)
    }
  }
  async function save() {
    if (!input) return
    setBusy(true)
    setError('')
    try {
      const data = saved
        ? await statisticsApi.update(match.id, input)
        : await statisticsApi.create(match.id, input)
      setSaved(data)
      setPreview(null)
      setInput(null)
      setRaw('')
      setConfirm(false)
      onSaved()
    } catch (reason) {
      setError(errorMessage(reason))
    } finally {
      setBusy(false)
    }
  }
  const shown = preview ?? saved
  return (
    <Modal
      className="match-statistics-modal"
      title={`Thống kê · ${match.homeTeamName} – ${match.awayTeamName}`}
      onClose={onClose}
      busy={busy}
      dirty={!!raw}
    >
      <div className="dialog-body match-statistics-dialog">
        <p className="muted">
          {match.competitionSeasonName} · {formatVietnamTime(match.kickoffTime)}{' '}
          (giờ Việt Nam)
        </p>
        {loading ? (
          <p>Đang tải thống kê…</p>
        ) : (
          <>
            <p className="muted">
              {saved ? 'Đã lưu thống kê' : 'Chưa có thống kê'}
            </p>
            {shown && (
              <div className="table-scroll">
                <table>
                  <thead>
                    <tr>
                      <th>Chỉ số</th>
                      <th>{match.homeTeamName} (nhà)</th>
                      <th>{match.awayTeamName} (khách)</th>
                    </tr>
                  </thead>
                  <tbody>
                    {statisticFields.map(([key, label]) => (
                      <tr key={key}>
                        <th>{label}</th>
                        <td>{shown.home[key] ?? 'Chưa có dữ liệu'}</td>
                        <td>{shown.away[key] ?? 'Chưa có dữ liệu'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
            {saved && !preview && (
              <p className="muted">
                Tạo: {formatVietnamTime(saved.createdAt!)} · Cập nhật:{' '}
                {formatVietnamTime(saved.updatedAt!)}
              </p>
            )}
            {preview && (
              <p className="muted">
                Đây là bản xem trước. Hãy kiểm tra đúng đội nhà và đội khách
                trước khi lưu.
              </p>
            )}
            <label className="admin-field">
              <span>
                {saved ? 'Import JSON mới để cập nhật' : 'Dán JSON thống kê'}
              </span>
              <textarea
                value={raw}
                onChange={(event) => {
                  setRaw(event.target.value)
                  setPreview(null)
                  setInput(null)
                  setConfirm(false)
                }}
                rows={8}
                placeholder='{"home": {"shots": 10}, "away": {"shots": 8}}'
              />
            </label>
            <ErrorNotice message={error} />
            {confirm && (
              <p className="error-notice">
                Thao tác này thay thế toàn bộ chỉ số đã lưu. Các chỉ số thiếu
                trong JSON mới sẽ thành “Chưa có dữ liệu”.
              </p>
            )}
            <div className="dialog-actions">
              <Button
                secondary
                onClick={examine}
                disabled={busy || !raw.trim()}
              >
                Kiểm tra và xem trước
              </Button>
              {preview && (
                <Button
                  onClick={() => {
                    if (saved && !confirm) setConfirm(true)
                    else void save()
                  }}
                  disabled={busy}
                >
                  {saved
                    ? confirm
                      ? 'Xác nhận cập nhật'
                      : 'Cập nhật thống kê'
                    : 'Lưu thống kê'}
                </Button>
              )}
            </div>
          </>
        )}
      </div>
    </Modal>
  )
}
