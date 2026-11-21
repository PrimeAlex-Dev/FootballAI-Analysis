import { useState } from 'react'
import { competitionApi, type Competition } from './api'
import { Button, Field, Modal, ErrorNotice } from '../shared/ui'
import { errorMessage } from '../shared/api'
export default function CompetitionForm({
  value,
  onClose,
  onSuccess,
}: {
  value?: Competition
  onClose: () => void
  onSuccess: () => void
}) {
  const [name, setName] = useState(value?.name || '')
  const [type, setType] = useState<'LEAGUE' | 'CUP'>(value?.type || 'LEAGUE')
  const [logo, setLogo] = useState(value?.logo || '')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const dirty =
    name !== (value?.name || '') ||
    type !== (value?.type || 'LEAGUE') ||
    logo !== (value?.logo || '')
  return (
    <Modal
      title={value ? 'Chỉnh sửa giải đấu' : 'Thêm giải đấu'}
      onClose={onClose}
      busy={busy}
      dirty={dirty}
    >
      <form
        className="dialog-body"
        onSubmit={async (event) => {
          event.preventDefault()
          if (busy) return
          setBusy(true)
          setError('')
          try {
            const input = { name: name.trim(), type, logo: logo.trim() || null }
            if (value) await competitionApi.update(value.id, input)
            else await competitionApi.create(input)
            onSuccess()
          } catch (reason) {
            setError(errorMessage(reason))
          } finally {
            setBusy(false)
          }
        }}
      >
        <Field label="Tên giải đấu *">
          <input
            required
            value={name}
            onChange={(event) => setName(event.target.value)}
            disabled={busy}
          />
        </Field>
        <Field label="Loại giải đấu *">
          <select
            value={type}
            onChange={(event) =>
              setType(event.target.value as 'LEAGUE' | 'CUP')
            }
            disabled={busy}
          >
            <option value="LEAGUE">Vô địch quốc gia / League</option>
            <option value="CUP">Đấu cúp / Cup</option>
          </select>
        </Field>
        <Field
          label="Logo"
          hint="Nhập giá trị logo theo nguồn dữ liệu. Chưa hỗ trợ tải ảnh lên."
        >
          <input
            value={logo}
            onChange={(event) => setLogo(event.target.value)}
            disabled={busy}
          />
        </Field>
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
          <Button type="submit" disabled={busy || !name.trim()}>
            {busy ? 'Đang lưu…' : 'Lưu giải đấu'}
          </Button>
        </div>
      </form>
    </Modal>
  )
}
