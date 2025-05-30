import React from "react";
import Toolbar from "@mui/material/Toolbar";
import Grid from "@mui/material/Grid2";
import FormControl from "@mui/material/FormControl";
import InputLabel from "@mui/material/InputLabel";
import Select from "@mui/material/Select";
import MenuItem from "@mui/material/MenuItem";
import ListSubheader from "@mui/material/ListSubheader";
import PropTypes from "prop-types";
import {makeStyles} from "@mui/styles";
import Input from "@mui/material/Input";
import {Autocomplete} from "@mui/material";
import TextField from "@mui/material/TextField";
import Chip from "@mui/material/Chip";
import { lighten } from "@mui/material";

const useToolbarStyles = makeStyles((theme) => ({
    root: {
         paddingLeft: theme.spacing(2),
         paddingRight: theme.spacing(1),
    },
    toolbar: { overflow: 'hidden', width:"100%"},
    formControl: {
        margin: theme.spacing(1),
        minWidth: 160,
        maxWidth: "95%",
    },
    selectEmpty: {
        marginTop: theme.spacing(2),
    },
    highlight:
        theme.palette.type === 'light'
            ? {
                color: theme.palette.info.dark,
                backgroundColor: lighten(theme.palette.primary.dark, 0.85),
            }
            : {
                color: theme.palette.text.primary.dark,
                backgroundColor: theme.palette.success.dark,
            },
    title: {
        flex: '1 1 100%',
    },
    dropdownuser: {
        padding: '5px'
    },
}));

