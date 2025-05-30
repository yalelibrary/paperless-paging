import React, {useEffect, useState} from 'react';
import {apiCurrentTaskBatch, apiSaveTaskBatchAssigner, parseDate,} from "../../api/Client";
import Paper from "@mui/material/Paper";
import TableContainer from "@mui/material/TableContainer";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableRow from "@mui/material/TableRow";
import TableCell from "@mui/material/TableCell";
import WarningIcon from "@mui/icons-material/Warning";
import TablePagination from "@mui/material/TablePagination";
import Typography from "@mui/material/Typography";
import Grid from "@mui/material/Grid2";
import Button from "@mui/material/Button"
import Dialog from '@mui/material/Dialog';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle'
import CloseIcon from '@mui/icons-material/Close';
import FormControl from "@mui/material/FormControl";
import InputLabel from "@mui/material/InputLabel";
import Select from "@mui/material/Select";
import MenuItem from "@mui/material/MenuItem";
import {ConfirmDialog} from "../ui-components/ConfirmDialog";
import {TableHeaders} from "../ui-components/TableHeaders";
import {sortedAndFilteredBatchInfos, sortedAndFilteredTasks} from "../../helpers/SortFilterOrder";


function TextAndContent(props) {
    return <div className={props.classes.flexCenter} title={props.tooltip}>
        {props.text}
        {props.children}
    </div>
}

const headCellsBatch = [
    {id: 'callNumber', numeric: false, disablePadding: false, label: 'Call Number', sortable: true},
    {id: 'taskLocation', numeric: false, disablePadding: false, label: 'Location', sortable: true},
    {id: 'status', numeric: false, disablePadding: false, label: 'Task Status', sortable: true},
    {id: 'taskFillProblems', numeric: false, disablePadding: false, label: 'Problems', sortable: false},
];
const headCellsBatchInfosCallNumber = [
    {id: 'actions', numeric: false, disablePadding: false, label: 'Actions', sortable: false},
    {id: 'userSort', numeric: false, disablePadding: false, label: 'Retriever', sortable: true},
    {id: 'taskLocations', numeric: false, disablePadding: false, label: 'CircDesk (total)', sortable: true},
    {id: 'minCallNumber', numeric: false, disablePadding: false, label: 'Beginning Call Number', sortable: true},
    {id: 'maxCallNumber', numeric: false, disablePadding: false, label: 'Ending Call Number', sortable: true},
    {id: 'startDateTime', numeric: false, disablePadding: false, label: 'Start Time', sortable: true},
    {id: 'mostRecentResponse', numeric: false, disablePadding: false, label: 'Last Response', sortable: true},
    {id: 'remaining', numeric: false, disablePadding: false, label: 'Remaining', sortable: false},
];
const headCellsBatchInfosTitle = [
    {id: 'actions', numeric: false, disablePadding: false, label: 'Actions', sortable: false},
    {id: 'userSort', numeric: false, disablePadding: false, label: 'Retriever', sortable: true},
    {id: 'taskLocations', numeric: false, disablePadding: false, label: 'CircDesk (total)', sortable: true},
    {id: 'minSortTitle', numeric: false, disablePadding: false, label: 'Beginning Title', sortable: true},
    {id: 'maxSortTitle', numeric: false, disablePadding: false, label: 'Ending Title', sortable: true},
    {id: 'startDateTime', numeric: false, disablePadding: false, label: 'Start Time', sortable: true},
    {id: 'mostRecentResponse', numeric: false, disablePadding: false, label: 'Last Response', sortable: true},
    {id: 'remaining', numeric: false, disablePadding: false, label: 'Remaining', sortable: false},
];


