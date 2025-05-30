package edu.yale.library.paperless.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskListTest {

    @Test
    void taskList() {
        assertEquals(0, new TaskList().size());
    }

}