import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import {List, ListItem} from "@mui/material";
import Grid from "@mui/material/Grid2";
import TextField from "@mui/material/TextField";
import FormControl from "@mui/material/FormControl";
import InputLabel from "@mui/material/InputLabel";
import Select from "@mui/material/Select";
import MenuItem from "@mui/material/MenuItem";
import Checkbox from "@mui/material/Checkbox";
import ListItemText from "@mui/material/ListItemText";
import FormGroup from "@mui/material/FormGroup";
import FormControlLabel from "@mui/material/FormControlLabel";
import DialogActions from "@mui/material/DialogActions";
import Button from "@mui/material/Button";
import React from "react";
import Typography from "@mui/material/Typography";
import * as PropTypes from "prop-types";
import ReactImageUploading from "react-images-uploading";
import {ImageOutlined, RemoveCircleOutline} from "@mui/icons-material";

function ImageUploader({user, onChange}) {
    console.log("x", user);
    return <ReactImageUploading
        multiple={false}
        value={[user]}
        onChange={onChange}
        maxNumber={1}
        dataURLKey="avatar"
        acceptType={["jpg", "png"]}
    >
        {({ imageList, onImageUpdate, onImageRemove}) => (
        <div onClick={(e)=>e.preventDefault()}>
            {imageList.map((image, index) => { return (
                <div>
                    <Button onClick={() => onImageUpdate(index)} aria-label={"Upload Avatar Image"} variant={"contained"}>Add Avatar Image <ImageOutlined /></Button>
                    <br /><br />
                    {image.avatar && <div><img key={index} src={image.avatar} width={100} alt={"Avatar Image"}/>
                        <Button onClick={(e)=>{e.preventDefault();onChange([])}} aria-label={'Remove Image'} color={"error"}><RemoveCircleOutline/></Button></div>}

                </div>
            )})}
        </div>
        )}
    </ReactImageUploading>
}

ImageUploader.propTypes = {};
export const EditUserDialog = ({
                                   user,
                                   classes,
                                   open,
                                   handleSave,
                                   handleDelete,
                                   handleClose,
                                   handleChange,
                                   circDesks,
                                   handleAvatarChanged
                               }) => {

    return (
        <Dialog role={(open && "presentation") || "none"} open={open} onClose={handleClose}
                aria-labelledby="edit-user-dialog" fullWidth={true}>
            <DialogTitle id="edit-user-dialog">{(user && user.isNew) ? "Add" : "Edit"} User & Role</DialogTitle>
            <DialogContent>
                <List>
                    <ListItem>
                        <Grid container>
                            <Grid size={6}>
                                <TextField
                                    id={`firstName-${user.id}`}
                                    className={classes.dialogControl}
                                    label="First Name"
                                    name="firstName"
                                    value={(user && user.firstName) || ''}
                                    margin="normal"
                                    variant="outlined"
                                    onChange={handleChange}
                                />
                            </Grid>
                            <Grid size={6}>
                                <TextField
                                    id={`lastName-${user.id}`}
                                    className={classes.dialogControl}
                                    label="Last Name"
                                    name="lastName"
                                    value={(user && user.lastName) || ''}
                                    margin="normal"
                                    variant="outlined"
                                    onChange={handleChange}
                                />
                            </Grid>
                        </Grid>
                    </ListItem>
                    <ListItem>
                        <Grid container>
                            <Grid size={6}>
                                <FormControl variant="outlined"
                                             className={classes.dialogControl}
                                             margin="normal">
                                    <InputLabel className="bg-white" htmlFor="circDesks-select">Circ Desk</InputLabel>
                                    <Select
                                        variant="outlined"
                                        id={"circDesks-select"}
                                        placeholder={"CircDesks"}
                                        label={"CircDesks"}
                                        name={"circDesks"}
                                        multiple
                                        value={user.circDesks && (user.circDesks.map((l) => circDesks.filter((circDesk) => circDesk.id === l.id)[0]) || [])}
                                        onChange={handleChange}
                                        renderValue={(selected) => selected.map((circDesk) => circDesk && circDesk.name).join(', ')}
                                    >
                                        {circDesks && circDesks.map((circDesk) => {
                                                return (
                                                    <MenuItem key={circDesk.name} value={circDesk}>
                                                        <Checkbox
                                                            checked={user.circDesks && user.circDesks.some((l) => l.id === circDesk.id)}/>
                                                        <ListItemText id={`${user.netId}-circDesk-${circDesk.id}`}
                                                                      primary={circDesk.name}/>
                                                    </MenuItem>
                                                )
                                            }
                                        )}
                                    </Select>
                                </FormControl>
                            </Grid>
                            <Grid size={6}>
                                <TextField
                                    className={classes.dialogControl}
                                    label="Net-id"
                                    id={`netId-${user.netId}`}
                                    value={user.netId}
                                    margin="normal"
                                    name={"netId"}
                                    variant="outlined"
                                    onChange={handleChange}
                                />
                            </Grid>
                        </Grid>
                    </ListItem>
                    <ListItem>
                        <Grid container sx={{ flexGrow: 1 }}>
                            <Grid size={6}>
                                <List>
                                    <ListItem>
                                        <FormGroup id="outlined-margin-normal"
                                                   label="Role"
                                        >
                                            <FormControlLabel
                                                control={<Checkbox onChange={handleChange} checked={user.assign}
                                                                   name="assign" color="primary"/>}
                                                label="Assign Task"
                                            />
                                            <FormControlLabel
                                                control={
                                                    <Checkbox
                                                        onChange={handleChange}
                                                        name="retrieve"
                                                        color="primary"
                                                        checked={user.retrieve}
                                                    />
                                                }
                                                label="Retrieve"
                                            />
                                            <FormControlLabel
                                                control={
                                                    <Checkbox
                                                        onChange={handleChange}
                                                        name="admin"
                                                        color="primary"
                                                        checked={user.admin}
                                                    />
                                                }
                                                label="Admin"
                                            />
                                            <FormControlLabel
                                                control={
                                                    <Checkbox
                                                        onChange={handleChange}
                                                        name="enabled"
                                                        color="primary"
                                                        checked={user.enabled}
                                                    />
                                                }
                                                label="Active User"
                                            />
                                        </FormGroup>
                                    </ListItem>
                                </List>
                            </Grid>
                            <Grid size={6} style={{textAlign:'right'}}>
                                <label>
                                    <Typography style={{'display': 'none'}}>
                                        Image Uploader
                                    </Typography>
                                    <ImageUploader user={user} onChange={handleAvatarChanged} />
                                </label>
                            </Grid>
                        </Grid>
                    </ListItem>
                </List>
            </DialogContent>
            <DialogActions>
                <Button id={`delete-${user.id}`} onClick={handleDelete} color="primary">
                    Delete
                </Button>
                <div style={{flex: '1 0 0'}}/>
                <Button id={`save-${user.id}`} onClick={handleSave} color="primary">
                    Save
                </Button>
                <Button onClick={handleClose} color="primary">
                    Cancel
                </Button>
            </DialogActions>
        </Dialog>
    )
};