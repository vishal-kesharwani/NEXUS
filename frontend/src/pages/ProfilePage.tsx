import React, { useState, useEffect } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { MainLayout } from '../layouts/MainLayout';
import { authService, organizationService } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { User as UserIcon } from 'lucide-react';
import type { UpdateProfileRequest, User } from '../types';

export const ProfilePage: React.FC = () => {
  const { user, setUser } = useAuth();
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    headline: '',
    bio: '',
    experience: '',
    role: '',
    company: '',
    location: '',
    linkedIn: '',
    github: '',
  });

  const { data: organizationSuggestions = [] } = useQuery({
    queryKey: ['organizations', formData.company],
    queryFn: () => organizationService.search(formData.company).then((res) => res.data),
    enabled: isEditing,
  });

  // Fetch current user data
  const { data: userData, isLoading } = useQuery({
    queryKey: ['currentUser'],
    queryFn: () => authService.getCurrentUser().then((res) => res.data),
  });

  // Update profile mutation
  const updateMutation = useMutation({
    mutationFn: (data: UpdateProfileRequest) =>
      authService.updateProfile(data).then((res) => res.data),
    onSuccess: (data: User) => {
      setUser(data);
      setIsEditing(false);
    },
  });

  // Initialize form with user data
  useEffect(() => {
    if (userData) {
      setFormData({
        firstName: userData.firstName || '',
        lastName: userData.lastName || '',
        headline: userData.headline || '',
        bio: userData.bio || '',
        experience: userData.experience || '',
        role: userData.role || '',
        company: userData.company || '',
        location: userData.location || '',
        linkedIn: userData.linkedIn || '',
        github: userData.github || '',
      });
    }
  }, [userData]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateMutation.mutate({
      ...formData,
      experience: formData.experience ? Number(formData.experience) : undefined,
    });
  };

  const displayData = userData || user;

  if (isLoading) {
    return (
      <MainLayout>
        <div className="text-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading profile...</p>
        </div>
      </MainLayout>
    );
  }

  return (
    <MainLayout>
      <div className="mx-auto max-w-6xl space-y-6">
        <section className="rounded-[2rem] border border-white/80 bg-[linear-gradient(135deg,#ffffff_0%,#f8fafc_55%,#eef2ff_100%)] p-6 shadow-[0_16px_40px_rgba(15,23,42,0.08)] sm:p-8">
          <div className="flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
            <div className="max-w-2xl">
              <p className="text-xs font-semibold uppercase tracking-[0.28em] text-indigo-600">
                Profile studio
              </p>
              <h1 className="mt-3 text-4xl font-semibold tracking-tight text-slate-900">
                My Profile
              </h1>
              <p className="mt-3 text-base leading-7 text-slate-600">
                Shape your public mentor presence with a polished bio, role, company, and links.
              </p>
            </div>
          <button
            onClick={() => setIsEditing(!isEditing)}
              className="inline-flex items-center justify-center rounded-2xl bg-[linear-gradient(135deg,#4f46e5_0%,#6366f1_55%,#7c3aed_100%)] px-5 py-3 text-sm font-semibold text-white shadow-[0_16px_32px_rgba(79,70,229,0.26)] transition hover:-translate-y-0.5"
          >
            {isEditing ? 'Cancel' : 'Edit Profile'}
          </button>
          </div>
        </section>

        {/* Profile Card */}
        <div className="rounded-[2rem] border border-white/80 bg-white/92 p-6 shadow-[0_16px_40px_rgba(15,23,42,0.08)] sm:p-8">
          {isEditing ? (
            <form onSubmit={handleSubmit} className="space-y-6">
              {/* Basic Info */}
              <div className="rounded-[1.5rem] border border-slate-200/80 bg-slate-50/80 p-5">
                <h2 className="text-lg font-semibold text-slate-900 mb-4">Basic Information</h2>
                <div className="grid gap-4 md:grid-cols-2">
                  <div>
                    <label className="mb-2 block text-sm font-medium text-slate-700">
                      First Name
                    </label>
                    <input
                      type="text"
                      name="firstName"
                      value={formData.firstName}
                      onChange={handleChange}
                      className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-100"
                    />
                  </div>
                  <div>
                    <label className="mb-2 block text-sm font-medium text-slate-700">
                      Last Name
                    </label>
                    <input
                      type="text"
                      name="lastName"
                      value={formData.lastName}
                      onChange={handleChange}
                      className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-100"
                    />
                  </div>
                </div>
              </div>

              {/* Professional Info */}
              <div className="rounded-[1.5rem] border border-slate-200/80 bg-slate-50/80 p-5">
                <h2 className="text-lg font-semibold text-slate-900 mb-4">Professional Information</h2>
                <div className="space-y-4">
                  <div>
                    <label className="mb-2 block text-sm font-medium text-slate-700">
                      Headline
                    </label>
                    <input
                      type="text"
                      name="headline"
                      value={formData.headline}
                      onChange={handleChange}
                      placeholder="e.g., Senior Software Engineer"
                      className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-100"
                    />
                  </div>

                  <div className="grid gap-4 md:grid-cols-2">
                    <div>
                      <label className="mb-2 block text-sm font-medium text-slate-700">
                        Role
                      </label>
                      <input
                        type="text"
                        name="role"
                        value={formData.role}
                        onChange={handleChange}
                        placeholder="e.g., Full Stack Developer"
                        className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-100"
                      />
                    </div>
                    <div>
                      <label className="mb-2 block text-sm font-medium text-slate-700">
                        Company
                      </label>
                      <input
                        list="organization-suggestions"
                        type="text"
                        name="company"
                        value={formData.company}
                        onChange={handleChange}
                        placeholder="e.g., Google, Apple, SteepGraph"
                        className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-100"
                      />
                      <datalist id="organization-suggestions">
                        {organizationSuggestions.map((organization) => (
                          <option key={organization} value={organization} />
                        ))}
                      </datalist>
                    </div>
                  </div>

                  <div className="grid gap-4 md:grid-cols-2">
                    <div>
                      <label className="mb-2 block text-sm font-medium text-slate-700">
                        Experience
                      </label>
                      <input
                        type="number"
                        name="experience"
                        value={formData.experience}
                        onChange={handleChange}
                        min="0"
                        placeholder="e.g., 5"
                        className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-100"
                      />
                    </div>
                    <div>
                      <label className="mb-2 block text-sm font-medium text-slate-700">
                        Location
                      </label>
                      <input
                        type="text"
                        name="location"
                        value={formData.location}
                        onChange={handleChange}
                        placeholder="e.g., San Francisco, CA"
                        className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-100"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="mb-2 block text-sm font-medium text-slate-700">
                      Bio
                    </label>
                    <textarea
                      name="bio"
                      value={formData.bio}
                      onChange={handleChange}
                      placeholder="Tell us about yourself..."
                      rows={4}
                      className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-100"
                    />
                  </div>
                </div>
              </div>

              {/* Social Links */}
              <div className="rounded-[1.5rem] border border-slate-200/80 bg-slate-50/80 p-5">
                <h2 className="text-lg font-semibold text-slate-900 mb-4">Social Links</h2>
                <div className="grid gap-4 md:grid-cols-2">
                  <div>
                    <label className="mb-2 block text-sm font-medium text-slate-700">
                      LinkedIn
                    </label>
                    <input
                      type="text"
                      name="linkedIn"
                      value={formData.linkedIn}
                      onChange={handleChange}
                      placeholder="linkedin.com/in/username"
                      className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-100"
                    />
                  </div>
                  <div>
                    <label className="mb-2 block text-sm font-medium text-slate-700">
                      GitHub
                    </label>
                    <input
                      type="text"
                      name="github"
                      value={formData.github}
                      onChange={handleChange}
                      placeholder="github.com/username"
                      className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-100"
                    />
                  </div>
                </div>
              </div>

              <button
                type="submit"
                disabled={updateMutation.isPending}
                className="w-full rounded-2xl bg-[linear-gradient(135deg,#4f46e5_0%,#6366f1_55%,#7c3aed_100%)] py-3.5 font-semibold text-white shadow-[0_16px_32px_rgba(79,70,229,0.26)] transition hover:-translate-y-0.5 disabled:bg-slate-300 disabled:shadow-none"
              >
                {updateMutation.isPending ? 'Saving...' : 'Save Changes'}
              </button>
            </form>
          ) : (
            <div className="space-y-8">
              {/* Profile Header */}
              <div className="flex flex-col gap-6 md:flex-row md:items-start">
                <div className="flex h-24 w-24 items-center justify-center rounded-[1.5rem] bg-[linear-gradient(135deg,#e0e7ff_0%,#ede9fe_100%)] shadow-inner">
                  <UserIcon className="h-12 w-12 text-indigo-600" />
                </div>
                <div className="flex-1">
                  <div className="inline-flex rounded-full bg-indigo-50 px-3 py-1 text-xs font-semibold uppercase tracking-[0.24em] text-indigo-700">
                    Profile preview
                  </div>
                  <h2 className="mt-3 text-3xl font-semibold tracking-tight text-slate-900">
                    {displayData?.firstName} {displayData?.lastName}
                  </h2>
                  {displayData?.headline && (
                    <p className="mt-2 text-lg text-slate-600">{displayData.headline}</p>
                  )}
                  <p className="mt-3 text-sm text-slate-500">{displayData?.email}</p>
                </div>
              </div>

              {/* Professional Info */}
              {(displayData?.role || displayData?.company || displayData?.experience) && (
                <div className="rounded-[1.5rem] border border-slate-200/80 bg-slate-50/80 p-5">
                  <h3 className="mb-4 text-lg font-semibold text-slate-900">Professional Information</h3>
                  <div className="grid gap-4 md:grid-cols-2">
                    {displayData?.role && (
                      <div className="rounded-2xl bg-white px-4 py-3">
                        <p className="text-xs uppercase tracking-[0.18em] text-slate-500">Role</p>
                        <p className="mt-1 font-medium text-slate-900">{displayData.role}</p>
                      </div>
                    )}
                    {displayData?.company && (
                      <div className="rounded-2xl bg-white px-4 py-3">
                        <p className="text-xs uppercase tracking-[0.18em] text-slate-500">Company</p>
                        <p className="mt-1 font-medium text-slate-900">{displayData.company}</p>
                      </div>
                    )}
                    {displayData?.experience && (
                      <div className="rounded-2xl bg-white px-4 py-3">
                        <p className="text-xs uppercase tracking-[0.18em] text-slate-500">Experience</p>
                        <p className="mt-1 font-medium text-slate-900">{displayData.experience}</p>
                      </div>
                    )}
                    {displayData?.location && (
                      <div className="rounded-2xl bg-white px-4 py-3">
                        <p className="text-xs uppercase tracking-[0.18em] text-slate-500">Location</p>
                        <p className="mt-1 font-medium text-slate-900">{displayData.location}</p>
                      </div>
                    )}
                  </div>
                </div>
              )}

              {displayData?.bio && (
                <div className="rounded-[1.5rem] border border-slate-200/80 bg-white p-5">
                  <h3 className="mb-3 text-lg font-semibold text-slate-900">Bio</h3>
                  <p className="leading-7 text-slate-600">{displayData.bio}</p>
                </div>
              )}

              {/* Social Links */}
              {(displayData?.linkedIn || displayData?.github) && (
                <div className="rounded-[1.5rem] border border-slate-200/80 bg-slate-50/80 p-5">
                  <h3 className="mb-3 text-lg font-semibold text-slate-900">Social Links</h3>
                  <div className="flex flex-wrap gap-3">
                    {displayData?.linkedIn && (
                      <a
                        href={displayData.linkedIn}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="rounded-full bg-white px-4 py-2 text-sm font-medium text-indigo-700 shadow-sm transition hover:-translate-y-0.5"
                      >
                        LinkedIn
                      </a>
                    )}
                    {displayData?.github && (
                      <a
                        href={displayData.github}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="rounded-full bg-white px-4 py-2 text-sm font-medium text-indigo-700 shadow-sm transition hover:-translate-y-0.5"
                      >
                        GitHub
                      </a>
                    )}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </MainLayout>
  );
};
