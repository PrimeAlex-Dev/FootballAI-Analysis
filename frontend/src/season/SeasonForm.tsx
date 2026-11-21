import { useState } from 'react'
import { seasonApi, type Season } from './api'
import { Button, Field, Modal, ErrorNotice } from '../shared/ui'
import { errorMessage } from '../shared/api'
export default function SeasonForm({
  value,
  onClose,
  onSuccess,
}: {
  value?: Season
  onClose: () => void
  onSuccess: () => void
}) {
  const [name, setName] = useState(value?.name || '')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const dirty = name !== (value?.name || '')
  return (
    <Modal
      title={value ? 'Chỉnh sửa mùa giải' : 'Thêm mùa giải'}
      onClose={onClose}
      busy={busy}
      dirty={dirty}
    >
      <form
        className="dialog-body"
        onSubmit={async (event) => {
          event.preventDefault()
          if (busy) return
          const clean = name.trim()
          if (
            !/^\d{4}(\/\d{4})?$/.test(clean) ||
            (clean.length === 9 &&
              Number(clean.slice(5)) !== Number(clean.slice(0, 4)) + 1)
          ) {
            setError('Tên phải là YYYY hoặc YYYY/YYYY với hai năm liên tiếp.')
            return
          }
          setBusy(true)
          setError('')
          try {
            if (value) await seasonApi.update(value.id, { name: clean })
            else await seasonApi.create({ name: clean })
            onSuccess()
          } catch (reason) {
            setError(errorMessage(reason))
          } finally {
            setBusy(false)
          }
        }}
      >
        <Field
          label="Tên mùa giải *"
          hint="Ví dụ: 2026 hoặc 2026/2027. Mùa giải đã được sử dụng không thể đổi tên."
        >
          <input
            required
            value={name}
            onChange={(event) => setName(event.target.value)}
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
            {busy ? 'Đang lưu…' : 'Lưu mùa giải'}
          </Button>
        </div>
      </form>
    </Modal>
  )
}
