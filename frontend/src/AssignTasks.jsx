import React, {useEffect, useRef, useState} from 'react';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TablePagination from '@mui/material/TablePagination';
import TableRow from '@mui/material/TableRow';
import Typography from '@mui/material/Typography';
import Paper from '@mui/material/Paper';
import Checkbox from '@mui/material/Checkbox';
import IconButton from '@mui/material/IconButton';
import Avatar from '@mui/material/Avatar';
import Cancel from "@mui/icons-material/Cancel";
import Button from '@mui/material/Button';
import SaveIcon from '@mui/icons-material/Save';
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import SelectDownIcon from '@mui/icons-material/SystemUpdateAlt';
import Grid from "@mui/material/Grid2";
import WarningIcon from '@mui/icons-material/Warning';
import TableToolbar from "./components/ui-components/TableToolbar";
import {
  apiAssignUserTasks,
  apiFetchAssignableUsers,
  apiFetchOpenAndUnassignedTasks,
  apiFetchBatchInfos,
  avatarImagePath
} from "./api/Client";
import UserBatch from "./components/assign-task/UserBatch";
import {Tabs, Tab, Grid2, LinearProgress} from "@mui/material";
import {extractUserDisplayName, formatDate, formatDateOnly} from "./helpers/UiFunctions";
import {TabPanel} from "./components/ui-components/TabPanel";
import RefreshIcon from '@mui/icons-material/Refresh';
import {assignSortedAndFiltered} from "./helpers/SortFilterOrder";
import {useStyles} from "./styles/AssignTaskStyle";
import {messages} from "./toast-messages/AssignTasksMessages";
import {TextAndContent} from "./components/ui-components/TextAndContent";
import {EnhancedTableHead} from "./components/ui-components/TableHeadersAssign";
import CircularProgress from "@mui/material/CircularProgress";
import {DialogButton} from "./components/ui-components/DialogButton";
import Autocomplete from '@mui/material/Autocomplete';
import TextField from "@mui/material/TextField";
import {InfoRounded} from "@mui/icons-material";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import Strings from "./text/Strings.jsx";
import DialogContent from "@mui/material/DialogContent";
import List from "@mui/material/List";
import {KeyValue} from "./components/ui-components/KeyValue.jsx";
import ListItem from "@mui/material/ListItem";
import DoneIcon from "@mui/icons-material/Done";
import DialogActions from "@mui/material/DialogActions";
import {Link} from "react-router-dom";

const callNumberTableColumns = [
  { id: 'callNumberNormalized', sortable: true, numeric: false, disablePadding: false, label: 'Call Number' },
  { id: 'itemPermLocation', sortable: true, numeric: false, disablePadding: false, label: 'Item Location' },
  { id: 'status', sortable: true, numeric: false, disablePadding: false, label: 'Task Status' },
  { id: 'patronRequestDate', sortable: true, numeric: false, disablePadding: false, label: 'Request Time' },
  { id: 'info', numeric: false, sortable: false, disablePadding: false, label: 'Info', sx: {padding:0, textAlign: "center"} }
];

const titleTableColumns = [
  { id: 'sortTitle', sortable: true, numeric: false, disablePadding: false, label: 'Title' },
  { id: 'itemPermLocation', sortable: true, numeric: false, disablePadding: false, label: 'Item Location' },
  { id: 'status', sortable: true, numeric: false, disablePadding: false, label: 'Task Status' },
  { id: 'patronRequestDate', sortable: true, numeric: false, disablePadding: false, label: 'Request Time' },
  { id: 'info', numeric: false, sortable: false, disablePadding: false, label: 'Info', sx: {padding:0, textAlign: "center"}}
];

function MenuItemUser(props) {
  props = {...props};
  delete props.key;
  return <MenuItem value={props.value} className={props.classes.dropdownuser} id={props.id} classes={props.classes} {...props}>
    <Grid2 container>
      <Grid2>
        <Avatar alt={`${extractUserDisplayName(props.value)} Avatar`} src={avatarImagePath(props.value)}
                className={props.classes.dropdownuserAvatar}/>
      </Grid2>
      <Grid2>
        <Typography className={props.classes.dropdownuserText}>
          {extractUserDisplayName(props.value)}
        </Typography>
      </Grid2>
    </Grid2>
  </MenuItem>;
}

