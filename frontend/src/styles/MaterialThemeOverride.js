import { createTheme, responsiveFontSizes } from "@mui/material/styles";
import { yellow } from "@mui/material/colors";


export const appTheme = responsiveFontSizes(
  createTheme({
    palette: {
      primary: {
          light: '#002884',
          main: '#286dc0',
          dark: '#00356b',
          contrastText: '#fff'
      },
      secondary: {
          light: '#dddddd',
          main: '#',
          dark: '#222222',
          contrastText: '#fff',
      },
      warning: {
          light: yellow[500],
          main: yellow[700],
          dark: yellow[900],
          contrastText: '#fff',
      }
    },
    mainContainer: {maxWidth: 1024}
  }))