const BatchTable = ({classes, tasks, open, user, handleClose}) => {

    const [order, setOrder] = useState('avatar');
    const [orderBy, setOrderBy] = useState('avatar');
    const [page, setPage] = useState(0);
    const dense = true;
    const [rowsPerPage, setRowsPerPage] = useState(10);
    const [anchorEl, setAnchorEl] = useState(null);
    const filters = {
        problemFilter: "all",
        statusFilter: "all",
        locationFilter: "all"
    };

    const getSortedAndFilteredTasks = () => {
        return sortedAndFilteredTasks(tasks, order, orderBy, filters);
    };

    const handleRequestSort = (event, property) => {
        const isAsc = orderBy === property && order === 'asc';
        setOrder(isAsc ? 'desc' : 'asc');
        setOrderBy(property);
    };

    const handleChangePage = (event, newPage) => {
        setPage(newPage);
    };

    const handleChangeRowsPerPage = (event) => {
        setRowsPerPage(parseInt(event.target.value, 10));
        setPage(0);
    };
    return (

        <Dialog fullScreen open={open} onClose={handleClose} aria-labelledby="form-dialog-title">
            <DialogTitle id="form-dialog-title">
                <Grid container>
                    <Grid className={classes.dialogTitle}>
                        Tasks assigned to retriever {user}
                    </Grid>
                    <Grid>
                        <Button color={"primary"} onClick={handleClose} startIcon={<CloseIcon/>}
                                variant={"contained"}>Close</Button>
                    </Grid>
                </Grid>
            </DialogTitle>
            <DialogContent>
                <TableContainer component={Paper}>
                    <Table
                        className={classes.table}
                        aria-labelledby="tableTitle"
                        size={dense ? 'small' : 'medium'}
                        aria-label="enhanced table"
                    >
                        <TableHeaders
                            headCells={headCellsBatch}
                            classes={classes}
                            order={order}
                            orderBy={orderBy}
                            onRequestSort={handleRequestSort}
                            anchorEl={anchorEl}
                            setAnchorEl={setAnchorEl}
                        >Select All</TableHeaders>
                        <TableBody>
                            {getSortedAndFilteredTasks()
                                .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                                .map((row, index) => {
                                    return (
                                        <TableRow
                                            hover
                                            role="checkbox"
                                            tabIndex={-1}
                                            key={index}
                                        >
                                            <TableCell align="left">
                                                <TextAndContent text={row.callNumber || "No Callnumber"}
                                                                classes={classes}
                                                                tooltip={row.taskProblem && row.taskProblem.problemType === "callnumber" && row.taskProblem.name}>{row.taskProblem && row.taskProblem.problemType === "callnumber" &&
                                                    <WarningIcon className={classes.warningIcon}/>}</TextAndContent>
                                            </TableCell>
                                            <TableCell align="left">
                                                <TextAndContent text={row.taskLocation} classes={classes}
                                                                tooltip={row.taskProblem && row.taskProblem.problemType === "location" && row.taskProblem.name}>{row.taskProblem && row.taskProblem.problemType === "location" &&
                                                    <WarningIcon className={classes.warningIcon}/>}</TextAndContent>
                                            </TableCell>
                                            <TableCell align="left">
                                                {row.status}{row.incomingStatus === 'NOS' && row.status === 'New' && ' (2x)'}
                                            </TableCell>
                                            <TableCell align="left">
                                                {row.taskFillProblems.map((p) => p.name).join(", ")}
                                            </TableCell>
                                        </TableRow>
                                    );
                                })}
                        </TableBody>
                    </Table>
                </TableContainer>
                <TablePagination
                    rowsPerPageOptions={[10, 25, 50]}
                    component="div"
                    count={getSortedAndFilteredTasks().length}
                    rowsPerPage={rowsPerPage}
                    page={page}
                    onPageChange={handleChangePage}
                    onRowsPerPageChange={handleChangeRowsPerPage}
                />
            </DialogContent>
        </Dialog>
    )
};

