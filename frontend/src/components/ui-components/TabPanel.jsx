import React from "react";

export function TabPanel({children, value, index,}) {
    return (
        <div key={index}
             role="tabpanel"
             hidden={value !== index}
             tabIndex={"-1"}
             autoFocus={true}
        >
            {value === index && (
                children
            )}
        </div>
    );
}