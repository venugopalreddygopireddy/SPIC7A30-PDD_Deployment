"use client";

import React, { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { ArrowLeft, Bell, CheckCircle2, Clock, Trash2 } from 'lucide-react';
import { useNotifications, AppNotification } from '@/components/NotificationProvider';

function timeAgo(ts: number): string {
  const diff = Date.now() - ts;
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return 'Just now';
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  const days = Math.floor(hrs / 24);
  return `${days}d ago`;
}

function NotifIcon({ type }: { type: AppNotification['type'] }) {
  if (type === 'reminder') return (
    <div className="w-10 h-10 rounded-full bg-blue-500/20 flex items-center justify-center flex-shrink-0">
      <Clock size={20} className="text-blue-400" />
    </div>
  );
  if (type === 'completion') return (
    <div className="w-10 h-10 rounded-full bg-emerald-500/20 flex items-center justify-center flex-shrink-0">
      <CheckCircle2 size={20} className="text-emerald-400" />
    </div>
  );
  return (
    <div className="w-10 h-10 rounded-full bg-slate-700/50 flex items-center justify-center flex-shrink-0">
      <Bell size={20} className="text-slate-400" />
    </div>
  );
}

export default function NotificationsPage() {
  const router = useRouter();
  const { notifications, markAllRead, markRead, clearAll } = useNotifications();

  // Mark all as read when page opens
  useEffect(() => {
    markAllRead();
  }, [markAllRead]);

  return (
    <div className="min-h-screen bg-[#050810] text-slate-200 flex flex-col">
      {/* Header */}
      <div className="flex items-center gap-4 px-6 py-5 border-b border-slate-800">
        <button
          onClick={() => router.back()}
          className="w-9 h-9 flex items-center justify-center rounded-full hover:bg-slate-800 transition-colors"
        >
          <ArrowLeft size={22} />
        </button>
        <h1 className="text-xl font-bold text-white flex-1">Notifications</h1>
        {notifications.length > 0 && (
          <button
            onClick={clearAll}
            className="flex items-center gap-1.5 text-slate-400 hover:text-rose-400 transition-colors text-sm font-medium"
          >
            <Trash2 size={16} />
            Clear all
          </button>
        )}
      </div>

      {/* Content */}
      <div className="flex-1 overflow-y-auto">
        {notifications.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-full py-24 text-center px-6">
            <div className="w-20 h-20 rounded-full bg-slate-800/50 flex items-center justify-center mb-6">
              <Bell size={36} className="text-slate-600" />
            </div>
            <h2 className="text-xl font-bold text-slate-400 mb-2">No Notifications</h2>
            <p className="text-slate-600 text-sm max-w-xs leading-relaxed">
              You're all caught up! Reminders and session updates will appear here.
            </p>
          </div>
        ) : (
          <div className="divide-y divide-slate-800/50">
            {notifications.map(notif => (
              <div
                key={notif.id}
                onClick={() => markRead(notif.id)}
                className={`flex items-start gap-4 px-6 py-4 transition-colors cursor-pointer ${
                  !notif.read ? 'bg-slate-800/30 hover:bg-slate-800/50' : 'hover:bg-slate-900/30'
                }`}
              >
                <NotifIcon type={notif.type} />
                <div className="flex-1 min-w-0">
                  <div className="flex items-start justify-between gap-2">
                    <p className={`font-semibold text-sm leading-snug ${!notif.read ? 'text-white' : 'text-slate-300'}`}>
                      {notif.title}
                    </p>
                    {!notif.read && (
                      <div className="w-2 h-2 rounded-full bg-emerald-400 flex-shrink-0 mt-1.5" />
                    )}
                  </div>
                  <p className="text-slate-400 text-sm mt-0.5 leading-relaxed">{notif.body}</p>
                  <p className="text-slate-600 text-xs mt-1.5 font-medium">{timeAgo(notif.timestamp)}</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
