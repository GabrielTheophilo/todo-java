package com.alex.guima.domain.entity;

import com.alex.guima.domain.interfaces.Completable;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class Task extends PanacheEntity implements Completable {

    @Column(nullable = false, length = 36)
    private String title;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean completed;

    private LocalDateTime dueDate;

    public Task() {
    }

    public Task(String title, boolean completed, LocalDateTime dueDate) {
        this.title = title;
        this.completed = completed;
        this.dueDate = dueDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void complete() {
        this.completed = true;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
}
