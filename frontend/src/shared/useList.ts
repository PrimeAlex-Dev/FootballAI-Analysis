import { useCallback, useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { errorMessage, type Page } from './api'

export function useList<T>(
  load: (params: string, signal?: AbortSignal) => Promise<Page<T>>,
) {
  const [params, setParams] = useSearchParams()
  const [data, setData] = useState<Page<T>>()
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [revision, setRevision] = useState(0)
  const pageValue = Number(params.get('page') || 0)
  const sizeValue = Number(params.get('size') || 20)
  const page = Number.isInteger(pageValue) && pageValue >= 0 ? pageValue : 0
  const size = [10, 20, 50, 100].includes(sizeValue) ? sizeValue : 20
  const normalized = new URLSearchParams(params)
  normalized.set('page', String(page))
  normalized.set('size', String(size))
  const key = normalized.toString()
  useEffect(() => {
    const controller = new AbortController()
    queueMicrotask(() => {
      if (!controller.signal.aborted) {
        setLoading(true)
        setError('')
      }
    })
    load(key, controller.signal)
      .then((result) => {
        if (controller.signal.aborted) return
        if (page > 0 && page >= result.page.totalPages) {
          const next = new URLSearchParams(key)
          next.set('page', String(Math.max(0, result.page.totalPages - 1)))
          setParams(next, { replace: true })
        } else setData(result)
      })
      .catch((reason) => {
        if (!controller.signal.aborted) setError(errorMessage(reason))
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false)
      })
    return () => controller.abort()
  }, [key, revision, load, page, setParams])
  function change(values: Record<string, string>) {
    const next = new URLSearchParams(params)
    next.set('page', '0')
    Object.entries(values).forEach(([key, value]) => {
      if (value) next.set(key, value)
      else next.delete(key)
    })
    setParams(next)
  }
  const refresh = useCallback(() => setRevision((value) => value + 1), [])
  return {
    params,
    data,
    loading,
    error,
    page,
    size,
    change,
    refresh,
    go: (value: number) => {
      const next = new URLSearchParams(params)
      next.set('page', String(value))
      setParams(next)
    },
  }
}
