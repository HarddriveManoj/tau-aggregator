package com.mgk.tau.input.data;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.STIconSetType;

import java.util.ArrayList;
import java.util.List;

public class GroupInfo {

    private String groupName;

    private List<String> colNames = new ArrayList<>();

    public GroupInfo(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<String> getColNames() {
        return colNames;
    }

    public void setColNames(List<String> colNames) {
        this.colNames = colNames;
    }
}
