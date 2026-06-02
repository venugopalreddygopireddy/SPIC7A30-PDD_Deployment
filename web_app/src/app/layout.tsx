import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "CortiSense",
  description: "CortiSense Stress Tracker",
};

import { TranslationProvider } from "@/components/TranslationProvider";

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="h-full antialiased font-sans">
      <body className="min-h-full flex flex-col">
        <TranslationProvider>{children}</TranslationProvider>
      </body>
    </html>
  );
}
