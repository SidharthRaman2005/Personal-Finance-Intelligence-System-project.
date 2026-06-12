import type { Config } from 'tailwindcss'

export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        bgDark: "#0B0F14",
        cardDark: "#171718",
        primaryGreen: "#A3E635",
        accentOrange: "#F59E0B",
        textMain: "#E5E7EB",
        textMuted: "#6B7280",
      },
    },
  },
  plugins: [],
} satisfies Config
