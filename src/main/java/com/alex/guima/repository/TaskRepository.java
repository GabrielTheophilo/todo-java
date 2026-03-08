package com.alex.guima.repository;

import com.alex.guima.domain.entity.Task;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import org.jspecify.annotations.NonNull;

@ApplicationScoped
public class TaskRepository implements PanacheRepository<Task> {
}
