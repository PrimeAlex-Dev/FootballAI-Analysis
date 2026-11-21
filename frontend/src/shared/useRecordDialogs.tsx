import { useState, type ReactNode } from 'react'
import { DeleteDialog, DetailDialog } from './ui'
export function useRecordDialogs<T extends { id: number }>(
  api: {
    get: (id: number) => Promise<T>
    remove: (id: number) => Promise<void>
  },
  refresh: () => void,
) {
  const [selection, setSelection] = useState<{
    mode: 'view' | 'edit' | 'delete'
    value: T
  }>()
  const [creating, setCreating] = useState(false)
  const [notice, setNotice] = useState('')
  const close = () => {
    setSelection(undefined)
    setCreating(false)
  }
  const success = () => {
    close()
    setNotice('Đã lưu thay đổi thành công.')
    refresh()
  }
  function dialogs(
    name: (value: T) => string,
    render: (value: T) => ReactNode,
  ) {
    if (selection?.mode === 'delete')
      return (
        <DeleteDialog
          name={name(selection.value)}
          remove={() => api.remove(selection.value.id)}
          onClose={close}
          onSuccess={success}
        />
      )
    if (selection?.mode === 'view')
      return (
        <DetailDialog
          key={selection.value.id}
          load={() => api.get(selection.value.id)}
          render={render}
          onClose={close}
        />
      )
    return null
  }
  return {
    selection,
    creating,
    notice,
    close,
    success,
    dialogs,
    create: () => {
      setNotice('')
      setCreating(true)
    },
    select: (mode: 'view' | 'edit' | 'delete', value: T) => {
      setNotice('')
      setSelection({ mode, value })
    },
  }
}
