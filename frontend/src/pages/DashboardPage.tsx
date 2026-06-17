import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { MainLayout } from '../layouts/MainLayout';
import { dashboardService, recommendationService } from '../services/api';
import {
  ArrowRight,
  BarChart3,
  BookOpen,
  Send,
  Inbox,
  Lightbulb,
  Sparkles,
  TrendingUp,
  Users,
  Video,
  Star,
  GraduationCap,
  CheckCircle2,
} from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const { data: dashboardData, isLoading: dashboardLoading } = useQuery({
    queryKey: ['dashboard'],
    queryFn: () => dashboardService.getDashboard().then((res) => res.data),
  });

  const { data: recommendations, isLoading: recommendationsLoading } = useQuery({
    queryKey: ['recommendations'],
    queryFn: () => recommendationService.getRecommendations().then((res) => res.data),
  });

  const isLoading = dashboardLoading || recommendationsLoading;

  const StatCard: React.FC<{
    icon: React.ReactNode;
    label: string;
    value: number | string;
    tone: string;
    subtext: string;
  }> = ({ icon, label, value, tone, subtext }) => (
    <div className="group rounded-[1.5rem] border border-white/80 bg-white/90 p-6 shadow-[0_16px_40px_rgba(15,23,42,0.08)] transition hover:-translate-y-1 hover:shadow-[0_24px_52px_rgba(15,23,42,0.12)]">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm font-medium uppercase tracking-[0.18em] text-slate-500">{label}</p>
          <p className="mt-3 text-4xl font-semibold tracking-tight text-slate-900">{value}</p>
          <p className="mt-2 text-sm leading-6 text-slate-500">{subtext}</p>
        </div>
        <div className={`grid h-14 w-14 place-items-center rounded-2xl ${tone} text-white shadow-lg`}>
          {icon}
        </div>
      </div>
    </div>
  );

  if (isLoading) {
    return (
      <MainLayout>
        <div className="text-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading dashboard...</p>
        </div>
      </MainLayout>
    );
  }

  const avgRatingDisplay = dashboardData?.averageRating && dashboardData.averageRating > 0
    ? `⭐ ${dashboardData.averageRating}`
    : 'N/A';

  return (
    <MainLayout>
      <section className="relative mb-8 overflow-hidden rounded-[2rem] border border-white/70 bg-[linear-gradient(135deg,#0f172a_0%,#172554_55%,#4338ca_100%)] px-6 py-6 text-white shadow-[0_24px_80px_rgba(15,23,42,0.18)] sm:px-8 sm:py-8">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,_rgba(255,255,255,0.14),_transparent_42%)]" />
        <div className="relative flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
          <div className="max-w-2xl">
            <div className="mb-4 inline-flex items-center gap-2 rounded-full border border-white/15 bg-white/10 px-3 py-1 text-xs font-medium uppercase tracking-[0.25em] text-cyan-100">
              <Sparkles size={14} />
              Mentor Growth Console
            </div>
            <h1 className="text-4xl font-semibold tracking-tight sm:text-5xl">Dashboard</h1>
            <p className="mt-4 max-w-2xl text-base leading-7 text-slate-200 sm:text-lg">
              A premium snapshot of your network, skills, requests, and mentor discovery flow.
            </p>
          </div>

          <div className="grid gap-3 sm:grid-cols-3">
            <div className="rounded-2xl border border-white/10 bg-white/10 px-4 py-3 backdrop-blur">
              <p className="text-xs uppercase tracking-[0.2em] text-slate-300">Skills</p>
              <p className="mt-1 text-2xl font-semibold">{dashboardData?.totalSkills || 0}</p>
            </div>
            <div className="rounded-2xl border border-white/10 bg-white/10 px-4 py-3 backdrop-blur">
              <p className="text-xs uppercase tracking-[0.2em] text-slate-300">Mentors</p>
              <p className="mt-1 text-2xl font-semibold">{dashboardData?.recommendations || 0}</p>
            </div>
            <div className="rounded-2xl border border-white/10 bg-white/10 px-4 py-3 backdrop-blur">
              <p className="text-xs uppercase tracking-[0.2em] text-slate-300">Sessions</p>
              <p className="mt-1 text-2xl font-semibold">
                {(dashboardData?.sessionsConducted || 0) + (dashboardData?.upcomingSessions || 0)}
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Mentor Analytics Section */}
      <div className="mb-8">
        <h2 className="mb-4 text-xl font-bold tracking-tight text-slate-800 flex items-center gap-2">
          <Users size={20} className="text-indigo-600" />
          Mentor Console (Your Activity Guiding Others)
        </h2>
        <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-4">
          <StatCard
            icon={<Users className="h-6 w-6" />}
            label="Total Mentees"
            value={dashboardData?.totalMentees || 0}
            tone="bg-gradient-to-br from-indigo-500 to-purple-500"
            subtext="Mentees connected with you."
          />
          <StatCard
            icon={<Inbox className="h-6 w-6" />}
            label="Active Chats"
            value={dashboardData?.activeChats || 0}
            tone="bg-gradient-to-br from-cyan-500 to-blue-500"
            subtext="Conversations with messages."
          />
          <StatCard
            icon={<Video className="h-6 w-6" />}
            label="Sessions Conducted"
            value={dashboardData?.sessionsConducted || 0}
            tone="bg-gradient-to-br from-emerald-500 to-teal-500"
            subtext="Completed mentorship calls."
          />
          <StatCard
            icon={<Star className="h-6 w-6" />}
            label="Average Rating"
            value={avgRatingDisplay}
            tone="bg-gradient-to-br from-amber-500 to-orange-500"
            subtext="Based on submitted reviews."
          />
        </div>
      </div>

      {/* Mentee Analytics Section */}
      <div className="mb-8">
        <h2 className="mb-4 text-xl font-bold tracking-tight text-slate-800 flex items-center gap-2">
          <GraduationCap size={20} className="text-indigo-600" />
          Mentee Console (Your Growth & Learning)
        </h2>
        <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-4">
          <StatCard
            icon={<BookOpen className="h-6 w-6" />}
            label="Skills Learned"
            value={dashboardData?.skillsLearned || 0}
            tone="bg-gradient-to-br from-sky-500 to-cyan-500"
            subtext="Interests & topics added."
          />
          <StatCard
            icon={<CheckCircle2 className="h-6 w-6" />}
            label="Accepted Requests"
            value={dashboardData?.acceptedRequests || 0}
            tone="bg-gradient-to-br from-teal-500 to-emerald-500"
            subtext="Requests accepted by mentors."
          />
          <StatCard
            icon={<Video className="h-6 w-6" />}
            label="Upcoming Sessions"
            value={dashboardData?.upcomingSessions || 0}
            tone="bg-gradient-to-br from-pink-500 to-rose-500"
            subtext="Scheduled upcoming sessions."
          />
          <StatCard
            icon={<Send className="h-6 w-6" />}
            label="Sent Requests"
            value={dashboardData?.sentRequests || 0}
            tone="bg-gradient-to-br from-violet-500 to-indigo-500"
            subtext="Total applications submitted."
          />
        </div>
      </div>

      {/* Recommended Mentors Section */}
      {recommendations && recommendations.length > 0 && (
        <div className="mt-6 rounded-[1.8rem] border border-white/80 bg-white/90 p-6 shadow-[0_16px_40px_rgba(15,23,42,0.08)]">
          <div className="mb-5 flex items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <div className="grid h-11 w-11 place-items-center rounded-2xl bg-gradient-to-br from-violet-500 to-indigo-500 text-white shadow-lg">
                <TrendingUp className="h-5 w-5" />
              </div>
              <div>
                <h2 className="text-2xl font-semibold text-slate-900">Recommended Mentors</h2>
                <p className="text-sm text-slate-500">Top matches based on your current profile and skills.</p>
              </div>
            </div>
            <button className="hidden items-center gap-2 rounded-full border border-slate-200 px-4 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-50 sm:inline-flex">
              View all
              <ArrowRight size={16} />
            </button>
          </div>
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {recommendations.slice(0, 3).map((mentor) => (
              <div
                key={mentor.id}
                className="rounded-[1.4rem] border border-slate-200/80 bg-gradient-to-br from-white to-slate-50 p-5 shadow-sm transition hover:-translate-y-1 hover:shadow-lg"
              >
                <h3 className="text-lg font-semibold text-slate-900">
                  {mentor.firstName} {mentor.lastName}
                </h3>
                <p className="mt-1 text-sm text-slate-500">{mentor.headline}</p>
                <p className="mt-3 inline-flex rounded-full bg-violet-50 px-3 py-1 text-sm font-medium text-violet-700">
                  Match Score: {(mentor.matchScore * 100).toFixed(0)}%
                </p>
                <p className="mt-3 text-sm leading-6 text-slate-600">{mentor.reason}</p>
              </div>
            ))}
          </div>
        </div>
      )}
    </MainLayout>
  );
};
