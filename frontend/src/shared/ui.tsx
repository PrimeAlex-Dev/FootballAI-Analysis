import { useEffect, useRef, useState, type ReactNode } from 'react'
import { Eye, Pencil, Trash2, X } from 'lucide-react'
import { errorMessage } from './api'

export function Button({
  children,
  secondary = false,
  ...props
}: React.ButtonHTMLAttributes<HTMLButtonElement> & { secondary?: boolean }) {
  return (
    <button
      type="button"
      className={secondary ? 'secondary-button' : 'primary-button'}
      {...props}
    >
      {children}
    </button>
  )
}
export function Field({
  label,
  hint,
  children,
}: {
  label: string
  hint?: string
  children: ReactNode
}) {
  return (
    <label className="admin-field">
      <span>{label}</span>
      {children}
      {hint && <small>{hint}</small>}
    </label>
  )
}
export function ErrorNotice({ message }: { message: string }) {
  return message ? (
    <div className="error-notice" role="alert">
      {message}
    </div>
  ) : null
}
export function Modal({
  title,
  children,
  onClose,
  busy = false,
  dirty = false,
}: {
  title: string
  children: ReactNode
  onClose: () => void
  busy?: boolean
  dirty?: boolean
}) {
  const ref = useRef<HTMLDialogElement>(null)
  const [confirm, setConfirm] = useState(false)
  const close = () => {
    if (!busy) {
      if (dirty) setConfirm(true)
      else onClose()
    }
  }
  useEffect(() => {
    const previous = document.activeElement as HTMLElement | null
    const dialog = ref.current!
    dialog.showModal()
    return () => {
      dialog.close()
      previous?.focus()
    }
  }, [])
  return (
    <dialog
      ref={ref}
      className="admin-dialog"
      aria-labelledby="dialog-title"
      onCancel={(event) => {
        event.preventDefault()
        close()
      }}
    >
      <div className="dialog-heading">
        <h2 id="dialog-title">{title}</h2>
        <button
          type="button"
          className="icon-button"
          aria-label="Đóng"
          disabled={busy}
          onClick={close}
        >
          <X size={18} />
        </button>
      </div>
      {confirm ? (
        <div className="dialog-body">
          <p>Bạn có thay đổi chưa lưu. Bỏ các thay đổi này?</p>
          <div className="dialog-actions">
            <Button secondary onClick={() => setConfirm(false)}>
              Tiếp tục chỉnh sửa
            </Button>
            <Button onClick={onClose}>Bỏ thay đổi</Button>
          </div>
        </div>
      ) : (
        children
      )}
    </dialog>
  )
}
export function RowActions({
  view,
  edit,
  remove,
}: {
  view: () => void
  edit?: () => void
  remove: () => void
}) {
  return (
    <div className="row-actions">
      <button title="Chi tiết" aria-label="Chi tiết" onClick={view}>
        <Eye size={17} />
      </button>
      {edit && (
        <button title="Chỉnh sửa" aria-label="Chỉnh sửa" onClick={edit}>
          <Pencil size={17} />
        </button>
      )}
      <button
        className="danger-text"
        title="Xóa"
        aria-label="Xóa"
        onClick={remove}
      >
        <Trash2 size={17} />
      </button>
    </div>
  )
}
export function PageHeading({
  title,
  description,
  createLabel,
  onCreate,
}: {
  title: string
  description: string
  createLabel: string
  onCreate: () => void
}) {
  return (
    <div className="page-heading">
      <div>
        <span className="eyebrow">FOOTBALL DATA / MANAGEMENT</span>
        <h1>
          {title}
          <span className="heading-dot">.</span>
        </h1>
        <p>{description}</p>
      </div>
      <Button onClick={onCreate}>+ {createLabel}</Button>
    </div>
  )
}
export function ListTable({
  headers,
  children,
  loading,
  error,
  empty,
  retry,
}: {
  headers: string[]
  children: ReactNode
  loading: boolean
  error: string
  empty: boolean
  retry: () => void
}) {
  return (
    <div className="table-scroll">
      <table>
        <thead>
          <tr>
            {headers.map((value) => (
              <th key={value} scope="col">
                {value}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {loading || error || empty ? (
            <tr>
              <td colSpan={headers.length}>
                <div className="table-state" role="status">
                  {loading ? (
                    'Đang tải dữ liệu…'
                  ) : error ? (
                    <>
                      <ErrorNotice message={error} />
                      <Button secondary onClick={retry}>
                        Thử lại
                      </Button>
                    </>
                  ) : (
                    'Không có dữ liệu phù hợp.'
                  )}
                </div>
              </td>
            </tr>
          ) : (
            children
          )}
        </tbody>
      </table>
    </div>
  )
}
export function Pagination({
  page,
  size,
  total,
  pages,
  disabled,
  changeSize,
  go,
}: {
  page: number
  size: number
  total: number
  pages: number
  disabled: boolean
  changeSize: (size: string) => void
  go: (page: number) => void
}) {
  return (
    <div className="pagination">
      <span>{total} bản ghi</span>
      <label>
        Số dòng{' '}
        <select
          value={size}
          onChange={(event) => changeSize(event.target.value)}
          disabled={disabled}
        >
          {[10, 20, 50, 100].map((value) => (
            <option key={value}>{value}</option>
          ))}
        </select>
      </label>
      <div>
        <Button
          secondary
          disabled={disabled || page === 0}
          onClick={() => go(page - 1)}
        >
          Trước
        </Button>
        <span>
          Trang {pages ? page + 1 : 0} / {pages}
        </span>
        <Button
          secondary
          disabled={disabled || page + 1 >= pages}
          onClick={() => go(page + 1)}
        >
          Sau
        </Button>
      </div>
    </div>
  )
}
export function DeleteDialog({
  name,
  remove,
  onClose,
  onSuccess,
}: {
  name: string
  remove: () => Promise<void>
  onClose: () => void
  onSuccess: () => void
}) {
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  async function submit() {
    setBusy(true)
    setError('')
    try {
      await remove()
      onSuccess()
    } catch (reason) {
      setError(errorMessage(reason))
    } finally {
      setBusy(false)
    }
  }
  return (
    <Modal title="Xác nhận xóa" onClose={onClose} busy={busy}>
      <div className="dialog-body">
        <p>
          Bạn muốn xóa <strong>{name}</strong>? Thao tác này không thể hoàn tác.
        </p>
        <ErrorNotice message={error} />
        <div className="dialog-actions">
          <Button secondary disabled={busy} onClick={onClose}>
            Hủy
          </Button>
          <Button className="danger-button" disabled={busy} onClick={submit}>
            {busy ? 'Đang xóa…' : 'Xóa bản ghi'}
          </Button>
        </div>
      </div>
    </Modal>
  )
}
export function DetailDialog<T>({
  load,
  render,
  onClose,
}: {
  load: () => Promise<T>
  render: (value: T) => ReactNode
  onClose: () => void
}) {
  const [data, setData] = useState<T>()
  const [error, setError] = useState('')
  const [retry, setRetry] = useState(0)
  useEffect(() => {
    let active = true
    load()
      .then((value) => {
        if (active) setData(value)
      })
      .catch((reason) => {
        if (active) setError(errorMessage(reason))
      })
    return () => {
      active = false
    }
  }, [load, retry])
  return (
    <Modal title="Chi tiết bản ghi" onClose={onClose}>
      <div className="dialog-body">
        {error ? (
          <>
            <ErrorNotice message={error} />
            <Button
              secondary
              onClick={() => {
                setError('')
                setRetry((value) => value + 1)
              }}
            >
              Thử lại
            </Button>
          </>
        ) : data ? (
          <dl className="detail-list">{render(data)}</dl>
        ) : (
          <p role="status">Đang tải chi tiết…</p>
        )}
      </div>
    </Modal>
  )
}
