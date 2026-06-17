import React, { useEffect, useRef, useState } from 'react';

type GoogleCallback = (credential: string) => void | Promise<void>;

interface GoogleSignInButtonProps {
  onCredential: GoogleCallback;
  className?: string;
}

declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize: (options: {
            client_id: string;
            callback: (response: { credential: string }) => void;
          }) => void;
          renderButton: (
            parent: HTMLElement,
            options: Record<string, unknown>
          ) => void;
        };
      };
    };
  }
}

const GOOGLE_SCRIPT_ID = 'google-identity-services';

export const GoogleSignInButton: React.FC<GoogleSignInButtonProps> = ({
  onCredential,
  className,
}) => {
  const buttonRef = useRef<HTMLDivElement | null>(null);
  const [isReady, setIsReady] = useState(false);
  const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID?.trim();

  useEffect(() => {
    if (!clientId) {
      return;
    }

    const existingScript = document.getElementById(GOOGLE_SCRIPT_ID) as HTMLScriptElement | null;

    const initGoogle = () => {
      if (!window.google || !buttonRef.current) {
        return;
      }

      window.google.accounts.id.initialize({
        client_id: clientId,
        callback: (response) => {
          void onCredential(response.credential);
        },
      });

      buttonRef.current.innerHTML = '';
      window.google.accounts.id.renderButton(buttonRef.current, {
        theme: 'outline',
        size: 'large',
        shape: 'pill',
        text: 'signin_with',
        width: buttonRef.current.clientWidth,
      });

      setIsReady(true);
    };

    if (window.google) {
      initGoogle();
      return;
    }

    if (existingScript) {
      existingScript.addEventListener('load', initGoogle);
      return () => existingScript.removeEventListener('load', initGoogle);
    }

    const script = document.createElement('script');
    script.id = GOOGLE_SCRIPT_ID;
    script.src = 'https://accounts.google.com/gsi/client';
    script.async = true;
    script.defer = true;
    script.onload = initGoogle;
    document.head.appendChild(script);

    return () => {
      script.onload = null;
    };
  }, [clientId, onCredential]);

  if (!clientId) {
    return (
      <div
        className={`rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-700 ${className ?? ''}`}
      >
        Set <code>VITE_GOOGLE_CLIENT_ID</code> to enable Google sign-in.
      </div>
    );
  }

  return (
    <div className={className}>
      <div ref={buttonRef} className="min-h-[44px] w-full" />
      {!isReady && (
        <div className="mt-3 text-center text-xs text-slate-400">Loading Google sign-in...</div>
      )}
    </div>
  );
};
