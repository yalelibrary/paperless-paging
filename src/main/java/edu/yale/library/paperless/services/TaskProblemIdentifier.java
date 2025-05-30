package edu.yale.library.paperless.services;

import edu.yale.library.paperless.entities.Task;

import java.util.Locale;

import static edu.yale.library.paperless.services.StringHelper.isBlank;

public class TaskProblemIdentifier {

    public static String identifyProblemCode(Task task) {
        //{"cnn","callnumber","Call Number Null"},
        if ( isBlank(task.getCallNumber())) {
            return "cnn";
        }
        // {"cns","callnumber","Call Number Suppressed"},
        if ( task.getCallNumber().toLowerCase().contains("suppressed")) {
            return "cns";
        }
        // {"cno","callnumber","Call Number On Order"},
        if ( task.getCallNumber().equalsIgnoreCase("on order")) {
            return "cno";
        }
        // {"cni","callnumber","Call Number In Process"},
        if ( task.getCallNumber().equalsIgnoreCase("in process")) {
            return "cni";
        }
        // {"cnu","callnumber","Call Number Uncat"},
        if ( task.getCallNumber().toLowerCase().startsWith("uncat")) {
            return "cnu";
        }
        // {"cnp","callnumber","Call Number smlpres"},
        if ( "smlpres".equalsIgnoreCase(task.getItemPermLocation())) {
            return "cnp";
        }
        // {"hli","location","Holding Loc is not Item Perm Loc"},
        if ( !task.getHoldingLocation().equals(task.getItemPermLocation()) ) {
            return "hli";
        }
//        if ( !task.getItemTempLocation().equals(task.getItemPermLocation()) ) {
//            return "itl";
//        }
        // {"hly","location","Holding Loc is sml Yale class call number"}
        if ( "Y".equalsIgnoreCase(task.getCallNumberType()) ) {
            return "hly";
        }
        //{"yta","location","NonCirculating Temp Location"},
        if ( task.getItemTempLocation() != null && task.getItemTempLocation().equalsIgnoreCase("yulinthtea")){
            return "yta";
        }
        return null;
    }

}
