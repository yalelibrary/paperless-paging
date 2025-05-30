import AppBar from "@mui/material/AppBar";
import React from "react";

export const WrapInAppBar = (props) => {
    if (props.wrap === true) {
        return (
            <AppBar className={props.classes.bottomAppBar}>
                {props.children}
            </AppBar>
        )
    } else {
        return (
            props.children
        )

    }
};