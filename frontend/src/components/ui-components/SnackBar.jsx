import {useEffect} from 'react';
import {useSnackbar} from 'notistack-v2-maintained';

let lastMessageToUse = null;

export const OurSnackbar = ({messageToUse, action, ...props}) => {
    const {enqueueSnackbar} = useSnackbar();
    useEffect(() => {
        messageToUse && messageToUse !== lastMessageToUse && enqueueSnackbar(messageToUse.message, {
            variant: messageToUse.severity,
            autoHideDuration: 3000,
            preventDuplicate: true,
            action,
        });
        lastMessageToUse = messageToUse;
    }, [messageToUse, action, enqueueSnackbar]);
    return (
        props.children
    );
};
