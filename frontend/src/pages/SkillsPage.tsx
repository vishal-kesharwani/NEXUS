import React, { useDeferredValue, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { MainLayout } from '../layouts/MainLayout';
import { skillService, userSkillService } from '../services/api';
import type { AddUserSkillRequest, Skill, SkillLevel, UpdateUserSkillRequest } from '../types';
import { Check, Pencil, Plus, Search, Sparkles, Trash2, X } from 'lucide-react';

const SKILL_LEVELS: { value: SkillLevel; label: string }[] = [
  { value: 'BEGINNER', label: 'Beginner' },
  { value: 'INTERMEDIATE', label: 'Intermediate' },
  { value: 'PRO', label: 'Pro' },
  { value: 'ADVANCED', label: 'Advanced' },
  { value: 'SUPERIOR', label: 'Superior' },
];

export const SkillsPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [isAdding, setIsAdding] = useState(false);
  const [selectedSkillId, setSelectedSkillId] = useState('');
  const [yearsOfExperience, setYearsOfExperience] = useState('1');
  const [canMentor, setCanMentor] = useState(false);
  const [selfLevel, setSelfLevel] = useState<SkillLevel>('BEGINNER');
  const [skillSearch, setSkillSearch] = useState('');
  const [editingSkillId, setEditingSkillId] = useState('');
  const [editForm, setEditForm] = useState<UpdateUserSkillRequest>({});
  const deferredSearch = useDeferredValue(skillSearch);

  const { data: userSkills, isLoading: isUserSkillsLoading } = useQuery({
    queryKey: ['userSkills'],
    queryFn: () => userSkillService.getUserSkills().then((res) => res.data),
  });

  const { data: catalogSkills, isLoading: isCatalogLoading } = useQuery({
    queryKey: ['skillCatalog', deferredSearch],
    queryFn: () => skillService.getSkillCatalog(deferredSearch).then((res) => res.data),
  });

  const availableSkills = useMemo(() => {
    const ownedSkillIds = new Set(userSkills?.map((skill) => skill.skillId));
    return catalogSkills?.filter((skill) => !ownedSkillIds.has(skill.id)) ?? [];
  }, [catalogSkills, userSkills]);

  const selectedSkill = availableSkills.find((skill) => skill.id === selectedSkillId);

  const addSkillMutation = useMutation({
    mutationFn: (data: AddUserSkillRequest) => userSkillService.addSkill(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['userSkills'] });
      setSelectedSkillId('');
      setYearsOfExperience('1');
      setCanMentor(false);
      setSelfLevel('BEGINNER');
      setIsAdding(false);
    },
  });

  const updateSkillMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateUserSkillRequest }) =>
      userSkillService.updateSkill(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['userSkills'] });
      setEditingSkillId('');
      setEditForm({});
    },
  });

  const deleteSkillMutation = useMutation({
    mutationFn: (id: string) => userSkillService.deleteSkill(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['userSkills'] });
    },
  });

  const handleAddSkill = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedSkillId) return;

    addSkillMutation.mutate({
      skillId: selectedSkillId,
      yearsOfExperience: Number.parseInt(yearsOfExperience, 10),
      canMentor,
      selfLevel,
    });
  };

  const topSuggestions: Skill[] = useMemo(() => availableSkills.slice(0, 6), [availableSkills]);

  if (isUserSkillsLoading) {
    return (
      <MainLayout>
        <div className="flex min-h-[50vh] items-center justify-center">
          <div className="text-center">
            <div className="mx-auto h-14 w-14 animate-spin rounded-full border-4 border-indigo-200 border-t-indigo-600" />
            <p className="mt-4 text-sm font-medium text-slate-500">Loading skills...</p>
          </div>
        </div>
      </MainLayout>
    );
  }

  return (
    <MainLayout>
      <div className="mx-auto max-w-6xl space-y-6">
        <section className="overflow-hidden rounded-[2rem] border border-white/80 bg-[linear-gradient(135deg,#ffffff_0%,#f8fafc_55%,#eef2ff_100%)] p-6 shadow-[0_16px_40px_rgba(15,23,42,0.08)] sm:p-8">
          <div className="flex flex-col gap-5 lg:flex-row lg:items-end lg:justify-between">
            <div className="max-w-2xl">
              <p className="text-xs font-semibold uppercase tracking-[0.3em] text-indigo-600">
                Skill portfolio
              </p>
              <h1 className="mt-3 text-4xl font-semibold tracking-tight text-slate-900">
                My Skills
              </h1>
              <p className="mt-3 text-base leading-7 text-slate-600">
                Discover high-signal skills from a live catalog, then add your stack to your profile
                so mentors and mentees can find you faster.
              </p>
            </div>

            {!isAdding ? (
              <button
                onClick={() => setIsAdding(true)}
                className="inline-flex items-center gap-2 rounded-2xl bg-[linear-gradient(135deg,#4f46e5_0%,#6366f1_55%,#7c3aed_100%)] px-5 py-3 text-sm font-semibold text-white shadow-[0_16px_32px_rgba(79,70,229,0.26)] transition hover:-translate-y-0.5"
              >
                <Plus size={18} />
                Add Skill
              </button>
            ) : null}
          </div>
        </section>

        <section className="rounded-[1.8rem] border border-white/80 bg-white/92 p-6 shadow-[0_16px_40px_rgba(15,23,42,0.08)]">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
            <div>
              <p className="text-sm font-semibold text-slate-900">Skill catalog</p>
              <p className="mt-1 text-sm text-slate-500">
                Powered by a live external API and synced into your local database.
              </p>
            </div>
            <div className="w-full lg:max-w-md">
              <label className="sr-only" htmlFor="skill-search">
                Search skills
              </label>
              <div className="relative">
                <Search className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <input
                  id="skill-search"
                  value={skillSearch}
                  onChange={(e) => setSkillSearch(e.target.value)}
                  placeholder="Search skill ideas like Spring Boot, React, AWS..."
                  className="w-full rounded-2xl border border-slate-200 bg-slate-50 py-3 pl-11 pr-4 text-sm outline-none transition focus:border-indigo-400 focus:bg-white focus:ring-4 focus:ring-indigo-100"
                />
              </div>
            </div>
          </div>

          <div className="mt-5 flex flex-wrap gap-2">
            {topSuggestions.map((skill) => (
              <button
                key={skill.id}
                type="button"
                onClick={() => {
                  setSelectedSkillId(skill.id);
                  setIsAdding(true);
                }}
                className={`rounded-full border px-4 py-2 text-sm font-medium transition ${
                  selectedSkillId === skill.id
                    ? 'border-indigo-200 bg-indigo-50 text-indigo-700'
                    : 'border-slate-200 bg-slate-50 text-slate-600 hover:border-indigo-200 hover:bg-indigo-50 hover:text-indigo-700'
                }`}
              >
                {skill.name}
              </button>
            ))}
          </div>
        </section>

        {isAdding && (
          <div className="rounded-[1.8rem] border border-white/80 bg-white/92 p-6 shadow-[0_16px_40px_rgba(15,23,42,0.08)]">
            <div className="mb-5 flex items-center gap-3">
              <div className="grid h-11 w-11 place-items-center rounded-2xl bg-gradient-to-br from-indigo-500 to-violet-500 text-white shadow-lg">
                <Sparkles className="h-5 w-5" />
              </div>
              <div>
                <h2 className="text-xl font-semibold text-slate-900">Add New Skill</h2>
                <p className="text-sm text-slate-500">
                  Choose from the live catalog, then set how strong your experience is.
                </p>
              </div>
            </div>

            <form onSubmit={handleAddSkill} className="space-y-4">
              <div className="space-y-2">
                <label className="text-sm font-medium text-slate-700">Select Skill</label>
                <select
                  value={selectedSkillId}
                  onChange={(e) => setSelectedSkillId(e.target.value)}
                  required
                  className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none transition focus:border-indigo-400 focus:bg-white focus:ring-4 focus:ring-indigo-100"
                >
                  <option value="">Choose a skill...</option>
                  {availableSkills.map((skill) => (
                    <option key={skill.id} value={skill.id}>
                      {skill.name}
                    </option>
                  ))}
                </select>
                <div className="text-xs text-slate-500">
                  {isCatalogLoading
                    ? 'Refreshing catalog...'
                    : `${availableSkills.length} matching skill${availableSkills.length === 1 ? '' : 's'} available`}
                </div>
              </div>

              {selectedSkill ? (
                <div className="rounded-2xl border border-indigo-100 bg-indigo-50/70 p-4 text-sm text-indigo-900">
                  <p className="font-semibold">{selectedSkill.name}</p>
                  {selectedSkill.description ? (
                    <p className="mt-1 text-indigo-800/80">{selectedSkill.description}</p>
                  ) : (
                    <p className="mt-1 text-indigo-800/80">
                      Great choice for building credibility on the platform.
                    </p>
                  )}
                </div>
              ) : null}

              <div className="grid gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <label className="text-sm font-medium text-slate-700">Years of Experience</label>
                  <input
                    type="number"
                    min="0"
                    max="60"
                    value={yearsOfExperience}
                    onChange={(e) => setYearsOfExperience(e.target.value)}
                    className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none transition focus:border-indigo-400 focus:bg-white focus:ring-4 focus:ring-indigo-100"
                  />
                </div>

                <label className="flex items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
                  <input
                    type="checkbox"
                    checked={canMentor}
                    onChange={(e) => setCanMentor(e.target.checked)}
                    className="h-4 w-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500"
                  />
                  <span className="text-sm font-medium text-slate-700">Can mentor others</span>
                </label>
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium text-slate-700">Current Skill Level</label>
                <select
                  value={selfLevel}
                  onChange={(e) => setSelfLevel(e.target.value as SkillLevel)}
                  className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none transition focus:border-indigo-400 focus:bg-white focus:ring-4 focus:ring-indigo-100"
                >
                  {SKILL_LEVELS.map((level) => (
                    <option key={level.value} value={level.value}>
                      {level.label}
                    </option>
                  ))}
                </select>
              </div>

              <div className="flex gap-3 pt-2">
                <button
                  type="submit"
                  disabled={addSkillMutation.isPending || !selectedSkillId}
                  className="flex-1 rounded-2xl bg-[linear-gradient(135deg,#4f46e5_0%,#6366f1_55%,#7c3aed_100%)] py-3 font-semibold text-white shadow-[0_16px_32px_rgba(79,70,229,0.26)] transition hover:-translate-y-0.5 disabled:bg-slate-300 disabled:shadow-none"
                >
                  {addSkillMutation.isPending ? 'Adding...' : 'Add Skill'}
                </button>
                <button
                  type="button"
                  onClick={() => setIsAdding(false)}
                  className="flex-1 rounded-2xl border border-slate-200 bg-white py-3 font-semibold text-slate-700 transition hover:bg-slate-50"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        )}

        {userSkills && userSkills.length > 0 ? (
          <div className="grid gap-4 md:grid-cols-2">
            {userSkills.map((userSkill) => {
              const isEditingSkill = editingSkillId === userSkill.id;
              return (
              <div
                key={userSkill.id}
                className="rounded-[1.5rem] border border-white/80 bg-white/92 p-5 shadow-[0_16px_40px_rgba(15,23,42,0.08)] transition hover:-translate-y-0.5"
              >
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold text-slate-900">{userSkill.skill.name}</h3>
                    {isEditingSkill ? (
                      <div className="mt-4 grid gap-3 sm:grid-cols-3">
                        <input
                          type="number"
                          min="0"
                          max="60"
                          value={editForm.yearsOfExperience ?? userSkill.yearsOfExperience}
                          onChange={(e) =>
                            setEditForm((prev) => ({ ...prev, yearsOfExperience: Number.parseInt(e.target.value, 10) }))
                          }
                          className="rounded-2xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm outline-none focus:border-indigo-400"
                        />
                        <select
                          value={editForm.selfLevel ?? userSkill.selfLevel}
                          onChange={(e) => setEditForm((prev) => ({ ...prev, selfLevel: e.target.value as SkillLevel }))}
                          className="rounded-2xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm outline-none focus:border-indigo-400"
                        >
                          {SKILL_LEVELS.map((level) => (
                            <option key={level.value} value={level.value}>
                              {level.label}
                            </option>
                          ))}
                        </select>
                        <label className="flex items-center gap-2 rounded-2xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-700">
                          <input
                            type="checkbox"
                            checked={editForm.canMentor ?? userSkill.canMentor}
                            onChange={(e) => setEditForm((prev) => ({ ...prev, canMentor: e.target.checked }))}
                          />
                          Mentor
                        </label>
                      </div>
                    ) : null}
                    <div className="mt-3 flex flex-wrap items-center gap-3">
                      <span className="rounded-full bg-slate-100 px-3 py-1 text-sm text-slate-600">
                        {userSkill.yearsOfExperience} year{userSkill.yearsOfExperience !== 1 ? 's' : ''}
                      </span>
                      <span className="rounded-full bg-indigo-50 px-3 py-1 text-xs font-semibold text-indigo-700">
                        Self: {SKILL_LEVELS.find((level) => level.value === userSkill.selfLevel)?.label ?? userSkill.selfLevel}
                      </span>
                      <span className="rounded-full bg-cyan-50 px-3 py-1 text-xs font-semibold text-cyan-700">
                        Current: {SKILL_LEVELS.find((level) => level.value === userSkill.adjustedLevel)?.label ?? userSkill.adjustedLevel}
                      </span>
                      {userSkill.canMentor && (
                        <span className="rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700">
                          Can Mentor
                        </span>
                      )}
                    </div>
                  </div>
                  {isEditingSkill ? (
                    <div className="flex gap-2">
                      <button
                        onClick={() =>
                          updateSkillMutation.mutate({
                            id: userSkill.id,
                            data: {
                              yearsOfExperience: editForm.yearsOfExperience ?? userSkill.yearsOfExperience,
                              canMentor: editForm.canMentor ?? userSkill.canMentor,
                              selfLevel: editForm.selfLevel ?? userSkill.selfLevel,
                            },
                          })
                        }
                        disabled={updateSkillMutation.isPending}
                        className="grid h-11 w-11 place-items-center rounded-2xl border border-emerald-100 bg-emerald-50 text-emerald-700 transition hover:bg-emerald-100"
                      >
                        <Check size={18} />
                      </button>
                      <button
                        onClick={() => {
                          setEditingSkillId('');
                          setEditForm({});
                        }}
                        className="grid h-11 w-11 place-items-center rounded-2xl border border-slate-200 bg-slate-50 text-slate-600 transition hover:bg-slate-100"
                      >
                        <X size={18} />
                      </button>
                    </div>
                  ) : (
                    <button
                      onClick={() => {
                        setEditingSkillId(userSkill.id);
                        setEditForm({
                          yearsOfExperience: userSkill.yearsOfExperience,
                          canMentor: userSkill.canMentor,
                          selfLevel: userSkill.selfLevel,
                        });
                      }}
                      className="grid h-11 w-11 place-items-center rounded-2xl border border-indigo-100 bg-indigo-50 text-indigo-600 transition hover:bg-indigo-100"
                    >
                      <Pencil size={18} />
                    </button>
                  )}
                  <button
                    onClick={() => deleteSkillMutation.mutate(userSkill.id)}
                    disabled={deleteSkillMutation.isPending}
                    className="grid h-11 w-11 place-items-center rounded-2xl border border-rose-100 bg-rose-50 text-rose-600 transition hover:-translate-y-0.5 hover:bg-rose-100"
                  >
                    <Trash2 size={18} />
                  </button>
                </div>
              </div>
            );
            })}
          </div>
        ) : (
          <div className="rounded-[1.8rem] border border-white/80 bg-white/92 p-12 text-center shadow-[0_16px_40px_rgba(15,23,42,0.08)]">
            <p className="text-slate-700">No skills added yet.</p>
            <p className="mt-1 text-sm text-slate-500">
              Add your first skill to help mentors find you!
            </p>
          </div>
        )}
      </div>
    </MainLayout>
  );
};
