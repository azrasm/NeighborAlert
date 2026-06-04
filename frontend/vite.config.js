import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig(({ mode }) => {
  // loadEnv ucitava odgovarajuci .env fajl na osnovu "mode" (development ili production)
  // treci parametar '' znaci da trazi sve varijable a ne samo one sa VITE_ prefiksom u Node okruzenju
  const env = loadEnv(mode, process.cwd(), '');

  return {
    plugins: [react()],
    server: {
      port: 3000,
      proxy: {
        '/api': {
          target: env.VITE_API_BASE || 'http://localhost:8080',
          changeOrigin: true,
        }
      }
    }
  }
})