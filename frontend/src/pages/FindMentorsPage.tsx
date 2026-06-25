import React, { useDeferredValue, useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { MainLayout } from '../layouts/MainLayout';
import {
  mentorSearchService,
  recommendationService,
  mentorshipService,
  skillService,
} from '../services/api';
import { Search, Send, Sparkles, Star } from 'lucide-react';

export const FindMentorsPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [searchQuery, setSearchQuery] = useState('');
  const [requestingMentorId, setRequestingMentorId] = useState<string | null>(null);
  const [selectedRequestSkills, setSelectedRequestSkills] = useState<Record<string, string>>({});
  const [viewMode, setViewMode] = useState<'recommendations' | 'search'>('recommendations');
  const deferredSearchQuery = useDeferredValue(searchQuery);
  const navigate = useNavigate();

  const { data: allSkills } = useQuery({
    queryKey: ['skillCatalog', deferredSearchQuery],
    queryFn: () => skillService.getSkillCatalog(deferredSearchQuery).then((res) => res.data),
  });

  const { data: recommendations, isLoading: recommendationsLoading } = useQuery({
    queryKey: ['recommendations'],
    queryFn: () => recommendationService.getRecommendations().then((res) => res.data),
  });

  const { data: searchResults, isLoading: searchLoading } = useQuery({
    queryKey: ['mentorSearch', searchQuery],
    queryFn: () => mentorSearchService.searchMentors(searchQuery).then((res) => res.data),
    enabled: searchQuery.length > 0,
  });

  const sendRequestMutation = useMutation({
    mutationFn: ({
      mentorId,
      skillId,
    }: {
      mentorId: string;
      skillId: string;
    }) =>
      mentorshipService.sendRequest({
        mentorId,
        skillId,
        message: 'I would like to learn from you.',
      }),

    onMutate: ({ mentorId }) => {
      setRequestingMentorId(mentorId);
    },

    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({
        queryKey: ['recommendations'],
      });

      queryClient.invalidateQueries({
        queryKey: ['mentorSearch'],
      });

      setSelectedRequestSkills((prev) => {
        const next = { ...prev };
        delete next[variables.mentorId];
        return next;
      });
    },

    onSettled: () => {
      setRequestingMentorId(null);
    },
  });

  const isLoading = recommendationsLoading || searchLoading;
  const displayMentors =
    viewMode === 'recommendations' ? recommendations ?? [] : searchResults ?? [];

  const MentorCard: React.FC<{ mentor: any }> = ({ mentor }) => {
    const mentorSkills = mentor.skills ?? [];
    const selectedSkillId = selectedRequestSkills[mentor.id] || '';

    return (
      <div className="group rounded-[1.5rem] border border-white/80 bg-white/92 p-5 shadow-[0_16px_40px_rgba(15,23,42,0.08)] transition hover:-translate-y-1 hover:shadow-[0_24px_52px_rgba(15,23,42,0.12)]">
      <div className="mb-4 flex items-start justify-between gap-4">
        <div>
          <h3 className="text-lg font-semibold text-slate-900">
            {mentor.firstName} {mentor.lastName}
          </h3>
          {mentor.headline && <p className="mt-1 text-sm text-slate-500">{mentor.headline}</p>}
          {mentor.company && <p className="text-sm text-slate-400">{mentor.company}</p>}
        </div>
        {mentor.matchScore != null && (
          <div className="inline-flex items-center gap-1 rounded-full bg-amber-50 px-3 py-1 text-sm font-semibold text-amber-700">
            <Star className="h-4 w-4 fill-amber-500 text-amber-500" />
            {/*
              NOTE: backend now returns matchScore already normalized as a
              0-100 percentage (see RecommendationService.java fix). Do NOT
              multiply by 100 here, or scores will be inflated 100x
              (e.g. 48% -> 4800%). Just round and display directly.
            */}
            {Math.round(mentor.matchScore)}%
          </div>
        )}
      </div>

      {mentorSkills.length > 0 && (
        <div className="mb-4">
          <p className="mb-2 text-xs font-semibold uppercase tracking-[0.18em] text-slate-500">Skills</p>
          <div className="flex flex-wrap gap-2">
            {mentorSkills.slice(0, 3).map((skill: any) => (
              <span
                key={skill.id}
                className="rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-700"
              >
                {skill.name}
              </span>
            ))}
            {mentorSkills.length > 3 && (
              <span className="rounded-full bg-indigo-50 px-3 py-1 text-xs font-medium text-indigo-700">
                +{mentorSkills.length - 3} more
              </span>
            )}
          </div>
        </div>
      )}

      {mentor.reason && <p className="mb-4 text-sm leading-6 text-slate-600">{mentor.reason}</p>}

      <div className="space-y-2">
        <select
          value={selectedSkillId}
          onChange={(e) =>
            setSelectedRequestSkills((prev) => ({
              ...prev,
              [mentor.id]: e.target.value,
            }))
          }
          disabled={!mentorSkills.length}
          className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-medium text-slate-700 outline-none transition focus:border-indigo-400 focus:bg-white focus:ring-4 focus:ring-indigo-100 disabled:cursor-not-allowed disabled:opacity-60"
        >
          <option value="">Choose mentorship skill...</option>
          {mentorSkills.map((skill: any) => (
            <option key={skill.id} value={skill.id}>
              {skill.name}
            </option>
          ))}
        </select>

        <button
          onClick={() => {
            if (!selectedSkillId) {
              alert('Please choose the skill you want mentorship for.');
              return;
            }
            sendRequestMutation.mutate({
              mentorId: mentor.id,
              skillId: selectedSkillId,
            });
          }}
          disabled={
            !mentorSkills.length ||
            !selectedSkillId ||
            sendRequestMutation.isPending ||
            requestingMentorId === mentor.id
          }
          className="flex w-full items-center justify-center gap-2 rounded-2xl bg-[linear-gradient(135deg,#4f46e5_0%,#6366f1_55%,#7c3aed_100%)] py-3 text-sm font-semibold text-white"
        >
          <Send size={16} />

          {sendRequestMutation.isPending &&
            requestingMentorId === mentor.id
            ? 'Sending...'
            : 'Send Request'}
        </button>

        <button
          onClick={() =>
            navigate(`/mentor/${mentor.id}`)
          }
          className="
      w-full
      rounded-2xl
      border
      border-slate-200
      bg-white
      py-3
      text-sm
      font-semibold
      text-slate-700
      transition
      hover:bg-slate-50
    "
        >
          View Profile
        </button>

      </div>
    </div>
    );
  };

  return (
    <MainLayout>
      <div className="mx-auto max-w-7xl space-y-6">
        <section className="rounded-[2rem] border border-white/80 bg-[linear-gradient(135deg,#0f172a_0%,#172554_55%,#4338ca_100%)] px-6 py-6 text-white shadow-[0_24px_80px_rgba(15,23,42,0.18)] sm:px-8 sm:py-8">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
            <div className="max-w-2xl">
              <div className="mb-3 inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/10 px-3 py-1 text-xs font-medium uppercase tracking-[0.24em] text-cyan-100">
                <Sparkles size={14} />
                Mentor discovery
              </div>
              <h1 className="text-4xl font-semibold tracking-tight sm:text-5xl">Find Your Mentor</h1>
              <p className="mt-3 max-w-2xl text-base leading-7 text-slate-200">
                Discover mentors who match your learning goals and send requests in a premium, focused workflow.
              </p>
            </div>

            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-1">
              <button
                onClick={() => setViewMode('recommendations')}
                className={`rounded-2xl px-5 py-3 text-sm font-semibold transition ${viewMode === 'recommendations'
                  ? 'bg-white text-slate-900 shadow-lg'
                  : 'border border-white/15 bg-white/10 text-white backdrop-blur hover:bg-white/15'
                  }`}
              >
                Recommended For You
              </button>
              <button
                onClick={() => setViewMode('search')}
                className={`rounded-2xl px-5 py-3 text-sm font-semibold transition ${viewMode === 'search'
                  ? 'bg-white text-slate-900 shadow-lg'
                  : 'border border-white/15 bg-white/10 text-white backdrop-blur hover:bg-white/15'
                  }`}
              >
                Search Skills
              </button>
            </div>
          </div>
        </section>

        {viewMode === 'search' && (
          <div className="rounded-[1.6rem] border border-white/80 bg-white/92 p-5 shadow-[0_16px_40px_rgba(15,23,42,0.08)]">
            <div className="relative">
              <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={20} />
              <input
                type="text"
                placeholder="Search by skill... (e.g., Spring Boot, React, AWS)"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                list="skills"
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 py-3 pl-12 pr-4 outline-none transition focus:border-indigo-400 focus:bg-white focus:ring-4 focus:ring-indigo-100"
              />
              <datalist id="skills">
                {allSkills?.map((skill) => (
                  <option key={skill.id} value={skill.name} />
                ))}
              </datalist>
            </div>
          </div>
        )}

        {isLoading && (
          <div className="flex min-h-[40vh] items-center justify-center">
            <div className="text-center">
              <div className="mx-auto h-14 w-14 animate-spin rounded-full border-4 border-indigo-200 border-t-indigo-600" />
              <p className="mt-4 text-sm font-medium text-slate-500">Finding mentors...</p>
            </div>
          </div>
        )}

        {!isLoading && displayMentors.length > 0 ? (
          <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
            {displayMentors.map((mentor) => (
              <MentorCard key={mentor.id} mentor={mentor} />
            ))}
          </div>
        ) : !isLoading ? (
          <div className="rounded-[1.8rem] border border-white/80 bg-white/92 p-12 text-center shadow-[0_16px_40px_rgba(15,23,42,0.08)]">
            <p className="text-slate-700">
              {viewMode === 'search'
                ? 'No mentors found for this skill. Try another skill.'
                : 'No recommendations available yet. Complete your profile and skills to get personalized recommendations!'}
            </p>
          </div>
        ) : null}
      </div>
    </MainLayout>
  );
};
