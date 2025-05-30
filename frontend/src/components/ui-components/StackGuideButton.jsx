import React, {useState} from 'react';
import StackGuideImage from "../../static/stackguide.png";
import {Button, Dialog, DialogContent, DialogContentText} from '@mui/material';
import {Close, LiveHelpSharp} from '@mui/icons-material';
import {Image} from "react-bootstrap";

const StackGuideButton = () => {

    const [open, setOpen] = useState(false);

    return (
        <div>
            <Button aria-label="StackGuide" onClick={() => setOpen(true)}>
                <LiveHelpSharp style={{color:"white"}}/>
            </Button>
            <Dialog
                open={open}
                onClose={() => setOpen(false)}
                aria-label="Stack Guide Dialog"
            >
                <DialogContent>
                    <Image src={StackGuideImage}/>
                    <DialogContentText>
                    </DialogContentText>
                </DialogContent>
                <Button color={"primary"} onClick={() => setOpen(false)} startIcon={<Close/>}
                        variant={"contained"}>Close</Button>
            </Dialog>
        </div>
    )
}

export default StackGuideButton;