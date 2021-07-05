export function formatDuration(seconds: number): string {
  const s = seconds % 60
  const m = Math.floor(seconds / 60) % 60
  const h = Math.floor(seconds / 60 / 60)

  let time = `${m.toString().padStart(1, '0')}:${s.toString().padStart(2, '0')}`
  if (h > 0) {
    time = `${h}:${time}`
  }
  return time
}
