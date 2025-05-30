import React from "react";
import InfoIcon from "@mui/icons-material/Info";
import Menu from "@mui/material/Menu";
import Typography from "@mui/material/Typography";

export const InfoButton = ({label, children, color, heading, className}) => {
    const [menuAnchor, setMenuAnchor] = React.useState(null);
    return (
        <>
            <InfoIcon className={className} style={{position: "relative", top: "6px"}} color={color || "primary"}
                      aria-label={label} onClick={(e) => setMenuAnchor(menuAnchor ? null : e.target)}/>
            <Menu anchorEl={menuAnchor} open={menuAnchor || false} onClose={() => setMenuAnchor(null)}
                  onClick={() => setMenuAnchor(null)}>
                <Typography style={{padding: "16px"}}>
                    {heading && <h4>{heading}</h4>}
                    {children}
                </Typography>
            </Menu>
        </>
    )
}