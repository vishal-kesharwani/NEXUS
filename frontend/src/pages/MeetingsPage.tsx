import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { MainLayout } from '../layouts/MainLayout';
import { API_BASE_URL, meetingService } from '../services/api';
import { Calendar, Clock, Video, User, Check, X, Hourglass } from 'lucide-react';
import React from 'react';

export const MeetingsPage: React.FC = () => {
  const queryClient = useQueryClient();
  const currentUserId = localStorage.getItem('userId');
  const googleConnectUrl = `${API_BASE_URL}/google/oauth/connect?token=${encodeURIComponent(localStorage.getItem('token') || '')}`;

  const { data: meetings = [], isLoading } = useQuery({
    queryKey: ['meetings'],
    queryFn: () => meetingService.getMeetings().then((res: any) => res.data),
  });

  const acceptMutation = useMutation({
    mutationFn: (id: string) => meetingService.acceptMeeting(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['meetings'] });
    },
    onError: (err: any) => {
      if (err.response?.status === 428) {
        alert('The session organizer needs to connect their Google account before this can be accepted.');
      } else {
        alert(err.response?.data?.message || 'Failed to accept session.');
      }
    },
  });

  const declineMutation = useMutation({
    mutationFn: (id: string) => meetingService.declineMeeting(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['meetings'] });
    },
    onError: (err: any) => {
      alert(err.response?.data?.message || 'Failed to decline session.');
    },
  });

  return (
    <MainLayout>
      <div className="mx-auto max-w-5xl">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-3xl font-bold tracking-tight text-slate-900">Your Sessions</h1>
            <p className="mt-1 text-sm text-slate-500">View, accept, and join your mentorship sessions.</p>
          </div>
        </div>

        {isLoading ? (
          <div className="mt-12 flex justify-center">
            <div className="h-8 w-8 animate-spin rounded-full border-4 border-indigo-600 border-t-transparent"></div>
          </div>
        ) : meetings.length === 0 ? (
          <div className="mt-12 flex flex-col items-center justify-center rounded-[2rem] border border-dashed border-slate-200 bg-white/50 p-12 text-center backdrop-blur-xl">
            <div className="grid h-16 w-16 place-items-center rounded-2xl bg-indigo-50 text-indigo-600">
              <Calendar size={28} />
            </div>
            <h3 className="mt-4 text-lg font-semibold text-slate-900">No scheduled sessions</h3>
            <p className="mt-2 max-w-sm text-sm text-slate-500">
              You haven't scheduled any mentorship sessions yet. Open any of your active chats to schedule a session.
            </p>
          </div>
        ) : (
          <div className="mt-8 grid gap-4 sm:grid-cols-2">
            {meetings.map((meeting: any) => {
              const date = new Date(meeting.scheduledAt);
              const formattedDate = date.toLocaleDateString(undefined, {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric',
              });
              const formattedTime = date.toLocaleTimeString(undefined, {
                hour: '2-digit',
                minute: '2-digit',
              });

              const isRecipient = String(meeting.recipientId) === String(currentUserId);
              const isPending = meeting.status === 'PENDING';
              const isAccepted = meeting.status === 'ACCEPTED';
              const isDeclined = meeting.status === 'DECLINED';

              const statusStyles = isAccepted
                ? 'bg-emerald-50 text-emerald-700'
                : isDeclined
                ? 'bg-rose-50 text-rose-700'
                : 'bg-indigo-50 text-indigo-600';

              return (
                <div
                  key={meeting.id}
                  className="group relative overflow-hidden rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm transition hover:shadow-md hover:border-slate-300"
                >
                  <div className="flex items-start justify-between">
                    <div className={`flex items-center gap-2 text-xs font-semibold uppercase tracking-wider px-2.5 py-1 rounded-full ${statusStyles}`}>
                      <Clock size={12} />
                      {meeting.status || 'PENDING'}
                    </div>
                  </div>

                  <div className="mt-4">
                    <h3 className="font-bold text-slate-900 text-lg">{formattedDate}</h3>
                    <div className="mt-2 flex items-center gap-2 text-sm text-slate-500">
                      <Clock size={16} />
                      <span>{formattedTime}</span>
                    </div>
                    {meeting.creatorName && (
                      <div className="mt-2 flex items-center gap-2 text-sm text-slate-500">
                        <User size={16} />
                        <span>Scheduled by {meeting.creatorName}</span>
                      </div>
                    )}
                  </div>

                  <div className="mt-6 flex items-center gap-3">
                    {isAccepted && meeting.meetLink && (
                      <a
                        href={meeting.meetLink}
                        target="_blank"
                        rel="noreferrer"
                        className="flex-1 flex items-center justify-center gap-2 rounded-2xl bg-indigo-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-indigo-700 shadow-sm"
                      >
                        <Video size={16} />
                        Join Google Meet
                      </a>
                    )}

                    {isPending && isRecipient && (
                      <>
                        <button
                          onClick={() => acceptMutation.mutate(meeting.id)}
                          disabled={acceptMutation.isPending}
                          className="flex-1 flex items-center justify-center gap-2 rounded-2xl bg-emerald-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-emerald-700 shadow-sm disabled:opacity-50"
                        >
                          <Check size={16} />
                          Accept
                        </button>
                        <button
                          onClick={() => declineMutation.mutate(meeting.id)}
                          disabled={declineMutation.isPending}
                          className="flex-1 flex items-center justify-center gap-2 rounded-2xl border border-slate-200 px-4 py-3 text-sm font-semibold text-slate-600 transition hover:bg-slate-50 disabled:opacity-50"
                        >
                          <X size={16} />
                          Decline
                        </button>
                      </>
                    )}

                    {isPending && !isRecipient && !meeting.organizerGoogleConnected && (
                      <a
                        href={googleConnectUrl}
                        className="flex-1 flex items-center justify-center gap-2 rounded-2xl bg-amber-500 px-4 py-3 text-sm font-semibold text-white transition hover:bg-amber-600 shadow-sm"
                      >
                        Connect Google Calendar
                      </a>
                    )}

                    {isPending && !isRecipient && meeting.organizerGoogleConnected && (
                      <div className="flex-1 flex items-center justify-center gap-2 rounded-2xl bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-500">
                        <Hourglass size={16} />
                        Waiting for them to accept
                      </div>
                    )}

                    {isDeclined && (
                      <div className="flex-1 flex items-center justify-center gap-2 rounded-2xl bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-400">
                        Session declined
                      </div>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </MainLayout>
  );
};
