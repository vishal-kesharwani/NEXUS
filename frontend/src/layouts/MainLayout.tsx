import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { 
  LayoutDashboard, Menu, Search, Sparkles, X, LogOut, UserRound, 
  BadgeInfo, MessageSquareText, Bell, Calendar 
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useQuery } from '@tanstack/react-query';
import { notificationService } from '../services/api';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const getWsUrl = () => {
  const rawApiUrl = import.meta.env.VITE_API_URL?.trim();
  const apiBase = rawApiUrl ? rawApiUrl.replace(/\/+$/, '') : 'http://localhost:8080/api';
  return apiBase.replace(/\/api$/, '') + '/ws';
};

const Sidebar: React.FC = () => {
  const location = useLocation();
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [isOpen, setIsOpen] = useState(false);
  const currentUserId = localStorage.getItem('userId');

  // Query for unread notifications count
  const { data: unreadCount = 0, refetch: refetchUnreadCount } = useQuery({
    queryKey: ['unread-notifications-count'],
    queryFn: () => notificationService.getUnreadCount().then((res) => res.data),
    enabled: !!currentUserId,
  });

  // Subscribe to real-time notification updates
  useEffect(() => {
    if (!currentUserId) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(getWsUrl()),
      connectHeaders: {
        Authorization: `Bearer ${localStorage.getItem('token')}`,
      },
      onConnect: () => {
        client.subscribe(`/topic/notifications/${currentUserId}`, () => {
          refetchUnreadCount();
        });
      },
    });

    client.activate();

    return () => {
      client.deactivate();
    };
  }, [currentUserId]);

  const menuItems = [
    { label: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
    { label: 'Profile', path: '/profile', icon: UserRound },
    { label: 'Skills', path: '/skills', icon: BadgeInfo },
    { label: 'Find Mentors', path: '/mentors', icon: Search },
    { label: 'Requests', path: '/requests', icon: MessageSquareText },
    { label: 'Chat', path: '/chat', icon: MessageSquareText },
    { label: 'Meetings', path: '/meetings', icon: Calendar },
    { label: 'Notifications', path: '/notifications', icon: Bell, badge: unreadCount },
  ];

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path: string) => location.pathname === path;

  return (
    <>
      {/* Mobile Menu Button */}
      <div className="lg:hidden fixed top-4 left-4 z-50">
        <button
          onClick={() => setIsOpen(!isOpen)}
          className="flex h-11 w-11 items-center justify-center rounded-2xl border border-white/70 bg-white/90 text-slate-700 shadow-lg backdrop-blur"
        >
          {isOpen ? <X size={24} /> : <Menu size={24} />}
        </button>
      </div>

      {/* Sidebar */}
      <aside
        className={`${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        } lg:translate-x-0 fixed lg:relative top-0 left-0 h-screen w-[288px] border-r border-white/10 bg-[linear-gradient(180deg,#081226_0%,#101b38_100%)] transition-transform duration-300 z-40 shadow-[0_32px_80px_rgba(2,6,23,0.35)]`}
      >
        <div className="flex h-full flex-col px-5 py-6">
          <div className="flex items-center gap-3 rounded-3xl border border-white/10 bg-white/5 p-4 shadow-inner">
            <div className="grid h-12 w-12 place-items-center rounded-2xl bg-gradient-to-br from-cyan-400 to-indigo-500 text-lg font-black text-white shadow-lg">
              K
            </div>
            <div>
              <h1 className="text-lg font-semibold tracking-tight text-white">Knowledge Nexus</h1>
              <p className="text-sm text-slate-300">Connect. Learn. Grow.</p>
            </div>
          </div>

          <div className="mt-6 rounded-3xl border border-white/10 bg-[radial-gradient(circle_at_top,_rgba(99,102,241,0.25),_rgba(8,18,38,0.1)_55%,_rgba(8,18,38,0.85)_100%)] p-5 text-white">
            <div className="flex items-center gap-2 text-xs uppercase tracking-[0.24em] text-cyan-200/80">
              <Sparkles size={14} />
              Premium space
            </div>
            <p className="mt-3 text-sm leading-6 text-slate-200">
              Mentor discovery, requests, and profile growth in one focused workspace.
            </p>
          </div>

          <nav className="mt-6 space-y-2 overflow-y-auto max-h-[50vh]">
            {menuItems.map((item) => {
              const Icon = item.icon;
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  onClick={() => setIsOpen(false)}
                  className={`flex items-center justify-between rounded-2xl px-4 py-3.5 text-sm font-medium transition-all ${
                    isActive(item.path)
                      ? 'bg-white/12 text-white shadow-[inset_0_1px_0_rgba(255,255,255,0.08)]'
                      : 'text-slate-300 hover:bg-white/8 hover:text-white'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <span
                      className={`grid h-9 w-9 place-items-center rounded-xl ${
                        isActive(item.path)
                          ? 'bg-white/12 text-cyan-200'
                          : 'bg-white/5 text-slate-300'
                      }`}
                    >
                      <Icon size={18} />
                    </span>
                    {item.label}
                  </div>

                  {item.badge !== undefined && item.badge > 0 && (
                    <span className="flex h-5 min-w-5 items-center justify-center rounded-full bg-rose-500 px-1.5 text-[10px] font-bold text-white">
                      {item.badge}
                    </span>
                  )}
                </Link>
              );
            })}
          </nav>

          <div className="mt-auto">
            <button
              onClick={handleLogout}
              className="flex w-full items-center justify-center gap-2 rounded-2xl border border-white/10 bg-white/5 px-4 py-3.5 text-sm font-semibold text-white transition hover:bg-white/10"
            >
              <LogOut size={18} />
              Logout
            </button>
          </div>
        </div>
      </aside>

      {/* Overlay */}
      {isOpen && (
        <div
          className="lg:hidden fixed inset-0 z-30 bg-slate-950/60 backdrop-blur-sm"
          onClick={() => setIsOpen(false)}
        />
      )}
    </>
  );
};

interface MainLayoutProps {
  children: React.ReactNode;
}

export const MainLayout: React.FC<MainLayoutProps> = ({ children }) => {
  return (
    <div className="flex h-screen overflow-hidden bg-[radial-gradient(circle_at_top_left,_#eef2ff_0%,_#f8fafc_42%,_#f1f5f9_100%)] text-slate-900">
      <Sidebar />
      <main
        className="
          flex-1
          h-screen
          overflow-hidden
        "
      >
        <div
          className="
            h-full
            overflow-y-auto
            px-4
            py-4
            sm:px-6
            sm:py-6
            lg:px-8
            lg:py-8
          "
        >
          {children}
        </div>
      </main>
    </div>
  );
};
