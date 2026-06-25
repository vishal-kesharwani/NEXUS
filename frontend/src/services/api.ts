import axios from 'axios';
import type { AxiosInstance } from 'axios';
import type {
  LoginRequest,
  RegisterRequest,
  GoogleAuthRequest,
  AuthResponse,
  User,
  UpdateProfileRequest,
  Skill,
  UserSkill,
  AddUserSkillRequest,
  UpdateUserSkillRequest,
  MentorAvailability,
  MentorAvailabilityRequest,
  MentorshipRequest,
  CreateMentorshipRequest,
  MentorSearchResponse,
  MentorRecommendationResponse,
  DashboardResponse,
  ConversationResponse,
  MessageResponse

} from '../types';

const rawApiUrl = import.meta.env.VITE_API_URL?.trim();
export const API_BASE_URL = rawApiUrl ? rawApiUrl.replace(/\/+$/, '') : '/api';

const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers = config.headers ?? {};
    (config.headers as Record<string, string>).Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle errors
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const requestUrl = error.config?.url ?? '';
    const isAuthEndpoint =
      requestUrl.includes('/auth/login') ||
      requestUrl.includes('/auth/register') ||
      requestUrl.includes('/auth/google');

    if ((error.response?.status === 401 || error.response?.status === 403) && !isAuthEndpoint) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const authService = {
  login: (data: LoginRequest) => apiClient.post<AuthResponse>('/auth/login', data),
  register: (data: RegisterRequest) => apiClient.post<AuthResponse>('/auth/register', data),
  googleLogin: (data: GoogleAuthRequest) => apiClient.post<AuthResponse>('/auth/google', data),
  getCurrentUser: () => apiClient.get<User>('/users/me'),
  updateProfile: (data: UpdateProfileRequest) => apiClient.put<User>('/users/me', data),
};

export const skillService = {
  getSkills: () => apiClient.get<Skill[]>('/skills'),
  getSkillCatalog: (query = '') => apiClient.get<Skill[]>('/skills/catalog', { params: { query } }),
  createSkill: (data: { name: string; category?: string }) =>
    apiClient.post<Skill>('/skills', data),
};

export const userSkillService = {
  addSkill: (data: AddUserSkillRequest) =>
    apiClient.post<UserSkill>('/user-skills', data),
  getUserSkills: () => apiClient.get<UserSkill[]>('/user-skills/me'),
  updateSkill: (id: string, data: UpdateUserSkillRequest) =>
    apiClient.put<UserSkill>(`/user-skills/${id}`, data),
  deleteSkill: (id: string) => apiClient.delete(`/user-skills/${id}`),
};

export const organizationService = {
  search: (query = '') => apiClient.get<string[]>('/organizations', { params: { query } }),
};

export const mentorAvailabilityService = {
  addAvailability: (data: MentorAvailabilityRequest) =>
    apiClient.post<MentorAvailability>('/availability', data),
  getMentorAvailability: (mentorId: string) =>
    apiClient.get<MentorAvailability[]>(`/availability/mentor/${mentorId}`),
  deleteAvailability: (id: string) => apiClient.delete(`/availability/${id}`),
};

export const mentorshipService = {
  sendRequest: (data: CreateMentorshipRequest) =>
    apiClient.post<MentorshipRequest>('/mentorship/request', data),
  getSentRequests: () => apiClient.get<MentorshipRequest[]>('/mentorship/sent'),
  getReceivedRequests: () => apiClient.get<MentorshipRequest[]>('/mentorship/received'),
  acceptRequest: (id: string) => apiClient.put(`/mentorship/${id}/accept`, {}),
  rejectRequest: (id: string) => apiClient.put(`/mentorship/${id}/reject`, {}),
};

export const mentorSearchService = {
  searchMentors: (skill: string) =>
    apiClient.get<MentorSearchResponse[]>('/mentors/search', { params: { skill } }),
};

export const recommendationService = {
  getRecommendations: () =>
    apiClient.get<MentorRecommendationResponse[]>('/recommendations'),
};

