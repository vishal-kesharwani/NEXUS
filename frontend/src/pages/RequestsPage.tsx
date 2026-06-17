import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { MainLayout } from '../layouts/MainLayout';
import { mentorshipService } from '../services/api';
import { CheckCircle, Clock, MessageSquareText, XCircle } from 'lucide-react';

export const RequestsPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [activeTab, setActiveTab] = useState<'sent' | 'received'>('sent');

  const { data: sentRequests, isLoading: sentLoading } = useQuery({
    queryKey: ['sentRequests'],
    queryFn: () => mentorshipService.getSentRequests().then((res) => res.data),
  });

  const { data: receivedRequests, isLoading: receivedLoading } = useQuery({
    queryKey: ['receivedRequests'],
    queryFn: () => mentorshipService.getReceivedRequests().then((res) => res.data),
  });

  const acceptMutation = useMutation({
    mutationFn: (id: string) => mentorshipService.acceptRequest(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['receivedRequests'] });
    },
  });

  const rejectMutation = useMutation({
    mutationFn: (id: string) => mentorshipService.rejectRequest(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['receivedRequests'] });
    },
  });

  const isLoading = activeTab === 'sent' ? sentLoading : receivedLoading;
  const requests = activeTab === 'sent' ? sentRequests : receivedRequests;

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'PENDING':
        return (
          <span className="inline-flex items-center gap-1 rounded-full bg-amber-50 px-3 py-1 text-xs font-semibold text-amber-700">
            <Clock size={12} />
            Pending
          </span>
        );
      case 'ACCEPTED':
        return (
          <span className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700">
            <CheckCircle size={12} />
            Accepted
          </span>
        );
      case 'REJECTED':
        return (
          <span className="inline-flex items-center gap-1 rounded-full bg-rose-50 px-3 py-1 text-xs font-semibold text-rose-700">
            <XCircle size={12} />
            Rejected
          </span>
        );
      default:
        return null;
    }
  };

  return (
    <MainLayout>
      <div className="mx-auto max-w-6xl space-y-6">
        <section className="rounded-[2rem] border border-white/80 bg-[linear-gradient(135deg,#ffffff_0%,#f8fafc_55%,#eef2ff_100%)] p-6 shadow-[0_16px_40px_rgba(15,23,42,0.08)] sm:p-8">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
            <div className="max-w-2xl">
              <div className="mb-3 inline-flex items-center gap-2 rounded-full bg-indigo-50 px-3 py-1 text-xs font-semibold uppercase tracking-[0.24em] text-indigo-700">
                <MessageSquareText size={14} />
                Request hub
              </div>
              <h1 className="text-4xl font-semibold tracking-tight text-slate-900">Mentorship Requests</h1>
              <p className="mt-3 text-base leading-7 text-slate-600">
                Manage your mentorship conversations with a cleaner, app-like request flow.
              </p>
            </div>
            <div className="flex rounded-2xl border border-slate-200 bg-white p-1 shadow-sm">
              <button
                onClick={() => setActiveTab('sent')}
                className={`rounded-2xl px-5 py-3 text-sm font-semibold transition ${
                  activeTab === 'sent'
                    ? 'bg-[linear-gradient(135deg,#4f46e5_0%,#6366f1_55%,#7c3aed_100%)] text-white shadow-lg'
                    : 'text-slate-600 hover:bg-slate-50'
                }`}
              >
                Sent ({sentRequests?.length || 0})
              </button>
              <button
                onClick={() => setActiveTab('received')}
                className={`rounded-2xl px-5 py-3 text-sm font-semibold transition ${
                  activeTab === 'received'
                    ? 'bg-[linear-gradient(135deg,#4f46e5_0%,#6366f1_55%,#7c3aed_100%)] text-white shadow-lg'
                    : 'text-slate-600 hover:bg-slate-50'
                }`}
              >
                Received ({receivedRequests?.length || 0})
              </button>
            </div>
          </div>
        </section>

        {isLoading && (
          <div className="flex min-h-[40vh] items-center justify-center">
            <div className="text-center">
              <div className="mx-auto h-14 w-14 animate-spin rounded-full border-4 border-indigo-200 border-t-indigo-600" />
              <p className="mt-4 text-sm font-medium text-slate-500">Loading requests...</p>
            </div>
          </div>
        )}

        {!isLoading && requests && requests.length > 0 ? (
          <div className="space-y-4">
            {requests.map((request) => (
              <div
                key={request.id}
                className="rounded-[1.6rem] border border-white/80 bg-white/92 p-5 shadow-[0_16px_40px_rgba(15,23,42,0.08)] transition hover:-translate-y-0.5"
              >
                <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
                  <div className="flex-1">
                    <div className="mb-2 flex items-center gap-3">
                      <h3 className="text-lg font-semibold text-slate-900">
                        {activeTab === 'sent'
                          ? `Request to Mentor #${request.id.slice(0, 8)}`
                          : `Request from #${request.id.slice(0, 8)}`}
                      </h3>
                      {getStatusBadge(request.status)}
                    </div>
                    {request.message && (
                      <p className="text-sm leading-6 text-slate-600">{request.message}</p>
                    )}
                    <p className="mt-3 text-xs uppercase tracking-[0.18em] text-slate-400">
                      {new Date(request.createdAt).toLocaleDateString()}
                    </p>
                  </div>

                  {activeTab === 'received' && request.status === 'PENDING' && (
                    <div className="flex gap-3">
                      <button
                        onClick={() => acceptMutation.mutate(request.id)}
                        disabled={acceptMutation.isPending}
                        className="inline-flex items-center gap-2 rounded-2xl bg-emerald-600 px-4 py-3 text-sm font-semibold text-white shadow-lg transition hover:-translate-y-0.5 disabled:bg-slate-300"
                      >
                        <CheckCircle size={16} />
                        Accept
                      </button>
                      <button
                        onClick={() => rejectMutation.mutate(request.id)}
                        disabled={rejectMutation.isPending}
                        className="inline-flex items-center gap-2 rounded-2xl bg-rose-600 px-4 py-3 text-sm font-semibold text-white shadow-lg transition hover:-translate-y-0.5 disabled:bg-slate-300"
                      >
                        <XCircle size={16} />
                        Reject
                      </button>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        ) : !isLoading ? (
          <div className="rounded-[1.8rem] border border-white/80 bg-white/92 p-12 text-center shadow-[0_16px_40px_rgba(15,23,42,0.08)]">
            <p className="text-slate-700">
              {activeTab === 'sent'
                ? "You haven't sent any mentorship requests yet."
                : "You don't have any incoming mentorship requests yet."}
            </p>
          </div>
        ) : null}
      </div>
    </MainLayout>
  );
};
