import { useState } from 'react'
export default function CompetitionLogo({
  name,
  logo,
}: {
  name: string
  logo: string | null
}) {
  const [failed, setFailed] = useState(false)
  const safe = !!logo && /^(https?:\/\/|\/[^/])/.test(logo)
  return safe && !failed ? (
    <img
      className="competition-logo"
      src={logo}
      alt=""
      referrerPolicy="no-referrer"
      onError={() => setFailed(true)}
    />
  ) : (
    <span className="logo-fallback">{name.slice(0, 2).toUpperCase()}</span>
  )
}
