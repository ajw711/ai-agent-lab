package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        todoRepository.deleteAll();
    }

    @Test
    void createTodo_success() throws Exception {
        TodoRequest request = new TodoRequest();
        request.setTitle("Buy milk");

        mockMvc.perform(post("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("Buy milk")));
    }

    // --- 보완된 Validation 테스트 --- //

    @Test
    void createTodo_failsWhenTitleIsBlank() throws Exception {
        TodoRequest request = new TodoRequest();
        request.setTitle(""); // 빈 문자열

        mockMvc.perform(post("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTodo_failsWhenTitleIsNull() throws Exception {
        TodoRequest request = new TodoRequest();
        request.setTitle(null); // NULL 값 (누락)

        mockMvc.perform(post("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTodo_failsWhenTitleIsWhitespace() throws Exception {
        TodoRequest request = new TodoRequest();
        request.setTitle("   "); // 공백 문자열

        mockMvc.perform(post("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // --- 경계값(Boundary) 테스트 --- //

    @Test
    void createTodo_successWhenTitleIsExactly100Characters() throws Exception {
        TodoRequest request = new TodoRequest();
        request.setTitle("A".repeat(100)); // 정확히 100자 (성공해야 함)

        mockMvc.perform(post("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("A".repeat(100))));
    }

    @Test
    void createTodo_failsWhenTitleExceeds100Characters() throws Exception {
        TodoRequest request = new TodoRequest();
        request.setTitle("A".repeat(101)); // 101자 (실패해야 함)

        mockMvc.perform(post("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // --- 일반 CRUD 기능 검증 --- //

    @Test
    void getTodos_returnsList() throws Exception {
        todoRepository.save(new Todo("Task 1"));
        todoRepository.save(new Todo("Task 2"));

        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Task 1")))
                .andExpect(jsonPath("$[1].title", is("Task 2")));
    }

    @Test
    void getTodo_returnsTodoWhenExists() throws Exception {
        Todo savedTodo = todoRepository.save(new Todo("My task"));

        mockMvc.perform(get("/todos/{id}", savedTodo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("My task")));
    }

    @Test
    void getTodo_returns404WhenNotFound() throws Exception {
        mockMvc.perform(get("/todos/999"))
                .andExpect(status().isNotFound());
    }

    // --- 보완된 DB 교차 검증 및 PUT Validation 테스트 --- //

    @Test
    void updateTodo_updatesAndReturnsTodoWhenExists() throws Exception {
        Todo savedTodo = todoRepository.save(new Todo("Old title"));
        TodoRequest request = new TodoRequest();
        request.setTitle("New title");

        // API 호출을 통한 업데이트 검증
        mockMvc.perform(put("/todos/{id}", savedTodo.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("New title")));

        // DB 교차 검증: 실제로 Repository에도 반영(Flush)되었는지 확인
        Todo updatedTodoInDb = todoRepository.findById(savedTodo.getId()).orElseThrow();
        assertThat(updatedTodoInDb.getTitle()).isEqualTo("New title");
    }

    @Test
    void updateTodo_failsValidationWhenTitleIsBlank() throws Exception {
        Todo savedTodo = todoRepository.save(new Todo("Old title"));
        TodoRequest request = new TodoRequest();
        request.setTitle(""); // 수정 시에도 유효성 검사가 동작해야 함

        mockMvc.perform(put("/todos/{id}", savedTodo.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTodo_returns404WhenNotFound() throws Exception {
        TodoRequest request = new TodoRequest();
        request.setTitle("New title");

        mockMvc.perform(put("/todos/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTodo_deletesWhenExists() throws Exception {
        Todo savedTodo = todoRepository.save(new Todo("To be deleted"));

        mockMvc.perform(delete("/todos/{id}", savedTodo.getId()))
                .andExpect(status().isNoContent());

        // DB 교차 검증
        assertThat(todoRepository.existsById(savedTodo.getId())).isFalse();
    }

    @Test
    void deleteTodo_returns404WhenNotFound() throws Exception {
        mockMvc.perform(delete("/todos/999"))
                .andExpect(status().isNotFound());
    }
}
