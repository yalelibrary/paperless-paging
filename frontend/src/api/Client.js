import Cookies from 'js-cookie'

export class FetchError extends Error {
    constructor(message, status, json = null) {
        super(message);
        this.status = status;
        this.response = json;
    }
}

export function baseUrl() {
    if (import.meta.env.PROD) {
        return "";
    } else {
        return "http://localhost:8080";
    }
}

export function apiUrl( uri ) {
    return (baseUrl()) + uri;
}

export function apiFetchJsonUri( uri ) {
    return fetch(apiUrl( uri ), {
        credentials: 'include'
    }).then(response => {
        if (response.ok) {
            return response.json();
        } else {
            throw new FetchError("Error Processing Response From Server", response.status, response.json());
        }
    })
}

export function apiFetchDataUri( uri ) {
    return fetch(apiUrl( uri ), {
        credentials: 'include'
    }).then(response => {
        if (response.ok) {
            return response.text();
        } else {
            throw new FetchError("Error Processing Response From Server", response.status, response.json());
        }
    })
}

export function apiPutJsonUri( uri, data ) {
    return fetch(apiUrl( uri ), {
        credentials: 'include',
        method: 'put',
        headers: {
            'Content-Type': 'application/json',
            'X-XSRF-TOKEN': Cookies.get('XSRF-TOKEN')
        },
        body: JSON.stringify(data)
    }).then(response => {
        if (response.ok) {
            return response.json();
        } else {
            throw new FetchError("Invalid Response From Server", response.status, response.json());
        }
    })
}

export function apiDeleteUri(uri) {
    return fetch(apiUrl( uri ), {
        credentials: 'include',
        method: 'delete',
        headers: {
            'X-XSRF-TOKEN': Cookies.get('XSRF-TOKEN')
        },
    }).then(response => {
        if (response.ok) {
            return response.json();
        } else {
            throw new FetchError("Invalid Response From Server", response.status, response.json());
        }
    })
}

export function apiFetchLoggedInUser() {
    return apiFetchJsonUri("/check-login");
}
export function apiLogoutUser() {
    return window.location.href=apiUrl("/logout");
}

export function apiFetchCircDesks() {
    return apiFetchJsonUri("/api/circDesks");
}

export function apiFetchAssignedTasks(userId) {
    return apiFetchJsonUri("/api/users/"+userId+"/tasks");
}

export function apiFetchAllTasks() {
    return apiFetchJsonUri("/api/tasks");
}

export function apiFetchOpenAndUnassignedTasks(reload) {
    if (reload) {
        return apiFetchJsonUri("/api/tasks/unassigned?reload=true");
    } else {
        return apiFetchJsonUri("/api/tasks/unassigned");
    }
}

export function apiFetchAllUsers() {
    return apiFetchJsonUri("/api/users");
}
export function apiFetchAssignableUsers() {
    return apiFetchJsonUri("/api/users/assignable");
}
export function apiCurrentTaskBatch( user ) {
    return apiFetchJsonUri("/api/users/"+user.id+"/task-batches/current");
}
export function apiAssignUserTasks(user, tasks) {
    return apiPutJsonUri("/api/users/" + user.id + "/task-batches", tasks);
}
export function apiSaveUser(user) {
    return apiPutJsonUri("/api/users", user);
}
export function apiDeleteUser(user) {
    return apiDeleteUri("/api/users/" + user.id);
}
export function apiSaveTaskBatch(taskBatch, updatedTasks) {
    return apiPutJsonUri("/api/task-batches/" + taskBatch.id, updatedTasks);
}
export function apiFetchFillProblems() {
    return apiFetchJsonUri("/api/task-fill-problems");
}

export function apiFetchBatchInfos() {
    return apiFetchJsonUri("/api/task-batch-infos");
}

export function apiFetchTaskLocationList() {
    return apiFetchJsonUri("/api/task-location-list");
}
export function apiFetchTaskStatusList() {
    return apiFetchJsonUri("/api/task-status-list");
}
export function apiStoreTaskProgress(batchId, taskId, taskResponse) {
    return apiPutJsonUri("/api/task-batches/"+batchId+"/task/"+taskId+"/batch-response", taskResponse);
}

export function apiSaveTaskBatchAssigner(taskBatchId) {
    return apiFetchJsonUri('/api/task-batches/'+taskBatchId+'/assigner-close');
}

export function apiFetchAllUsersWithBatches() {
    return apiFetchJsonUri("/api/users/active-batch");
}
export function apiFetchAvatarDataUrl(user) {
    return apiFetchDataUri("/api/users/" + user.id + "/avatar/data-url");
}
export function avatarImagePath(user) {
    return apiUrl("/api/users/" + user.id + "/avatar?" + user.version);
}
export function apiFetchBarcodeReport(barcode) {
    return apiFetchJsonUri("/reports/barcode?barcode=" + barcode);
}


export function apiFetchMmsidReport(mmsid) {
    return apiFetchJsonUri("/reports/mmsid?mmsid=" + mmsid);
}


export function parseDate(s) {
    return new Date(s);
}

