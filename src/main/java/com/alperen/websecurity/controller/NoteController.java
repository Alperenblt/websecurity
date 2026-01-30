package com.alperen.websecurity.controller;

import com.alperen.websecurity.dto.NoteCreateRequest;
import com.alperen.websecurity.dto.NoteResponse;
import com.alperen.websecurity.dto.NoteUpdateRequest;
import com.alperen.websecurity.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public List<NoteResponse> list(@AuthenticationPrincipal String username) {
        return noteService.list(username)
                .stream()
                .map(NoteResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public NoteResponse get(@PathVariable Long id,
                            @AuthenticationPrincipal String username) {
        return NoteResponse.from(noteService.get(id, username));
    }

    @PostMapping
    public NoteResponse create(@AuthenticationPrincipal String username,
                               @Valid @RequestBody NoteCreateRequest request) {
        return NoteResponse.from(noteService.create(username, request));
    }

    @PutMapping("/{id}")
    public NoteResponse update(@PathVariable Long id,
                               @AuthenticationPrincipal String username,
                               @Valid @RequestBody NoteUpdateRequest req) {
        return NoteResponse.from(noteService.update(username, id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       @AuthenticationPrincipal String username) {
        noteService.delete(username, id);
    }

    // Demonstration endpoint for SQL Injection prevention via JdbcTemplate prepared statement.
    @GetMapping("/_jdbc/count")
    public long countByTitleJdbc(@RequestParam String title,
                                 @AuthenticationPrincipal String username) {
        return noteService.countMyNotesWithTitleJdbc(username, title);
    }
}