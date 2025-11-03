/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",   // 👈 esto permite a Tailwind escanear todos los archivos Angular
  ],
  theme: {
    extend: {
      colors: {
        primary: "#0056A3",
        secondary: "#2BB673",
        accent: "#F5F6FA",
      },
      fontFamily: {
        sans: ["Inter", "system-ui", "sans-serif"],
      },
    },
  },
  plugins: [],
};
