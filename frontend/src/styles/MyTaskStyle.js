import { createStyles, makeStyles } from "@mui/styles";


export const useStyles = makeStyles((theme) =>
    createStyles({
        root: {
            width: '100%'
        },
        formControl: {
            margin: theme.spacing(1),
        },
        selectEmpty: {
            marginTop: theme.spacing(2),
        },
        tableCell: {

        },
        heading: {
            fontSize: theme.typography.pxToRem(15),
            fontWeight: theme.typography.fontWeightRegular,
        },
        card: {
            border: "solid #4caf50",
            borderWidth: "0 0 0 20px ",
            borderRadius: "20px",
            backgroundColor: theme.palette.grey[200]

        },
        chip:{
            margin: "0 5px 0 5px"
        },
        nos: {
            borderColor: "#e57373",
        },
        nos_2x: {
            borderColor: "#e57373",
        },
        new: {
            borderColor: "#2196f3",
        },
        floatRight: {
            float: "right"
        },
        dialogPaper: {
        },
        submitButton: {
            marginTop: '10px'
        },
        justifyEnd: {
            justifyContent: "flex-end"
        },
        workProgress: {
            background: theme.palette.grey[100]
        },
        circularProgress: {
            marginTop: "10px"
        },
        bottomAppBar: {
            top: 'auto !important',
            bottom: "0",
        },
        bottomPadding: {
            height: "100px"
        },
        bigFont: {
            fontSize: "100px"
        },
        radioGrid: {
            flexGrow: 1,
        },
        radioGroup:{
        },
        statusButton:{
            marginRight: "7.5px"
        },
        warningIcon: {
            color: "red",
            height: "18px",
            position: "relative",
            top: "3px"
        }
    }),
);
