import React, {useEffect} from 'react';
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
import AddBoxIcon from '@mui/icons-material/AddBox';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';
import EditIcon from '@mui/icons-material/Edit';
import CheckBoxIcon from '@mui/icons-material/CheckBox';
import UserIcon from '@mui/icons-material/AccountCircle';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';
import Grid from "@mui/material/Grid2";
import {
    apiDeleteUser,
    apiFetchAllUsers,
    apiFetchAvatarDataUrl,
    apiFetchCircDesks,
    apiSaveUser,
    avatarImagePath
} from "./api/Client";
import InputLabel from "@mui/material/InputLabel";
import {extractCircDeskDisplayName, extractUserDisplayName} from "./helpers/UiFunctions";
import {TableHeaders} from "./components/ui-components/TableHeaders";
import {getComparator, stableSort} from "./helpers/SortFilterOrder";
import {EditUserDialog} from "./components/user-admin/EditUserDialog";
import {useStyles} from "./styles/AdminUserRoleStyle";
import {messages} from "./toast-messages/UserAdminMessages";

const headCells = [
    {id: 'lastName', numeric: false, disablePadding: false, label: 'Name', sortable: true},
    {id: 'circDesks', numeric: false, disablePadding: false, label: 'Circ Desks', sortable: false},
    {id: 'enabled', numeric: false, disablePadding: true, label: 'User Status', sortable: true},
    {id: 'assign', numeric: false, disablePadding: true, label: 'Assign', sortable: true},
    {id: 'retrieve', numeric: false, disablePadding: true, label: 'Retrieve', sortable: true},
    {id: 'edit', numeric: false, disablePadding: false, label: 'Edit', sortable: false},
];

