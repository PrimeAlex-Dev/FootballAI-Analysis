import { useState } from 'react'
import {
  matchApi,
  statusLabels,
  toVietnamInput,
  toVietnamIso,
  type Match,
  type MatchStatus,
} from './api'
import MatchCatalogSelect from './MatchCatalogSelect'
import { Button, ErrorNotice, Field, Modal } from '../shared/ui'
import { errorMessage } from '../shared/api'
export default function MatchForm({
  value,
  onClose,
  onSuccess,
}: {
  value?: Match
  onClose: () => void
  onSuccess: () => void
}) {
  const [competition, setCompetition] = useState(
    String(value?.competitionSeasonId || ''),
  )
  const [home, setHome] = useState(String(value?.homeTeamId || ''))
  const [away, setAway] = useState(String(value?.awayTeamId || ''))
  const [kickoff, setKickoff] = useState(
    value ? toVietnamInput(value.kickoffTime) : '',
  )
  const [round, setRound] = useState(value?.round || '')
  const [status, setStatus] = useState<MatchStatus>(
    value?.status || 'SCHEDULED',
  )
  const [homeScore, setHomeScore] = useState(value?.homeScore?.toString() ?? '')
  const [awayScore, setAwayScore] = useState(value?.awayScore?.toString() ?? '')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const scored = status === 'LIVE' || status === 'FINISHED'
  const dirty =
    competition !== String(value?.competitionSeasonId || '') ||
    home !== String(value?.homeTeamId || '') ||
    away !== String(value?.awayTeamId || '') ||
    kickoff !== (value ? toVietnamInput(value.kickoffTime) : '') ||
    round !== (value?.round || '') ||
    status !== (value?.status || 'SCHEDULED') ||
    homeScore !== (value?.homeScore?.toString() ?? '') ||
    awayScore !== (value?.awayScore?.toString() ?? '')
  function changeStatus(next: MatchStatus) {
    const removing = next !== 'LIVE' && next !== 'FINISHED'
    if (
      removing &&
      value?.homeScore != null &&
      value?.awayScore != null &&
      !window.confirm(
        'Chuyển trạng thái sẽ xóa hai tỷ số đã lưu khi bạn lưu thay đổi. Tiếp tục?',
      )
    )
      return
    setStatus(next)
    if (removing) {
      setHomeScore('')
      setAwayScore('')
    }
  }
  return (
    <Modal
      title={value ? 'Chỉnh sửa trận đấu' : 'Thêm trận đấu'}
      onClose={onClose}
      busy={busy}
      dirty={dirty}
    >
      <form
        className="dialog-body match-form"
        onSubmit={async (event) => {
          event.preventDefault()
          if (busy) return
          setError('')
          if (!competition || !home || !away || !kickoff) {
            setError('Vui lòng chọn giải theo mùa, hai đội và thời gian.')
            return
          }
          if (home === away) {
            setError('Đội nhà và đội khách phải khác nhau.')
            return
          }
          if (scored && (homeScore === '') !== (awayScore === '')) {
            setError('Vui lòng nhập đủ hai tỷ số hoặc để trống cả hai.')
            return
          }
          if (status === 'FINISHED' && (homeScore === '' || awayScore === '')) {
            setError('Trận kết thúc bắt buộc có đủ hai tỷ số.')
            return
          }
          if (
            scored &&
            [homeScore, awayScore].some(
              (score) =>
                score !== '' &&
                (!/^\d+$/.test(score) || Number(score) > 2147483647),
            )
          ) {
            setError('Tỷ số phải là số nguyên không âm, tối đa 2147483647.')
            return
          }
          const vietnamTime = toVietnamIso(kickoff)
          if (!vietnamTime) {
            setError('Thời gian không hợp lệ.')
            return
          }
          setBusy(true)
          try {
            const input = {
              competitionSeasonId: Number(competition),
              homeTeamId: Number(home),
              awayTeamId: Number(away),
              kickoffTime: vietnamTime,
              round: round.trim() || null,
              status,
              homeScore: scored && homeScore !== '' ? Number(homeScore) : null,
              awayScore: scored && awayScore !== '' ? Number(awayScore) : null,
            }
            if (value) await matchApi.update(value.id, input)
            else await matchApi.create(input)
            onSuccess()
          } catch (reason) {
            setError(errorMessage(reason))
          } finally {
            setBusy(false)
          }
        }}
      >
        <MatchCatalogSelect
          kind="competition-season"
          label="Giải đấu theo mùa *"
          value={competition}
          selectedName={value?.competitionSeasonName}
          onChange={setCompetition}
          disabled={busy}
        />
        <div className="match-form-grid">
          <MatchCatalogSelect
            kind="team"
            label="Đội nhà *"
            value={home}
            selectedName={value?.homeTeamName}
            onChange={setHome}
            disabled={busy}
          />
          <MatchCatalogSelect
            kind="team"
            label="Đội khách *"
            value={away}
            selectedName={value?.awayTeamName}
            onChange={setAway}
            disabled={busy}
          />
        </div>
        <Field
          label="Thời gian thi đấu (giờ Việt Nam) *"
          hint="Giờ Việt Nam (Asia/Ho_Chi_Minh, UTC+7)."
        >
          <input
            type="datetime-local"
            step="1"
            required
            value={kickoff}
            onChange={(event) => setKickoff(event.target.value)}
            disabled={busy}
          />
        </Field>
        <Field label="Vòng đấu">
          <input
            value={round}
            onChange={(event) => setRound(event.target.value)}
            disabled={busy}
          />
        </Field>
        <Field label="Trạng thái">
          <select
            value={status}
            disabled={busy}
            onChange={(event) =>
              changeStatus(event.target.value as MatchStatus)
            }
          >
            {Object.entries(statusLabels).map(([key, label]) => (
              <option key={key} value={key}>
                {label}
              </option>
            ))}
          </select>
        </Field>
        {scored ? (
          <div className="match-form-grid">
            <Field label="Bàn thắng đội nhà">
              <input
                type="number"
                min="0"
                max="2147483647"
                step="1"
                required={status === 'FINISHED'}
                value={homeScore}
                onChange={(event) => setHomeScore(event.target.value)}
                disabled={busy}
              />
            </Field>
            <Field label="Bàn thắng đội khách">
              <input
                type="number"
                min="0"
                max="2147483647"
                step="1"
                required={status === 'FINISHED'}
                value={awayScore}
                onChange={(event) => setAwayScore(event.target.value)}
                disabled={busy}
              />
            </Field>
          </div>
        ) : (
          <div className="link-preview">
            <small>Tỷ số</small>
            <strong>- : -</strong>
          </div>
        )}
        <p className="muted">
          LIVE có thể chưa có tỷ số. Trận SCHEDULED đã đến giờ sẽ chuyển LIVE
          khi lưu.
        </p>
        <ErrorNotice message={error} />
        <div className="dialog-actions">
          <Button
            secondary
            disabled={busy}
            onClick={() => {
              if (!dirty || window.confirm('Bỏ các thay đổi chưa lưu?'))
                onClose()
            }}
          >
            Hủy
          </Button>
          <Button type="submit" disabled={busy}>
            {busy ? 'Đang lưu…' : 'Lưu trận đấu'}
          </Button>
        </div>
      </form>
    </Modal>
  )
}
