// User Types
export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  headline?: string;
  bio?: string;
  experience?: string;
  role?: string;
  company?: string;
  location?: string;
  linkedIn?: string;
  github?: string;
  createdAt: string;
  updatedAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface GoogleAuthRequest {
  credential: string;
}

export interface UpdateProfileRequest {
  firstName?: string;
  lastName?: string;
  headline?: string;
  bio?: string;
  experience?: number;
  role?: string;
  company?: string;
  location?: string;
  linkedIn?: string;
  github?: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

// Skill Types
export interface Skill {
  id: string;
  name: string;
  category?: string;
  description?: string;
}

export interface UserSkill {
  id: string;
  skillId: string;
  userId: string;
  yearsOfExperience: number;
  canMentor: boolean;
  skill: Skill;
}

export interface AddUserSkillRequest {
  skillId: string;
  yearsOfExperience: number;
  canMentor: boolean;
}

// Mentorship Types
export interface MentorAvailability {
  id: string;
  mentorId: string;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
}

export interface MentorAvailabilityRequest {
  dayOfWeek: string;
  startTime: string;
  endTime: string;
}

export interface MentorshipRequest {
  id: string;
  mentorId: string;
  menteId: string; // Note: backend returns 'menteId' (intentional consistency)
  skillId: string;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED';
  message?: string;
  createdAt: string;
}

export interface CreateMentorshipRequest {
  mentorId: string;
  skillId: string;
  message?: string;
}

// Mentor Types
export interface MentorSearchResponse {
  id: string;
  firstName: string;
  lastName: string;
  headline?: string;
  company?: string;
  skills: Skill[];
  availability: MentorAvailability[];
}

export interface MentorRecommendationResponse {
  id: string;
  firstName: string;
  lastName: string;
  headline?: string;
  company?: string;
  experience?: string;
  skills: Skill[];
  matchScore: number;
  reason: string;
}

// Dashboard Types
export interface DashboardResponse {
  totalSkills: number;
  mentoringSkills: number;
  sentRequests: number;
  receivedRequests: number;
  recommendations: number;
  totalMentees?: number;
  activeChats?: number;
  sessionsConducted?: number;
  averageRating?: number;
  acceptedRequests?: number;
  skillsLearned?: number;
  upcomingSessions?: number;
}

// API Response Types
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
  error?: string;
}

//chat
export interface ConversationResponse {
  id: string;

  mentorId: string;
  menteeId: string;

  mentorName: string;
  menteeName: string;

  displayName: string;
}

export interface MessageResponse {
  id: string;

  senderId: string;
  senderName: string;

  content: string;

  sentAt: string;
}

export interface SendMessageRequest {
  conversationId: string;
  content: string;
}

// Notification Types
export interface NotificationResponse {
  id: string;
  title: string;
  message: string;
  read: boolean;
  createdAt: string;
}

// Meeting 
export interface MeetingResponse {
  id: string;
  conversationId: string;
  scheduledAt: string;
  meetLink: string;
  status: string;
}


// Review Types 
export interface ReviewResponse {
  id: string;
  mentorId: string;
  reviewerId: string;
  rating: number;
  comment: string;
  createdAt: string;
}

// Mentor Profile Types
export interface MentorProfileResponse {
  id: string;
  firstName: string;
  lastName: string;
  headline?: string;
  company?: string;
  bio?: string;
  averageRating: number;
  reviews: ReviewResponse[];
}
