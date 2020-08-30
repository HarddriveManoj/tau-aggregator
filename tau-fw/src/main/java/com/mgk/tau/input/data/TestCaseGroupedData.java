package com.mgk.tau.input.data;

import java.util.LinkedHashMap;
import java.util.List;

public class TestCaseGroupedData {

    private String testCaseId;

    private LinkedHashMap<String, List<LinkedHashMap<String, Object>>> groups = new LinkedHashMap<String, List<LinkedHashMap<String, Object>>>();

    public String getTestCaseId() {return testCaseId;};

    public void setTestCaseId(String testCaseId) {
        this.testCaseId = testCaseId;
    }

    public LinkedHashMap<String, List<LinkedHashMap<String, Object>>> getGroups() {
        return groups;
    }

    public void setGroups(LinkedHashMap<String, List<LinkedHashMap<String, Object>>> groups) {
        this.groups = groups;
    }
}