export function AssignTasks({loggedInUser, toast}) {
  const classes = useStyles();
  const [tab, setTab] = React.useState(0);
  const [selectedUser, setSelectedUser] = React.useState(null);
  const [order, setOrder] = React.useState('asc');
  const [orderBy, setOrderBy] = React.useState('callNumberNormalized');
  const [selected, setSelected] = React.useState([]);
  const [page, setPage] = React.useState(0);
  const dense = true;
  const [rowsPerPage, setRowsPerPage] = React.useState(10);
  const [anchorEl, setAnchorEl] = React.useState(null);
  const [selectNum, setSelectNum] = React.useState(35);
  const [tasks, setTasks] = React.useState([]);
  const [ageOfTaskList, setAgeOfTaskList] = React.useState({});
  const [ageOfTaskListMinutes, setAgeOfTaskListMinutes] = React.useState(null);
  const [users, setUsers] = React.useState([]);
  const [taskBatchInfos, setTaskBatchInfos] = React.useState([]);
  const [taskLocations, setTaskLocations] = React.useState(loggedInUser.circDesks.map(l=>l.name));
  const [sortBy, setSortBy] = React.useState("callNumberNormalized");
  const [tableColumns, setTableColumns] = React.useState(callNumberTableColumns);
  const [moreInfoTask, setMoreInfoTask] = useState(null);
  const [refreshingData, setRefreshingData] = useState(true);
  const timerId = useRef(null);

  useEffect(()=>{
    refreshTaskAndUserData(tab);
  }, [tab]);

  useEffect(() => {
    const f = () => {
      if (ageOfTaskList && (ageOfTaskList.age || ageOfTaskList.age === 0) && ageOfTaskList.at) {
        let fullAge = ageOfTaskList.age + ((new Date().getTime() / 1000) - ageOfTaskList.at);
        let minutes = Math.floor(fullAge/60);
        setAgeOfTaskListMinutes(minutes);
      }
    };
    f();
    timerId.current = setInterval(f, 60000);
    return () => {
      clearInterval(timerId.current);
    }
  }, [ageOfTaskList]);

  const refreshTaskAndUserData = (tab, refresh) => {
    if ( tab === 0 ) {
      setRefreshingData(refresh);
      apiFetchAssignableUsers().then((users) => setUsers(users));
      apiFetchOpenAndUnassignedTasks(refresh).then((tasks) => {
        let age = {age: tasks.age, at: (new Date().getTime() / 1000)};
        setTasks(tasks.tasks);
        setAgeOfTaskList(age);
      }).finally(()=>setRefreshingData(false));
    } else {
      apiFetchBatchInfos().then((taskBatchInfos) => setTaskBatchInfos(taskBatchInfos));
    }
  };

  const refreshBatchesAndUsers = (refresh) => {
    refreshTaskAndUserData(tab, refresh);
  };

  const handleSelectNumDropDownClick = (event) => {
    event.stopPropagation();
    setAnchorEl(event.currentTarget);
  };

  const getSortedAndFiltered = (useLocationFilter) =>{
   return assignSortedAndFiltered(tasks, useLocationFilter, tableColumns, taskLocations, callNumberTableColumns,filters,order, orderBy)
  };

  const getSortedAndFilteredTasks = () => {
    return getSortedAndFiltered( true);
  };

  const extractAllItemLocations = () => {
    let list = getSortedAndFiltered( false);
    let r = new Set(list.map((cs)=>cs.itemTempLocation || cs.itemPermLocation).filter((loc)=>loc));
    return [...r];
  };

  const handleRequestSort = (event, property) => {
    const isAsc = orderBy === property && order === 'asc';
    setOrder(isAsc ? 'desc' : 'asc');
    setOrderBy(property);
  };

  const handleSelectClick = ( num) => {

    if ( !selectedUser ) {
      return;
    }
      const newSelecteds = getSortedAndFilteredTasks().slice(0,num).map((n) => {
        return n.id});
      setSelected(newSelecteds);
  };

  const handleSelectFromClick = (event, fromRowId ) => {
    event.stopPropagation();
    if ( !selectedUser ) {
      toast(messages.selectWithoutUser);
      return;
    }
    if ( !selectedUser) return;

    let sortedTasks = getSortedAndFilteredTasks();
    let newSelecteds = [];
    let cnt = selectNum;
    let fnd = false;
    for ( let i=0;i<sortedTasks.length && cnt>0;i++) {
      if ( sortedTasks[i].id === fromRowId ) {
        fnd = true;
      }
      if ( fnd ) {
        newSelecteds.push( sortedTasks[i].id);
        cnt--;
      }
    }
    setSelected(newSelecteds);
  }

  const handleTaskClick = (event, id) => {
    if ( !selectedUser ) {
      toast(messages.selectWithoutUser);
      return;
    }
    const selectedIndex = selected.indexOf(id);
    let newSelected = [];
    if (selectedIndex === -1) {
      newSelected = newSelected.concat(selected, id);
    } else if (selectedIndex === 0) {
      newSelected = newSelected.concat(selected.slice(1));
    } else if (selectedIndex === selected.length - 1) {
      newSelected = newSelected.concat(selected.slice(0, -1));
    } else if (selectedIndex > 0) {
      newSelected = newSelected.concat(
          selected.slice(0, selectedIndex),
          selected.slice(selectedIndex + 1),
      );
    }
    setSelected(newSelected);
  };

  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const handleTabChange = (event, newTab) => {
    setTab(newTab);
  };

  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const saveUserBatch = (event) => {
    let selectedTasks = tasks.filter((task)=>isSelected(task.id));
    return apiAssignUserTasks(selectedUser, selectedTasks).then(()=> {
          setSelected([]);
          setPage(0);
          setSelectedUser(null);
          setTaskLocations(loggedInUser.circDesks.map(l=>l.name));
          refreshBatchesAndUsers();
          toast(messages.saveAssignSuccess);
        }
    ).catch((error)=>{
      toast(messages.saveAssignFailure);
    });
  };

  const selectNone = (event) => {
    setSelected([])
    //setSelectedUser(null)
  };

  const handleSelectNumClose = (count) => {
    setSelectNum(count);
    setAnchorEl(null);
    //handleSelectClick(count)
  };

  const [filters, setFilters] = React.useState({
    problemFilter:"none",
    statusFilter: "New",
    locationFilter: "all",
    itemLocationFilter: ["all"],
    textFilter: null
  });

  const handleFilterSet = (key, value) => {
    const newFilters = {...filters, [key]:value};
    if ( key === "locationFilter" && value !== "all" ) {
      newFilters["itemLocationFilter"] = ["all"];
    }
    setPage(0);
    setFilters( newFilters);
  };

  const handleSortByChanged = (newSortBy) => {
    setSortBy(newSortBy);
    if (newSortBy === "title") {
      setTableColumns(titleTableColumns);
      setOrderBy("sortTitle");
    } else {
      setTableColumns(callNumberTableColumns);
      setOrderBy("callNumberNormalized")
    }
  }

  const isSelected = (id) => selected.indexOf(id) !== -1;

  const emptyRows = rowsPerPage - Math.min(rowsPerPage, tasks.length - page * rowsPerPage);
  const numSelected=selected.length;

  const filteredUsers = users.filter((u)=>{
    const locationFilter = filters["locationFilter"];
    return locationFilter === "all" || u.circDesks.some((l)=>l.name === locationFilter);
  });

  const taskLocationCounts = {};
  tasks.forEach((task)=>{
    if (task.taskLocation && task.status === "New") {
      let cnt = taskLocationCounts[task.taskLocation] || 0;
      taskLocationCounts[task.taskLocation] = ++cnt;
    }
  });

  const showDetails = (e, row) => {
    e.stopPropagation();
    setMoreInfoTask(row);
  }

  const handleMoreInfoClose = () => {
    setMoreInfoTask(null);
  }

  return (
      <div className={classes.root}>

          {moreInfoTask &&
              <Dialog open={moreInfoTask != null} classes={{ paper: classes.dialogPaper }} onClose={handleMoreInfoClose} aria-labelledby="form-dialog-title" fullWidth={true}>
                <DialogTitle id="form-dialog-title">{Strings.retrieve.moreInfo}</DialogTitle>
                <DialogContent>
                  <List>
                    <KeyValue name={"Call Number"} singleLine={true} value={moreInfoTask.callNumber} />
                    <KeyValue name={"Title"} singleLine={true} value={moreInfoTask.title} />
                    <KeyValue name={"Author"} singleLine={true} value={moreInfoTask.author} />
                    <KeyValue name={"Publisher"} singleLine={true} value={moreInfoTask.publisher} />
                    <KeyValue name={"Published"} singleLine={true} value={moreInfoTask.publicationYear} />
                    <KeyValue name={"Item Barcode"} singleLine={true} value={moreInfoTask.itemBarcode && <Link to={`./barcode/${moreInfoTask.itemBarcode}`}>{moreInfoTask.itemBarcode}</Link> || "No Barcode"}/>
                    {moreInfoTask.physicalDescription && <KeyValue name={"Physical Desc."} singleLine={true} value={moreInfoTask.physicalDescription} />}
                    <KeyValue name={"Request ID"} singleLine={true} value={moreInfoTask.almaRequestId} />
                    <KeyValue name={"MMS ID"} singleLine={true} value={moreInfoTask.almaBibMmsId && <Link to={`./mmsid/${moreInfoTask.almaBibMmsId}`}>{moreInfoTask.almaBibMmsId}</Link> || "No MMS ID"} />
                    <KeyValue name={"Holding ID"} singleLine={true} value={moreInfoTask.almaHoldingId} />
                    {moreInfoTask.enumeration && <KeyValue name={"Enumeration"} singleLine={true} value={moreInfoTask.enumeration} />}
                    <KeyValue name={"Requested"} singleLine={true} value={formatDate(moreInfoTask.patronRequestDate)} />
                    {moreInfoTask.patronComment && <KeyValue name={"Comment"} singleLine={true} value={moreInfoTask.patronComment} />}
                    <KeyValue name={"Destination"} singleLine={true} value={moreInfoTask.destination} />
                    {moreInfoTask.problem && <KeyValue name={"Problem"} singleLine={true} value={moreInfoTask.problem.name} />}
                    {moreInfoTask.itemTempLocation != moreInfoTask.itemPermLocation &&
                        <KeyValue name={"Temp / Perm"} singleLine={true} value={`${moreInfoTask.itemTempLocation} / ${moreInfoTask.itemPermLocation}`} />
                    }
                    {moreInfoTask.lastDischargeDateTime &&
                        <KeyValue name={"Item Modified"} value={formatDate(moreInfoTask.lastDischargeDateTime)}
                                  singleLine={true}/>}
                  </List>
                </DialogContent>
                <DialogActions>
                  <Button onClick={handleMoreInfoClose} autoFocus>
                    Close
                  </Button>
                </DialogActions>
              </Dialog>
          }
          <Paper className={classes.paper}>
            <Grid container>
              <Grid>
                <Tabs
                    value={tab}
                    onChange={handleTabChange}
                    indicatorColor="primary"
                    textColor="primary"
                    variant="scrollable"
                    scrollButtons="auto"
                    aria-label="scrollable auto tabs"
                >
                  <Tab label="Assign to Retrievers" />
                  <Tab label="Review Assignments"  />
                </Tabs>
              </Grid>
              <Grid style={{flexGrow:1}}>
                {refreshingData && <div className={classes.refreshProgress} title={"Refreshing..."}>Refreshing...<br/><LinearProgress/></div> ||
                <Button startIcon={<RefreshIcon/>} onClick={()=>refreshBatchesAndUsers(true)} className={classes.refreshButton} title={ageOfTaskListMinutes && `The task list is ${ageOfTaskListMinutes} minutes old` || (ageOfTaskListMinutes === 0 && 'Tasks are up to date')}>
                  Refresh
                </Button>}
              </Grid>
            </Grid>
            <TabPanel value={tab} index={0}>
              <Grid container spacing={4}>
                <Grid>
                  <div className={classes.dropdownuserwrapper}>
                    <FormControl variant={"outlined"} className={classes.formControl} fullWidth={true}>
                      <Autocomplete
                          id="user-select"
                          variant={"outlined"}
                          value={(selectedUser && users.find((u)=>u.id === selectedUser.id)) || ""}
                          options={filteredUsers}
                          onChange={(event,value)=>{
                            setPage(0);
                            setSelected([]);
                            if(!value){
                              setSelectedUser(null);
                              setTaskLocations(loggedInUser.circDesks.map(l=>l.name));
                              return;
                            }
                            setTaskLocations(value.circDesks
                                .map((circDesk)=>circDesk.name)
                                .filter((location)=>loggedInUser.circDesks.some((l)=>l.name === location)));
                            setSelectedUser(value);
                          }}
                          renderInput={(params) => {
                            return (
                                <TextField
                                    {...params}
                                    variant="standard"
                                    label={<b className="cross-subheader"> Retriever </b>}
                                />
                            )
                          }}
                          renderTags={(value, getTagProps) =>
                              value.map((option, index) => {
                                return <MenuItemUser id={`tag-user-${option.id}`} key={index} value={option} classes={classes}/>
                              })
                          }
                          getOptionLabel={(option) => option? extractUserDisplayName(option): ""}
                          renderOption={
                            (props, option) => {
                                props = {...props};
                                let key = props.key;
                                delete props.key;
                                return <MenuItemUser id={`option-user-${1}`} key={key} value={option} classes={classes} {...props} />
                              }
                            }
                      />
                    </FormControl>
                  </div>
                </Grid>
                <Grid className={classes.userButtons}>
                    <DialogButton
                        id={"assign-button"}
                        variant="contained"
                        color="primary"
                        size="medium"
                        className={classes.button}
                        icon={<SaveIcon />}
                        disabled={selectedUser===null || selected.length === 0 }
                        onClick={saveUserBatch}
                        name={"Assign Tasks to Retriever"}
                    >
                    </DialogButton>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <Button
                        id={"cancel-button"}
                        variant="contained"
                        color="primary"
                        size="medium"
                        className={classes.button}
                        startIcon={<Cancel />}
                        onClick={selectNone}
                    >
                      Cancel
                    </Button>
                </Grid>
              </Grid>
              <TableToolbar numSelected={selected.length}
                            onFilterSet={handleFilterSet}
                            showProblems={true}
                            showNOSOptions={true}
                            showSortBy={true}
                            onSortByChanged={handleSortByChanged}
                            sortBy={sortBy}
                            showTextFilter={true}
                            taskLocations={taskLocations}
                            taskLocationCounts={taskLocationCounts}
                            itemLocations={extractAllItemLocations()}
                            filters={filters}/>
              <TableContainer>
                <Table
                    className={classes.table}
                    aria-labelledby="tableTitle"
                    size={dense ? 'small' : 'medium'}
                    aria-label="enhanced table"
                >
                  <EnhancedTableHead
                      classes={classes}
                      order={order}
                      orderBy={orderBy}
                      onSelectClick={handleSelectClick}
                      onRequestSort={handleRequestSort}
                      anchorEl={anchorEl}
                      setAnchorEl={setAnchorEl}
                      handleSelectNumClose={handleSelectNumClose}
                      handleSelectNumDropDownClick={handleSelectNumDropDownClick}
                      tableColumns={tableColumns}
                      selectNum={selectNum}

                  >Select All</EnhancedTableHead>
                  <TableBody>
                    {getSortedAndFilteredTasks()
                        .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                        .map((row, index) => {
                          const isItemSelected = isSelected(row.id);
                          const labelId = `enhanced-table-checkbox-${index}`;
                          return (
                              <TableRow
                                  hover
                                  onClick={(event) => handleTaskClick(event, row.id)}
                                  role="checkbox"
                                  aria-checked={isItemSelected}
                                  tabIndex={-1}
                                  key={row.id}
                                  selected={isItemSelected}
                              >
                                <TableCell padding="checkbox">
                                  <Grid2 container spacing={2}>
                                    <Grid2 size={6} >
                                      <Checkbox
                                          checked={isItemSelected}
                                          id={`checkbox-${index+page*rowsPerPage}`}
                                          inputProps={{ 'aria-label': `checkbox-${labelId}`}}
                                      />
                                    </Grid2>
                                    <Grid2 size={6} >
                                      <IconButton aria-label={"select next " + selectNum} title={"select next " + selectNum} color="primary" onClick={(e)=>handleSelectFromClick(e,row.id)}>
                                        <SelectDownIcon />
                                      </IconButton>
                                    </Grid2>
                                  </Grid2>
                                </TableCell>
                                <TableCell padding="checkbox">
                                </TableCell>
                                <TableCell align="left">
                                  {tableColumns === callNumberTableColumns && <>
                                  <TextAndContent tooltip={(row.taskProblem && row.taskProblem.problemType==="callnumber" && row.taskProblem.name) || ""}
                                      text={row.callNumber || "No Callnumber"} classes={classes} >
                                    {row.taskProblem && row.taskProblem.problemType==="callnumber" &&
                                    <WarningIcon className={classes.warningIcon} />}
                                    {row.voyagerTaskStatus === 3 && ` (Reassigned: ${row.taskLocation})`}
                                  </TextAndContent>
                                  </>}
                                  {tableColumns === titleTableColumns && <>
                                    <TextAndContent text={row.title} classes={classes}>
                                      {row.title}
                                    </TextAndContent>
                                  </>}
                                </TableCell>
                                <TableCell align="left">
                                  <TextAndContent text={row.itemTempLocation || row.itemPermLocation}
                                                  classes={classes}
                                                  tooltip={row.taskProblem && row.taskProblem.problemType==="location" && (row.taskProblem.name +
                                                      " (Holding: "+(row.holdingLocation || "none")+", Item: "+(row.itemPermLocation || "none")+")")}>
                                  {row.taskProblem && row.taskProblem.problemType==="location" &&
                                  <WarningIcon className={classes.warningIcon} />}
                                  </TextAndContent>
                                </TableCell>
                                <TableCell align="left">
                                  {row.status}
                                </TableCell>
                                <TableCell align="left">
                                  {formatDate(row.patronRequestDate)}
                                </TableCell>
                                <TableCell align="center" sx={{padding:0}}>
                                  <Button title="More Task Info" aria-label={"More Task Info"} onClick={(e)=>showDetails(e, row)}><InfoRounded/></Button>
                                </TableCell>
                              </TableRow>
                          );
                        })}
                    {(!tasks) && (
                        <TableRow>
                          <TableCell colSpan={6} style={{textAlign:"center", height: (dense ? 33 : 53) * emptyRows}}>
                            <h3>LOADING..</h3><CircularProgress />
                          </TableCell>
                        </TableRow>
                    )}
                    {tasks.length === 0 && (
                        <TableRow>
                          <TableCell colSpan={6} style={{textAlign:"center", height: (dense ? 33 : 53) * emptyRows}}>
                            <h3>No tasks available.</h3>
                          </TableCell>
                        </TableRow>
                    )}
                    {tasks && tasks.length > 0 && emptyRows > 0 && (
                        <TableRow style={{ height: (dense ? 33 : 53) * emptyRows }}>

                          <TableCell colSpan={6} >
                          </TableCell>
                        </TableRow>
                    )}
                  </TableBody>
                </Table>

              </TableContainer>

              <Grid container component={"div"}>
                <Grid>
                  <Typography className={classes.selectedCountText} color="inherit" component="div">
                    <Typography  className={classes.ml1}>{numSelected} selected</Typography>
                  </Typography>
                </Grid>
                <Grid>
                  <TablePagination component="div"
                      rowsPerPageOptions={[10, 25, 50]}
                      count={getSortedAndFilteredTasks().length}
                      rowsPerPage={rowsPerPage}
                      page={page}
                      onPageChange={handleChangePage}
                      onRowsPerPageChange={handleChangeRowsPerPage}
                  />
                </Grid>
              </Grid>
            </TabPanel>
            <TabPanel index={1} value={tab}>
              <UserBatch classes={classes} refreshUserWithBatches={refreshBatchesAndUsers} batchInfos={taskBatchInfos} toast={toast} loggedInUser={loggedInUser}/>
            </TabPanel>
          </Paper>
      </div>
  );
}