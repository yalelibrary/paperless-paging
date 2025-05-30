import Box from "@mui/material/Box";
import CircularProgress from "@mui/material/CircularProgress";
import Typography from "@mui/material/Typography";
import React from "react";
import Grid from "@mui/material/Grid2";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableRow from "@mui/material/TableRow";
import TableCell from "@mui/material/TableCell";
import DoneIcon from "@mui/icons-material/Done";
import {createStyles, makeStyles} from "@mui/styles";
import {DialogButton} from "./DialogButton";
import {WrapInAppBar} from "./WrapInAppBar";
import Strings from "../../text/Strings";

const useStyles = makeStyles((theme) =>
    createStyles({
        root: {
            width: '100%',
        },
        formControl: {
            margin: theme.spacing(1),
        },
        selectEmpty: {
            marginTop: theme.spacing(2),
        },
        tableCell: {},
        nos1: {
            borderColor: "#e57373",
        },
        nos1Text: {
            color: "#e57373",
        },
        nos2: {
            borderColor: "#e57373",
        },
        nos2Text: {
            color: "#e57373",
        },
        nos2Button: {
            background: "#e57373",
        },

        fosText: {
            color: "#4caf50"
        },
        newText: {
            color: "#2196f3"
        },
        floatRight: {
            float: "right"
        },
        submitButton: {
            marginTop: '10px'
        },
        justifyEnd: {
            justifyContent: "flex-end"
        },
        workProgress: {
            background: theme.palette.grey[100]
        },
        circularProgress: {
            marginTop: "10px"
        },
        bottomAppBar: {
            top: 'auto !important',
            bottom: 0,
        },
        bottomPadding: {
            height: "100px"
        },
        bigFont: {
            fontSize: "100px"
        }
    }),
);

export function CircularProgressWithLabel(props) {
    return (
        <Box position="relative" display="inline-flex">
            <CircularProgress variant="determinate" aria-label={"Progress Bar"} {...props} />
            <Box
                top={0}
                left={0}
                bottom={0}
                right={0}
                position="absolute"
                display="flex"
                alignItems="center"
                justifyContent="center"
            >
                <Typography variant="caption" component="div" color="textSecondary">{`${Math.round(
                    props.value,
                )}%`}</Typography>
            </Box>
        </Box>
    );
}

export const WorkProgress = ({
                                 toolBar = true,
                                 adminSubmit = false,
                                 tasks,
                                 loggedInUser,
                                 onTasksSubmitted,
                                 startTime,
                                 newCount,
                                 fosCount
                             }) => {
    const classes = useStyles();
    return (
        <WrapInAppBar classes={classes} wrap={toolBar}>
            <Grid container className={classes.workProgress}>
                <Grid container size={6}>
                    <Grid>
                        <Table>
                            <TableBody>
                                <TableRow>
                                    <TableCell>
                                        Total Tasks
                                    </TableCell>
                                    <TableCell>
                                        {tasks.length}
                                    </TableCell>
                                </TableRow>
                            </TableBody>
                        </Table>
                    </Grid>
                    <Grid>
                        <Table>
                            <TableBody>
                                <TableRow>
                                    <TableCell>
                                        <Typography component={"span"}
                                                    className={classes.newText}>New: {newCount}</Typography>&nbsp;/&nbsp;
                                        <Typography component={"span"}
                                                    className={classes.fosText}>FOS: {fosCount}</Typography>&nbsp;/&nbsp;
                                        <Typography component={"span"}
                                                    className={classes.nos1Text}>NOS: {tasks.length - newCount - fosCount}</Typography>
                                    </TableCell>
                                </TableRow>
                            </TableBody>
                        </Table>
                    </Grid>
                    <Grid>
                        <CircularProgressWithLabel className={classes.circularProgress}
                                                   value={100 * ((tasks.length - newCount) / tasks.length)}
                                                   variant="determinate"/>
                    </Grid>
                </Grid>
                <Grid container size={6} className={classes.justifyEnd}>
                    <Grid>
                        <Table>
                            <TableBody>
                                <TableRow>
                                    <TableCell>
                                        Start Time
                                    </TableCell>
                                    <TableCell>
                                        {startTime.toLocaleString()}
                                    </TableCell>
                                </TableRow>
                            </TableBody>
                        </Table>
                    </Grid>
                    <Grid>
                        <DialogButton name={"Submit Work"}
                                      icon={<DoneIcon/>}
                                      label={"Submit Work"}
                                      disabled={newCount === tasks.length}
                                      onClick={onTasksSubmitted}
                        />
                    </Grid>
                </Grid>
            </Grid>
        </WrapInAppBar>
    )
}