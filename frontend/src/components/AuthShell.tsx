import React from 'react';
import { ArrowRight, BrainCircuit, ShieldCheck, Sparkles, Users } from 'lucide-react';
import heroImage from '../assets/hero.png';

type AuthShellVariant = 'login' | 'register';

interface AuthShellProps {
  variant: AuthShellVariant;
  title: string;
  subtitle: string;
  children: React.ReactNode;
  footer?: React.ReactNode;
}

const copy = {
  login: {
    headline: 'Learn from experts.',
    body: 'Connect with mentors, gain sharper feedback, and accelerate your career with a smarter network.',
    accent: 'From first intro to first breakthrough.',
  },
  register: {
    headline: 'Start your journey towards growth.',
    body: 'Build your profile, showcase your skills, and get matched with people who can help you level up.',
    accent: 'Your learning network starts here.',
  },
} satisfies Record<AuthShellVariant, { headline: string; body: string; accent: string }>;

export const AuthShell: React.FC<AuthShellProps> = ({
  variant,
  title,
  subtitle,
  children,
  footer,
}) => {
  const isLogin = variant === 'login';

  return (
    <div className="min-h-screen overflow-hidden bg-[radial-gradient(circle_at_top_left,_#eff6ff,_#f8fafc_42%,_#eef2ff_100%)] px-3 py-3 sm:px-4 sm:py-4 lg:px-5 lg:py-5">
      <div className="mx-auto grid min-h-[calc(100vh-1.5rem)] w-full max-w-[1650px] overflow-hidden rounded-[2.4rem] border border-white/75 bg-white/88 shadow-[0_30px_100px_rgba(15,23,42,0.14)] backdrop-blur-xl lg:grid-cols-[1.05fr_0.95fr]">
        <aside
          className={`relative hidden overflow-hidden p-10 text-white lg:flex lg:flex-col lg:justify-between ${
            isLogin
              ? 'bg-[linear-gradient(160deg,#0b1026_0%,#16255f_55%,#2f3db5_100%)]'
              : 'bg-[linear-gradient(160deg,#eff2ff_0%,#dce4ff_55%,#ccd7ff_100%)] text-slate-900'
          }`}
        >
          <div className="absolute inset-0">
            <div
              className={`absolute -left-24 top-10 h-72 w-72 rounded-full blur-3xl ${
                isLogin ? 'bg-sky-500/20' : 'bg-indigo-400/30'
              }`}
            />
            <div
              className={`absolute right-[-80px] top-32 h-80 w-80 rounded-full blur-3xl ${
                isLogin ? 'bg-violet-500/20' : 'bg-white/35'
              }`}
            />
            <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,_rgba(255,255,255,0.1)_0%,_transparent_1%)] bg-[length:18px_18px] opacity-20" />
          </div>

          <div className="relative z-10 flex items-center gap-3">
            <div
              className={`grid h-12 w-12 place-items-center rounded-2xl ${
                isLogin ? 'bg-white/12' : 'bg-white/70'
              } shadow-lg`}
            >
              <span className={`text-3xl font-black ${isLogin ? 'text-cyan-300' : 'text-indigo-600'}`}>
                K
              </span>
            </div>
            <div>
              <p className={`text-xl font-semibold ${isLogin ? 'text-white' : 'text-slate-900'}`}>
                Knowledge Nexus
              </p>
              <p className={`text-sm ${isLogin ? 'text-white/65' : 'text-slate-500'}`}>
                Connect. Learn. Grow.
              </p>
            </div>
          </div>

          <div className="relative z-10 max-w-md space-y-6 pt-6">
            <div className="space-y-4">
              <p className={`max-w-xl text-5xl font-semibold leading-tight tracking-tight ${isLogin ? 'text-white' : 'text-slate-900'}`}>
                {copy[variant].headline}
              </p>
              <p className={`text-lg leading-8 ${isLogin ? 'text-white/75' : 'text-slate-600'}`}>
                {copy[variant].body}
              </p>
            </div>

            <div className={`grid grid-cols-3 gap-3 text-sm ${isLogin ? 'text-white/85' : 'text-slate-700'}`}>
              <div className={`rounded-2xl p-3 ${isLogin ? 'bg-white/10 border border-white/10' : 'bg-white/70 border border-white/60'}`}>
                <ShieldCheck className="mb-2 h-5 w-5" />
                Safe network
              </div>
              <div className={`rounded-2xl p-3 ${isLogin ? 'bg-white/10 border border-white/10' : 'bg-white/70 border border-white/60'}`}>
                <Users className="mb-2 h-5 w-5" />
                Mentor matching
              </div>
              <div className={`rounded-2xl p-3 ${isLogin ? 'bg-white/10 border border-white/10' : 'bg-white/70 border border-white/60'}`}>
                <BrainCircuit className="mb-2 h-5 w-5" />
                Smart growth
              </div>
            </div>

            <div
              className={`relative overflow-hidden rounded-[1.75rem] border p-6 shadow-2xl ${
                isLogin ? 'border-white/10 bg-white/8' : 'border-white/70 bg-white/65'
              }`}
            >
              <div className="mb-4 flex items-center gap-3">
                <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-gradient-to-br from-indigo-500 to-fuchsia-500 text-white shadow-lg">
                  <Sparkles className="h-5 w-5" />
                </div>
                <div>
                  <p className={`text-sm font-medium ${isLogin ? 'text-white/60' : 'text-slate-500'}`}>
                    Community wisdom
                  </p>
                  <p className={`text-lg font-semibold ${isLogin ? 'text-white' : 'text-slate-900'}`}>
                    {copy[variant].accent}
                  </p>
                </div>
              </div>
              <p className={`max-w-sm text-base leading-7 ${isLogin ? 'text-white/72' : 'text-slate-600'}`}>
                Mentor discovery feels less like searching a directory and more like joining a network that already understands your goals.
              </p>
              <ArrowRight className={`absolute right-6 top-6 h-7 w-7 ${isLogin ? 'text-white/30' : 'text-indigo-500/40'}`} />
            </div>
          </div>

          <div className="relative z-10 flex items-end justify-center pt-4">
            <img
              src={heroImage}
              alt=""
              className={`w-[330px] select-none object-contain drop-shadow-[0_24px_32px_rgba(15,23,42,0.35)] ${
                isLogin ? 'opacity-90' : 'opacity-95'
              }`}
            />
          </div>
        </aside>

        <section className="flex flex-col justify-center px-4 py-6 sm:px-8 lg:px-14">
          <div className="mx-auto w-full max-w-lg">
            <div className="mb-8 flex items-center gap-3 lg:hidden">
              <div className="grid h-11 w-11 place-items-center rounded-2xl bg-gradient-to-br from-indigo-500 to-violet-600 text-white shadow-lg">
                <span className="text-2xl font-black">K</span>
              </div>
              <div>
                <p className="text-lg font-semibold text-slate-900">Knowledge Nexus</p>
                <p className="text-sm text-slate-500">Connect. Learn. Grow.</p>
              </div>
            </div>

            <div className="mb-8 space-y-2">
              <p className="text-sm font-semibold uppercase tracking-[0.28em] text-indigo-600">
                {isLogin ? 'Welcome back' : 'Create account'}
              </p>
              <h1 className="text-4xl font-semibold tracking-tight text-slate-900">
                {title}
              </h1>
              <p className="max-w-md text-base leading-7 text-slate-600">
                {subtitle}
              </p>
            </div>

            <div className="rounded-[1.5rem] border border-slate-200/80 bg-white/90 p-5 shadow-[0_16px_40px_rgba(15,23,42,0.08)] sm:p-6">
              {children}
            </div>

            {footer ? <div className="mt-6">{footer}</div> : null}
          </div>
        </section>
      </div>
    </div>
  );
};
