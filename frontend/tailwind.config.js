/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        cyberBlue: '#0f172a',
        neonBlue: '#00d1ff',
        alertRed: '#ff3b5f'
      },
      boxShadow: {
        glow: '0 0 18px rgba(0, 209, 255, 0.35)',
        danger: '0 0 18px rgba(255, 59, 95, 0.45)'
      },
      animation: {
        pulseGlow: 'pulseGlow 1.8s ease-in-out infinite'
      },
      keyframes: {
        pulseGlow: {
          '0%, 100%': { boxShadow: '0 0 12px rgba(0, 209, 255, 0.25)' },
          '50%': { boxShadow: '0 0 24px rgba(0, 209, 255, 0.55)' }
        }
      }
    }
  },
  plugins: []
}
