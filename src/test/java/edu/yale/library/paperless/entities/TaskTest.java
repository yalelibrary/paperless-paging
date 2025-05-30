package edu.yale.library.paperless.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void getSortTitle() {
        Task task = new Task();
        task.setTitle("The man on the moon");
        assertEquals("MAN ON THE MOON", task.getSortTitle());
        task.setTitle("moon walker");
        assertEquals("MOON WALKER", task.getSortTitle());
    }
}