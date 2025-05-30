import React, {Fragment, useEffect, useState} from 'react';
import ReactDOM from 'react-dom'
import './App.css';
import {apiFetchLoggedInUser, apiLogoutUser, baseUrl} from "./api/Client";
import {UserAdmin} from "./UserAdmin";
import {AssignTasks} from "./AssignTasks";
import {RetrieveTasks} from "./RetrieveTasks";
import {appTheme} from './styles/MaterialThemeOverride'
import {
    Container,
    Card,
    Dialog,
    DialogTitle,
    DialogContent,
    List,
    ListItem,
    Menu,
    MenuItem,
    Button,
    CardContent,
    Typography,
    Grid2
} from '@mui/material';
import { AccountCircle, NotificationImportant, Cancel } from '@mui/icons-material';
import {makeStyles} from '@mui/styles'
import {extractUserDisplayName} from "./helpers/UiFunctions";
import {DialogButton} from "./components/ui-components/DialogButton";
import {OurSnackbar} from "./components/ui-components/SnackBar";
import {BrowserRouter as Router, Switch} from "react-router-dom";
import { Route } from "react-router-dom"
import StackGuideButton from "./components/ui-components/StackGuideButton";
import Strings from "./text/Strings"
import { useSnackbar } from 'notistack-v2-maintained';
import Toolbar from "@mui/material/Toolbar";
import AppBar from "@mui/material/AppBar";
import IconButton from "@mui/material/IconButton";
import {MainTabs} from "./components/ui-components/MainTabs.jsx";
import * as PropTypes from "prop-types";
import {TaskReport} from "./TaskReport.jsx";
const useStyles = makeStyles((theme) => ({
    root: {
        flexGrow: 1
    },
    title: {
        flexGrow: 1,
    },
    offset: theme.mixins.toolbar,
    breadcrumbs: {
        marginTop: 20,
        marginBottom: 20
    },
    fullWidth: {
        marginTop: 25,
        marginBottom: 10,
        height: 70,
        width: '100%'
    },
    icon: {
        marginRight: theme.spacing(0.5),
        width: 18,
        height: 18,
        marginBottom: -3,
        borderBottom: 0
    },
    online: {
        color: "inherit"
    },
    notonline: {
        color: "#222222"
    }
}));
let toastCount = 0;

const initialRoutes = {assign: ["/assign"], task: ["/tasks"], admin: ["/admin"], barcodeReport: ["/barcode", "/barcode/:id?", '/mmsid', '/mmsid/:id?']};



