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
    // raw json properties
    private String nid;
    private String changed;

    // properties as needed by application
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

    public Date getLastChanged() {
        if (lastChanged == null && changed != null) {
            lastChanged = new Date(new Long(changed));
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
