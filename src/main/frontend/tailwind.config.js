/** @type {import('tailwindcss').Config} */
const defaultTheme = require('tailwindcss/defaultTheme');
module.exports = {
    darkMode: "class",
    content: ["../resources/templates/**/*.{html,js}"],
    theme: {
        extend: {
            fontFamily: {
                sans: ["Inter var", ...defaultTheme.fontFamily.sans]
            },
            keyframes: {
                fadeOutWithDelay: {
                    "0%": { opacity: "1" },
                    "90%": { opacity: "1" },
                    "100%": { opacity: "0" }
                },
            },
            animation: {
                "fade-out-delay": "fadeOutWithDelay 10s ease-out forwards",
            }
        }
    },
    plugins: [
        "@tailwindcss/forms",
    ]
}