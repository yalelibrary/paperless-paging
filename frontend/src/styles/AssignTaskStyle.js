import { createStyles, makeStyles } from "@mui/styles";
export const useStyles = makeStyles((theme) => ({
    root: {
        width: '100%',
        paddingTop: '10px',
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
        marginBottom: theme.spacing(2),
    },
    table: {
        minWidth: 750,
        minHeight: 50
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
    dropdownuser:{
        padding: '5px'
    },
    dropdownuserwrapper: {
        marginLeft: "30px",
        marginTop: "5px",
        minWidth: 320
    },
    dropdownuserText: {
        padding: "8px"
    },
    dropdownuserAvatar: {
        maxWidth: 40,
        maxHeight: 40
    },

    MuiFormControl: {
        styleOverrides: {
            root: {
                margin: theme.spacing(1),
                minWidth: 250,
            },
        },
    },
    userBatch: {
        marginTop: 20
    },
    topControls: {
        marginLeft: 15
    },
    formControl: {
        margin: theme.spacing(1),
        minWidth: "250px !important",
    },
    ml1: {
        marginLeft: "10px !important"
    },
    warningIcon: {
        color: theme.palette.warning.main,
        height: "20px"
    },
    flexCenter: {
        display: 'flex',
        alignItems: 'center'
    },
    selectButton: {
        margin: "2px 0"
    },
    userButtons: {
        marginTop: "10px"
    },
    actionButton: {
        marginRight: "10px"
    },
    selectedCountText: {
        margin: "13px 5px !important"
    },
    pt2: {
        margin: "0px"
    },
    item: {
        padding: "0 0 0 0",
        margin: "0 0 0 0"
    },
    refreshButton:{
        margin: "5px 8px 0 0",
        float: "right",
    },
    refreshProgress:{
        width: "100px",
        paddingTop: "8px",
        margin: "5px 8px 0 0",
        float: "right",
        fontSize: ".8em"
    },
    dialogTitle:{
        flexGrow: 1,
    },
    noWrap: {
        whiteSpace:"nowrap"
    }
}));
