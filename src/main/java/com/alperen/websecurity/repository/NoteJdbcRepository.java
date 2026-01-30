package com.alperen.websecurity.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NoteJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public NoteJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    public List<Long> findNoteIdsByUserId(Long userId) {
        String sql = "SELECT id FROM notes WHERE user_id = ?";
        return jdbcTemplate.query(sql, ps -> ps.setLong(1, userId),
                (rs, rowNum) -> rs.getLong("id"));
    }

    public long countNotesByTitleForUser(Long userId, String title) {
        String sql = "SELECT COUNT(*) FROM notes WHERE user_id = ? AND title = ?";
        // Uses parameter binding (PreparedStatement) to prevent SQL injection.
        Long count = jdbcTemplate.queryForObject(sql, Long.class, userId, title);
        return count == null ? 0L : count;
    }
}
