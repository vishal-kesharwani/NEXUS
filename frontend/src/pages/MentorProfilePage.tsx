import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { MainLayout } from '../layouts/MainLayout';
import { mentorProfileService, reviewService } from '../services/api';
import { Star, MessageSquare, ArrowLeft, Send } from 'lucide-react';
import React, { useState } from 'react';

export const MentorProfilePage: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const currentUserId = localStorage.getItem('userId');

  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: ['mentor-profile', id],
    queryFn: () => mentorProfileService.getMentorProfile(id!).then((res) => res.data),
    enabled: !!id,
  });

  const reviewMutation = useMutation({
    mutationFn: () =>
      reviewService.createReview({
        mentorId: id!,
        rating,
        comment: comment.trim(),
      }),
    onSuccess: () => {
      setComment('');
      setRating(5);
      queryClient.invalidateQueries({ queryKey: ['mentor-profile', id] });
      alert('Thank you! Your review has been submitted.');
    },
    onError: (err: any) => {
      alert(err.response?.data?.message || 'Failed to submit review. Have you already reviewed this mentor?');
    },
  });

  const handleReviewSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!comment.trim()) return;
    reviewMutation.mutate();
  };

  if (isLoading) {
    return (
      <MainLayout>
        <div className="flex h-[50vh] items-center justify-center">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-indigo-600 border-t-transparent"></div>
        </div>
      </MainLayout>
    );
  }

  if (!data) {
    return (
      <MainLayout>
        <div className="text-center py-12">
          <h2 className="text-xl font-semibold text-slate-800">Mentor profile not found</h2>
          <button
            onClick={() => navigate('/mentors')}
            className="mt-4 inline-flex items-center gap-2 text-indigo-600 hover:text-indigo-700"
          >
            <ArrowLeft size={16} /> Back to discovery
          </button>
        </div>
      </MainLayout>
    );
  }

  const { user, averageRating = 0, reviews = [] } = data;
  const isSelf = String(user.id) === String(currentUserId);

  return (
    <MainLayout>
      <div className="mx-auto max-w-4xl pb-12">
        <button
          onClick={() => navigate(-1)}
          className="mb-6 flex items-center gap-2 text-sm font-semibold text-slate-500 hover:text-slate-800 transition"
        >
          <ArrowLeft size={16} /> Back
        </button>

        {/* Profile Info Header */}
        <div className="overflow-hidden rounded-[2.5rem] border border-slate-200 bg-white shadow-sm">
          <div className="bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500 h-32 w-full"></div>
          <div className="px-8 pb-8">
            <div className="relative -mt-16 flex items-end justify-between">
              <div className="grid h-28 w-28 place-items-center rounded-3xl bg-gradient-to-br from-indigo-500 to-purple-500 text-3xl font-black text-white border-4 border-white shadow-md">
                {user.firstName?.charAt(0)}{user.lastName?.charAt(0)}
              </div>
            </div>

            <div className="mt-6">
              <h1 className="text-3xl font-bold text-slate-900">
                {user.firstName} {user.lastName}
              </h1>
              <p className="mt-2 text-lg font-medium text-slate-600">
                {user.headline || 'Mentor & Advisor'}
              </p>
              {user.company && (
                <p className="mt-1 text-sm text-slate-500">
                  {user.currentRole || 'Software Engineer'} at <span className="font-semibold text-slate-700">{user.company}</span>
                </p>
              )}
            </div>

            <div className="mt-6 flex items-center gap-6 border-y border-slate-100 py-4">
              <div>
                <span className="block text-xs uppercase tracking-wider text-slate-400 font-semibold">Experience</span>
                <span className="mt-1 block font-bold text-slate-800 text-lg">{user.experienceYears || 3} years</span>
              </div>
              <div className="h-8 w-px bg-slate-100"></div>
              <div>
                <span className="block text-xs uppercase tracking-wider text-slate-400 font-semibold">Rating</span>
                <span className="mt-1 flex items-center gap-1.5 font-bold text-slate-800 text-lg">
                  <Star size={18} className="fill-amber-500 text-amber-500" />
                  {averageRating > 0 ? averageRating : 'No reviews'}
                </span>
              </div>
            </div>

            <div className="mt-6">
              <h2 className="text-sm font-bold uppercase tracking-wider text-slate-400">About Me</h2>
              <p className="mt-2 text-sm leading-relaxed text-slate-600 whitespace-pre-wrap">
                {user.bio || 'This mentor has not written a bio yet.'}
              </p>
            </div>
          </div>
        </div>

        {/* Reviews Section */}
        <div className="mt-8 grid gap-8 md:grid-cols-12">
          {/* Reviews List */}
          <div className={`${isSelf ? 'md:col-span-12' : 'md:col-span-7'} space-y-4`}>
            <div className="flex items-center gap-2">
              <MessageSquare size={20} className="text-indigo-600" />
              <h2 className="text-xl font-bold text-slate-900">Reviews ({reviews.length})</h2>
            </div>

            {reviews.length === 0 ? (
              <div className="rounded-[2rem] border border-dashed border-slate-200 bg-white/50 p-8 text-center">
                <p className="text-sm text-slate-500">No reviews yet. Be the first to leave a review!</p>
              </div>
            ) : (
              <div className="space-y-3">
                {reviews.map((review: any) => (
                  <div key={review.id} className="rounded-2xl border border-slate-100 bg-white p-5 shadow-sm">
                    <div className="flex items-center justify-between">
                      <h4 className="font-semibold text-slate-800">{review.reviewerName}</h4>
                      <span className="flex items-center gap-1 text-sm font-bold text-amber-500">
                        <Star size={14} className="fill-amber-500 text-amber-500" />
                        {review.rating}
                      </span>
                    </div>
                    <p className="mt-2 text-sm text-slate-600">{review.comment}</p>
                    <span className="mt-3 inline-block text-[10px] text-slate-400">
                      {new Date(review.createdAt).toLocaleDateString()}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Add Review Form (Hide if viewing own profile) */}
          {!isSelf && (
            <div className="md:col-span-5">
              <div className="sticky top-6 rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm">
                <h3 className="text-lg font-bold text-slate-900">Leave a Review</h3>
                <p className="mt-1 text-xs text-slate-500">Share your mentorship experience with this mentor.</p>

                <form onSubmit={handleReviewSubmit} className="mt-4 space-y-4">
                  <div>
                    <label className="block text-xs font-bold uppercase tracking-wider text-slate-400">Rating</label>
                    <div className="mt-2 flex gap-1.5">
                      {[1, 2, 3, 4, 5].map((star) => (
                        <button
                          key={star}
                          type="button"
                          onClick={() => setRating(star)}
                          className="text-amber-500 transition hover:scale-110"
                        >
                          <Star
                            size={28}
                            className={star <= rating ? 'fill-amber-500 text-amber-500' : 'text-slate-300'}
                          />
                        </button>
                      ))}
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs font-bold uppercase tracking-wider text-slate-400">Comment</label>
                    <textarea
                      value={comment}
                      onChange={(e) => setComment(e.target.value)}
                      required
                      placeholder="How did this mentor help you? Write your feedback here..."
                      rows={4}
                      className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition"
                    />
                  </div>

                  <button
                    type="submit"
                    disabled={reviewMutation.isPending || !comment.trim()}
                    className="flex w-full items-center justify-center gap-2 rounded-2xl bg-indigo-600 py-3 text-sm font-semibold text-white shadow hover:bg-indigo-700 disabled:opacity-50 transition"
                  >
                    <Send size={16} />
                    {reviewMutation.isPending ? 'Submitting...' : 'Submit Review'}
                  </button>
                </form>
              </div>
            </div>
          )}
        </div>
      </div>
    </MainLayout>
  );
};