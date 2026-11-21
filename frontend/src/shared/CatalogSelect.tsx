import { useEffect, useState } from 'react'
import { competitionApi } from '../competition/api'
import { seasonApi } from '../season/api'
import { errorMessage } from './api'
import { Button, ErrorNotice, Field } from './ui'
interface Option {
  id: number
  name: string
}
export default function CatalogSelect({
  kind,
  value,
  onChange,
  disabled = false,
}: {
  kind: 'competition' | 'season'
  value: string
  onChange: (value: string, name: string) => void
  disabled?: boolean
}) {
  const [items, setItems] = useState<Option[]>([])
  const [page, setPage] = useState(0)
  const [pages, setPages] = useState(1)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [revision, setRevision] = useState(0)
  useEffect(() => {
    const controller = new AbortController()
    queueMicrotask(() => {
      if (!controller.signal.aborted) {
        setBusy(true)
        setError('')
      }
    })
    const api = kind === 'competition' ? competitionApi : seasonApi
    api
      .list(`page=${page}&size=100`, controller.signal)
      .then((result) => {
        if (controller.signal.aborted) return
        setItems((previous) => {
          const all =
            page === 0 ? result.content : [...previous, ...result.content]
          return [...new Map(all.map((item) => [item.id, item])).values()]
        })
        setPages(result.page.totalPages)
      })
      .catch((reason) => {
        if (!controller.signal.aborted) setError(errorMessage(reason))
      })
      .finally(() => {
        if (!controller.signal.aborted) setBusy(false)
      })
    return () => controller.abort()
  }, [kind, page, revision])
  return (
    <div className="catalog-select">
      <Field label={kind === 'competition' ? 'Giải đấu' : 'Mùa giải'}>
        <select
          value={value}
          disabled={disabled}
          onChange={(event) =>
            onChange(
              event.target.value,
              items.find((item) => String(item.id) === event.target.value)
                ?.name || '',
            )
          }
        >
          <option value="">
            Chọn {kind === 'competition' ? 'giải đấu' : 'mùa giải'}
          </option>
          {value && !items.some((item) => String(item.id) === value) && (
            <option value={value}>ID #{value}</option>
          )}
          {items.map((item) => (
            <option key={item.id} value={item.id}>
              {item.name}
            </option>
          ))}
        </select>
      </Field>
      {busy && <small role="status">Đang tải lựa chọn…</small>}
      <ErrorNotice message={error} />
      {error ? (
        <Button secondary onClick={() => setRevision((value) => value + 1)}>
          Thử lại
        </Button>
      ) : (
        page + 1 < pages && (
          <Button
            secondary
            disabled={busy || disabled}
            onClick={() => setPage((value) => value + 1)}
          >
            Tải thêm lựa chọn
          </Button>
        )
      )}
    </div>
  )
}
