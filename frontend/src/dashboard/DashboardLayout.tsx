import { useState } from 'react'
import {
  ArrowUpRight,
  CalendarDays,
  Layers,
  LayoutDashboard,
  Moon,
  Sun,
  Trophy,
  Users,
} from 'lucide-react'
import { Link, NavLink, Outlet } from 'react-router-dom'
import '../template-ui/template-ui.css'
import './dashboard.css'
const entries = [
  {
    path: '/dashboard/teams',
    label: 'Đội bóng',
    description: 'Quản lý câu lạc bộ và đội tuyển quốc gia.',
    icon: Users,
  },
  {
    path: '/dashboard/competitions',
    label: 'Giải đấu',
    description: 'Quản lý thông tin và loại hình giải đấu.',
    icon: Trophy,
  },
  {
    path: '/dashboard/seasons',
    label: 'Mùa giải',
    description: 'Quản lý danh mục mùa giải dùng chung.',
    icon: CalendarDays,
  },
  {
    path: '/dashboard/competition-seasons',
    label: 'Giải đấu theo mùa',
    description: 'Kết nối giải đấu với các mùa giải.',
    icon: Layers,
  },
]
export default function DashboardLayout() {
  const [dark, setDark] = useState(() => {
    try {
      return localStorage.getItem('football-ui-theme') === 'dark'
    } catch {
      return false
    }
  })
  return (
    <div className="ui-template admin" data-theme={dark ? 'dark' : 'light'}>
      <aside className="sidebar">
        <Link to="/dashboard" className="brand">
          <span className="brand-mark">
            <Trophy size={21} />
          </span>
          Football AI
        </Link>
        <span className="sidebar-caption">QUẢN LÝ DỮ LIỆU</span>
        <nav aria-label="Điều hướng quản trị">
          <NavLink to="/dashboard" end>
            <LayoutDashboard size={19} />
            Tổng quan
          </NavLink>
          {entries.map(({ path, label, icon: Icon }) => (
            <NavLink to={path} key={path}>
              <Icon size={19} />
              {label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <div className="workspace-note">
            <Layers size={22} />
            <strong>Football Data Foundation</strong>
            <p>
              Dữ liệu có cấu trúc.
              <br />
              Kết nối và tái sử dụng.
            </p>
            <Link to="/template-ui">Xem mẫu thiết kế ↗</Link>
          </div>
        </div>
      </aside>
      <main>
        <header className="topbar">
          <span className="breadcrumb">
            Football AI <span>/</span>
            <strong>Quản trị dữ liệu</strong>
          </span>
          <div className="topbar-actions">
            <span className="preview-label">Football Data Workspace</span>
            <button
              className="icon-button"
              aria-label={
                dark
                  ? 'Chuyển sang giao diện sáng'
                  : 'Chuyển sang giao diện tối'
              }
              onClick={() =>
                setDark((value) => {
                  try {
                    localStorage.setItem(
                      'football-ui-theme',
                      value ? 'light' : 'dark',
                    )
                  } catch {
                    /* Storage is optional. */
                  }
                  return !value
                })
              }
            >
              {dark ? <Sun size={19} /> : <Moon size={19} />}
            </button>
          </div>
        </header>
        <div className="page-content">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
export function DashboardHome() {
  return (
    <>
      <div className="page-heading">
        <div>
          <span className="eyebrow">FOOTBALL DATA / WORKSPACE</span>
          <h1>
            Không gian quản trị<span className="heading-dot">.</span>
          </h1>
          <p>Quản lý danh mục bóng đá trên cùng một nền tảng.</p>
        </div>
      </div>
      <section className="dashboard-intro">
        <span className="eyebrow">COLLECT ONCE. REUSE EVERYWHERE.</span>
        <h2>
          Nền tảng dữ liệu bắt đầu
          <br />
          từ những kết nối đúng.
        </h2>
        <p>
          Giải đấu và mùa giải là các danh mục độc lập.
          <br />
          Liên kết chúng để tổ chức từng giải đấu theo mùa.
        </p>
      </section>
      <div className="management-links">
        {entries.map(({ path, label, description, icon: Icon }, index) => (
          <Link className="ui-card management-link" key={path} to={path}>
            <span className="metric-icon">
              <Icon size={23} />
            </span>
            <span className="eyebrow">0{index + 1} / DANH MỤC</span>
            <h2>{label}</h2>
            <p>{description}</p>
            <span className="text-link">
              Mở quản lý <ArrowUpRight size={17} />
            </span>
          </Link>
        ))}
      </div>
    </>
  )
}
