import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import List from "@mui/material/List";
import ListItem from "@mui/material/ListItem";
import DoneIcon from "@mui/icons-material/Done";
import Cancel from "@mui/icons-material/Cancel";
import React from "react";
import {DialogButton} from "./DialogButton";

export const ConfirmDialog = ({confirmTitle, open, handleClose, handleConfirm, classes, ...props}) => {
    return (
        <Dialog open={open} classes={{paper: classes.dialogPaper}} onClose={handleClose}
                aria-labelledby="form-dialog-title">
            <DialogTitle id="form-dialog-title">{confirmTitle}</DialogTitle>
            <DialogContent>
                <List>
                    <ListItem>
                        {props.children}
                    </ListItem>
                    <ListItem>
                        <DialogButton name={"confirm"} icon={<DoneIcon/>} label={"confirm"} onClick={handleConfirm}/>
                        <DialogButton name={"cancel"} color="secondary" icon={<Cancel/>} label={"Cancel"}
                                      onClick={handleClose}/>
                    </ListItem>
                </List>
            </DialogContent>
        </Dialog>
    )
};
