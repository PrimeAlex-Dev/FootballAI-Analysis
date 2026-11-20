import TemplateUi from './template-ui/TemplateUi'

export default function App() {
  if (window.location.pathname === '/template-ui') return <TemplateUi />
  return (
    <main className="welcome">
      <h1>Football AI Platform</h1>
      <a href="/template-ui">Mở trang mẫu giao diện →</a>
    </main>
  )
}
