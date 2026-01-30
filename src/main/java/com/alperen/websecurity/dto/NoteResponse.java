package com.alperen.websecurity.dto;

import com.alperen.websecurity.model.Note;

import java.time.LocalDateTime;

public class NoteResponse {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NoteResponse from(Note note) {
        NoteResponse r = new NoteResponse();
        r.id = note.getId();
        r.title = note.getTitle();
        r.content = note.getContent();
        r.createdAt = note.getCreatedAt();
        r.updatedAt = note.getUpdatedAt();
        return r;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}