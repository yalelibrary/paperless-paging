import React, { useEffect, useState} from 'react';
import Typography from '@mui/material/Typography';
import FormControlLabel from '@mui/material/FormControlLabel';
import Card from '@mui/material/Card';
import {CardContent, Tab, Tabs, TextField} from '@mui/material';
import Button from "@mui/material/Button";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import Input from "@mui/material/Input";
import List from "@mui/material/List";
import ListItem from "@mui/material/ListItem";
import Cancel from "@mui/icons-material/Cancel";
import DoneIcon from "@mui/icons-material/Done";
import Checkbox from "@mui/material/Checkbox";
import NotificationImportant from '@mui/icons-material/NotificationImportant';
import Grid from "@mui/material/Grid2";
import TableToolbar from "./components/ui-components/TableToolbar";
import CircularProgress from '@mui/material/CircularProgress';
import localForage from 'localforage';
import {ConfirmDialog} from './components/ui-components/ConfirmDialog'
import {WorkProgress} from './components/ui-components/Progress'
import {DialogButton} from './components/ui-components/DialogButton'
import PowerSettingsNewIcon from '@mui/icons-material/PowerSettingsNew';
import {
    apiCurrentTaskBatch,
    apiFetchTaskStatusList,
    apiFetchFillProblems,
    apiLogoutUser,
    apiSaveTaskBatch,
    apiStoreTaskProgress, parseDate
} from "./api/Client";
import {sortedAndFilteredTasks} from "./helpers/SortFilterOrder";
import {SingleViewTask} from "./components/retrieve-tasks/SingleViewTask";
import {Task} from "./components/retrieve-tasks/Task";
import {KeyValue} from "./components/ui-components/KeyValue";
import {useStyles} from "./styles/MyTaskStyle";
import {messages} from "./toast-messages/RetrieveTasksMessages";
import Strings from "./text/Strings";
import {appTheme} from "./styles/MaterialThemeOverride.js";

