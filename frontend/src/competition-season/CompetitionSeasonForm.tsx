import { useState } from 'react'
import { competitionSeasonApi } from './api'
import CatalogSelect from '../shared/CatalogSelect'
import { Button, Modal, ErrorNotice } from '../shared/ui'
import { errorMessage } from '../shared/api'
export default function CompetitionSeasonForm({
  onClose,
  onSuccess,
}: {
  onClose: () => void
  onSuccess: () => void
}) {
  const [competition, setCompetition] = useState({ id: '', name: '' })
  const [season, setSeason] = useState({ id: '', name: '' })
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  return (
    <Modal
      title="Liên kết giải đấu và mùa giải"
      onClose={onClose}
      busy={busy}
      dirty={!!competition.id || !!season.id}
    >
      <form
        className="dialog-body"
        onSubmit={async (event) => {
          event.preventDefault()
          if (busy || !competition.id || !season.id) return
          setBusy(true)
          setError('')
          try {
            await competitionSeasonApi.create({
              competitionId: Number(competition.id),
              seasonId: Number(season.id),
            })
            onSuccess()
          } catch (reason) {
            setError(errorMessage(reason))
          } finally {
            setBusy(false)
          }
        }}
      >
        <CatalogSelect
          kind="competition"
          value={competition.id}
          disabled={busy}
          onChange={(id, name) => setCompetition({ id, name })}
        />
        <CatalogSelect
          kind="season"
          value={season.id}
          disabled={busy}
          onChange={(id, name) => setSeason({ id, name })}
        />
        <div className="link-preview">
          <small>Tên hiển thị dự kiến</small>
          <strong>
            {competition.name && season.name
              ? `${competition.name} ${season.name}`
              : 'Chọn giải đấu và mùa giải'}
          </strong>
        </div>
        <ErrorNotice message={error} />
        <div className="dialog-actions">
          <Button
            secondary
            disabled={busy}
            onClick={() => {
              if (
                (!competition.id && !season.id) ||
                window.confirm('Bỏ các thay đổi chưa lưu?')
              )
                onClose()
            }}
          >
            Hủy
          </Button>
          <Button
            type="submit"
            disabled={busy || !competition.id || !season.id}
          >
            {busy ? 'Đang lưu…' : 'Tạo liên kết'}
          </Button>
        </div>
      </form>
    </Modal>
  )
}
