import Grid from "@mui/material/Grid2";
import ListItem from "@mui/material/ListItem";
import ListItemText from "@mui/material/ListItemText";
import React from "react";

export const KeyValue = ({name, value, singleLine, className}) => {
    if (singleLine) {
        return <Grid container><Grid size={3} className={"text-label"}>{name}:</Grid><Grid size={9} >{value}</Grid></Grid>
    } else {
        return (<ListItem>
            <ListItemText
                primary={name}
                secondary={value}
            />
        </ListItem>);
    }
};