export function UserAdmin({onUpdateLoggedInUser, loggedInUser, toast}) {
    const classes = useStyles();
    const [order, setOrder] = React.useState('asc');
    const [orderBy, setOrderBy] = React.useState('avatar');
    const [page, setPage] = React.useState(0);
    const [usersPerPage, setUsersPerPage] = React.useState(5);
    const [dialogOpen, setDialogOpen] = React.useState(false);
    const [activeOnly, setActiveOnly] = React.useState(true);
    const [selectedUserIndex, setSelectedUserIndex] = React.useState(0)
    const [users, setUsers] = React.useState([]);
    const [filter, setFilter] = React.useState("");
    const [circDesks, setCircDesks] = React.useState([]);
    const [selectedUser, setSelectedUser] = React.useState({
        name: "",
        netid: "",
        avatar: "",
    });

    useEffect(() => {
        apiFetchAllUsers().then(
            (users) => setUsers(users)
        )
    }, []);
    useEffect(() => {
        apiFetchCircDesks().then(
            (circDesks) => setCircDesks(circDesks)
        )
    }, []);


    const filteredUsers = () => {
        if (!users) return;

        return users.filter((u) => {
            return (u.enabled || !activeOnly) && ((u.circDesks.some((l)=>{ return loggedInUser.circDesks.some((ll)=> {return l.id === ll.id})})) || (u.circDesks.length === 0 && loggedInUser.admin)) &&
                (!filter || [u.firstName, u.lastName, u.netId].join(" ").toLowerCase().indexOf(filter.toLowerCase()) >= 0)
        });
    };

    const getSortedUsers = () => {
        return stableSort(userList, getComparator(order, orderBy))
    };

    const handleChangeUserInfo = (event) => {
        let newUser = Object.assign({}, selectedUser);
        newUser[event.target.name] = event.target.type === "checkbox" ? event.target.checked : event.target.value;
        if (newUser.netId) newUser.netId = newUser.netId.toLowerCase();
        setSelectedUser(newUser)
    };

    const handleAvatarChanged = (avatars) => {
        console.log(avatars);
        let newUser = Object.assign({}, selectedUser);
        if (avatars.length === 0) {
            newUser.avatar = null;
        } else {
            newUser.avatar = avatars[0].avatar;
        }
        setSelectedUser(newUser);
    };

    const handleSave = () => {
        // make the API call to save the user.
        let newUsers = [...users];
        apiSaveUser(selectedUser).then((newUser) => {
            selectedUser.isNew = false;
            if (selectedUser.id === loggedInUser.id) onUpdateLoggedInUser();
            if (selectedUserIndex === -1) {
                newUsers.unshift(newUser);
            } else {
                newUsers = newUsers.map((user) => {
                    if (user.id === newUser.id) {
                        return newUser;
                    }
                    return user;
                });
            }
            setUsers(newUsers);
            setDialogOpen(false);
            toast(messages.saveUserSuccess);
        }).catch((error) => {
            error.response.then(
                (error_json) => {
                    toast({message: error_json.message, severity: 'error'});
                }
            ).catch((error) => {
                toast(messages.saveUserFailure);
            });
        });
    };

    const handleDelete = () => {
        apiDeleteUser(selectedUser).then((result) => {
            let newUsers = [...users].filter(user => user.id !== selectedUser.id);
            setUsers(newUsers);
            setDialogOpen(false);
            toast(messages.deleteUserSuccess)
        }).catch((error) => {
            error.response.then(
                (error_json) => {
                    toast({message: error_json.message, severity: 'error'});
                }
            ).catch((error) => {
                toast(messages.deleteUserFailure);
            });
        })
    }

    const handleClose = () => {
        setDialogOpen(false)
    };

    const handleRequestSort = (event, property) => {
        const isAsc = orderBy === property && order === 'asc';
        setOrder(isAsc ? 'desc' : 'asc');
        setOrderBy(property);
    };

    const handleEditUser = (event, user, index) => {
        event.stopPropagation();
        setSelectedUserIndex(index);
        if (user.avatar) {
            setSelectedUser({...user});
            setDialogOpen(true);
        } else {
            apiFetchAvatarDataUrl(user).then((dataUrl) => {
                user = {...user, avatar: dataUrl};
                setSelectedUser(user);
                setDialogOpen(true);
            }).catch((e) => {
                toast(e);
                console.error(e);
            })
        }
    };

    const handleNewUser = (event, user, index) => {
        event.stopPropagation();
        setSelectedUser({name: "", isNew: true, circDesks: []});
        setSelectedUserIndex(-1);
        setDialogOpen(true);
    }

    const handleChangePage = (event, newPage) => {
        setPage(newPage);
    };

    const handleChangeUsersPerPage = (event) => {
        setUsersPerPage(parseInt(event.target.value, 10));
        setPage(0);
    };

    const userList = filteredUsers();
    const emptyUsers = usersPerPage - Math.min(usersPerPage, userList.length - page * usersPerPage);

    return (
        <div className={classes.root}>
            <EditUserDialog
                handleAvatarChanged={handleAvatarChanged}
                circDesks={circDesks}
                user={selectedUser}
                open={dialogOpen}
                classes={classes}
                handleChange={handleChangeUserInfo}
                handleSave={handleSave}
                handleDelete={handleDelete}
                handleClose={handleClose}/>
            <Paper className={classes.paper}>
                <Grid container>
                    <Grid>
                        <TextField id={"userSearchAdmin"}
                                   className={classes.textField}
                                   onChange={
                                       (e) => {
                                           setFilter(e.target.value);
                                           setPage(0)
                                       }
                                   }
                                   label={"Search by Name/NetID"}
                                   InputLabelProps={{
                                       "aria-label": "userSearchAdmin"
                                   }}
                        >
                        </TextField>
                    </Grid>
                    <Grid>
                        <InputLabel className={classes.activeOnlyButton}><Checkbox id={"activeOnlyButton"}
                                                                                   checked={activeOnly}
                                                                                   onChange={(e) => setActiveOnly(e.target.checked)}/> Active
                            User Only</InputLabel>
                    </Grid>
                </Grid>
                <TableContainer>
                    <Table
                        className={classes.table}
                        aria-labelledby="tableTitle"
                        size={'medium'}
                        aria-label="enhanced table"
                    >
                        <TableHeaders
                            classes={classes}
                            order={order}
                            orderBy={orderBy}
                            onRequestSort={handleRequestSort}
                            userCount={userList.length}
                            headCells={headCells}
                        />
                        <TableBody>
                            {getSortedUsers()
                                .slice(page * usersPerPage, page * usersPerPage + usersPerPage)
                                .map((user, index) => {
                                    const labelId = `enhanced-table-checkbox-${index}`;
                                    return (
                                        <TableRow
                                            hover
                                            tabIndex={-1}
                                            key={user.id}
                                            className={`user-row-${user.netId}`}
                                        >
                                            <TableCell className={classes.userDisplay}>
                                                <Grid container>
                                                    <Grid>
                                                        <Avatar alt={`${extractUserDisplayName(user)} Avatar`} src={avatarImagePath(user)}
                                                                className={classes.avatar}/>
                                                    </Grid>
                                                    <Grid>
                                                        <Typography
                                                            className={classes.userNameText}>{extractUserDisplayName(user)} </Typography>
                                                    </Grid>
                                                </Grid>
                                            </TableCell>
                                            <TableCell className={classes.circDeskNameText}>
                                                <Typography
                                                    className={classes.circDeskNameText}>{extractCircDeskDisplayName(user)} </Typography>
                                            </TableCell>
                                            <TableCell padding="checkbox">
                                                <UserIcon color={user.enabled ? "primary" : "disabled"}
                                                          titleAccess={user.enabled ? "Active User" : "Inactive User"}/>
                                            </TableCell>
                                            <TableCell padding="checkbox">
                                                {(user.assign && <CheckBoxIcon titleAccess="Can Assign"/>) ||
                                                <CheckBoxOutlineBlankIcon titleAccess="Can Not Assign"/>}
                                            </TableCell>
                                            <TableCell padding="checkbox">
                                                {(user.retrieve && <CheckBoxIcon titleAccess="Can Retrieve"/>) ||
                                                <CheckBoxOutlineBlankIcon titleAccess="Can Not Retrieve"/>}
                                            </TableCell>
                                            <TableCell id={labelId} scope="col" padding="none">
                                                <IconButton id={`edit-${user.netId}`} aria-label={`Edit ${user.netId}`}
                                                            onClick={(event) => handleEditUser(event, user, index + (page * usersPerPage))}>
                                                    <EditIcon titleAccess={`Edit ${user.netId}`}/>
                                                </IconButton>
                                            </TableCell>
                                        </TableRow>
                                    );
                                })}
                            {emptyUsers > 0 && (
                                <TableRow style={{height: 73 * emptyUsers}}>
                                    <TableCell colSpan={6}/>
                                </TableRow>
                            )}
                        </TableBody>
                    </Table>
                </TableContainer>
                <Grid container>
                    <Grid size={6} className={classes.buttonArea}>
                        <Button
                            variant="outlined"
                            color="primary"
                            className={classes.button}
                            startIcon={<AddBoxIcon/>}
                            onClick={handleNewUser}
                        >
                            Add
                        </Button>
                    </Grid>
                    <Grid size={6}>
                        <TablePagination
                            rowsPerPageOptions={[5, 10]}
                            component="div"
                            count={userList.length}
                            rowsPerPage={usersPerPage}
                            page={page}
                            onPageChange={handleChangePage}
                            onRowsPerPageChange={handleChangeUsersPerPage}
                        />
                    </Grid>
                </Grid>
            </Paper>
        </div>
    );
}