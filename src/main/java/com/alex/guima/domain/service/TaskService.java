package com.alex.guima.domain.service;

import com.alex.guima.application.dto.TaskDTO;
import com.alex.guima.domain.entity.Task;
import com.alex.guima.domain.exception.IllegalUpdate;
import com.alex.guima.repository.TaskRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.jspecify.annotations.NonNull;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class TaskService {
    private final TaskRepository taskRepository;

    @Inject
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Uni<List<Task>> listAll() {
        return taskRepository.findAll().list();
    }

    public Uni<Task> findById(Long id) {
        return taskRepository.findById(id).onItem().ifNull().failWith(NotFoundException::new);
    }

    @WithTransaction
    public Uni<Task> createTask(@NonNull TaskDTO task) {
        LocalDateTime dueDate = validateDueDate(task.dueDate());
        return taskRepository.persist(new Task(task.title(), task.completed(), dueDate));
    }

    @WithTransaction
    public Uni<Task> updateTask(Long id, TaskDTO updatedTask) {
        Uni<Task> original = findById(id).onItem().ifNull().failWith(NotFoundException::new);
        return original.onItem().transformToUni(task -> {
            if (task.isCompleted()) {
                throw new IllegalUpdate("Cannot update a completed task");
            }
            task.setDueDate(validateDueDate(updatedTask.dueDate()));
            if (updatedTask.completed()) task.complete();
            task.setTitle(updatedTask.title());
            return taskRepository.persist(task);
        });
    }

    @WithTransaction
    public Uni<Void> deleteTask(Long id) throws NotFoundException {
        return taskRepository.deleteById(id).onItem().invoke((deleted) -> {
            if (!deleted) {
                throw new NotFoundException("Task with id " + id + " not found");
            }
        }).replaceWithVoid();
    }

    private @NonNull LocalDateTime validateDueDate(@NonNull LocalDateTime dueDate) {
        if (dueDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Due date cannot be in the past");
        }

        return dueDate;
    }
}
