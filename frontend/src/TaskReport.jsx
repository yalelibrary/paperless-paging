import * as PropTypes from "prop-types";
import TextField from "@mui/material/TextField";
import React, {useEffect, useState} from "react";
import {ArrowDownward, Search} from "@mui/icons-material";
import Button from "@mui/material/Button";
import {apiFetchBarcodeReport, apiFetchMmsidReport} from "./api/Client.js";
import {KeyValue} from "./components/ui-components/KeyValue.jsx";
import TableCell from "@mui/material/TableCell";
import TableBody from "@mui/material/TableBody";
import TableRow from "@mui/material/TableRow";
import TableContainer from "@mui/material/TableContainer";
import Table from "@mui/material/Table";
import {TableHeaders} from "./components/ui-components/TableHeaders.jsx";
import {Accordion, AccordionDetails, AccordionSummary, Grid2, useEventCallback} from "@mui/material";
import {formatDate} from "./helpers/UiFunctions.js";
import Typography from "@mui/material/Typography";
import {useParams, useHistory, useLocation, Link} from "react-router-dom";

const assignmentHeadCells = [
    {id: 'timestamp', numeric: false, disablePadding: false, label: 'Timestamp', sortable: false},
    {id: 'assigner', numeric: false, disablePadding: false, label: 'Assigner', sortable: false},
    {id: 'retriever', numeric: false, disablePadding: false, label: 'Retriever', sortable: false},
    {id: 'barcode', numeric: false, disablePadding: false, label: 'Barcode', sortable: false},
];
const logHeadCells = [
    {id: 'timestamp', numeric: false, disablePadding: false, label: 'Timestamp', sortable: false},
    {id: 'message', numeric: false, disablePadding: false, label: 'Message', sortable: false},
    {id: 'user', numeric: false, disablePadding: false, label: 'User', sortable: false},
    {id: 'barcode', numeric: false, disablePadding: false, label: 'Barcode', sortable: false},
];


function TaskReportEntry({task, index, count}) {
    return <div key={index}>
        <hr/>
        <Grid2 container>
            <Grid2 flexGrow={1}>
                <h4>Task {index + 1} of {count}</h4>
            </Grid2>
            <Grid2>
                <KeyValue name={"Created"} value={formatDate(task.task.createDateTime)}/>
            </Grid2>
            <Grid2>
                <KeyValue name={"Current Status"} value={task.task.status}/>
            </Grid2>
        </Grid2>
        {task.assignments && task.assignments.length > 0 &&
            <Accordion>
                <AccordionSummary
                    expandIcon={<ArrowDownward />}
                    aria-controls="assignments-content"
                >
                    <Typography>Assignments</Typography>
                </AccordionSummary>
                <AccordionDetails id="assignments-content">
                    <TableContainer>
                        <Table>
                            <TableHeaders headCells={assignmentHeadCells}>
                            </TableHeaders>
                            <TableBody>
                                {task.assignments.map((assignment, index) => <TableRow key={index}>
                                        <TableCell>{formatDate(assignment.timestamp)}</TableCell>
                                        <TableCell>{assignment.assigner}</TableCell>
                                        <TableCell>{assignment.retriever}</TableCell>
                                        <TableCell>{assignment.itemBarcode ? <Link to={`/barcode/${assignment.itemBarcode}`}>{assignment.itemBarcode}</Link> : "unavailable"}</TableCell>
                                    </TableRow>
                                )}
                            </TableBody>
                        </Table>
                    </TableContainer>
                </AccordionDetails>
            </Accordion>
        }

        {task.logs && task.logs.length > 0 &&
            <Accordion>
                <AccordionSummary
                    expandIcon={<ArrowDownward />}
                    aria-controls="logs-content"
                >
                    <Typography>Logs</Typography>
                </AccordionSummary>
                <AccordionDetails id="logs-content">
                    <TableContainer>
                        <Table>
                            <TableHeaders headCells={logHeadCells}>
                            </TableHeaders>
                            <TableBody>
                                {task.logs.map((log, index) => <TableRow key={index}>
                                        <TableCell>{formatDate(log.timestamp)}</TableCell>
                                        <TableCell>{log.message}</TableCell>
                                        <TableCell>{log.userName}</TableCell>
                                        <TableCell>{log.itemBarcode ? <Link to={`/barcode/${log.itemBarcode}`}>{log.itemBarcode}</Link> : "unavailable"}</TableCell>
                                    </TableRow>
                                )}
                            </TableBody>
                        </Table>
                    </TableContainer>
                </AccordionDetails>
            </Accordion>
        }
    </div>;
}

