package au.gov.ga.hydroid.dto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Created by u26360 on 30/03/2017.
 */
public class CmiNodeDTO {
    private String nid;
    private String changed;

    private int nodeId = 0;
    private Date lastChanged = null;

    public int getNodeId() {
        if (nodeId == 0 && nid != null) {
            nodeId = Integer.parseInt(nid);
        }
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public Date getLastChanged() { //Sun Jan 18 15:52:49 AEST 1970
        if (lastChanged == null && changed != null) {
            lastChanged = new Date(new Long(changed));
           /* try {
                SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
                lastChanged = dateFormatter.parse(changed);
                System.out.println("lastChanged >> " + lastChanged);
            }
            catch (ParseException pe) {
                pe.printStackTrace();
                lastChanged = null;
            }*/
        }
        return lastChanged;
    }

    public void setLastChanged(Date lastChanged) {
        this.lastChanged = lastChanged;
    }

    @Override
    public String toString() {
        return "CmiNodeDTO{" +
                "nid='" + nid + '\'' +
                ", changed='" + changed + '\'' +
                ", nodeId=" + getNodeId() +
                ", lastChanged=" + getLastChanged() +
                '}';
    }
}
