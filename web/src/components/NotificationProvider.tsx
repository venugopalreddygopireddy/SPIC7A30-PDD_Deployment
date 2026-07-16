"use client";

import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';

export interface AppNotification {
  id: string;
  title: string;
  body: string;
  type: 'reminder' | 'completion' | 'info';
  timestamp: number;
  read: boolean;
}

interface NotificationContextType {
  notifications: AppNotification[];
  unreadCount: number;
  addNotification: (n: Omit<AppNotification, 'id' | 'timestamp' | 'read'>) => void;
  markAllRead: () => void;
  markRead: (id: string) => void;
  clearAll: () => void;
}

const NotificationContext = createContext<NotificationContextType>({
  notifications: [],
  unreadCount: 0,
  addNotification: () => {},
  markAllRead: () => {},
  markRead: () => {},
  clearAll: () => {},
});

export function useNotifications() {
  return useContext(NotificationContext);
}

const STORAGE_KEY = 'cortisense_notifications';

function loadFromStorage(): AppNotification[] {
  if (typeof window === 'undefined') return [];
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch {
    return [];
  }
}

function saveToStorage(notifs: AppNotification[]) {
  if (typeof window === 'undefined') return;
  localStorage.setItem(STORAGE_KEY, JSON.stringify(notifs));
}

export function NotificationProvider({ children }: { children: React.ReactNode }) {
  const [notifications, setNotifications] = useState<AppNotification[]>([]);

  // Load on mount
  useEffect(() => {
    setNotifications(loadFromStorage());
  }, []);

  const addNotification = useCallback((n: Omit<AppNotification, 'id' | 'timestamp' | 'read'>) => {
    const newNotif: AppNotification = {
      ...n,
      id: `notif_${Date.now()}_${Math.random().toString(36).slice(2)}`,
      timestamp: Date.now(),
      read: false,
    };
    setNotifications(prev => {
      const updated = [newNotif, ...prev].slice(0, 50); // Keep max 50
      saveToStorage(updated);
      return updated;
    });

    // Also fire a browser notification if permission granted
    if (typeof window !== 'undefined' && 'Notification' in window && Notification.permission === 'granted') {
      new Notification(n.title, { body: n.body, icon: '/favicon.ico' });
    }
  }, []);

  const markRead = useCallback((id: string) => {
    setNotifications(prev => {
      const updated = prev.map(n => n.id === id ? { ...n, read: true } : n);
      saveToStorage(updated);
      return updated;
    });
  }, []);

  const markAllRead = useCallback(() => {
    setNotifications(prev => {
      const updated = prev.map(n => ({ ...n, read: true }));
      saveToStorage(updated);
      return updated;
    });
  }, []);

  const clearAll = useCallback(() => {
    setNotifications([]);
    saveToStorage([]);
  }, []);

  const unreadCount = notifications.filter(n => !n.read).length;

  // Request browser notification permission on mount
  useEffect(() => {
    if (typeof window !== 'undefined' && 'Notification' in window && Notification.permission === 'default') {
      Notification.requestPermission();
    }
  }, []);

  // Schedule daily reminders at 9 AM, 2 PM, and 8 PM
  useEffect(() => {
    const scheduleReminder = () => {
      const now = new Date();
      const reminderHours = [9, 14, 20]; // 9AM, 2PM, 8PM
      
      // Check if any reminder time just passed (within last minute)
      reminderHours.forEach(hour => {
        if (now.getHours() === hour && now.getMinutes() === 0) {
          const labels: Record<number, string> = {
            9: 'Morning Check-in Reminder',
            14: 'Afternoon Check-in Reminder',
            20: 'Evening Check-in Reminder',
          };
          const bodies: Record<number, string> = {
            9: "Good morning! Start your day right – complete your wellness check-in now.",
            14: "Afternoon check-in time! Take 2 minutes to log your stress levels.",
            20: "Evening reminder! Don't forget to complete your daily check-in before bed.",
          };
          addNotification({
            title: labels[hour],
            body: bodies[hour],
            type: 'reminder',
          });
        }
      });
    };

    // Check every minute
    const interval = setInterval(scheduleReminder, 60 * 1000);
    return () => clearInterval(interval);
  }, [addNotification]);

  return (
    <NotificationContext.Provider value={{ notifications, unreadCount, addNotification, markAllRead, markRead, clearAll }}>
      {children}
    </NotificationContext.Provider>
  );
}
