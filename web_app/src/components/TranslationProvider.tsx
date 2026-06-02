"use client";

import React, { createContext, useContext, useState, useEffect } from 'react';

type Language = 'English' | 'Telugu' | 'Hindi' | 'Tamil';

interface TranslationContextType {
  language: Language;
  setLanguage: (lang: Language) => void;
  t: (key: string, variables?: Record<string, string | number>) => string;
}

const TranslationContext = createContext<TranslationContextType | undefined>(undefined);

// This is a minimal dictionary. We will expand this in translations.ts later if needed, 
// or fetch it. For now, it's defined here or in a separate file.
import { translations } from '@/lib/translations';

export const TranslationProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [language, setLanguageState] = useState<Language>('English');
  const [isMounted, setIsMounted] = useState(false);

  useEffect(() => {
    const savedLang = localStorage.getItem('appLanguage') as Language;
    if (savedLang && ['English', 'Telugu', 'Hindi', 'Tamil'].includes(savedLang)) {
      setLanguageState(savedLang);
    }
    setIsMounted(true);
  }, []);

  const setLanguage = (lang: Language) => {
    setLanguageState(lang);
    localStorage.setItem('appLanguage', lang);
  };

  const t = (key: string, variables?: Record<string, string | number>): string => {
    const dict = translations[language] || translations['English'];
    let text = dict[key] || translations['English'][key] || key;
    
    if (variables) {
      Object.keys(variables).forEach((varKey) => {
        text = text.replace(`{${varKey}}`, String(variables[varKey]));
      });
    }
    
    return text;
  };

  // Prevent hydration mismatch by returning null until mounted, 
  // or just render with default language and let it swap on client.
  // Rendering immediately to avoid flickering, but hydration mismatch might occur if server renders English and client has Telugu.
  // We'll render a loader or just children if mounted.
  if (!isMounted) return null;

  return (
    <TranslationContext.Provider value={{ language, setLanguage, t }}>
      {children}
    </TranslationContext.Provider>
  );
};

export const useTranslation = () => {
  const context = useContext(TranslationContext);
  if (context === undefined) {
    throw new Error('useTranslation must be used within a TranslationProvider');
  }
  return context;
};
