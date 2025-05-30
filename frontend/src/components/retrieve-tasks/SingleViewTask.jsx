import Card from "@mui/material/Card";
import {CardContent} from "@mui/material";
import Typography from "@mui/material/Typography";
import {DialogButton} from "../ui-components/DialogButton";
import {InfoButton} from "../ui-components/InfoButton";
import Grid from "@mui/material/Grid2";
import RadioGroup from "@mui/material/RadioGroup";
import FormControlLabel from "@mui/material/FormControlLabel";
import Radio from "@mui/material/Radio";
import LinkIcon from "@mui/icons-material/Link";
import ArrowBack from "@mui/icons-material/ArrowBack";
import NotificationImportant from "@mui/icons-material/NotificationImportant";
import WarningIcon from "@mui/icons-material/Warning";
import PriorIcon from "@mui/icons-material/NavigateBefore";
import NextIcon from "@mui/icons-material/NavigateNext";
import React from "react";
import {KeyValue} from "../ui-components/KeyValue";
import {getBarcodes} from "../../helpers/Barcode";
import Strings from "../../text/Strings.jsx";
import {formatDate, formatDateOnly} from "../../helpers/UiFunctions.js";


export const SingleViewTask = ({
                                       classes,
                                       task,
                                       online,
                                       setTaskIndex,
                                       taskIndex,
                                       tasks,
                                       onUpdateTaskStatus,
                                       onProblem,
                                       taskFillProblems,
                                       statusOptions,
                                       statusMap,
                                       setTab
                                   }) => {
    return (
        <Card>
            <CardContent className={[classes.card, classes[task.status.toLowerCase()]].join(" ")}>
                <Typography className={classes.heading} component={'div'}>
                    <div className={[classes.card, classes[task.status.toLowerCase()]]}>
                        <KeyValue singleLine={true}
                                  name={
                                      <>
                                          <InfoButton className={classes.statusButton}>
                                              {statusOptions.map(o => <span key={statusMap[o]}>{o}: {statusMap[o]}<br/></span>)}
                                          </InfoButton>
                                          Task Status
                                      </>
                                  }
                                  value={
                                      <Grid container>
                                          <Grid container className={classes.radioGrid}>
                                              <RadioGroup className={classes.radioGroup} row aria-label="status"
                                                          sx={{flexGrow:1}}
                                                          name="status" value={task.status}>
                                                  <Grid container flexGrow={1}>
                                                  {statusOptions.filter((o)=> o !== 'New').map((option, index) => {
                                                      return <Grid flexGrow={1} >
                                                          <FormControlLabel key={option} value={option}
                                                                               control={<Radio color="primary" onClick={(e)=>onUpdateTaskStatus(task, option)}/>}
                                                                                     label={Strings["statuses"][option] || option}/>
                                                      </Grid>
                                                  })}
                                                  </Grid>
                                              </RadioGroup>
                                          </Grid>
                                          <Grid>{task.status === "FOS_2x" &&
                                              <DialogButton name={"Note"} color={"secondary"}
                                                            icon={<NotificationImportant/>} label={"Note"}
                                                            onClick={onProblem}/>}
                                              <DialogButton name={"Back to List"} color={"secondary"}
                                                            icon={<ArrowBack/>} label={"Back to List"}
                                                            onClick={() => (setTab(0))}/>
                                          </Grid>
                                      </Grid>
                                  }
                        />
                        {((task.itemPermLocation != task.itemTempLocation) && (task.itemPermLocation && task.itemTempLocation) )?<>
                            <KeyValue name={"Item Temp/Perm Location"} value={(task.itemTempLocation + " / " + task.itemPermLocation)}
                                      singleLine={true}/>
                        </>:<>
                            <KeyValue name={"Item Location"} value={(task.itemTempLocation || task.itemPermLocation || "none")}
                                                                                            singleLine={true}/>
                        </>}
                        <KeyValue name={"Call Number"} value={task.callNumber} singleLine={true}/>
                        {task.voyagerTaskStatus === 3 && <KeyValue name={"Reassigned"}
                                                                           value={<Typography>True <WarningIcon
                                                                               className={classes.warningIcon}/></Typography>}
                                                                           singleLine={true}/>}
                        {task.enumeration &&
                            <KeyValue name={"Enum / Chron / Year"} value={task.enumeration || "none"}
                                      singleLine={true}/>}
                        <KeyValue name={"Item Barcode"} value={getBarcodes(task)} singleLine={true}/>
                        <KeyValue name={"Title"} value={task.title} singleLine={true}/>
                        <KeyValue name={"Author"} value={task.author || "none"} singleLine={true}/>
                        {task.publisher &&
                            <KeyValue name={"Publisher"} value={task.publisher} singleLine={true}/>}
                        {task.physicalDescription &&
                            <KeyValue name={"Physical Description"} value={task.physicalDescription} singleLine={true}/>}
                        {task.publicationDate &&
                            <KeyValue name={"Publication Date"} value={task.publicationDate} singleLine={true}/>}
                        {/*<KeyValue name={"Holding / Temp Location"}*/}
                        {/*          value={(task.holdingLocation || "none") + " / " + (task.itemTempLocation || "none")}*/}
                        {/*          singleLine={true}/>*/}
                        <KeyValue name={"Route To"} value={task.pickupLocationDisplay} singleLine={true}/>
                        <KeyValue name={"Destination"} value={task.destination} singleLine={true}/>
                        <KeyValue name={"Patron Request Date"} value={formatDate(task.patronRequestDate)} singleLine={true}/>
                        {task.patronComment &&
                            <KeyValue name={"Patron Comment"} value={task.patronComment} singleLine={true}/>}
                        {task.taskFillProblems && task.taskFillProblems.length > 0 &&
                            <KeyValue name={"Problems"}
                                      value={task.taskFillProblems.map((p) => p.name).join("; ")}
                                      singleLine={true}/>}
                        {task.lastDischargeDateTime &&
                            <KeyValue name={"Item Modified"} value={formatDate(task.lastDischargeDateTime)}
                                      singleLine={true}/>}
                    </div>
                    <div>
                        <Grid container direction={"row-reverse"}>
                            <Grid>
                                <DialogButton name={"Previous"} icon={<PriorIcon/>} label={"Previous"}
                                              onClick={() => setTaskIndex(Math.max(0, taskIndex - 1))}/>
                                {taskIndex + 1} of {tasks.length}
                                <DialogButton name={"Next"} icon={<NextIcon/>} label={"Next"}
                                              onClick={() => setTaskIndex(Math.min(tasks.length - 1, taskIndex + 1))}/>
                            </Grid>
                        </Grid>
                    </div>
                </Typography>
            </CardContent>
        </Card>
    )
};