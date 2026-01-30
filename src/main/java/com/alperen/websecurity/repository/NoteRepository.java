package com.alperen.websecurity.repository;

import com.alperen.websecurity.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findAllByUser_Id(Long userId);
    Optional<Note> findByIdAndUser_Id(Long id, Long userId);
    void deleteByIdAndUser_Id(Long id, Long userId);
}
