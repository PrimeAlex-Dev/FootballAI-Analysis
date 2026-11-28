export interface Page<T> {
  content: T[]
  page: {
    size: number
    number: number
    totalElements: number
    totalPages: number
  }
}
const messages: Record<string, string> = {
  'Match analysis not found': 'Trận đấu chưa có nhận định.',
  'Match analysis already exists':
    'Trận đấu đã có nhận định. Vui lòng tải lại để chỉnh sửa.',
  'Analysis status is required': 'Phải chọn trạng thái nhận định.',
  'Match analysis word limit exceeded':
    'Nội dung nhận định vượt giới hạn số từ.',
  'Published analysis requires both contents':
    'Cần nhập cả phân tích và dự đoán trước khi xuất bản.',
  'Match status is required when updating':
    'Phải chọn trạng thái khi cập nhật trận đấu.',
  'Match not found': 'Trận đấu không còn tồn tại.',
  'Standing not found': 'Chưa có bảng xếp hạng.',
  'Standing already exists': 'Giải đấu theo mùa đã có bảng xếp hạng.',
  'Invalid standing data':
    'JSON bảng xếp hạng hoặc đối chiếu Team không hợp lệ.',
  'Match statistics not found': 'Trận đấu chưa có thống kê.',
  'Match statistics already exists':
    'Trận đấu đã có thống kê. Vui lòng cập nhật.',
  'Invalid match statistics':
    'JSON thống kê có cấu trúc hoặc giá trị không hợp lệ.',
  'Home and away teams must be different':
    'Đội nhà và đội khách phải khác nhau.',
  'Both scores must be provided together':
    'Phải nhập đủ hai tỷ số hoặc để trống cả hai.',
  'Finished match requires both scores':
    'Trận kết thúc bắt buộc có đủ hai tỷ số.',
  'Start time must not be after end time':
    'Thời gian bắt đầu lọc phải trước hoặc bằng thời gian kết thúc.',
  'Team name already exists': 'Tên đội bóng đã tồn tại.',
  'Competition name already exists': 'Tên giải đấu đã tồn tại.',
  'Team has dependent data': 'Đội bóng có dữ liệu phụ thuộc, không thể xóa.',
  'Team not found': 'Đội bóng không còn tồn tại.',
  'Competition is in use': 'Giải đấu đang được sử dụng, không thể xóa.',
  'Competition has dependent data': 'Giải đấu có dữ liệu phụ thuộc.',
  'Season is in use': 'Mùa giải đang được sử dụng, không thể sửa hoặc xóa.',
  'Season has dependent data': 'Mùa giải có dữ liệu phụ thuộc.',
  'Season name already exists': 'Tên mùa giải đã tồn tại.',
  'Competition season already exists':
    'Liên kết giải đấu và mùa giải đã tồn tại.',
  'Competition season has dependent data':
    'Liên kết có dữ liệu phụ thuộc, không thể xóa.',
  'Invalid request data': 'Dữ liệu không hợp lệ. Vui lòng kiểm tra các trường.',
}
export async function request<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  let response: Response
  try {
    response = await fetch(`/api${path}`, {
      ...options,
      headers: { 'Content-Type': 'application/json', ...options.headers },
    })
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError')
      throw error
    throw new Error('Không thể kết nối máy chủ. Vui lòng thử lại.')
  }
  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    const detail = problem?.detail as string | undefined
    throw new Error(
      (detail && messages[detail]) ||
        (response.status === 404 ? 'Bản ghi không còn tồn tại.' : detail) ||
        `Yêu cầu thất bại (${response.status}).`,
    )
  }
  return response.status === 204 ? (undefined as T) : response.json()
}
export function query(values: Record<string, string | number>) {
  const params = new URLSearchParams()
  Object.entries(values).forEach(([key, value]) => {
    if (value !== '') params.set(key, String(value))
  })
  return params.toString()
}
export const errorMessage = (error: unknown) =>
  error instanceof Error ? error.message : 'Đã có lỗi xảy ra.'
