import React from "react";

export function TextAndContent(props) {
    return <div className={props.classes.flexCenter} title={props.tooltip}>
        {props.text}
        {props.children}
    </div>
}