import React, { useEffect, useMemo, useRef, useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { Send, MessageCircle, Sparkles, Video, X } from 'lucide-react';
import { MainLayout } from '../layouts/MainLayout';
import { chatService, meetingService, aiService } from '../services/api';
import type { ConversationResponse, MessageResponse } from '../types';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const getWsUrl = () => {
  const rawApiUrl = import.meta.env.VITE_API_URL?.trim();
  const apiBase = rawApiUrl ? rawApiUrl.replace(/\/+$/, '') : 'http://localhost:8080/api';
  return apiBase.replace(/\/api$/, '') + '/ws';
};

export const ChatPage: React.FC = () => {
  const [selectedConversation, setSelectedConversation] = useState<ConversationResponse | null>(null);
  const [message, setMessage] = useState('');
  const [showScheduleModal, setShowScheduleModal] = useState(false);
  const [scheduleDateTime, setScheduleDateTime] = useState('');

  // WebSocket state
  const [stompClient, setStompClient] = useState<Client | null>(null);
  const [onlineUsers, setOnlineUsers] = useState<string[]>([]);
  const [typingUsers, setTypingUsers] = useState<Record<string, boolean>>({});
  const [localMessages, setLocalMessages] = useState<MessageResponse[]>([]);

  // AI Drawer state
  const [showAiDrawer, setShowAiDrawer] = useState(false);
  const [aiPrompt, setAiPrompt] = useState('');
  const [aiResponse, setAiResponse] = useState('');
  const [aiLoading, setAiLoading] = useState(false);

  const currentUserId = localStorage.getItem('userId');
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Fetch conversations
  const { data: conversations = [] } = useQuery({
    queryKey: ['conversations'],
    queryFn: () => chatService.getConversations().then((res) => res.data),
  });

  // Default select first conversation
  useEffect(() => {
    if (conversations.length > 0) {
      const stillValid = conversations.some((c) => c.id === selectedConversation?.id);
      if (!selectedConversation || !stillValid) {
        setSelectedConversation(conversations[0]);
      }
    } else {
      setSelectedConversation(null);
    }
  }, [conversations]);

  // Fetch messages (Initial load & cache)
  useQuery({
        queryKey: ['messages', selectedConversation?.id],
        queryFn: () => chatService.getMessages(selectedConversation!.id).then((res) => {
            setLocalMessages(res.data);
            return res.data;
        }),
        enabled: !!selectedConversation,
    });

  // Scroll to bottom when messages change
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({
      behavior: 'smooth',
    });
  }, [localMessages]);

  // WebSocket connection & presence
  useEffect(() => {
    if (!currentUserId) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(getWsUrl()),
      connectHeaders: {
        Authorization: `Bearer ${localStorage.getItem('token')}`,
      },
      reconnectDelay: 5000,
      debug: (str) => console.log('[STOMP]', str),
      onConnect: () => {
        // Broadcast presence
        client.publish({
          destination: '/app/chat.presence',
          body: JSON.stringify({
            userId: currentUserId,
            status: 'ONLINE',
          }),
        });

        // Subscribe to presences
        client.subscribe('/topic/presence', (frame) => {
          const status = JSON.parse(frame.body);
          setOnlineUsers((prev) => {
            if (status.status === 'ONLINE') {
              if (prev.includes(status.userId)) return prev;
              return [...prev, status.userId];
            } else {
              return prev.filter((id) => id !== status.userId);
            }
          });
        });
      },
    });

    client.activate();
    setStompClient(client);

    return () => {
      if (client.connected) {
        client.publish({
          destination: '/app/chat.presence',
          body: JSON.stringify({
            userId: currentUserId,
            status: 'OFFLINE',
          }),
        });
      }
      client.deactivate();
    };
  }, [currentUserId]);

  // Subscribe to conversation topics
  useEffect(() => {
    if (!selectedConversation || !stompClient || !stompClient.connected) return;

    // Subscribe to new messages
    const messageSub = stompClient.subscribe(
      `/topic/messages/${selectedConversation.id}`,
      (frame) => {
        const newMsg = JSON.parse(frame.body);
        setLocalMessages((prev) => {
          if (prev.some((m) => m.id === newMsg.id)) return prev;
          return [...prev, newMsg];
        });
      }
    );

    // Subscribe to typing indicators
    const typingSub = stompClient.subscribe(
      `/topic/typing/${selectedConversation.id}`,
      (frame) => {
        const status = JSON.parse(frame.body);
        if (String(status.userId) !== String(currentUserId)) {
          setTypingUsers((prev) => ({
            ...prev,
            [status.userId]: status.typing,
          }));
        }
      }
    );

    return () => {
      messageSub.unsubscribe();
      typingSub.unsubscribe();
    };
  }, [selectedConversation, stompClient, stompClient?.connected]);

  const selectedName = useMemo(() => {
    if (!selectedConversation) return '';
    return selectedConversation.displayName;
  }, [selectedConversation]);

  // Check if other participant is online
  const isRecipientOnline = useMemo(() => {
    if (!selectedConversation) return false;
    const recipientId =
      String(selectedConversation.mentorId) === String(currentUserId)
        ? selectedConversation.menteeId
        : selectedConversation.mentorId;
    return onlineUsers.includes(recipientId);
  }, [selectedConversation, onlineUsers, currentUserId]);

  // Check if other participant is typing
  const isRecipientTyping = useMemo(() => {
    if (!selectedConversation) return false;
    const recipientId =
      String(selectedConversation.mentorId) === String(currentUserId)
        ? selectedConversation.menteeId
        : selectedConversation.mentorId;
    return !!typingUsers[recipientId];
  }, [selectedConversation, typingUsers, currentUserId]);

  const sendTypingStatus = (isTyping: boolean) => {
    if (!stompClient || !stompClient.connected || !selectedConversation) return;
    stompClient.publish({
      destination: '/app/chat.typing',
      body: JSON.stringify({
        conversationId: selectedConversation.id,
        userId: currentUserId,
        username: localStorage.getItem('userName'),
        typing: isTyping,
      }),
    });
  };

  const handleInputChange = (val: string) => {
    setMessage(val);
    sendTypingStatus(val.trim().length > 0);
  };

  const handleSend = () => {
    if (!message.trim() || !selectedConversation || !stompClient || !stompClient.connected) return;

    stompClient.publish({
      destination: '/app/chat.sendMessage',
      body: JSON.stringify({
        conversationId: selectedConversation.id,
        content: message.trim(),
      }),
    });

    setMessage('');
    sendTypingStatus(false);
  };

  // Schedule Session Mutation
  const scheduleMutation = useMutation({
    mutationFn: (scheduledAt: string) =>
      meetingService.createMeeting({
        conversationId: selectedConversation!.id,
        scheduledAt,
      }),
    onSuccess: () => {
      setShowScheduleModal(false);
      setScheduleDateTime('');
      alert('Session request sent! Waiting for the other participant to accept.');
    },
    onError: (err: any) => {
      alert(err.response?.data?.message || 'Failed to schedule session.');
    },
  });

  // AI assistant call
  const handleAskAi = async (presetPrompt?: string) => {
    const promptToUse = presetPrompt || aiPrompt;
    if (!promptToUse.trim()) return;

    setAiLoading(true);
    setAiResponse('');
    try {
      const res = await aiService.askAssistant({
        conversationId: selectedConversation!.id,
        prompt: promptToUse,
      });
      setAiResponse(res.data.response);
    } catch (err: any) {
      setAiResponse('AI Assistant Error: ' + (err.response?.data?.message || 'Failed to query assistant.'));
    } finally {
      setAiLoading(false);
    }
  };

  return (
    <MainLayout>
      <div className="h-[80vh] max-h-[80vh] min-h-0 overflow-hidden rounded-[2rem] border border-slate-200 bg-white shadow-xl">
        <div className="grid h-full min-h-0 grid-cols-12">
          {/* Sidebar */}
          <div className="col-span-4 flex h-full min-h-0 flex-col overflow-hidden border-r border-slate-200 bg-white">
            <div className="shrink-0 border-b border-slate-200 p-5">
              <h2 className="text-xl font-bold text-slate-900">Conversations</h2>
            </div>
            <div className="min-h-0 flex-1 overflow-y-auto custom-scrollbar">
              {conversations.map((conversation) => {
                const active = selectedConversation?.id === conversation.id;
                return (
                  <button
                    key={conversation.id}
                    onClick={() => setSelectedConversation(conversation)}
                    className={`flex w-full items-center gap-3 border-b border-slate-100 p-4 text-left transition ${
                      active ? 'bg-indigo-50/70 border-l-4 border-l-indigo-600' : 'hover:bg-slate-50'
                    }`}
                  >
                    <div className="grid h-12 w-12 place-items-center rounded-full bg-gradient-to-br from-indigo-500 to-violet-500 font-bold text-white shadow-sm">
                      {conversation.displayName
                        .split(' ')
                        .map((w) => w[0])
                        .join('')
                        .substring(0, 2)
                        .toUpperCase()}
                    </div>
                    <div className="flex-1">
                      <h3 className="font-semibold text-slate-900">{conversation.displayName}</h3>
                      <p className="text-xs text-slate-500">Active Mentorship</p>
                    </div>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Chat Area */}
          <div className="col-span-8 flex h-full min-h-0 flex-col overflow-hidden bg-slate-50 relative">
            {/* Header */}
            <div className="flex shrink-0 items-center justify-between border-b border-slate-200 bg-white px-6 py-4">
              <div className="flex items-center gap-3">
                <div className="grid h-12 w-12 place-items-center rounded-full bg-gradient-to-br from-cyan-500 to-indigo-500 font-bold text-white shadow-sm">
                  {selectedName
                    ?.split(' ')
                    .map((w) => w[0])
                    .join('')
                    .substring(0, 2)
                    .toUpperCase()}
                </div>
                <div>
                  <h2 className="font-semibold text-slate-900">{selectedName || 'Select Chat'}</h2>
                  {selectedConversation && (
                    <div className="flex items-center gap-1.5 mt-0.5">
                      <span className={`h-2.5 w-2.5 rounded-full ${isRecipientOnline ? 'bg-emerald-500' : 'bg-slate-300'}`} />
                      <span className="text-xs text-slate-500">{isRecipientOnline ? 'Online' : 'Offline'}</span>
                    </div>
                  )}
                </div>
              </div>

              {selectedConversation && (
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => {
                      setAiResponse('');
                      setAiPrompt('');
                      setShowAiDrawer(true);
                    }}
                    className="flex items-center gap-1.5 rounded-2xl border border-indigo-100 bg-indigo-50 px-3.5 py-2 text-xs font-bold text-indigo-700 hover:bg-indigo-100 transition"
                  >
                    <Sparkles size={14} />
                    Ask AI
                  </button>
                  <button
                    onClick={() => setShowScheduleModal(true)}
                    className="flex items-center gap-1.5 rounded-2xl bg-indigo-600 px-4 py-2 text-xs font-bold text-white hover:bg-indigo-700 shadow transition"
                  >
                    <Video size={14} />
                    Schedule Meeting
                  </button>
                </div>
              )}
            </div>

            {/* Messages */}
            <div className="min-h-0 flex-1 overflow-y-auto px-6 py-4 custom-scrollbar">
              {!selectedConversation ? (
                <div className="flex h-full flex-col items-center justify-center text-slate-400">
                  <MessageCircle size={60} />
                  <p className="mt-4 text-base">Select a conversation to start chatting</p>
                </div>
              ) : (
                <div className="space-y-4">
                  {localMessages.map((msg) => {
                    const mine = String(msg.senderId) === String(currentUserId);
                    return (
                      <div key={msg.id} className={`flex ${mine ? 'justify-end' : 'justify-start'}`}>
                        <div
                          className={`max-w-[70%] px-4 py-3 shadow-sm ${
                            mine
                              ? 'rounded-3xl rounded-tr-md bg-indigo-600 text-white'
                              : 'rounded-3xl rounded-tl-md bg-white text-slate-800'
                          }`}
                        >
                          <div className={`text-[10px] mb-1 font-semibold ${mine ? 'text-indigo-200' : 'text-slate-400'}`}>
                            {msg.senderName}
                          </div>
                          <p className="text-sm leading-relaxed whitespace-pre-wrap">{msg.content}</p>
                          <div className={`text-[9px] mt-1.5 text-right ${mine ? 'text-indigo-200' : 'text-slate-400'}`}>
                            {new Date(msg.sentAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                          </div>
                        </div>
                      </div>
                    );
                  })}
                  {isRecipientTyping && (
                    <div className="flex justify-start">
                      <div className="rounded-3xl rounded-tl-md bg-white px-4 py-3 text-sm text-slate-400 italic shadow-sm">
                        {selectedName} is typing...
                      </div>
                    </div>
                  )}
                  <div ref={messagesEndRef} />
                </div>
              )}
            </div>

            {/* Input Bar */}
            {selectedConversation && (
              <div className="shrink-0 border-t border-slate-200 bg-white p-4">
                <div className="flex items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-2 focus-within:border-indigo-400 focus-within:bg-white transition-all">
                  <input
                    value={message}
                    onChange={(e) => handleInputChange(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') handleSend();
                    }}
                    placeholder="Type a message..."
                    className="flex-1 bg-transparent px-3 py-1.5 text-sm outline-none text-slate-800"
                  />
                  <button
                    onClick={handleSend}
                    disabled={!message.trim()}
                    className="grid h-10 w-10 place-items-center rounded-xl bg-indigo-600 text-white shadow-md hover:bg-indigo-700 disabled:opacity-50 transition"
                  >
                    <Send size={16} />
                  </button>
                </div>
              </div>
            )}

            {/* AI Side Drawer */}
            {showAiDrawer && (
              <div className="absolute top-0 right-0 h-full w-[380px] bg-white border-l border-slate-200 shadow-2xl z-30 flex flex-col animate-slide-in">
                <div className="flex items-center justify-between border-b border-slate-100 p-4">
                  <div className="flex items-center gap-2 text-indigo-700 font-bold text-sm">
                    <Sparkles size={16} />
                    AI Assistant
                  </div>
                  <button onClick={() => setShowAiDrawer(false)} className="text-slate-400 hover:text-slate-700">
                    <X size={18} />
                  </button>
                </div>

                <div className="flex-1 overflow-y-auto p-4 space-y-4 custom-scrollbar">
                  {/* Preset Prompt Buttons */}
                  <div className="space-y-2">
                    <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-400">Quick Actions</h4>
                    <div className="grid gap-2">
                      <button
                        onClick={() => handleAskAi('Summarize conversation')}
                        disabled={aiLoading}
                        className="w-full text-left rounded-xl border border-slate-100 bg-slate-50/50 p-3 text-xs font-medium text-slate-700 hover:bg-indigo-50 hover:border-indigo-100 transition"
                      >
                        📝 Summarize our chat
                      </button>
                      <button
                        onClick={() => handleAskAi('Generate learning roadmap')}
                        disabled={aiLoading}
                        className="w-full text-left rounded-xl border border-slate-100 bg-slate-50/50 p-3 text-xs font-medium text-slate-700 hover:bg-indigo-50 hover:border-indigo-100 transition"
                      >
                        🗺️ Create learning roadmap
                      </button>
                      <button
                        onClick={() => handleAskAi('Create mock interview questions')}
                        disabled={aiLoading}
                        className="w-full text-left rounded-xl border border-slate-100 bg-slate-50/50 p-3 text-xs font-medium text-slate-700 hover:bg-indigo-50 hover:border-indigo-100 transition"
                      >
                        💡 Generate interview questions
                      </button>
                    </div>
                  </div>

                  {/* Output Display */}
                  {(aiResponse || aiLoading) && (
                    <div className="rounded-2xl bg-indigo-50/30 border border-indigo-100/50 p-4">
                      {aiLoading ? (
                        <div className="flex items-center gap-2.5 text-xs text-indigo-600 font-medium">
                          <div className="h-4 w-4 animate-spin rounded-full border-2 border-indigo-600 border-t-transparent"></div>
                          Consulting AI...
                        </div>
                      ) : (
                        <div className="prose prose-sm text-slate-700 text-xs leading-relaxed whitespace-pre-wrap">
                          {aiResponse}
                        </div>
                      )}
                    </div>
                  )}
                </div>

                {/* Input block */}
                <div className="border-t border-slate-100 p-4">
                  <div className="flex gap-2">
                    <input
                      value={aiPrompt}
                      onChange={(e) => setAiPrompt(e.target.value)}
                      placeholder="Ask AI assistant anything..."
                      className="flex-1 rounded-xl border border-slate-200 px-3.5 py-2 text-xs outline-none focus:border-indigo-500"
                    />
                    <button
                      onClick={() => handleAskAi()}
                      disabled={aiLoading || !aiPrompt.trim()}
                      className="rounded-xl bg-indigo-600 px-3 text-white disabled:opacity-50"
                    >
                      Ask
                    </button>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Meeting Modal */}
      {showScheduleModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm">
          <div className="w-full max-w-md rounded-[2rem] border border-slate-100 bg-white p-6 shadow-2xl">
            <h3 className="text-xl font-bold text-slate-900">Schedule Mentorship Session</h3>
            <p className="mt-2 text-sm text-slate-500">
              Pick a date and time. The other participant will need to accept before a real Google Meet link is created.
            </p>

            <div className="mt-4">
              <label className="block text-sm font-semibold text-slate-700">Date & Time</label>
              <input
                type="datetime-local"
                value={scheduleDateTime}
                onChange={(e) => setScheduleDateTime(e.target.value)}
                className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 outline-none focus:border-indigo-500"
              />
            </div>

            <div className="mt-6 flex justify-end gap-3">
              <button
                onClick={() => setShowScheduleModal(false)}
                className="rounded-2xl border border-slate-200 px-4 py-2.5 text-sm font-semibold text-slate-600 hover:bg-slate-50"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  if (!scheduleDateTime) return;
                  scheduleMutation.mutate(scheduleDateTime);
                }}
                disabled={!scheduleDateTime || scheduleMutation.isPending}
                className="rounded-2xl bg-indigo-600 px-5 py-2.5 text-sm font-semibold text-white hover:bg-indigo-700 disabled:opacity-50"
              >
                {scheduleMutation.isPending ? 'Scheduling...' : 'Schedule'}
              </button>
            </div>
          </div>
        </div>
      )}
    </MainLayout>
  );
};