package com.alex.guima.domain.service;

import com.alex.guima.application.dto.TaskDTO;
import com.alex.guima.domain.entity.Task;
import com.alex.guima.domain.exception.IllegalUpdate;
import com.alex.guima.repository.TaskRepository;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    @DisplayName("listAll - Deve retornar lista de tarefas em um fluxo Uni")
    void listAll_DeveRetornarListaDeTasks() {
        // Arrange
        Task task = new Task("Estudar Quarkus", false, LocalDateTime.now().plusDays(1));
        List<Task> lista = List.of(task);

        PanacheQuery<Task> queryMock = Mockito.mock(PanacheQuery.class);
        when(taskRepository.findAll()).thenReturn(queryMock);
        // Retorna um Uni resolvido com sucesso contendo a lista
        when(queryMock.list()).thenReturn(Uni.createFrom().item(lista));

        // Act
        // Em testes reativos, extraímos o valor do pipeline bloqueando a thread do teste
        List<Task> resultado = taskService.listAll().await().indefinitely();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Estudar Quarkus", resultado.get(0).getTitle());
    }

    @Test
    @DisplayName("findById - Deve falhar com NotFoundException se a tarefa for nula")
    void findById_QuandoNulo_DeveLancarNotFoundException() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Uni.createFrom().nullItem());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> taskService.findById(1L).await().indefinitely());
    }

    @Test
    @DisplayName("createTask - Deve criar e persistir tarefa quando a data for no futuro")
    void createTask_ComDataFutura_DevePersistirComSucesso() {
        // Arrange
        LocalDateTime dataFutura = LocalDateTime.now().plusDays(2);
        TaskDTO dto = new TaskDTO("Nova Tarefa", false, dataFutura);
        Task taskPersistida = new Task(dto.title(), dto.completed(), dto.dueDate());

        when(taskRepository.persist(any(Task.class))).thenReturn(Uni.createFrom().item(taskPersistida));

        // Act
        Task resultado = taskService.createTask(dto).await().indefinitely();

        // Assert
        assertNotNull(resultado);
        assertEquals("Nova Tarefa", resultado.getTitle());
        verify(taskRepository, times(1)).persist(any(Task.class));
    }

    @Test
    @DisplayName("createTask - Deve lançar IllegalArgumentException quando a data for no passado")
    void createTask_ComDataPassada_DeveLancarExcecao() {
        // Arrange
        LocalDateTime dataPassada = LocalDateTime.now().minusDays(1);

        // Act & Assert
        // A validação ocorre de forma síncrona antes do retorno do Uni
        assertThrowsExactly(IllegalArgumentException.class,
                () -> taskService.createTask(new TaskDTO("Tarefa Atrasada", false, dataPassada)));

        verifyNoInteractions(taskRepository);
    }

    @Test
    @DisplayName("updateTask - Deve bloquear atualização de tarefa já concluída (IllegalUpdate)")
    void updateTask_TarefaJaConcluida_DeveLancarIllegalUpdate() {
        // Arrange
        Long taskId = 1L;
        LocalDateTime dataFutura = LocalDateTime.now().plusDays(1);
        TaskDTO dto = new TaskDTO("Atualizar", true, dataFutura);

        Task tarefaExistente = new Task("Original", true, dataFutura); // true = já concluída

        when(taskRepository.findById(taskId)).thenReturn(Uni.createFrom().item(tarefaExistente));

        // Act & Assert
        assertThrows(IllegalUpdate.class, () -> taskService.updateTask(taskId, dto).await().indefinitely());
        verify(taskRepository, never()).persist(any(Task.class));
    }

    @Test
    @DisplayName("deleteTask - Deve retornar Uni<Void> com sucesso quando o repositório deletar (true)")
    void deleteTask_QuandoExistir_DeveDeletarComSucesso() {
        // Arrange
        when(taskRepository.deleteById(1L)).thenReturn(Uni.createFrom().item(true));

        // Act & Assert
        assertDoesNotThrow(() -> taskService.deleteTask(1L).await().indefinitely());
        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteTask - Deve propagar NotFoundException quando o repositório retornar false")
    void deleteTask_QuandoNaoExistir_DeveLancarNotFoundException() {
        // Arrange
        when(taskRepository.deleteById(2L)).thenReturn(Uni.createFrom().item(false));

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> taskService.deleteTask(2L).await().indefinitely());

        assertTrue(exception.getMessage().contains("Task with id 2 not found"));
    }
}