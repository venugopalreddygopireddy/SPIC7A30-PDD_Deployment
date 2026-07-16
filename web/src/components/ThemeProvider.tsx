"use client";

import { useEffect } from 'react';

export default function ThemeProvider() {
  useEffect(() => {
    // Theme logic
    const theme = localStorage.getItem('theme');
    
    if (theme === 'light') {
      document.documentElement.classList.add('light-theme');
    } else if (theme === 'dark') {
      document.documentElement.classList.remove('light-theme');
    } else {
      if (window.matchMedia('(prefers-color-scheme: light)').matches) {
         document.documentElement.classList.add('light-theme');
      } else {
         document.documentElement.classList.remove('light-theme');
      }
    }
  }, []);

  return null;
}