export default function TableToolbar(props) {
    const classes = useToolbarStyles();
    const {
        showTextFilter, onFilterSet, filters, showSortBy, onSortByChanged, sortBy, showNOSOptions, showProblems,
        taskLocations, itemLocations, disableLocationsAlls, taskLocationCounts,
    } = props;

    const handleFilterSelectChange = (event) => {
        if (event.target.name && onFilterSet) {
            onFilterSet(event.target.name, event.target.value);
        }
    };

    const handleFilterMultipleSelectChange = (name, newValue) => {
        if (name && onFilterSet) {
            let valueToPass = newValue;
            if (valueToPass && (valueToPass.length > 0)) {
                if (valueToPass[0] === 'all' && valueToPass.length > 1) {
                    valueToPass.shift();
                } else if (valueToPass.indexOf('all') > 0) {
                    valueToPass = ['all'];
                }
            } else valueToPass = ['all'];


            onFilterSet(name, valueToPass);
        }
    };

    const handleSortBySelectChange = (event) => {
        if (event.target.name && onSortByChanged) {
            onSortByChanged(event.target.value);
        }
    };

    let sizeBoost = 0;
    let cntNoShow = 0;
    [showSortBy, itemLocations, showTextFilter, showProblems].forEach((x)=>{
        if (!x) cntNoShow += 1;
    })
    if (cntNoShow > 1) {
        sizeBoost = 1;
    }

    return (
        <Toolbar className={classes.toolbar}>
            <Grid container spacing={3} className={classes.toolbar}>
                <Grid size={{xs:2 + sizeBoost}} >
                    <div className={"filter-item"}>
                        <FormControl className={classes.formControl} fullWidth={true}>
                            <InputLabel id="task-location-select-label" className={"offset-label"}><b className="cross-subheader"> Circ
                                Desk </b></InputLabel>
                            <Select
                                variant={"standard"}
                                labelId="task-location-select-label"
                                id="task-location-select"
                                name={"locationFilter"}
                                onChange={handleFilterSelectChange}
                                value={filters.locationFilter || ""}
                            >{!disableLocationsAlls && <MenuItem value='all'>All</MenuItem>}
                                {taskLocations && taskLocations.filter((location, index) => !taskLocationCounts || taskLocationCounts[location]).map((location, index) => <MenuItem key={index}
                                                                                                           value={location}>{location}{taskLocationCounts && ` (${taskLocationCounts[location] || 'None'})`}</MenuItem>)}
                            </Select>
                        </FormControl>
                    </div>
                </Grid>
                {itemLocations &&
                    <Grid size={{xs:2 + sizeBoost}}>
                        <div className={"filter-item"}>
                            <FormControl className={classes.formControl} fullWidth={true}>
                                <InputLabel id="task-location-select-label" shrink={true} className={"offset-label"}><b className="cross-subheader" > Locations </b></InputLabel>
                                <Autocomplete
                                    id="item-location-select"
                                    options={['all', itemLocations].flat()}
                                    name={"itemLocationFilter"}
                                    onChange={(event, newValue) =>
                                        handleFilterMultipleSelectChange("itemLocationFilter", newValue)
                                    }
                                    value={[filters.itemLocationFilter].flat() || ["all"]}
                                    multiple
                                    disableClearable={true}
                                    getOptionLabel={
                                        (option) => {
                                            return `${option}`;
                                        }
                                    }
                                    renderTags={(value, getTagProps) =>
                                        value.map((option, index) => {
                                            if (option.includes('all')) {
                                                return "All"
                                            } else
                                                return <Chip size={"small"} variant={"outlined"} color={"primary"}
                                                             label={option} {...getTagProps({index})} />
                                        })

                                    }
                                    renderInput={(params) => {
                                        return (

                                            <TextField
                                                {...params}
                                                variant="standard"
                                                placeholder={filters.itemLocationFilter.length === 0 ? "All" : ""}
                                                label={<></>}
                                            />
                                        )
                                    }}
                                />
                            </FormControl>
                        </div>
                    </Grid>}
                <Grid size={{xs:2 + sizeBoost}}>
                    <div className={"filter-item"}>
                        <FormControl className={classes.formControl} fullWidth={true} >
                            <InputLabel id="status-filter-select-label" className={"offset-label"}><b className="cross-subheader"> Task
                                Status </b></InputLabel>
                            <Select variant={"standard"}
                                labelId="status-filter-select-label"
                                id="status-filter-selec"
                                name={"statusFilter"}
                                value={filters.statusFilter || ""}
                                onChange={handleFilterSelectChange}
                            >
                                <MenuItem value='all'>All</MenuItem>
                                <MenuItem value='New'>New</MenuItem>
                                {showNOSOptions && <MenuItem value='NOS'>NOS</MenuItem>}
                                {!showNOSOptions && <MenuItem value='FOS'>FOS</MenuItem>}
                                {!showNOSOptions && <MenuItem value='NOS'>NOS</MenuItem>}
                            </Select>
                        </FormControl>
                    </div>
                </Grid>
                {showSortBy &&
                    <Grid size={{xs:2 + sizeBoost}}>
                        <div className={"filter-item"}>
                            <FormControl className={classes.formControl} fullWidth={true}>
                                <InputLabel id="list-by-select-label" className={"offset-label"}><b className="cross-subheader"> List
                                    By </b></InputLabel>
                                <Select
                                    variant={"standard"}
                                    labelId="list-by-select-label"
                                    id="list-by-select"
                                    onChange={handleSortBySelectChange}
                                    name={"list-by"}
                                    value={sortBy}
                                >
                                    <MenuItem value='callNumberNormalized'>Call Number</MenuItem>
                                    <MenuItem value='title'>Title</MenuItem>
                                </Select>
                            </FormControl>
                        </div>
                    </Grid>}
                {showTextFilter &&
                    <Grid size={{xs:2 + sizeBoost}}>
                        <div className={"filter-item"}>
                            <FormControl className={classes.formControl} fullWidth={true}>
                                <InputLabel id={"text-filter-label"} shrink className={"offset-label"}><b className="cross-subheader">Find in Call
                                    No/Title</b></InputLabel>
                                <Input onChange={handleFilterSelectChange}
                                       inputProps={{'aria-labelledby': "text-filter-label"}} id={"text-filter"}
                                       name={"textFilter"} value={filters.textFilter || ""}/>
                            </FormControl>
                        </div>
                    </Grid>
                }
                {showProblems &&
                    <Grid size={{xs:2 + sizeBoost}}>
                        <div className={"filter-item"}>
                            <FormControl className={classes.formControl} fullWidth={true}>
                                <InputLabel id="filter-item-select-label" className={"offset-label"}><b className="cross-subheader"> Problem
                                    Category </b></InputLabel>
                                <Select
                                    variant={"standard"}
                                    labelId="filter-item-select-label"
                                    id="filter-item-select"
                                    onChange={handleFilterSelectChange}
                                    name={"problemFilter"}
                                    value={filters.problemFilter}
                                >
                                    <ListSubheader> General </ListSubheader>
                                    <MenuItem value='none'>No Problems</MenuItem>
                                    <MenuItem value='only'>Only Problems</MenuItem>
                                    <MenuItem value='all'>All Tasks</MenuItem>
                                    <ListSubheader> Specific Problems </ListSubheader>
                                    <MenuItem value='cns'>Call Number Suppressed</MenuItem>
                                    <MenuItem value='cno'>Call Number On Order</MenuItem>
                                    <MenuItem value='cni'>Call Number In Process</MenuItem>
                                    <MenuItem value='cnu'>Call Number Uncat</MenuItem>
                                    <MenuItem value='cnn'>Call Number Null</MenuItem>
                                    <MenuItem value='cnp'>Call Number smlpres</MenuItem>
                                    <MenuItem value='hli'>Holding Loc is not Item Perm Loc</MenuItem>
                                    <MenuItem value='hly'>Holding Loc is sml Yale class call number</MenuItem>
                                    <MenuItem value='yta'>NonCirculating Temp Location</MenuItem>
                                </Select>
                            </FormControl>
                        </div>
                    </Grid>}
            </Grid>
        </Toolbar>
    );
};

TableToolbar.propTypes = {
    numSelected: PropTypes.number.isRequired,
};
