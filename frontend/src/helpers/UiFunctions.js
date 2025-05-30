export const extractUserDisplayName = (user) => {
    return [user.lastName, user.firstName].join(", ") + " (" + user.netId + ")";
};
export const extractCircDeskDisplayName = (user) => {
    return user.circDesks.map((circDesk)=>circDesk.name).join(', ');
};

let dateFormatter =
    new Intl.DateTimeFormat('en-US', {
        dateStyle: 'medium',
        timeStyle: 'medium',
        timeZone: 'US/Eastern',
    });

let dateOnlyFormatter =
    new Intl.DateTimeFormat('en-US', {
        dateStyle: 'medium'
    });

export const formatDate = (date) => {
    let d = new Date(date);
    let s = dateFormatter.format(d)
    if (s.match(/\d+:00:00 [AP]M/)) { // if time is not helpful...
        s = dateOnlyFormatter.format(d);
    }
    return s;
}

export const formatDateOnly = (date) => {
    let d = new Date(date);
    return dateOnlyFormatter.format(d);
}