export const dashboardService = {
  getDashboard: () => apiClient.get<DashboardResponse>('/dashboard'),
};

// Chat service
export const chatService = {

  getConversations: () =>
    apiClient.get<ConversationResponse[]>(
      '/chat/conversations'
    ),

  getMessages: (
    conversationId: string
  ) =>
    apiClient.get<MessageResponse[]>(
      `/chat/${conversationId}`
    ),

  sendMessage: (
    conversationId: string,
    content: string
  ) =>
    apiClient.post<MessageResponse>(
      '/chat/send',
      {
        conversationId,
        content,
      }
    ),

  closeConversation: (conversationId: string) =>
    apiClient.post<ConversationResponse>(`/chat/conversations/${conversationId}/close`),
};

export interface NotificationResponse {
  id: string;
  title: string;
  message: string;
  read: boolean;
  createdAt: string;
}


// Notification service
export const notificationService = {

  getNotifications: () =>
    apiClient.get<NotificationResponse[]>(
      '/notifications'
    ),

  getUnreadCount: () =>
    apiClient.get<number>(
      '/notifications/unread-count'
    ),

  markRead: (id: string) =>
    apiClient.put(
      `/notifications/${id}/read`,
      {}
    ),
};

export interface CreateMeetingRequest {
  conversationId: string;
  scheduledAt: string;
}

export interface MeetingResponse {
  id: string;
  conversationId: string;
  creatorId: string;
  creatorName: string;
  recipientId: string;
  scheduledAt: string;
  meetLink?: string;
  meetLinkExpiresAt?: string;
  status: string;
  organizerGoogleConnected: boolean;
}

// Meeting service
export const meetingService = {
  createMeeting: (
    data: CreateMeetingRequest
  ) =>
    apiClient.post<MeetingResponse>(
      '/meetings',
      data
    ),

  getMeetings: () =>
    apiClient.get<MeetingResponse[]>(
      '/meetings'
    ),

    // ---- Add these two methods inside your existing `meetingService` object in services/api.ts ----
// (alongside getMeetings / createMeeting, using the same `api` axios instance)

  acceptMeeting: (id: string) => apiClient.post(`/meetings/${id}/accept`),
  declineMeeting: (id: string) => apiClient.post(`/meetings/${id}/decline`),
  removeMeetLink: (id: string) => apiClient.delete(`/meetings/${id}/link`),

  getConversationMeetings: (
    conversationId: string
  ) =>
    apiClient.get<MeetingResponse[]>(
      `/meetings/conversation/${conversationId}`
    ),
};

export interface CreateReviewRequest {
  mentorId: string;
  skillId?: string;
  mentorshipRequestId?: string;
  rating: number;
  skillLevelRating?: string;
  comment: string;
}

export interface ReviewResponse {
  id: string;
  mentorId: string;
  reviewerId: string;
  rating: number;
  comment: string;
  createdAt: string;
}

// Review service
export const reviewService = {

  createReview: (
    data: CreateReviewRequest
  ) =>
    apiClient.post<ReviewResponse>(
      '/reviews',
      data
    ),

  getMentorReviews: (
    mentorId: string
  ) =>
    apiClient.get<ReviewResponse[]>(
      `/reviews/mentor/${mentorId}`
    ),

  getAverageRating: (
    mentorId: string
  ) =>
    apiClient.get<number>(
      `/reviews/mentor/${mentorId}/average`
    ),
};

// Mentor profile service
export const mentorProfileService = {

  getMentorProfile: (
    mentorId: string
  ) =>
    apiClient.get(
      `/users/mentor/${mentorId}`
    ),
};


// AI service
export interface AiChatRequest {
  conversationId: string;
  prompt: string;
}

export const aiService = {

  getMatchReason: (
    mentorId: string
  ) =>
    apiClient.get(
      `/ai/match/${mentorId}`
    ),

  askAssistant: (
    data: AiChatRequest
  ) =>
    apiClient.post(
      '/ai/chat',
      data
    ),
};



export default apiClient;