function App() {
    const [loggedInUser, setLoggedInUser] = useState(null);
    const [online, setOnline] = useState(true);
    const [version, setVersion] = useState('');
    const [forbidden, setForbidden] = useState(false);
    const classes = useStyles(appTheme);
    const [userMenuOpen, setUserMenuOpen] = useState(false);
    const [anchorEl, setAnchorEl] = useState(null);
    const [messageToUse, setMessageToUse] = React.useState(null);
    const {closeSnackbar} = useSnackbar();
    const [routes, setRoutes] = React.useState(initialRoutes);

    const toastActions = (key) => (
        <Fragment>
            <Button onClick={() => {
                closeSnackbar(key)
            }}>
                close
            </Button>
        </Fragment>
    );
    const setUpToastMessage = (message) => {
        let newMessage = Object.assign({}, message);
        newMessage["key"] = toastCount;
        toastCount++;
        setMessageToUse(newMessage)
    };

    const fetchLoggedInUser = async () => {
        try {
            const loggedInUser = await apiFetchLoggedInUser();
            if (!loggedInUser) {
                throw new Error("Not Logged In");
            } else {
                let newRoutes = initialRoutes;
                if (loggedInUser.admin) {
                    newRoutes.assign.push("/");
                } else if (loggedInUser.assign) {
                    newRoutes.assign.push("/");
                } else {
                    newRoutes.task.push("/");
                }
                setRoutes(newRoutes);
                setLoggedInUser(loggedInUser);
            }
        } catch (e) {
            if (e.status === 403) {
                window.location.replace(baseUrl() + "/api/entry-point")
            }
        }
    };

    useEffect(() => {
        fetch('/version.txt')
            .then(response => response.text())
            .then(data => setVersion(data))
            .catch(error => console.error('Error fetching version:', error));
    });

    useEffect(() => {
        fetchLoggedInUser();
    }, []);

    useEffect(() => {
        const timerInterval = setInterval(() => {
            apiFetchLoggedInUser().then((u) => {
                setForbidden(false);
                setOnline(u && true);
            }).catch((e) => {
                setOnline(false);
                if (e.status === 403) {
                    setForbidden(true);
                }
            })
        }, 10000);
        return () => clearInterval(timerInterval);
    }, []);

    const handleMenuOpen = (event) => {
        setAnchorEl(event.currentTarget);
        setUserMenuOpen(!userMenuOpen);
    };

    if (!loggedInUser) {
        return (
            <main>
                <Card>
                    <CardContent>
                        <Typography component={"h1"} role={"heading"} variant={"h6"}>{Strings.text.loading}</Typography>
                    </CardContent>
                </Card>
            </main>
        )
    }
    return (
        <main>
            <div className={classes.root}>
                <OurSnackbar messageToUse={messageToUse} action={toastActions}>
                    <Container disableGutters={true} className={classes.mainContainer} >
                        <Dialog open={forbidden} classes={{paper: classes.dialogPaper}}
                                onClose={() => setForbidden(false)}
                                aria-labelledby="form-dialog-title">
                            <DialogTitle id="form-dialog-title">{Strings.text.sessionTimeout}</DialogTitle>
                            <DialogContent>
                                <List>
                                    <ListItem>
                                        {Strings.text.sessionTimeoutText}
                                    </ListItem>
                                    <ListItem>
                                        <DialogButton name={"Reload"} icon={<NotificationImportant/>}
                                                      label={"Reload"}
                                                      onClick={() => window.location.reload()}/>
                                        <DialogButton name={"Cancel"} color="secondary" icon={<Cancel/>}
                                                      label={"Cancel"} onClick={() => setForbidden(false)}/>
                                    </ListItem>
                                </List>
                            </DialogContent>
                        </Dialog>
                        <nav>
                            <AppBar position="sticky">
                                <Toolbar>
                                    <Typography variant="h6"
                                                component="h1"
                                                role={"heading"}
                                                className={classes.title}>{Strings.text.appTitle}</Typography>
                                    <IconButton onClick={handleMenuOpen}
                                                edge="end"
                                                aria-label="account of current user"
                                                aria-haspopup="true"
                                                color="inherit"
                                    >
                                        {loggedInUser && extractUserDisplayName(loggedInUser)}&nbsp;&nbsp;
                                        <AccountCircle className={online ? classes.online : classes.notonline}/>
                                    </IconButton>
                                    <Menu open={userMenuOpen} onClose={() => {
                                        setAnchorEl(null);
                                        setUserMenuOpen(false);
                                    }} anchorEl={anchorEl} elevation={0}>
                                        <MenuItem onClick={apiLogoutUser}>{Strings.text.logout}</MenuItem>
                                    </Menu>
                                    {loggedInUser && <StackGuideButton/>}
                                </Toolbar>
                            </AppBar>
                        </nav>
                        <div>
                            <Router>
                                {loggedInUser && (loggedInUser.assign || loggedInUser.admin) &&
                                    <MainTabs loggedInUser={loggedInUser}/>
                                }
                                <Switch>
                                    <Route exact path={routes.admin}>
                                        {(loggedInUser && loggedInUser.admin) &&
                                            <UserAdmin loggedInUser={loggedInUser}
                                                       onUpdateLoggedInUser={fetchLoggedInUser}
                                                       online={online} toast={setUpToastMessage}/>
                                        }
                                    </Route>
                                    <Route exact path={routes.assign}>
                                        {(loggedInUser && loggedInUser.assign) &&
                                            <AssignTasks loggedInUser={loggedInUser} online={online}
                                                             toast={setUpToastMessage}/>
                                        }
                                    </Route>
                                    <Route exact path={routes.task}>
                                        {(loggedInUser && loggedInUser.retrieve) &&
                                            <RetrieveTasks loggedInUser={loggedInUser} online={online}
                                                           toast={setUpToastMessage}/>
                                        }
                                    </Route>
                                    <Route exact path={routes.barcodeReport}>
                                        {(loggedInUser && loggedInUser.assign) &&
                                            <TaskReport loggedInUser={loggedInUser} online={online}
                                                        toast={setUpToastMessage}/>
                                        }
                                    </Route>
                                </Switch>
                            </Router>
                        </div>
                        <Grid2 container>
                            <Grid2 textAlign={'right'} flexGrow={1} paddingRight={1} fontSize={'10px'}>
                                {version}
                            </Grid2>
                        </Grid2>
                    </Container>
                </OurSnackbar>
            </div>
        </main>
    );
}

export default App;