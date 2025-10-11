/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        darkBg: "#0a0a0f",
        accentBlue: "#00b3ff",
        accentRed: "#ff0040",
      },
    },
  },
  plugins: [],
};