export const UserBatch = ({classes, refreshUserWithBatches, batchInfos, loggedInUser}) => {
    const [tasks, setTasks] = useState([]);
    const [selectedBatchInfo, setSelectedBatchInfo] = useState({});
    const [order, setOrder] = useState('avatar');
    const [orderBy, setOrderBy] = useState('avatar');
    const [page, setPage] = useState(0);
    const dense = true;
    const [rowsPerPage, setRowsPerPage] = useState(10);
    const [openDialog, setOpenDialog] = useState(false);
    const [anchorEl, setAnchorEl] = useState(null);
    const [displayColumn, setDisplayColumn] = useState("callNumber");
    const [batchToSubmit, setBatchToSubmit] = useState(null);
    const [taskLocation, setTaskLocation] = useState("All");

    useEffect(() => {
        selectedBatchInfo && selectedBatchInfo.userId && apiCurrentTaskBatch({id: selectedBatchInfo.userId}).then((taskBatch) => {
            setTasks(taskBatch.tasks.map((cs) => {
                return cs;
            }));
        }).catch(() => {
        })
    }, [selectedBatchInfo]);

    const getSortedAndFilteredBatchInfos = () => {
        return sortedAndFilteredBatchInfos(batchInfos, order, orderBy, taskLocation)
    };

    const handleRequestSort = (event, property) => {
        const isAsc = orderBy === property && order === 'asc';
        setOrder(isAsc ? 'desc' : 'asc');
        setOrderBy(property);
    };

    const handleChangePage = (event, newPage) => {
        setPage(newPage);
    };

    const handleChangeRowsPerPage = (event) => {
        setRowsPerPage(parseInt(event.target.value, 10));
        setPage(0);
    };

    const handleCloseDialog = () => {
        setOpenDialog(false)
    };


    const handleSubmitBatch = (event, batchInfo) => {
        setBatchToSubmit(batchInfo);
    };

    const handleSubmitBatchCancel = (event) => {
        setBatchToSubmit(null);
    };

    const handleSubmitBatchConfirm = (event) => {
        apiSaveTaskBatchAssigner(batchToSubmit.taskBatchId).then(savedBatch => {
            refreshUserWithBatches();
            setBatchToSubmit(null);
        }).catch(e =>
            console.log("Error:", e)
        );
    };

    const handleDisplayColumnChange = (event) => {
        setDisplayColumn(event.target.value);
        if (event.target.value === "title") {
            setOrderBy("minSortTitle");
        } else {
            setOrderBy("minCallNumber")
        }
    };
    const handleTaskLocation = (event) => {
        setTaskLocation(event.target.value);
    }
    // , setTaskLocation] = useState("All");

    return (
        <div>
            <ConfirmDialog
                open={batchToSubmit || false}
                handleClose={handleSubmitBatchCancel}
                handleConfirm={handleSubmitBatchConfirm}
                classes={classes}
                confirmTitle={batchToSubmit && (batchToSubmit.cancellable ? "Cancel Batch?" : "Submit Batch for Retriever?")}
            >
                {batchToSubmit && (batchToSubmit.cancellable ?
                        <Typography>Are you sure you want to cancel this batch?</Typography>
                        :
                        <Typography>The retriever has already started working on this batch. Are you sure you want to
                            submit
                            their responses and close the batch?</Typography>
                )
                }
            </ConfirmDialog>
            <BatchTable classes={classes}
                        handleClose={handleCloseDialog}
                        open={openDialog}
                        tasks={tasks}
                        user={[selectedBatchInfo.userLastName, selectedBatchInfo.userFirstName].join(", ")}
            />
            {batchInfos && batchInfos.length > 0 &&
                <div className={classes.userBatch}>

                    <div className={classes.topControls}>
                        <FormControl className={classes.formControl}>
                            <InputLabel id="select-display-column-lbl" className={"offset-label"}><b className="cross-subheader"> List
                                By </b></InputLabel>
                            <Select
                                variant={"standard"}
                                labelId="select-display-column-lbl"
                                onChange={handleDisplayColumnChange}
                                value={displayColumn}
                            >
                                <MenuItem value='callNumber'>Call Number</MenuItem>
                                <MenuItem value='title'>Title</MenuItem>
                            </Select>
                        </FormControl>
                        <FormControl className={`${classes.formControl} ${classes.ml1}`}>
                            <InputLabel id="task-group-column-lbl" className={"offset-label"}><b className="cross-subheader"> Circ
                                Desk </b></InputLabel>
                            <Select
                                variant={"standard"}
                                labelId="task-group-column-lbl"
                                value={taskLocation}
                                onChange={handleTaskLocation}
                            ><MenuItem value="All"> All </MenuItem>
                                {loggedInUser.circDesks.map((circDesk) => <MenuItem
                                    value={circDesk.name}>{circDesk.name}</MenuItem>)}
                            </Select>
                        </FormControl>
                    </div>
                    <TableContainer>
                        <Table
                            className={classes.table}
                            aria-labelledby="tableTitle"
                            size={dense ? 'small' : 'medium'}
                            aria-label="enhanced table"
                        >
                            <TableHeaders
                                headCells={displayColumn === "title" ? headCellsBatchInfosTitle : headCellsBatchInfosCallNumber}
                                classes={classes}
                                order={order}
                                orderBy={orderBy}
                                onRequestSort={handleRequestSort}
                                anchorEl={anchorEl}
                                setAnchorEl={setAnchorEl}
                            >Select All</TableHeaders>
                            <TableBody>
                                {getSortedAndFilteredBatchInfos()
                                    .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                                    .map((row, index) => {
                                        return (
                                            <TableRow
                                                hover
                                                role="checkbox"
                                                tabIndex={-1}
                                                key={index}
                                            >
                                                <TableCell align="left" className={classes.noWrap}>
                                                    <Button className={classes.actionButton} color={"primary"}
                                                            variant={"outlined"} onClick={() => {
                                                        setOpenDialog(true);
                                                        setSelectedBatchInfo(row)
                                                    }}>View</Button>
                                                    <Button className={classes.actionButton} color={"primary"}
                                                            variant={"outlined"} onClick={(event) => {
                                                        handleSubmitBatch(event, row)
                                                    }}>{row.cancellable ? "Cancel Batch" : "Submit Batch"}</Button>
                                                </TableCell>
                                                <TableCell align="left">
                                                    {`${row.userLastName}, ${row.userFirstName}`}
                                                </TableCell>
                                                <TableCell align="left">
                                                    {row.taskLocations.join(", ")}
                                                    &nbsp;({row.taskCount})
                                                </TableCell>
                                                <TableCell align="left">
                                                    {displayColumn === "title" ? row.minTitle : row.minCallNumber}
                                                </TableCell>
                                                <TableCell align="left">
                                                    {displayColumn === "title" ? row.maxTitle : row.maxCallNumber}
                                                </TableCell>
                                                <TableCell align="left">
                                                    {row.startDateTime && parseDate(row.startDateTime).toLocaleString()}
                                                </TableCell>
                                                <TableCell align="left">
                                                    {row.mostRecentResponse && parseDate(row.mostRecentResponse).toLocaleString()}
                                                </TableCell>
                                                <TableCell align="left">
                                                    {(row.taskCount - row.responseCount) + " of " + row.taskCount}
                                                </TableCell>
                                            </TableRow>
                                        );
                                    })}
                            </TableBody>
                        </Table>
                    </TableContainer>
                    <TablePagination
                        rowsPerPageOptions={[10, 25, 50]}
                        component="div"
                        count={getSortedAndFilteredBatchInfos().length}
                        rowsPerPage={rowsPerPage}
                        page={page}
                        onPageChange={handleChangePage}
                        onRowsPerPageChange={handleChangeRowsPerPage}
                    />
                </div>
            }
            {(!batchInfos || batchInfos.length === 0) &&
                <Typography>No assigned Task Batches</Typography>
            }
        </div>

    )
};

export default UserBatch

