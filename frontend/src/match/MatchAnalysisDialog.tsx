import { useEffect, useState } from 'react'
import { Button, ErrorNotice, Field, Modal } from '../shared/ui'
import { errorMessage } from '../shared/api'
import { matchAnalysisApi, type Match, type MatchAnalysisStatus } from './api'

const countWords = (value: string) => value.match(/\S+/gu)?.length ?? 0

export default function MatchAnalysisDialog({
  match,
  onClose,
}: {
  match: Match
  onClose: () => void
}) {
  const [analysisContent, setAnalysisContent] = useState('')
  const [predictionContent, setPredictionContent] = useState('')
  const [status, setStatus] = useState<MatchAnalysisStatus>('DRAFT')
  const [exists, setExists] = useState(false)
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [dirty, setDirty] = useState(false)
  const [error, setError] = useState('')
  const [loadFailed, setLoadFailed] = useState(false)
  const [notice, setNotice] = useState('')
  const [confirmDelete, setConfirmDelete] = useState(false)

  useEffect(() => {
    let active = true
    matchAnalysisApi
      .get(match.id)
      .then((data) => {
        if (!active) return
        setAnalysisContent(data.analysisContent)
        setPredictionContent(data.predictionContent)
        setStatus(data.status)
        setExists(true)
      })
      .catch((reason: unknown) => {
        if (active && errorMessage(reason) !== 'Trận đấu chưa có nhận định.') {
          setError(errorMessage(reason))
          setLoadFailed(true)
        }
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [match.id])

  async function save(nextStatus: MatchAnalysisStatus) {
    const analysisWords = countWords(analysisContent)
    const predictionWords = countWords(predictionContent)
    if (analysisWords > 20000 || predictionWords > 2000) {
      setError('Nội dung nhận định vượt giới hạn số từ.')
      return
    }
    if (
      nextStatus === 'PUBLISHED' &&
      (countWords(analysisContent) === 0 || countWords(predictionContent) === 0)
    ) {
      setError('Cần nhập cả phân tích và dự đoán trước khi xuất bản.')
      return
    }
    setBusy(true)
    setError('')
    setNotice('')
    const input = { analysisContent, predictionContent, status: nextStatus }
    try {
      const saved = exists
        ? await matchAnalysisApi.update(match.id, input)
        : await matchAnalysisApi.create(match.id, input)
      setAnalysisContent(saved.analysisContent)
      setPredictionContent(saved.predictionContent)
      setStatus(saved.status)
      setExists(true)
      setDirty(false)
      setNotice(
        nextStatus === 'DRAFT' ? 'Đã lưu bản nháp.' : 'Đã xuất bản nhận định.',
      )
    } catch (reason) {
      setError(errorMessage(reason))
    } finally {
      setBusy(false)
    }
  }

  async function remove() {
    setBusy(true)
    setError('')
    try {
      await matchAnalysisApi.remove(match.id)
      setExists(false)
      setAnalysisContent('')
      setPredictionContent('')
      setStatus('DRAFT')
      setDirty(false)
      setConfirmDelete(false)
      setNotice('Đã xóa nhận định.')
    } catch (reason) {
      setError(errorMessage(reason))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Modal
      title={`Nhận định AI · ${match.homeTeamName} – ${match.awayTeamName}`}
      onClose={onClose}
      busy={busy}
      dirty={dirty}
      className="match-analysis-modal"
    >
      <div className="dialog-body match-analysis-dialog">
        {loading ? (
          <p>Đang tải nhận định…</p>
        ) : loadFailed ? (
          <ErrorNotice message={error} />
        ) : (
          <>
            <p className="muted">
              Trạng thái:{' '}
              {exists
                ? status === 'DRAFT'
                  ? 'Bản nháp'
                  : 'Đã xuất bản'
                : 'Chưa có nhận định'}
            </p>
            <ErrorNotice message={error} />
            {notice && (
              <p className="success-notice" role="status">
                {notice}
              </p>
            )}
            <Field
              label={`Nội dung phân tích (${countWords(analysisContent)} / 20.000 từ)`}
            >
              <textarea
                value={analysisContent}
                onChange={(event) => {
                  setAnalysisContent(event.target.value)
                  setDirty(true)
                  setNotice('')
                }}
                rows={10}
                disabled={busy}
              />
            </Field>
            <Field
              label={`Dự đoán AI (${countWords(predictionContent)} / 2.000 từ)`}
            >
              <textarea
                value={predictionContent}
                onChange={(event) => {
                  setPredictionContent(event.target.value)
                  setDirty(true)
                  setNotice('')
                }}
                rows={6}
                disabled={busy}
              />
            </Field>
            {confirmDelete ? (
              <div className="dialog-actions">
                <span>Xóa hẳn nhận định của trận đấu này?</span>
                <Button
                  secondary
                  onClick={() => setConfirmDelete(false)}
                  disabled={busy}
                >
                  Hủy
                </Button>
                <Button onClick={remove} disabled={busy}>
                  Xác nhận xóa
                </Button>
              </div>
            ) : (
              <div className="dialog-actions">
                {exists && (
                  <Button
                    secondary
                    onClick={() => setConfirmDelete(true)}
                    disabled={busy}
                  >
                    Xóa nhận định
                  </Button>
                )}
                <Button secondary onClick={() => save('DRAFT')} disabled={busy}>
                  Lưu nháp
                </Button>
                <Button onClick={() => save('PUBLISHED')} disabled={busy}>
                  Xuất bản
                </Button>
              </div>
            )}
          </>
        )}
      </div>
    </Modal>
  )
}
