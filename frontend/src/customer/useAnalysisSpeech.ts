import { useCallback, useEffect, useRef, useState } from 'react'

type SpeechStatus = 'idle' | 'playing' | 'paused' | 'stopped' | 'finished'
export type SpeechRate = 1 | 1.25 | 1.5

const MAX_CHARS = 220
const scoreWords = [
  'không',
  'một',
  'hai',
  'ba',
  'bốn',
  'năm',
  'sáu',
  'bảy',
  'tám',
  'chín',
]

function readingText(content: string) {
  return content
    .replace(/\bxG\b/gi, 'bàn thắng kỳ vọng')
    .replace(/\bTài\s+2[.,]5\b/giu, 'Tài hai phẩy năm')
    .replace(
      /\b(\d)\s*[-–:]\s*(\d)\b/gu,
      (_, home: string, away: string) =>
        `${scoreWords[Number(home)]} bàn so với ${scoreWords[Number(away)]}`,
    )
}

function splitForSpeech(content: string) {
  const chunks: string[] = []
  const sentences = readingText(content).split(/(?<=[.!?。！？])\s+|\n+/u)
  let current = ''
  for (const sentence of sentences) {
    for (const word of sentence.trim().split(/\s+/u)) {
      if (!word) continue
      if (current && current.length + word.length + 1 > MAX_CHARS) {
        chunks.push(current)
        current = ''
      }
      current = current ? `${current} ${word}` : word
    }
    if (current) {
      chunks.push(current)
      current = ''
    }
  }
  return chunks
}

export function useAnalysisSpeech(content: string) {
  const [status, setStatus] = useState<SpeechStatus>('idle')
  const [rate, setRate] = useState<SpeechRate>(1)
  const [voice, setVoice] = useState<SpeechSynthesisVoice | null>(null)
  const [voiceLoading, setVoiceLoading] = useState(true)
  const [error, setError] = useState('')
  const session = useRef(0)
  const chunks = useRef<string[]>([])
  const index = useRef(0)
  const selectedVoice = useRef<SpeechSynthesisVoice | null>(null)
  const selectedRate = useRef<SpeechRate>(1)
  const supported =
    typeof window !== 'undefined' &&
    'speechSynthesis' in window &&
    'SpeechSynthesisUtterance' in window

  useEffect(() => {
    if (!supported) {
      return
    }
    const synth = window.speechSynthesis
    const refreshVoices = () => {
      const available = synth.getVoices()
      setVoice(available.find((item) => /^vi(?:-|$)/i.test(item.lang)) ?? null)
      if (available.length > 0) setVoiceLoading(false)
    }
    refreshVoices()
    synth.addEventListener('voiceschanged', refreshVoices)
    const timeout = window.setTimeout(() => setVoiceLoading(false), 2000)
    const cancelOnLeave = () => {
      session.current += 1
      synth.cancel()
      setStatus('stopped')
    }
    window.addEventListener('pagehide', cancelOnLeave)
    return () => {
      session.current += 1
      synth.cancel()
      synth.removeEventListener('voiceschanged', refreshVoices)
      window.removeEventListener('pagehide', cancelOnLeave)
      window.clearTimeout(timeout)
    }
  }, [supported])

  const stop = useCallback(() => {
    session.current += 1
    if (supported) window.speechSynthesis.cancel()
    setStatus('stopped')
  }, [supported])

  const speakNext = useCallback(function speakNext(token: number) {
    if (token !== session.current) return
    const text = chunks.current[index.current]
    if (!text) {
      setStatus('finished')
      return
    }
    const utterance = new SpeechSynthesisUtterance(text)
    utterance.lang = 'vi-VN'
    utterance.voice = selectedVoice.current
    utterance.rate = selectedRate.current
    utterance.onstart = () => {
      if (token === session.current) setStatus('playing')
    }
    utterance.onend = () => {
      if (token !== session.current) return
      index.current += 1
      speakNext(token)
    }
    utterance.onerror = (event) => {
      if (token !== session.current) return
      setStatus('stopped')
      setError(
        `Không thể tiếp tục đọc nhận định (${event.error}). Vui lòng thử lại.`,
      )
    }
    window.speechSynthesis.speak(utterance)
  }, [])

  function play() {
    if (!supported || !voice) return
    const parts = splitForSpeech(content)
    if (!parts.length) return
    session.current += 1
    window.speechSynthesis.cancel()
    window.speechSynthesis.resume()
    selectedVoice.current = voice
    selectedRate.current = rate
    chunks.current = parts
    index.current = 0
    setError('')
    setStatus('playing')
    speakNext(session.current)
  }

  function pause() {
    if (!supported || status !== 'playing') return
    window.speechSynthesis.pause()
    setStatus('paused')
  }

  function resume() {
    if (!supported || status !== 'paused') return
    window.speechSynthesis.resume()
    setStatus('playing')
  }

  function changeRate(nextRate: SpeechRate) {
    selectedRate.current = nextRate
    setRate(nextRate)
  }

  return {
    status,
    rate,
    voiceLoading,
    voice,
    supported,
    error,
    play,
    pause,
    resume,
    stop,
    changeRate,
  }
}
