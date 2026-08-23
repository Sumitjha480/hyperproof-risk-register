import type {
  ApiErrorBody,
  Mitigation,
  MitigationPayload,
  RiskCategory,
  RiskDetail,
  RiskPayload,
  RiskStatus,
  RiskSummary,
  SortDirection,
} from '../types/risk'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? '/api').replace(/\/$/, '')
const API_TIMEOUT_MS = 2_000

export class ApiClientError extends Error {
  readonly status: number
  readonly code?: string
  readonly fieldErrors: Record<string, string>

  constructor(status: number, body: ApiErrorBody) {
    super(body.message ?? `Request failed with status ${status}`)
    this.name = 'ApiClientError'
    this.status = status
    this.code = body.code
    this.fieldErrors = body.fieldErrors ?? {}
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const controller = new AbortController()
  const timeoutId = window.setTimeout(() => controller.abort(), API_TIMEOUT_MS)

  try {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      signal: controller.signal,
      headers: {
        'Content-Type': 'application/json',
        ...init?.headers,
      },
    })

    if (!response.ok) {
      let body: ApiErrorBody = {}
      try {
        body = (await response.json()) as ApiErrorBody
      } catch {
        body = { message: response.statusText }
      }
      throw new ApiClientError(response.status, body)
    }

    if (response.status === 204) {
      return undefined as T
    }

    return (await response.json()) as T
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') {
      throw new ApiClientError(0, {
        code: 'REQUEST_TIMEOUT',
        message: 'The API request timed out. Please check that the API is running and try again.',
      })
    }

    throw error
  } finally {
    window.clearTimeout(timeoutId)
  }
}

export const riskApi = {
  list(params: {
    category?: RiskCategory
    status?: RiskStatus
    sortDirection: SortDirection
  }): Promise<RiskSummary[]> {
    const query = new URLSearchParams()
    if (params.category) query.set('category', params.category)
    if (params.status) query.set('status', params.status)
    query.set('sort', `residualScore,${params.sortDirection}`)
    return request<RiskSummary[]>(`/risks?${query.toString()}`)
  },

  get(id: string): Promise<RiskDetail> {
    return request<RiskDetail>(`/risks/${id}`)
  },

  create(payload: RiskPayload): Promise<RiskDetail> {
    return request<RiskDetail>('/risks', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },

  update(id: string, payload: RiskPayload): Promise<RiskDetail> {
    return request<RiskDetail>(`/risks/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    })
  },

  delete(id: string): Promise<void> {
    return request<void>(`/risks/${id}`, { method: 'DELETE' })
  },

  addMitigation(riskId: string, payload: MitigationPayload): Promise<Mitigation> {
    return request<Mitigation>(`/risks/${riskId}/mitigations`, {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },

  updateMitigation(
    riskId: string,
    mitigationId: string,
    payload: MitigationPayload,
  ): Promise<Mitigation> {
    return request<Mitigation>(`/risks/${riskId}/mitigations/${mitigationId}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    })
  },

  deleteMitigation(riskId: string, mitigationId: string): Promise<void> {
    return request<void>(`/risks/${riskId}/mitigations/${mitigationId}`, {
      method: 'DELETE',
    })
  },
}
