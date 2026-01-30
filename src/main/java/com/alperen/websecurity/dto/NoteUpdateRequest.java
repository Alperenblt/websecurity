package com.alperen.websecurity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NoteUpdateRequest {

    @NotBlank(message = "title is required")
    @Size(max = 100, message = "title max 100 chars")
    private String title;

    @NotBlank(message = "content is required")
    @Size(max = 2000, message = "content max 2000 chars")
    private String content;

    public NoteUpdateRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}