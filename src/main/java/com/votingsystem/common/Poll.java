package com.votingsystem.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a poll in the voting system
 */
public class Poll implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String title;
    private String description;
    private List<PollOption> options;
    private Date startDate;
    private Date endDate;
    
    public Poll() {
        this.options = new ArrayList<>();
    }
    
    public Poll(int id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.options = new ArrayList<>();
        this.startDate = new Date();
        // Default end date is 1 day from now
        this.endDate = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<PollOption> getOptions() {
        return options;
    }

    public void setOptions(List<PollOption> options) {
        this.options = options;
    }
    
    public void addOption(PollOption option) {
        this.options.add(option);
    }
    
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
    public boolean isActive() {
        Date now = new Date();
        return now.after(startDate) && now.before(endDate);
    }
    
    @Override
    public String toString() {
        return title;
    }
}
