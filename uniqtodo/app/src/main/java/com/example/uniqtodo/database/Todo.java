package com.example.uniqtodo.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "todos")
public  class Todo {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String description;
    private String taskDate;
    private int priority;
    @ColumnInfo(name = "updated_at")
    private Date updatedAt;


    public Todo(int id, String description, String taskDate, int priority, Date updatedAt) {
        this.id = id;
        this.description = description;
        this.taskDate = taskDate;
        this.priority = priority;
        this.updatedAt = updatedAt;
    }

    @Ignore
    public Todo(String description, String  taskDate, int priority, Date updatedAt) {
        this.description = description;
        this.taskDate = taskDate;
        this.priority = priority;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTaskDate() {
        return taskDate;
    }

    public void setTaskDate(String taskDate) {
        this.taskDate = taskDate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

}
