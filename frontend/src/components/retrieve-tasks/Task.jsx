import Card from "@mui/material/Card";
import {CardContent} from "@mui/material";
import TableContainer from "@mui/material/TableContainer";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableRow from "@mui/material/TableRow";
import TableCell from "@mui/material/TableCell";
import {DialogButton} from "../ui-components/DialogButton";
import {InfoButton} from "../ui-components/InfoButton";
import FormControl from "@mui/material/FormControl";
import RadioGroup from "@mui/material/RadioGroup";
import FormControlLabel from "@mui/material/FormControlLabel";
import Radio from "@mui/material/Radio";
import Chip from "@mui/material/Chip";
import InfoIcon from "@mui/icons-material/Info";
import NotificationImportant from "@mui/icons-material/NotificationImportant";
import React from "react";
import {getBarcodes} from "../../helpers/Barcode";
import WarningIcon from "@mui/icons-material/Warning";
import Strings from "../../text/Strings.jsx";
import {formatDate} from "../../helpers/UiFunctions.js";
import Grid from "@mui/material/Grid2";

export const Task = ({
                             classes,
                             task,
                             online,
                             onUpdateTaskStatus,
                             onMoreInformation,
                             onProblem,
                             statusOptions,
                             statusMap
                         }) => {
    return (
        <Card key={task.id}>
            <div style={{position: "relative", top: "-80px"}} ref={task.ref}></div>
            <CardContent>
                <TableContainer className={[classes[`card`], classes[task.status.toLowerCase()]].join(" ")}>
                    <Table className={classes.table} size="small" aria-label="a dense table">
                        <TableBody>
                            <TableRow>
                                <TableCell align="right" sx={{width:'175px'}}>
                                    <InfoButton className={classes.statusButton}>
                                        {statusOptions.map(o => <span
                                            key={statusMap[o]}>{o}: {statusMap[o]}<br/></span>)}
                                    </InfoButton>
                                    Task Status:</TableCell>
                                <TableCell align="left">
                                    <Grid container fullWidth>
                                        <Grid flexGrow={1}>
                                            <FormControl component="fieldset" sx={{flexGrow:1, display: 'flex'}}>
                                                <RadioGroup row aria-label="status" name="status" value={task.status} flexGrow={1}>
                                                    <Grid container flexGrow={1}>
                                                        {statusOptions.filter((o)=>o !== "New").map((option, index) => {
                                                            return <Grid flexGrow={1}>
                                                                <FormControlLabel key={option} value={option}
                                                                               control={<Radio color="primary" onClick={(e) => onUpdateTaskStatus(task, option)}/>}
                                                                               label={Strings["statuses"][option] || option}/></Grid>
                                                        })}
                                                    </Grid>
                                                </RadioGroup>
                                            </FormControl>
                                        </Grid>
                                        <Grid>
                                            <div className={classes.floatRight}>
                                                {((task.taskFillProblems && task.taskFillProblems.length > 0) || (task.notes)) &&
                                                    <Chip className={classes.chip} size="small" color={"primary"}
                                                          label="Note Recorded"/>}
                                                {task.status === "FOS_2x" &&
                                                    <DialogButton name={"Note"} color={"secondary"}
                                                                  icon={<NotificationImportant/>} label={"Note"}
                                                                  onClick={onProblem}/>
                                                }
                                                <DialogButton name={"Details"} color={"secondary"} icon={<InfoIcon/>}
                                                              label={"Details"} onClick={onMoreInformation}/>
                                            </div>
                                        </Grid>
                                    </Grid>
                                </TableCell>
                            </TableRow>
                            <TableRow>
                                <TableCell align="right">Call Number:</TableCell>
                                <TableCell align="left">{task.callNumber}</TableCell>
                            </TableRow>
                            {(task.voyagerTaskStatus === 3 && <TableRow>
                                <TableCell align="right">Reassigned:</TableCell>
                                <TableCell align="left">True <WarningIcon className={classes.warningIcon}/></TableCell>
                            </TableRow>) || null}
                            {(task.enumeration &&
                                <TableRow>
                                    <TableCell align="right">Enum / Chron / Year:</TableCell>
                                    <TableCell align="left">{task.enumeration || "none"}</TableCell>
                                </TableRow>) || null}
                            <TableRow>
                                <TableCell align="right">Title:</TableCell>
                                <TableCell align="left">{task.title}</TableCell>
                            </TableRow>
                            <TableRow>
                                <TableCell align="right">Item Barcode:</TableCell>
                                <TableCell align="left">{getBarcodes(task)}</TableCell>
                            </TableRow>
                            {/*{task.physicalDescription &&*/}
                            {/*<TableRow>*/}
                            {/*    <TableCell align="right">Physical Description:</TableCell>*/}
                            {/*    <TableCell align="left">{task.physicalDescription}</TableCell>*/}
                            {/*</TableRow>}*/}
                            {((task.itemPermLocation != task.itemTempLocation) && (task.itemPermLocation && task.itemTempLocation) )?<>
                                <TableRow>
                                    <TableCell align="right">Temp / Perm Location:</TableCell>
                                    <TableCell
                                        align="left">{task.itemTempLocation + " / " + task.itemPermLocation}</TableCell>
                                </TableRow>
                            </>:<>
                                <TableRow>
                                    <TableCell align="right">Location:</TableCell>
                                    <TableCell
                                        align="left">{task.itemPermLocation || "none"}</TableCell>
                                </TableRow>
                            </>}
                            {(task.taskFillProblems && task.taskFillProblems.length > 0 &&
                                <TableRow>
                                    <TableCell align="right">Problems:</TableCell>
                                    <TableCell
                                        align="left">{task.taskFillProblems.map((p) => p.name).join("; ")}</TableCell>
                                </TableRow>) || null
                            }

                            {task.lastDischargeDateTime &&
                                <TableRow>
                                    <TableCell align="right">Item Modified:</TableCell>
                                    <TableCell
                                        align="left">{formatDate(task.lastDischargeDateTime)}</TableCell>
                                </TableRow>}
                        </TableBody>
                    </Table>
                </TableContainer>
            </CardContent>
        </Card>
    )
};