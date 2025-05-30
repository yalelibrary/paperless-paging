import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import TableCell from "@mui/material/TableCell";
import Grid from "@mui/material/Grid2";
import Typography from "@mui/material/Typography";
import Select from "@mui/material/Select";
import MenuItem from "@mui/material/MenuItem";
import TableSortLabel from "@mui/material/TableSortLabel";
import React from "react";

export function EnhancedTableHead({
                                      classes,
                                      order,
                                      orderBy,
                                      onRequestSort,
                                      handleSelectNumClose,
                                      tableColumns,
                                      selectNum
                                  }) {
    const createSortHandler = (property) => (event) => {
        onRequestSort(event, property);
    };

    return (
        <TableHead>
            <TableRow>
                <TableCell padding="checkbox">
                    <Grid container>
                        <Grid size={6}><Typography className={classes.selectButton}>#</Typography></Grid>
                        <Grid size={6}><Select variant={"standard"}
                                                  name={"selectCount"}
                                                  labelId="select-count-label"
                                                  onChange={(e) => handleSelectNumClose(e.target.value)}
                                                  value={selectNum}
                        >
                            <MenuItem value='10'>10</MenuItem>
                            <MenuItem value='20'>20</MenuItem>
                            <MenuItem value='35'>35</MenuItem>
                        </Select></Grid>
                    </Grid>
                </TableCell>
                <TableCell>
                </TableCell>
                {tableColumns.map((headCell) => (
                    <TableCell
                        key={headCell.id}
                        align={headCell.numeric ? 'right' : 'left'}
                        padding={headCell.disablePadding ? 'none' : 'normal'}
                        sortDirection={orderBy === headCell.id ? order : false}
                        sx={headCell.sx}
                    >
                        {headCell.sortable &&
                        <TableSortLabel
                            active={orderBy === headCell.id}
                            direction={orderBy === headCell.id ? order : 'asc'}
                            onClick={createSortHandler(headCell.id)}
                        >
                            {headCell.label}
                            {orderBy === headCell.id ? (
                                <span className={classes.visuallyHidden}>
                  {order === 'desc' ? 'sorted descending' : 'sorted ascending'}
                </span>
                            ) : null}
                        </TableSortLabel>
                        ||
                        <>{headCell.label}</>}
                    </TableCell>
                ))}
            </TableRow>
        </TableHead>
    );
}