import { useEffect, useState } from 'react'
import { RotateCcw } from 'lucide-react'
import { teamApi } from '../team/api'
import { competitionSeasonApi } from '../competition-season/api'
import { Button, ErrorNotice, Field } from '../shared/ui'
import { errorMessage } from '../shared/api'
interface Option {
  id: number
  name: string
}
export default function MatchCatalogSelect({
  kind,
  label,
  value,
  selectedName,
  onChange,
  disabled = false,
}: {
  kind: 'team' | 'competition-season'
  label: string
  value: string
  selectedName?: string
  onChange: (id: string) => void
  disabled?: boolean
}) {
  const [selected, setSelected] = useState<Option | undefined>(() =>
    value && selectedName
      ? { id: Number(value), name: selectedName }
      : undefined,
  )
  const [items, setItems] = useState<Option[]>([])
  const [page, setPage] = useState(0)
  const [pages, setPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [search, setSearch] = useState('')
  const [name, setName] = useState('')
  const [revision, setRevision] = useState(0)
  function applySearch() {
    setPage(0)
    setName(search.trim())
    setRevision((value) => value + 1)
  }
  function resetSearch() {
    setSearch('')
    setName('')
    setPage(0)
    setRevision((value) => value + 1)
  }
  useEffect(() => {
    const controller = new AbortController()
    queueMicrotask(() => {
      if (!controller.signal.aborted) {
        setLoading(true)
        setError('')
      }
    })
    const params = new URLSearchParams({ page: String(page), size: '50' })
    if (kind === 'team' && name) params.set('name', name)
    const load =
      kind === 'team'
        ? teamApi.list(params.toString(), controller.signal).then((result) => ({
            content: result.content.map((item) => ({
              id: item.id,
              name: item.name,
            })),
            page: result.page,
          }))
        : competitionSeasonApi
            .list(params.toString(), controller.signal)
            .then((result) => ({
              content: result.content.map((item) => ({
                id: item.id,
                name: item.displayName,
              })),
              page: result.page,
            }))
    load
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
        if (!controller.signal.aborted) setLoading(false)
      })
    return () => controller.abort()
  }, [kind, page, name, revision])
  return (
    <div className="catalog-select match-catalog-select">
      {kind === 'team' && (
        <div className="match-picker-search">
          <input
            aria-label={`Tìm ${label.toLowerCase()}`}
            value={search}
            disabled={disabled}
            placeholder="Tìm tên đội…"
            onChange={(event) => setSearch(event.target.value)}
            onKeyDown={(event) => {
              if (event.key === 'Enter') {
                event.preventDefault()
                if (!disabled && !loading) applySearch()
              }
            }}
          />
          <Button
            secondary
            disabled={disabled || loading}
            onClick={applySearch}
          >
            Tìm
          </Button>
          <Button
            secondary
            title="Đặt lại tìm đội"
            aria-label={`Đặt lại tìm ${label.toLowerCase()}`}
            disabled={disabled || (!search && !name)}
            onClick={resetSearch}
          >
            <RotateCcw size={16} />
          </Button>
        </div>
      )}
      <Field label={label}>
        <select
          value={value}
          disabled={disabled}
          onChange={(event) => {
            setSelected(
              items.find((item) => String(item.id) === event.target.value),
            )
            onChange(event.target.value)
          }}
        >
          <option value="">Chọn {label.toLowerCase()}</option>
          {value && !items.some((item) => String(item.id) === value) && (
            <option value={value}>
              {selected && String(selected.id) === value
                ? selected.name
                : `ID #${value}`}
            </option>
          )}
          {items.map((item) => (
            <option key={item.id} value={item.id}>
              {item.name}
            </option>
          ))}
        </select>
      </Field>
      {loading && <small role="status">Đang tải lựa chọn…</small>}
      <ErrorNotice message={error} />
      {error ? (
        <Button
          secondary
          disabled={disabled}
          onClick={() => setRevision((value) => value + 1)}
        >
          Thử lại
        </Button>
      ) : (
        page + 1 < pages && (
          <Button
            secondary
            disabled={disabled || loading}
            onClick={() => setPage((value) => value + 1)}
          >
            Tải thêm lựa chọn
          </Button>
        )
      )}
    </div>
  )
}