export function TaskReport(props) {
    const [report, setReport] = useState(null);
    const [reportId, setReportId] = useState("");
    let routerParams = useParams();
    let history = useHistory();
    let location = useLocation();

    const reportType = () => location.pathname.replace(/[0-9\/]/g, '');
    const reportName = () => {
        let val = reportType();
        if (val === 'mmsid') return 'MMS ID';
        return String(val).charAt(0).toUpperCase() + String(val).slice(1);
    }

    const lookupId = () => {
        if (reportId) {
            history.push (`/${reportType()}/${reportId}`);
        }
    }

    const cleanId = (s) => {
        return s.replace(/[^0-9]/g, '');
    }

    useEffect(()=> {
        setReport(null);
        setReportId(routerParams.id || ""   );
        if (routerParams.id) {
            if (reportType() === 'mmsid') {
                apiFetchMmsidReport(routerParams.id).then((data) => setReport(data));
            } else {
                apiFetchBarcodeReport(routerParams.id).then((data) => setReport(data));
            }
        }
    }, [routerParams.id])

    let mmsId = '';
    let barcodes = new Set();

    if (report && reportType() === 'barcode' && report.length > 0) {
        mmsId = report[0].task.almaBibMmsId;
    }

    if (report && reportType() === 'mmsid') {
        report.forEach( (r)=> {
            r.task.itemBarcode && barcodes.add(r.task.itemBarcode);
            r.assignments.forEach((a)=>{
                a.itemBarcode && barcodes.add(a.itemBarcode);
            });
            r.logs.forEach((a)=>{
                a.itemBarcode && barcodes.add(a.itemBarcode);
            })
        });
    }

    return <div className={"pt-5"}>
        <Grid2 container>
            <Grid2>
                <TextField
                    variant="standard"
                    label={<b className="cross-subheader"> {reportName()} </b>}
                    value={reportId}
                    onChange={(e) => setReportId(cleanId(e.target.value))}
                />
            </Grid2>
            <Grid2 className={'pt-2'} flexGrow={1}>
                <Button
                    id={"search-button"}
                    variant="contained"
                    color="primary"
                    size="medium"
                    startIcon={<Search/>}
                    onClick={lookupId}
                >Search</Button>
            </Grid2>
            <Grid2>
                {reportType() !== 'mmsid' && <Link to={`/mmsid/${mmsId}`}>Search by MMS ID</Link>}
                {reportType() !== 'barcode' && (barcodes.size <= 1 ? <Link to={`/barcode/${[...barcodes][0] || ''}`}>Search by Barcode</Link> : (<span>Search by Barcode: {[...barcodes].map((s)=> <Link to={`/barcode/${s}`}>{s}</Link>)}</span>))}
            </Grid2>
        </Grid2>
            <div>
                {report && <div>
                    <h3 className={"pt-5"}>Report for {reportName()}: {routerParams.id}</h3>
                    {(report.length===0) && <>No information for {routerParams.id}.</>}
                    {report.map( (task, index) => <TaskReportEntry task={task} key={index} index={index} count={report.length}/>)
                    }
                </div>}

            </div>
        </div>;
}

TaskReport.propTypes = {
    toast: PropTypes.func,
    online: PropTypes.bool,
    loggedInUser: PropTypes.any
};