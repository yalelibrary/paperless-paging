import { makeStyles } from "@mui/styles";


export const useStyles = makeStyles((theme) => ({
    root: {
        width: '100%',
        display: 'flex',
        '& > *': {
            margin: theme.spacing(1),
        },
    },
    textField: {
        marginLeft: theme.spacing(1),
        marginRight: theme.spacing(1),
        width: '25ch',
    },
    paper: {
        width: '100%',
        paddingTop: '10px',
        marginBottom: theme.spacing(2),
    },
    table: {
        minWidth: 750,
    },
    visuallyHidden: {
        border: 0,
        clip: 'rect(0 0 0 0)',
        height: 1,
        margin: -1,
        overflow: 'hidden',
        padding: 0,
        position: 'absolute',
        top: 20,
        width: 1,
    },
    role:{
        width: "15px"
    },
    userDisplay: {
        display: "flex",
        width: "300px"
    },
    userNameText: {
        margin: "10px 0 0 8px !important"
    },
    circDeskNameText: {
        margin: "0 0 0 0",
        maxHeight: 50,
        overflow: "hidden"
    },
    active: {
        color: theme.palette.primary.main
    },
    inactive: {
        color: theme.palette.grey[100]
    },
    buttonArea: {
        padding: "8px"
    },
    toolbar: {
        textAlign: "right",
        paddingRight: "10px"

    },
    activeOnlyButton: {
        marginTop: "5px"
    },
    dialogControl: {
        width: "90%"
    }
}));
