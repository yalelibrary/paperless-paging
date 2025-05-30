export const getBarcodes = (task) => {
    if ( !task.itemBarcode ) return "none";
    return `${task.itemBarcode}${  task.originalItemBarcode? " (Original: "+task.originalItemBarcode + ")": ""}`
};