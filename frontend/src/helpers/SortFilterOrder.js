export function stableSort(array, comparator) {
    const stabilizedThis = array.map((el, index) => [el, index]);
    stabilizedThis.sort((a, b) => {
        const order = comparator(a[0], b[0]);
        if (order !== 0) return order;
        return a[1] - b[1];
    });
    return stabilizedThis.map((el) => el[0]);
}

export function descendingComparator(a, b, orderBy) {
    if (b[orderBy] < a[orderBy]) {
        return -1;
    }
    if (b[orderBy] > a[orderBy]) {
        return 1;
    }
    return 0;
}

export function getComparator(order, orderBy) {
    return order === 'desc'
        ? (a, b) => descendingComparator(a, b, orderBy)
        : (a, b) => -descendingComparator(a, b, orderBy);
}

export const sortedAndFilteredTasks = (tasks, order, orderBy, filters) => {
    if ( !tasks ) return [];
    return stableSort(tasks.filter((cs) => {
        return (
            (!filters.locationFilter || filters.locationFilter === "all" || cs.taskLocation === filters.locationFilter ) &&
            (!filters.itemLocationFilter ||
                filters.itemLocationFilter.includes("all") ||
                filters.itemLocationFilter.includes(cs.itemTempLocation || cs.itemPermLocation) ) &&
            ( !filters.statusFilter || filters.statusFilter === "all" || cs.status === filters.statusFilter ) &&
            ( !filters.problemFilter ||
                filters.problemFilter === "all" ||
                (filters.problemFilter === "none" && !cs.taskProblem) ||
                (filters.problemFilter === "only" && cs.taskProblem) ||
                (cs.taskProblem && cs.taskProblem.value === filters.problemFilter)
            )
        );
    }), getComparator(order, orderBy));
};

export const assignSortedAndFiltered = (tasks, useLocationFilter, tableColumns, taskLocations, callNumberTableColumns, filters, order, orderBy) => {
    return stableSort(tasks.filter((cs)=>{
        let text = (tableColumns===callNumberTableColumns?cs.callNumber:cs.title);
        if(text === null){
            text = '';
        }
        text = text.toLowerCase();
        return (
            (!filters.textFilter || (text.indexOf(filters.textFilter.toLowerCase())===0) || (text.substring(0, filters.textFilter.length)>filters.textFilter.toLowerCase())) &&
            (!filters.locationFilter || cs.taskLocation === filters.locationFilter ||
                (filters.locationFilter === "all" && (!taskLocations || taskLocations.some((l)=>l === cs.taskLocation ))))  &&
            (!useLocationFilter || (!filters.itemLocationFilter ||
                    filters.itemLocationFilter.includes("all") ||
                    filters.itemLocationFilter.includes(cs.itemTempLocation || cs.itemPermLocation))
            ) &&
            ( !filters.statusFilter || filters.statusFilter === "all" || cs.status === filters.statusFilter ) &&
            ( !filters.problemFilter ||
                filters.problemFilter === "all" ||
                (filters.problemFilter === "none" && !cs.taskProblem) ||
                (filters.problemFilter === "only" && cs.taskProblem) ||
                ((cs.taskProblem && cs.taskProblem.value) === filters.problemFilter) )
        );
    }), getComparator(order, orderBy));
};

export const sortedAndFilteredBatchInfos = (batchInfos, order, orderBy, taskLocation, filter = null) => {
    return stableSort(batchInfos.filter(bi => {
        if (taskLocation === "All") {
            return true;
        } else {
            return bi.taskLocations.some(l => l === taskLocation)
        }
    }), getComparator(order,orderBy));
}