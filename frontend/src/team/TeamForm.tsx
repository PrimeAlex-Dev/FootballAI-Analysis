import { useState } from 'react'
import { teamApi, type Team } from './api'
import { Button, Field, Modal, ErrorNotice } from '../shared/ui'
import { errorMessage } from '../shared/api'
export default function TeamForm({
  value,
  onClose,
  onSuccess,
}: {
  value?: Team
  onClose: () => void
  onSuccess: () => void
}) {
  const [name, setName] = useState(value?.name || '')
  const [type, setType] = useState<'NATIONAL' | 'CLUB'>(
    value?.type || 'NATIONAL',
  )
  const [logo, setLogo] = useState(value?.logo || '')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const dirty =
    name !== (value?.name || '') ||
    type !== (value?.type || 'NATIONAL') ||
    logo !== (value?.logo || '')
  return (
    <Modal
      title={value ? 'Chỉnh sửa đội bóng' : 'Thêm đội bóng'}
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
            if (value) await teamApi.update(value.id, input)
            else await teamApi.create(input)
            onSuccess()
          } catch (reason) {
            setError(errorMessage(reason))
          } finally {
            setBusy(false)
          }
        }}
      >
        <Field label="Tên đội bóng *">
          <input
            required
            value={name}
            onChange={(event) => setName(event.target.value)}
            disabled={busy}
          />
        </Field>
        <Field label="Loại đội bóng *">
          <select
            value={type}
            onChange={(event) =>
              setType(event.target.value as 'NATIONAL' | 'CLUB')
            }
            disabled={busy}
          >
            <option value="NATIONAL">Đội tuyển quốc gia</option>
            <option value="CLUB">Câu lạc bộ</option>
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
            {busy ? 'Đang lưu…' : 'Lưu đội bóng'}
          </Button>
        </div>
      </form>
    </Modal>
  )
}