export function RetrieveTasks({loggedInUser, online, toast})  {

    const classes = useStyles();
    const [tasks, setTasks] = useState(null);
    const [taskBatch, setTaskBatch] = useState( []);
    const [moreInfoTask, setMoreInfoTask] = useState(null);
    const [problemTask, setProblemTask] = useState(null);
    const [currentTab, setCurrentTab] = useState(0);
    const [taskIndex, setTaskIndex] = useState(0);
    const [tasksSubmitted, setTasksSubmitted] = useState(false);
    const order = 'asc';
    const [orderBy, setOrderBy] = React.useState('callNumberNormalized');
    const [startTime, setStartTime] = React.useState(new Date());
    const [endTime, setEndTime] = React.useState(null);
    const [itemIssues, setItemIssues] = React.useState([]);
    const [itemNotes, setItemNotes] = React.useState("");
    const [taskLoadError, setTaskLoadError] = React.useState(null);
    const [taskFillProblems, setTaskFillProblems] = React.useState([]);
    const [showSubmitConfirmDialog, setShowSubmitConfirmDialog] = React.useState(false);
    const [taskLocations, setTaskLocations] = React.useState([]);
    const [reportProblemSaveDisable, setReportProblemSaveDisable] = React.useState(false);
    const [firstSearchStatusOptions, setFirstSearchStatusOptions] = React.useState([]);
    const [secondSearchStatusOptions, setSecondSearchStatusOptions] = React.useState([]);
    const [reloadCacheConfirmOpen, setReloadCacheConfirmOpen] = React.useState(false);
    const [statusMap, setStatusMap] = React.useState({});
    const [submitDisable, setSubmitDisable] = React.useState(false);
    const cachingFieldList = ["status", "taskFillProblems"];

    const storeProgress = ( taskBatch, task ) => {
        const progress = {
            status: task.status,
            taskFillProblemList: task.taskFillProblems,
            notes: task.notes
        }
        apiStoreTaskProgress(taskBatch.id, task.id, progress).catch(
            ()=>console.log("Unable to save")
        );
    };

    const restoreCache = (taskBatch, tasks)=> {
        localForage.getItem("batchCache").then((cache)=>{
            if ( cache && cache.id === taskBatch.id ) {
                const cacheMap = {};
                cache.tasks.forEach((task)=>cacheMap[task.id] = task);
                const newTasks = tasks.map((cs)=>{
                    const cachedRecord = cacheMap[cs.id];
                    if ( cachedRecord ) {
                        cs = {...cs}; // copy
                        cachingFieldList.forEach((field) =>
                            cs[field] = cachedRecord[field]
                        );
                        storeProgress( taskBatch, cs);
                    }
                    return cs;
                });
                setTasks(newTasks);
            }
        })
    }

    // Checks if the cache will alter the values.  If yes, display confirmation dialog.
    const checkCache = (taskBatch, tasks)=> {
        localForage.getItem("batchCache").then((cache)=>{
            if ( cache && cache.id === taskBatch.id ) {
                const cacheMap = {};
                cache.tasks.forEach((task)=>cacheMap[task.id] = task);
                const someChanged = tasks.some((cs)=>{
                    const cachedRecord = cacheMap[cs.id];
                    if ( cachedRecord ) {
                        if (cs["status"] !== cachedRecord["status"]) return true;
                        if ((cs.taskFillProblems === null || cs.taskFillProblems.length === 0) !==
                            (cachedRecord.taskFillProblems === null || cachedRecord.taskFillProblems.length === 0) )
                            return true;
                        if ( cs.taskFillProblems !== null ) {
                            if ( cs.taskFillProblems.length !== cachedRecord.taskFillProblems.length )
                                return true;
                            for ( let i=0;i<cs.taskFillProblems.length;i++ ) {
                                if ( cs.taskFillProblems[i].id !== cachedRecord.taskFillProblems[i].id )
                                    return true;
                            }
                        }
                    }
                    return false;
                });
                setReloadCacheConfirmOpen(someChanged);
            }
        })
    }

    const storeCache = (tasks)=> {
        const cache = {
            id: taskBatch.id,
            tasks: tasks.map((task)=> {
                const r = {id: task.id};
                cachingFieldList.forEach((field) =>
                    r[field] = task[field]
                );
                return r;
            })};
        localForage.setItem("batchCache", cache);
    };

    useEffect(()=> {
        apiFetchTaskStatusList().then((list)=>{
            let newStatusMap = {}
            list.forEach( (status)=> {
                newStatusMap[status.value] = status.description;
            });
            setStatusMap(newStatusMap);
            setFirstSearchStatusOptions(list.filter((status)=>[0, 1].includes(status.searchIndex))
                .map((status)=>status.value));
            setSecondSearchStatusOptions(list.filter((status)=>[0,2].includes(status.searchIndex))
                .map((status)=>status.value));
        })
    },[]);



    useEffect(() => {
        apiCurrentTaskBatch(loggedInUser).then((taskBatch) => {
            setTaskBatch( taskBatch );
            setTasks( taskBatch.tasks.map((task) =>{
                task.ref = React.createRef()
                return task
            }) );
            setTaskLocations([...(new Set(taskBatch.tasks.map((cs)=>cs.taskLocation)))]);
            // check that cache exists and will change values
            checkCache(taskBatch, taskBatch.tasks )
        }).catch(() => {
            setTasks([]);
            setTaskLoadError("No Tasks Available")
        })
    }, [loggedInUser]);

    useEffect( () => {
        const fetchFillProblems = () => {
            apiFetchFillProblems().then((problems) => {
                if (!problems  || problems.length === 0) {
                } else {
                    setTaskFillProblems(problems);
                }
            });
        };
        fetchFillProblems();
    },[])

    const handleScrollToItem = task => {
        if (task.ref.current) task.ref.current.scrollIntoView({
            behavior: 'smooth',
            block: 'start',
        });
    }

    const handleStatusChange = (task, status) => {
        const newTasks = tasks.map((cs)=> {
            if ( cs === task ) {
                if (task.status === status) {
                    status = 'New';
                }
                // clear taskFillProblems on status change
                cs = {...cs, status: status, taskFillProblems: []};
                task = cs;
                storeProgress(taskBatch, cs);
            }
            return cs;
        });
        setTasks([...newTasks]);
        storeCache(newTasks);
    };

    const handleIssuesChange = (task, issues, itemNotes) => {
        const newTasks = tasks.map((cs)=> {
            if ( cs === task ) {
                cs = {...cs, taskFillProblems: issues, notes: itemNotes};
                storeProgress(taskBatch, cs);
            }
            return cs;
        });
        setTasks(newTasks);
        storeCache(newTasks);
    };

    const handleMoreInfoClose = () => {
        setMoreInfoTask(null);
    }
    const handleSetIssuesAndProblemClose = () => {
        let newIssues = itemIssues;
        if  ( !newIssues.some((v)=>v)) {
            newIssues = null;
        }
        toast(messages.problemsReported);
        handleIssuesChange(problemTask, newIssues, itemNotes);
        setProblemTask(null);
    };

    const handleProblemClose = (issues) => {
        setProblemTask(null);
    };

    const handleTabChange = (event, newValue) => {
        setCurrentTab(newValue);
    };

    const extractAllItemLocations = (tasks) => {
        if ( !tasks ) return [];
        let list = tasks.filter((cs)=>{
            return (
                (!filters.locationFilter || filters.locationFilter === "all" || cs.taskLocation === filters.locationFilter ) &&
                ( !filters.statusFilter || filters.statusFilter === "all" || cs.status === filters.statusFilter ) &&
                ( !filters.problemFilter ||
                    filters.problemFilter === "all" ||
                    (filters.problemFilter === "none" && !cs.taskProblem) ||
                    (filters.problemFilter === "only" && cs.taskProblem) ||
                    (cs.taskProblem && cs.taskProblem.value === filters.problemFilter )
                )
            );
        });
        return [...new Set(list.map((cs)=>cs.itemTempLocation || cs.itemPermLocation))];
    };

    const getSortedAndFilteredTasks = () => {
       return sortedAndFilteredTasks(tasks,order,orderBy,filters)
    };

    const [filters, setFilters] = React.useState({
        problemFilter:"all",
        statusFilter: "all",
        locationFilter: "all",
        itemLocationFilter: "all"
    });

    const handleProblemClick = (event, task) => {
        if ( task.status === "New"){
            toast(messages.problemOnNew);
            return;
        }
        setItemIssues((task.taskFillProblems && task.taskFillProblems.slice())||[]);
        setItemNotes(task.notes || "")
        setProblemTask(task);
        setReportProblemSaveDisable(true);
    };

    const handleFilterSet = (key, value) => {
        const newFilters = {...filters, [key]:value};
        if ( key === "locationFilter" && value !== "all" ) {
            newFilters["itemLocationFilter"] = "all";
        }
        setFilters(newFilters);
        setTaskIndex(0);
    };

    const handleSortByChanged = (sortBy ) => {
        setOrderBy(sortBy);
    };

    let sortedTasks = getSortedAndFilteredTasks();
    // useEffect(()=>{
    //     currentTab === 0 && taskIndex > 0 &&
    //     handleScrollToItem(sortedTasks[taskIndex]);
    // },[currentTab,taskIndex,sortedTasks]);

    if ( tasksSubmitted ) {
        let newCount = 0;
        let fosCount = 0;
        tasks.forEach((cs)=> {
                if (cs.status === "New") newCount++;
                if ("FOS_2x".includes(cs.status)) fosCount++;
            }
        );
        return <Grid
            container alignContent={"center"}>
            <Grid size={2}></Grid>
            <Grid size={8}>
                <div className={classes.bottomPadding}>
                    <h1>{Strings.retrieve.submitted}</h1>
                </div>
            </Grid>
            <Grid size={2}></Grid>
            <Grid size={2}></Grid>
            <Grid size={8}>
                <List>
                    <KeyValue name={"Start Time"} value={startTime.toLocaleString()}  />
                    <KeyValue name={"End Time"} value={endTime.toLocaleString()}  />
                    <KeyValue name={"Status Totals"}  value={<><Typography component={"span"} className={classes.newText} >New: {newCount}</Typography>&nbsp;/&nbsp;
                    <Typography component={"span"} className={classes.fosText} >FOS: {fosCount}</Typography>&nbsp;/&nbsp;
                    <Typography component={"span"} className={classes.nos1Text} >NOS: {tasks.length - newCount - fosCount}</Typography></>} />
                    <ListItem>
                        <Button color={'primary'}
                                startIcon={<PowerSettingsNewIcon/>}
                                variant={"contained"}
                                onClick={apiLogoutUser}>
                            {Strings.text.logout}
                        </Button>
                    </ListItem>
                </List>
            </Grid>
            <Grid size={2}></Grid>
        </Grid>
    }

    const handleTasksSubmitted = () => {
        setShowSubmitConfirmDialog(true);
    };

    const handleTasksSubmittedConfirmed = () => {
        return apiSaveTaskBatch(taskBatch, tasks.map((cs)=>{ return { id: cs.id, status: cs.status, taskFillProblems: cs.taskFillProblems, notes: cs.notes }})).then( (taskBatch)=> {
            setEndTime(parseDate(taskBatch.endTime));
            setStartTime(parseDate(taskBatch.startTime));
            setTasksSubmitted(true);
        }).catch( (e)=> {
            toast(messages.saveBatchError);
            setShowSubmitConfirmDialog(false);
        });
    };

    const handleItemIssue = (problem, value ) => {
        let newItemIssues;
        if ( value ) {
            newItemIssues = [problem];
        } else {
            newItemIssues = [];
        }
        setItemIssues(newItemIssues);
        setReportProblemSaveDisable(false);
    };

    const handleItemNotes = (e) => {
        setItemNotes(e.target.value);
        setReportProblemSaveDisable(false);
    };


    let newCount = 0;
    let fosCount = 0;
    if ( tasks ) {
        tasks.forEach((cs)=> {
                if (cs.status === "New") newCount++;
                if ("FOS_2x".includes(cs.status)) fosCount++;
            }
        )
    }
    return (
      <div className={classes.root}>
              {showSubmitConfirmDialog &&
              <Dialog open={showSubmitConfirmDialog} classes={{ paper: classes.dialogPaper }} onClose={()=>setShowSubmitConfirmDialog(false)} aria-labelledby="form-dialog-title">
                  <DialogTitle id="form-dialog-title">Confirm Submit Batch</DialogTitle>
                  <DialogContent>
                      <List>
                          <ListItem>
                              <Typography component={"span"} className={classes.newText} >New: {newCount}</Typography>&nbsp;/&nbsp;
                              <Typography component={"span"} className={classes.fosText} >FOS: {fosCount}</Typography>&nbsp;/&nbsp;
                              <Typography component={"span"} className={classes.nos1Text} >NOS: {tasks.length - newCount - fosCount}</Typography>
                          </ListItem>
                          <ListItem>
                              <DialogButton name={"Submit Batch"} disabled={submitDisable} setDisabledState={setSubmitDisable} icon={<NotificationImportant />} label={"Submit Batch"} onClick={()=>handleTasksSubmittedConfirmed()} />
                              <DialogButton name={"Cancel"} disabled={submitDisable} setDisabledState={setSubmitDisable} color="secondary" icon={<Cancel />} label={"Cancel"} onClick={()=>setShowSubmitConfirmDialog(false)} />
                          </ListItem>
                      </List>
                  </DialogContent>
              </Dialog>
              }
              {problemTask &&
              <Dialog open={problemTask} classes={{ paper: classes.dialogPaper }} onClose={()=>handleProblemClose()} aria-labelledby="form-dialog-title">
                  <DialogTitle id="form-dialog-title">Report Problem</DialogTitle>
                  <DialogContent>
                      <List>
                          {taskFillProblems.filter((v)=>!v.secondSearch || problemTask.incomingStatus === "NOS").map((v, ix)=> <><FormControlLabel
                              control={<Checkbox name={v.name}
                                                 checked={itemIssues.some((issue)=>issue.id === v.id )}
                                                 onChange={(e)=>handleItemIssue(v, e.target.checked)} />}
                              label={v.name}
                          /><br /></>)}
                          <TextField
                              id='itemNotes'
                              label="Notes"
                              multiline
                              name="itemNotes"
                              value={itemNotes || ''}
                              margin="normal"
                              fullWidth={true}
                              variant="outlined"
                              inputProps={{ maxLength: 250 }}
                              onChange={handleItemNotes}
                          />
                          <ListItem>
                              <DialogButton disabled={reportProblemSaveDisable} name={"Save"} icon={<NotificationImportant />} label={Strings.text.save} onClick={()=>handleSetIssuesAndProblemClose()} />
                              <DialogButton name={"Cancel"} color="secondary" icon={<Cancel />} label={Strings.text.cancel} onClick={()=>handleProblemClose()} />
                          </ListItem>
                      </List>
                  </DialogContent>
              </Dialog>
              }
              {moreInfoTask &&
              <Dialog open={moreInfoTask} classes={{ paper: classes.dialogPaper }} onClose={handleMoreInfoClose} aria-labelledby="form-dialog-title">
                  <DialogTitle id="form-dialog-title">{Strings.retrieve.moreInfo}</DialogTitle>
                  <DialogContent>
                      <List>
                          <KeyValue name={"Route To"} value={moreInfoTask.routeTo}/>
                          <KeyValue name={"Call Number"} value={moreInfoTask.callNumber} />
                          <KeyValue name={"Title"} value={moreInfoTask.title} />
                          <KeyValue name={"Author"} value={moreInfoTask.author} />
                          <KeyValue name={"Publisher"} value={moreInfoTask.publisher} />
                          <KeyValue name={"Physical Description"} value={moreInfoTask.physicalDescription} />
                          <KeyValue name={"Publication Date"} value={moreInfoTask.publicationDate} />
                          <KeyValue name={"Item Barcode"} value={moreInfoTask.itemBarcode}/>
                          <KeyValue name={"Patron Barcode"} value={moreInfoTask.patronBarcode}/>
                          <KeyValue name={"Enumeration"} value={moreInfoTask.enumeration} />
                          <KeyValue name={"Patron Request Date"} value={moreInfoTask.patronRequestDate} />
                          <KeyValue name={"Patron Comment"} value={moreInfoTask.patronComment} />
                          <KeyValue name={"Destination"} value={moreInfoTask.destination} />
                          {moreInfoTask.problem && <KeyValue name={"Problem"} value={moreInfoTask.problem.name} />}
                          <ListItem>
                              <DialogButton name={"Close"} icon={<DoneIcon />} label={"Close"} onClick={handleMoreInfoClose} />
                          </ListItem>
                      </List>
                  </DialogContent>
              </Dialog>
              }
              {reloadCacheConfirmOpen && <ConfirmDialog
                  open={reloadCacheConfirmOpen}
                  handleClose={()=>setReloadCacheConfirmOpen(false)}
                  handleConfirm={()=>{
                      restoreCache(taskBatch, taskBatch.tasks );
                      setReloadCacheConfirmOpen(false);
                  }}
                  classes={classes}
                  confirmTitle={"Reload Device Cache?"}
              >
                  <Typography>
                      {Strings.retrieve.cacheReloadMessage}
                  </Typography>
              </ConfirmDialog>

              }
              <Tabs value={currentTab} onChange={handleTabChange} aria-label="task tabs">
                  <Tab label="List View"/>
                  <Tab label="Single Task View"/>
              </Tabs>
              <TableToolbar numSelected={getSortedAndFilteredTasks().length}
                            onFilterSet={handleFilterSet}
                            onSortByChanged={handleSortByChanged}
                            sortBy={orderBy==="sortTitle"?"title":orderBy}
                            showSortBy={true}
                            showNOSOptions={false}
                            taskLocations={taskLocations}
                            itemLocations={extractAllItemLocations(tasks)}
                            filters={filters}/>
              {(sortedTasks.length === 0 && (tasks === null) &&
                  (<Card><CardContent><Typography>Loading...</Typography></CardContent></Card>))
              || (sortedTasks.length === 0 &&
                  (<Card><CardContent><Typography>{taskLoadError || 'No Tasks based on Your Selection'}</Typography></CardContent></Card>))
              }
              {currentTab === 0 &&
              sortedTasks.map((task, ix)=>
                    <Task key={task.id} task={task} classes={classes} statusMap={statusMap}
                              onUpdateTaskStatus={(task, status)=> handleStatusChange(task, status)}
                              onMoreInformation={()=>{
                                  setTaskIndex(ix);
                                  setCurrentTab(1);
                              }}
                              onProblem={(event)=>{
                                 handleProblemClick(event,task)
                              }}
                              online={online}
                              useButtons={false}
                              taskFillProblems={taskFillProblems}
                              statusOptions={task.incomingStatus === "New" ? firstSearchStatusOptions : secondSearchStatusOptions}
                    />
                  )
              }
              {currentTab === 1 && sortedTasks.length > taskIndex &&
                  <div>
                      <SingleViewTask task={sortedTasks[taskIndex]} classes={classes}
                                onUpdateTaskStatus={(task, status)=> handleStatusChange(task, status)}
                                onMoreInformation={()=>setMoreInfoTask(sortedTasks[taskIndex])}
                                onProblem={(event)=>{
                                  let task = sortedTasks[taskIndex];
                                  handleProblemClick(event,task)
                                }}
                                useButtons={true}
                                online={online}
                                tasks={sortedTasks}
                                taskIndex={taskIndex}
                                setTaskIndex={setTaskIndex}
                                taskFillProblems={taskFillProblems}
                                statusOptions={sortedTasks[taskIndex].incomingStatus === "New" ? firstSearchStatusOptions : secondSearchStatusOptions}
                                statusMap={statusMap}
                                setTab={setCurrentTab}
                      />
                  </div>

              }
              <div className={classes.bottomPadding}></div>
              {tasks && tasks.length > 0 && <WorkProgress classes={classes} tasks={tasks} loggedInUser={loggedInUser}
                                                                  startTime={startTime}
                                                                  onTasksSubmitted={handleTasksSubmitted}
                                                                  newCount={newCount} fosCount={fosCount}
                                                                  toolBar={true}/>}
      </div>
  );
}