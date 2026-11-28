import { useEffect, useState } from 'react'
import { errorMessage } from '../shared/api'
import { Button, ErrorNotice, Modal } from '../shared/ui'
import { matchApi, type Match, type MatchAnalysisPrompt } from './api'

export default function MatchPromptDialog({
  match,
  onClose,
}: {
  match: Match
  onClose: () => void
}) {
  const [data, setData] = useState<MatchAnalysisPrompt | null>(null)
  const [error, setError] = useState('')
  const [copyMessage, setCopyMessage] = useState('')

  useEffect(() => {
    const controller = new AbortController()
    matchApi
      .exportPrompt(match.id, controller.signal)
      .then(setData)
      .catch((reason) => {
        if (!controller.signal.aborted) setError(errorMessage(reason))
      })
    return () => controller.abort()
  }, [match.id])

  async function copy() {
    if (!data) return
    try {
      await navigator.clipboard.writeText(data.prompt)
      setCopyMessage('Đã sao chép prompt.')
    } catch {
      setCopyMessage('Không thể sao chép. Vui lòng chọn và sao chép văn bản.')
    }
  }

  return (
    <Modal
      title={`Export Prompt · ${match.homeTeamName} – ${match.awayTeamName}`}
      onClose={onClose}
      className="match-prompt-modal"
    >
      <div className="dialog-body match-prompt-dialog">
        {!data && !error && <p role="status">Đang tổng hợp dữ liệu…</p>}
        <ErrorNotice message={error} />
        {data && (
          <>
            <p className="match-prompt-coverage">
              Đội nhà: {data.homeRecentCount}/5 trận,{' '}
              {data.homeMatchesWithStatistics} có thống kê. Đội khách:{' '}
              {data.awayRecentCount}/5 trận, {data.awayMatchesWithStatistics} có
              thống kê.
            </p>
            <textarea
              readOnly
              aria-label="Nội dung prompt phân tích trận đấu"
              value={data.prompt}
            />
            <div className="dialog-actions">
              <span role="status">{copyMessage}</span>
              <Button onClick={() => void copy()}>Copy Prompt</Button>
            </div>
          </>
        )}
      </div>
    </Modal>
  )
}
