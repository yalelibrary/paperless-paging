import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.jsx'
import './index.css'
import { ThemeProvider } from '@mui/styles'
import { SnackbarProvider } from 'notistack-v2-maintained'
import { appTheme } from './styles/MaterialThemeOverride.js'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <ThemeProvider theme={appTheme}>
        <SnackbarProvider preventDuplicate={false}>
        <App />
      </SnackbarProvider>
    </ThemeProvider>
  </StrictMode>,
)
