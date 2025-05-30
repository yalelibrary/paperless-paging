import Button from "@mui/material/Button";
import React from "react";
import {ThemeProvider} from "@mui/styles";
import {appTheme} from "../../styles/MaterialThemeOverride.js";

export const DialogButton = ({
                                 id, name, icon, label, onClick, color, className, disabled, setDisabledState = () => {
    }, variant = "contained",
                             }) => {
    const [clickDisable, setClickDisable] = React.useState(false);
    return (
        <ThemeProvider theme={appTheme} >
        <Button
            aria-label={label}
            id={id || null}
            variant={variant}
            color={color || "primary"}
            size="medium"
            startIcon={icon}
            onClick={async () => {
                setClickDisable(true);
                setDisabledState(true);
                await onClick();
                setDisabledState(false);
                setClickDisable(false);
            }}

            className={className}
            style={{margin: "6px 8px"}}
            disabled={disabled || clickDisable}
        >
            {name}
        </Button>
        </ThemeProvider>
    );
};