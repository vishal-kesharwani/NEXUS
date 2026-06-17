import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { MainLayout } from '../layouts/MainLayout';
import { notificationService } from '../services/api';
import { Bell, Check } from 'lucide-react';
import React from 'react';

export const NotificationsPage: React.FC = () => {
  const queryClient = useQueryClient();

  const { data: notifications = [], isLoading } = useQuery({
    queryKey: ['notifications'],
    queryFn: () => notificationService.getNotifications().then((res: any) => res.data),
  });

  const readMutation = useMutation({
    mutationFn: (id: string) => notificationService.markRead(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['unread-notifications-count'] });
    },
  });

  return (
    <MainLayout>
      <div className="mx-auto max-w-4xl">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold tracking-tight text-slate-900">Notifications</h1>
            <p className="mt-1 text-sm text-slate-500">Stay updated on your mentorship requests and messages.</p>
          </div>
        </div>

        {isLoading ? (
          <div className="mt-12 flex justify-center">
            <div className="h-8 w-8 animate-spin rounded-full border-4 border-indigo-600 border-t-transparent"></div>
          </div>
        ) : notifications.length === 0 ? (
          <div className="mt-12 flex flex-col items-center justify-center rounded-[2rem] border border-dashed border-slate-200 bg-white/50 p-12 text-center backdrop-blur-xl">
            <div className="grid h-16 w-16 place-items-center rounded-2xl bg-indigo-50 text-indigo-600">
              <Bell size={28} />
            </div>
            <h3 className="mt-4 text-lg font-semibold text-slate-900">All caught up!</h3>
            <p className="mt-2 max-w-sm text-sm text-slate-500">
              You don't have any notifications right now. We'll alert you when something happens.
            </p>
          </div>
        ) : (
          <div className="mt-8 space-y-3">
            {notifications.map((notif: any) => (
              <div
                key={notif.id}
                className={`flex items-start justify-between rounded-2xl border p-5 transition-all ${
                  notif.read
                    ? 'border-slate-100 bg-white/40 opacity-70'
                    : 'border-slate-200 bg-white shadow-sm hover:border-indigo-200'
                }`}
              >
                <div className="flex gap-4">
                  <div
                    className={`mt-1 flex h-10 w-10 shrink-0 items-center justify-center rounded-xl ${
                      notif.read ? 'bg-slate-100 text-slate-400' : 'bg-indigo-50 text-indigo-600'
                    }`}
                  >
                    <Bell size={18} />
                  </div>
                  <div>
                    <h3 className="font-semibold text-slate-900">{notif.title}</h3>
                    <p className="mt-1.5 text-sm text-slate-600 leading-relaxed">{notif.message}</p>
                    <span className="mt-2 inline-block text-xs text-slate-400">
                      {new Date(notif.createdAt).toLocaleString()}
                    </span>
                  </div>
                </div>

                {!notif.read && (
                  <button
                    onClick={() => readMutation.mutate(notif.id)}
                    disabled={readMutation.isPending}
                    className="flex h-9 w-9 items-center justify-center rounded-xl bg-slate-50 text-slate-500 hover:bg-indigo-50 hover:text-indigo-600 transition"
                    title="Mark as Read"
                  >
                    <Check size={16} />
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </MainLayout>
  );
};