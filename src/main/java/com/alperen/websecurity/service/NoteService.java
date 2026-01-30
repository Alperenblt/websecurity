package com.alperen.websecurity.service;

import com.alperen.websecurity.dto.NoteCreateRequest;
import com.alperen.websecurity.dto.NoteUpdateRequest;
import com.alperen.websecurity.model.Note;
import com.alperen.websecurity.model.User;
import com.alperen.websecurity.repository.NoteJdbcRepository;
import com.alperen.websecurity.repository.NoteRepository;
import com.alperen.websecurity.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final NoteJdbcRepository noteJdbcRepository;



    public NoteService(NoteRepository noteRepository, UserRepository userRepository, NoteJdbcRepository noteJdbcRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.noteJdbcRepository = noteJdbcRepository;
    }

    private User getUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private Note requireOwnedNote(Long noteId, Long userId) {
        // Intentionally return 404 for non-owned notes (anti-enumeration).
        return noteRepository.findByIdAndUser_Id(noteId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
    }

    public List<Note> listMyNotes(String username) {
        User user = getUserOrThrow(username);
        return noteRepository.findAllByUser_Id(user.getId());
    }

    public long countMyNotesWithTitleJdbc(String username, String title) {
        User user = getUserOrThrow(username);
        return noteJdbcRepository.countNotesByTitleForUser(user.getId(), title);
    }

    public Note getMyNote(String username, Long noteId) {
        User user = getUserOrThrow(username);
        return requireOwnedNote(noteId, user.getId());
    }

    @Transactional
    public Note createNote(String username, NoteCreateRequest req) {
        User user = getUserOrThrow(username);

        Note note = new Note();
        note.setUser(user);
        note.setTitle(req.getTitle());
        note.setContent(req.getContent());

        return noteRepository.save(note);
    }

    @Transactional
    public Note updateNote(String username, Long noteId, NoteUpdateRequest req) {
        User user = getUserOrThrow(username);

        Note note = requireOwnedNote(noteId, user.getId());
        note.setTitle(req.getTitle());
        note.setContent(req.getContent());

        return noteRepository.save(note);
    }

    @Transactional
    public void deleteNote(String username, Long noteId) {
        User user = getUserOrThrow(username);
        noteRepository.deleteByIdAndUser_Id(noteId, user.getId());
    }
    // Backwards-compatible method names used by controllers
    public List<Note> list(String username) {
        return listMyNotes(username);
    }

    public Note get(Long noteId, String username) {
        return getMyNote(username, noteId);
    }

    @Transactional
    public Note create(String username, NoteCreateRequest req) {
        return createNote(username, req);
    }

    @Transactional
    public Note update(String username, Long noteId, NoteUpdateRequest req) {
        return updateNote(username, noteId, req);
    }

    @Transactional
    public void delete(String username, Long noteId) {
        deleteNote(username, noteId);
    }
}