import { useState } from 'react'
import {
  ArrowDownToLine,
  ArrowRight,
  Bell,
  CalendarDays,
  ChartNoAxesCombined,
  CircleHelp,
  Layers,
  LayoutDashboard,
  Leaf,
  Moon,
  Search,
  Settings,
  ShoppingCart,
  Sun,
  Users,
  Wallet,
} from 'lucide-react'
import './template-ui.css'

const navigation = [
  LayoutDashboard,
  ChartNoAxesCombined,
  Users,
  Wallet,
  ShoppingCart,
  Settings,
]
const labels = [
  'Tổng quan',
  'Báo cáo',
  'Khách hàng',
  'Doanh thu',
  'Sản phẩm',
  'Cài đặt',
]
const metrics = [
  {
    label: 'Doanh thu',
    value: '428.500.000₫',
    change: '+12,4%',
    icon: ChartNoAxesCombined,
  },
  { label: 'Đơn hàng', value: '1.286', change: '+8,7%', icon: ShoppingCart },
  { label: 'Khách hàng mới', value: '342', change: '+18,1%', icon: Users },
  {
    label: 'Tỷ lệ chuyển đổi',
    value: '3,9%',
    change: '+0,6 điểm %',
    icon: Wallet,
  },
]

function RevenueChart() {
  return (
    <section className="ui-card revenue">
      <div className="card-heading">
        <h2>Doanh thu theo thời gian</h2>
        <span className="muted">Tháng 09, 2026</span>
      </div>
      <div className="chart">
        <div className="chart-labels">
          600Tr
          <br />
          400Tr
          <br />
          200Tr
          <br />0
        </div>
        <svg
          viewBox="0 0 700 180"
          role="img"
          aria-label="Biểu đồ minh họa doanh thu tăng trong bảy ngày"
        >
          <defs>
            <linearGradient id="chart-fill" x1="0" y1="0" x2="0" y2="1">
              <stop stopColor="#99ff33" stopOpacity=".3" />
              <stop offset="1" stopColor="#99ff33" stopOpacity="0" />
            </linearGradient>
          </defs>
          {[25, 70, 115, 160].map((y) => (
            <line key={y} x1="0" x2="700" y1={y} y2={y} className="grid-line" />
          ))}
          <path
            d="M0 130 C45 145 65 90 115 100 S180 125 230 88 S290 45 345 70 S400 110 460 69 S520 48 575 45 S640 65 700 22 L700 180 L0 180Z"
            fill="url(#chart-fill)"
          />
          <path
            d="M0 130 C45 145 65 90 115 100 S180 125 230 88 S290 45 345 70 S400 110 460 69 S520 48 575 45 S640 65 700 22"
            fill="none"
            stroke="#83df20"
            strokeWidth="3"
          />
          <circle cx="695" cy="24" r="5" fill="#99ff33" />
        </svg>
      </div>
      <div className="chart-dates">
        {['16/09', '17/09', '18/09', '19/09', '20/09', '21/09', '22/09'].map(
          (date) => (
            <span key={date}>{date}</span>
          ),
        )}
      </div>
    </section>
  )
}

