import { useEffect, useState } from 'react'
import {
  PolarAngleAxis,
  PolarGrid,
  PolarRadiusAxis,
  Radar,
  RadarChart,
  ResponsiveContainer,
  Tooltip,
} from 'recharts'
import { getPlayingStyleRadar } from './service'
import type { Match, PlayingStyleRadar, RadarAxis, RadarMetric } from './types'

function formatMetric(metric: RadarMetric) {
  if (metric.average === null) return 'Chưa có dữ liệu'
  return `${Number(metric.average.toFixed(2))}${metric.unit} · ${metric.availableMatches} trận`
}

function AxisDetails({
  axis,
  teamName,
}: {
  axis: RadarAxis
  teamName: string
}) {
  return (
    <div className="radar-axis-detail">
      <strong>
        {teamName}:{' '}
        {axis.score === null
          ? 'Chưa đủ dữ liệu'
          : `${Math.round(axis.score)} điểm`}
      </strong>
      <span>
        {axis.primary.name}: {formatMetric(axis.primary)}
      </span>
      {axis.supporting.map((metric) => (
        <span key={metric.name}>
          {metric.name}: {formatMetric(metric)}
        </span>
      ))}
      <small>Hồ sơ tham chiếu: {axis.referenceProfiles}</small>
    </div>
  )
}

export default function PlayingStyleRadarView({ match }: { match: Match }) {
  const [result, setResult] = useState<PlayingStyleRadar | null>(null)
  const [error, setError] = useState(false)
  const [showHome, setShowHome] = useState(true)
  const [showAway, setShowAway] = useState(true)

  useEffect(() => {
    let active = true
    getPlayingStyleRadar(match.id)
      .then((data) => {
        if (active) setResult(data)
      })
      .catch(() => {
        if (active) setError(true)
      })
    return () => {
      active = false
    }
  }, [match.id])

  if (error)
    return (
      <section className="customer-panel content-card" role="alert">
        Không tải được hồ sơ thi đấu. Vui lòng thử tải lại trang.
      </section>
    )
  if (!result)
    return (
      <section className="customer-panel content-card" role="status">
        Đang tải hồ sơ thi đấu…
      </section>
    )
  if (result.status === 'NOT_APPLICABLE')
    return (
      <section className="customer-panel content-card">
        Radar chỉ áp dụng cho trận sắp diễn ra.
      </section>
    )

  const ready = result.status === 'READY'
  const points = result.home.axes.map((axis, index) => ({
    label: axis.label,
    home: axis.score,
    away: result.away.axes[index]?.score ?? null,
    homeAxis: axis,
    awayAxis: result.away.axes[index],
  }))

  return (
    <section className="customer-panel content-card playing-radar">
      <span className="eyebrow">HỒ SƠ THI ĐẤU GẦN ĐÂY</span>
      <h2>So sánh xu hướng thi đấu</h2>
      <p className="muted">
        Tối đa {result.sampleLimit} trận đã kết thúc gần nhất trước trận này,
        trên mọi giải đấu. Đây là phân vị trong dữ liệu hiện có, không phải điểm
        sức mạnh hoặc dự đoán kết quả.
      </p>
      <div className="radar-legend" aria-label="Chọn đội hiển thị">
        <button
          type="button"
          aria-pressed={showHome}
          onClick={() => setShowHome(!showHome)}
        >
          <i className="home" />
          {result.home.teamName} · {result.home.historyMatches}/5 trận
        </button>
        <button
          type="button"
          aria-pressed={showAway}
          onClick={() => setShowAway(!showAway)}
        >
          <i className="away" />
          {result.away.teamName} · {result.away.historyMatches}/5 trận
        </button>
      </div>
      {ready ? (
        <div
          className="radar-chart"
          role="img"
          aria-label={`Biểu đồ Radar so sánh ${result.home.teamName} và ${result.away.teamName}`}
        >
          <ResponsiveContainer width="100%" height="100%">
            <RadarChart data={points} outerRadius="68%">
              <PolarGrid stroke="var(--border)" />
              <PolarAngleAxis
                dataKey="label"
                tick={{ fill: 'var(--text)', fontSize: 11 }}
              />
              <PolarRadiusAxis
                domain={[0, 100]}
                tickCount={5}
                tick={{ fill: 'var(--muted)', fontSize: 10 }}
              />
              {showHome && (
                <Radar
                  name={result.home.teamName}
                  dataKey="home"
                  stroke="var(--home)"
                  fill="var(--home)"
                  fillOpacity={0.26}
                />
              )}
              {showAway && (
                <Radar
                  name={result.away.teamName}
                  dataKey="away"
                  stroke="var(--away)"
                  fill="var(--away)"
                  fillOpacity={0.25}
                />
              )}
              <Tooltip
                content={({ active, payload }) => {
                  if (!active || !payload?.length) return null
                  const point = payload[0].payload as (typeof points)[number]
                  return (
                    <div className="radar-tooltip">
                      <strong>{point.label}</strong>
                      {showHome && (
                        <AxisDetails
                          axis={point.homeAxis}
                          teamName={result.home.teamName}
                        />
                      )}
                      {showAway && (
                        <AxisDetails
                          axis={point.awayAxis}
                          teamName={result.away.teamName}
                        />
                      )}
                    </div>
                  )
                }}
              />
            </RadarChart>
          </ResponsiveContainer>
        </div>
      ) : (
        <p className="radar-unavailable" role="status">
          {result.status === 'INSUFFICIENT_REFERENCE_DATA'
            ? 'Chưa đủ hồ sơ tham chiếu để vẽ Radar đáng tin cậy.'
            : 'Một hoặc hai đội chưa đủ chỉ số để vẽ Radar đầy đủ.'}{' '}
          Các số liệu hiện có được liệt kê bên dưới.
        </p>
      )}
      <div className="radar-details">
        {points.map((point) => (
          <div className="radar-detail-card" key={point.label}>
            <h3>{point.label}</h3>
            <AxisDetails
              axis={point.homeAxis}
              teamName={result.home.teamName}
            />
            <AxisDetails
              axis={point.awayAxis}
              teamName={result.away.teamName}
            />
          </div>
        ))}
      </div>
      <p className="muted radar-method">
        {result.referenceMethod}. Mỗi trục cần ít nhất 2 trận có chỉ số và 5 hồ
        sơ tham chiếu; đối thủ đã gặp không được điều chỉnh theo sức mạnh.
      </p>
    </section>
  )
}
