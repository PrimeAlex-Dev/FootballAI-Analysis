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
