import {Tab, Tabs} from "@mui/material";
import React from "react";
import {useHistory, useLocation} from "react-router-dom";
import Strings from "../../text/Strings";

export const MainTabs = ({loggedInUser}) => {
    const history = useHistory();
    const location = useLocation();
    const handleTabChange = (event, newValue) => {
        history.push(`/${newValue}`);
    }
    let currentTab = location.pathname.substring(1).replace(/\/.*/, '');
    if (currentTab === 'barcode' || currentTab === 'mmsid') currentTab = 'barcode';
    return (
        <Tabs value={currentTab} onChange={handleTabChange} aria-label="Main Tabs">
            {(loggedInUser && loggedInUser.admin) &&
                <Tab label={Strings.tabs.users} value={"admin"}/>}
            {(loggedInUser && loggedInUser.assign) &&
                <Tab label={Strings.tabs.assign} value={"assign"}/>}
            {(loggedInUser && loggedInUser.retrieve) &&
                <Tab label={Strings.tabs.retrieve} value={"tasks"}/>}
            {(loggedInUser && loggedInUser.admin) &&
                <Tab label={Strings.tabs.barcodeReport} value={"barcode"}/>}
        </Tabs>
    )
}