function OrdersTable() {
  return (
    <section className="ui-card orders">
      <div className="card-heading">
        <h2>Đơn hàng gần đây</h2>
        <span className="text-link">
          Xem tất cả <ArrowRight size={14} />
        </span>
      </div>
      <div className="table-scroll">
        <table>
          <thead>
            <tr>
              {[
                'Mã đơn hàng',
                'Khách hàng',
                'Giá trị',
                'Trạng thái',
                'Thời gian',
              ].map((label) => (
                <th key={label}>{label}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {[
              'Trần Gia Bảo',
              'Lê Minh Thư',
              'Phạm Hoàng Nam',
              'Nguyễn Hà An',
            ].map((name, index) => (
              <tr key={name}>
                <td>#DH-{10429 - index}</td>
                <td>{name}</td>
                <td>
                  {
                    ['2.350.000₫', '1.190.000₫', '3.480.000₫', '850.000₫'][
                      index
                    ]
                  }
                </td>
                <td>
                  <span className={`status ${index === 1 ? 'pending' : ''}`}>
                    {index === 1 ? 'Đang xử lý' : 'Hoàn thành'}
                  </span>
                </td>
                <td className="muted">22/09, 14:30</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  )
}

export default function TemplateUi() {
  const [dark, setDark] = useState(false)
  return (
    <div className="ui-template" data-theme={dark ? 'dark' : 'light'}>
      <aside className="sidebar">
        <a href="/template-ui" className="brand">
          <span className="brand-mark">
            <Leaf />
          </span>
          LeafDesk<span className="brand-dot">.</span>
        </a>
        <span className="sidebar-caption">WORKSPACE</span>
        <nav aria-label="Điều hướng mẫu">
          {navigation.map((Icon, index) => (
            <a
              className={index === 0 ? 'active' : ''}
              href={index === 0 ? '#overview' : '#components'}
              key={labels[index]}
            >
              <Icon size={19} />
              {labels[index]}
              {index === 0 && <span className="nav-count">4</span>}
            </a>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <div className="workspace-note">
            <Layers size={22} />
            <strong>Không gian thiết kế</strong>
            <p>
              Một ngôn ngữ giao diện.
              <br />
              Trải nghiệm nhất quán.
            </p>
            <span>UI TEMPLATE · V1.0</span>
          </div>
          <div className="profile">
            <span className="avatar">MA</span>
            <div>
              <strong>Minh Anh</strong>
              <small>Quản trị viên</small>
            </div>
            <Settings size={17} />
          </div>
        </div>
      </aside>
      <main>
        <header className="topbar">
          <span className="breadcrumb">
            Workspace <span>/</span> <strong>Tổng quan</strong>
          </span>
          <div className="topbar-actions">
            <span className="preview-label">Bản mẫu giao diện</span>
            <button
              className="icon-button"
              onClick={() => setDark(!dark)}
              aria-label={
                dark
                  ? 'Chuyển sang giao diện sáng'
                  : 'Chuyển sang giao diện tối'
              }
            >
              {dark ? <Sun size={19} /> : <Moon size={19} />}
            </button>
            <span className="icon-display">
              <Bell size={19} />
            </span>
            <span className="icon-display">
              <CircleHelp size={19} />
            </span>
          </div>
        </header>
        <div className="page-content">
          <div className="page-heading" id="overview">
            <div>
              <span className="eyebrow">DESIGN SYSTEM / DASHBOARD</span>
              <h1>
                Tổng quan hoạt động<span className="heading-dot">.</span>
              </h1>
              <p>Theo dõi hiệu suất và tình hình kinh doanh của bạn.</p>
            </div>
            <div className="heading-actions">
              <span className="date-control">
                <CalendarDays size={17} />7 ngày qua
              </span>
              <button className="primary-button" disabled>
                <ArrowDownToLine size={17} />
                Xuất báo cáo
              </button>
            </div>
          </div>
          <div className="demo-notice">
            <span className="notice-dot" />
            Trang tham chiếu UI · Toàn bộ dữ liệu dưới đây chỉ dùng minh họa
            thiết kế.
          </div>
          <div className="metrics">
            {metrics.map(({ label, value, change, icon: Icon }) => (
              <section className="ui-card metric" key={label}>
                <div className="metric-label">
                  <span className="metric-icon">
                    <Icon size={19} />
                  </span>
                  {label}
                </div>
                <strong>{value}</strong>
                <p>
                  <span className="positive">↗ {change}</span>
                  <span className="muted"> so với 7 ngày trước</span>
                </p>
              </section>
            ))}
          </div>
          <div className="dashboard-grid">
            <section className="promotion">
              <span className="promotion-icon">
                <Leaf size={34} />
              </span>
              <span className="eyebrow">GROW BETTER, EVERY DAY</span>
              <h2>
                Nâng cấp hiệu suất
                <br />
                kinh doanh của bạn
              </h2>
              <p>
                Khám phá các tính năng nâng cao
                <br />
                để tối ưu tăng trưởng.
              </p>
              <button className="primary-button" disabled>
                Khám phá ngay <ArrowRight size={17} />
              </button>
              <div className="promo-orbit orbit-one" />
              <div className="promo-orbit orbit-two" />
              <span className="promotion-footer">
                THINK SIMPLE. GROW SMART.
              </span>
            </section>
            <div className="data-panels">
              <RevenueChart />
              <OrdersTable />
            </div>
          </div>
          <section className="reference-section" id="components">
            <div className="section-title">
              <div>
                <span className="eyebrow">FOUNDATIONS</span>
                <h2>Ngôn ngữ thiết kế</h2>
              </div>
              <span className="muted">Đơn giản trong từng chi tiết.</span>
            </div>
            <div className="reference-grid">
              <article className="ui-card">
                <h3>01 / Bảng màu</h3>
                <div className="swatches">
                  {['#99FF33', '#F8FAF5', '#111111', '#6B7280'].map((color) => (
                    <div key={color}>
                      <span style={{ background: color }} />
                      <small>{color}</small>
                    </div>
                  ))}
                </div>
                <p className="muted">
                  Xanh làm điểm nhấn. Bề mặt trung tính giữ nội dung dễ đọc.
                </p>
              </article>
              <article className="ui-card">
                <h3>02 / Typography</h3>
                <div className="type-sample">
                  Aa <span>Rõ ràng. Hiện đại.</span>
                </div>
                <p className="muted">
                  System sans · Tiêu đề 28–32 · Nội dung 14–16 · Chú thích
                  12–13.
                </p>
              </article>
              <article className="ui-card">
                <h3>03 / Thành phần</h3>
                <label className="search-field">
                  <Search size={17} />
                  <input
                    placeholder="Tìm kiếm..."
                    aria-label="Ô tìm kiếm minh họa"
                  />
                </label>
                <div className="component-examples">
                  <button className="primary-button" disabled>
                    Nút chính
                  </button>
                  <button className="secondary-button" disabled>
                    Nút phụ
                  </button>
                  <span className="status">Thành công</span>
                </div>
              </article>
            </div>
          </section>
          <footer>
            LeafDesk UI Reference{' '}
            <span>Spacing 8 / 12 / 16 / 24 / 32 · Radius 12 / 16 / 20</span>
          </footer>
        </div>
      </main>
    </div>
  )
}
