package com.project.blog.dao;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BoardDao {
    @Autowired
    JdbcTemplate jt;

    // 글쓰기 분류 가져오기
    public List<Map<String, Object>> codeSelect()
    {
        String sqlStmt = "SELECT CODE_ID, CODE_NAME FROM tb_code_mst";
        return jt.queryForList(sqlStmt);
    }

    // 글쓰기 입력하기(Insert)
    public void insert(String subject, String title, String content, String name)
    {
        String sqlStmt = "INSERT INTO tb_board_mst (SUBJECT, TITLE, CONTENT, name) VALUES (?, ?, ?, ?)";
        jt.update(sqlStmt, subject, title, content, name);
    }

    // 게시판 표시
    public List<Map<String, Object>> boardSelect()
    {
        String sqlStmt = "SELECT    B.SEQ           AS SEQ, "           +
                         "          B.SUBJECT       AS SUBJECT, "       +
                         "          B.TITLE         AS TITLE, "         +
                         "          B.name          AS NAME, "          +
                         "          P.IMGPATH		AS imgPath, "       +
			             "          P.IMGNAME		AS imgName, "       +
                         "          M.GRADE         AS GRADE, "         +
                         "          B.SEARCH_CNT    AS SEARCH_CNT, "    +
                         "          B.REG_DT        AS REG_DT, "        +
                         "          B.LIKE_CNT      AS LIKE_CNT, "      +
                         "          B.dLIKE_CNT     AS DLIKE_CNT, "      +
                         "          COALESCE(C.comment_count, 0) AS COMMENT_COUNT " +
                         "FROM      tb_board_mst B  JOIN TB_MEMBER_MST M ON B.NAME = M.NAME "   +
                         "                          LEFT JOIN tb_profile_mst P ON M.ID = P.ID " +
                         "                          LEFT JOIN (SELECT board_id, count(*) as comment_count FROM tb_comment_mst GROUP BY board_id) C ON B.seq = C.board_id " +
                         "ORDER BY  SEQ DESC";
        return jt.queryForList(sqlStmt);
    }

    // 게시판 글 내용 가져오기
    public List<Map<String, Object>> getBlogDetail(String seq)
    {
        String sqlStmt = "SELECT    B.SEQ           AS SEQ, "           +
                         "          B.SUBJECT       AS SUBJECT, "       +
                         "          B.TITLE         AS TITLE, "         +
                         "          B.CONTENT       AS CONTENT, "       +
                         "          B.name          AS NAME, "          +
                         "          M.GRADE         AS GRADE, "         +
                         "          B.SEARCH_CNT    AS SEARCH_CNT, "    +
                         "          B.REG_DT        AS REG_DT, "        +
                         "          B.LIKE_CNT      AS LIKE_CNT, "      +
                         "          B.dLIKE_CNT     AS DLIKE_CNT "      +
                         "FROM      tb_board_mst B  JOIN TB_MEMBER_MST M ON B.NAME = M.NAME "   +
                         "WHERE     B.SEQ = ?";
        return jt.queryForList(sqlStmt, seq);
    }

    // 조회수 올리기
    public void viewCount(String seq)
    {
        String sqlStmt = "UPDATE tb_board_mst SET SEARCH_CNT = SEARCH_CNT + 1 WHERE SEQ = ?";
        jt.update(sqlStmt, seq);
    }

    // 글 가져오기
    public List<Map<String, Object>> boardSelect(String seq, String name)
    {
        String sqlStmt = "SELECT seq, title, subject, name, content FROM tb_board_mst WHERE seq = ? AND name = ?";
        return jt.queryForList(sqlStmt, seq, name);
    }

    // 글 수정
    public void boardUpdate(String seq, String title, String content, String subject, String name)
    {
        String sqlStmt = "UPDATE tb_board_mst SET title = ?, content = ?, subject = ? WHERE seq = ? AND name = ?";
        jt.update(sqlStmt, title, content, subject, seq, name);
    }

    // 글 삭제
    public void boardDelete(String seq, String name)
    {
        String sqlStmt1 = "DELETE FROM tb_board_mst WHERE seq = ?";
        String sqlStmt2 = "DELETE FROM tb_comment_mst WHERE board_id = ?";
        jt.update(sqlStmt1, seq);
        jt.update(sqlStmt2, seq);
    }

    // 댓글 가져오기
    public List<Map<String, Object>> getComment(String boardId)
    {
        String sqlStmt = "SELECT    C.SEQ           AS SEQ, "                               +
                         "          C.board_id      AS board_id, "                          +
                         "          C.id            AS id, "                                +
                         "          M.name          AS name, "                              +
                         "          P.IMGPATH		AS imgPath, "                           +
			             "          P.IMGNAME		AS imgName, "                           +
                         "          M.grade         AS grade, "                             +
                         "          C.COMMENT       AS COMMENT, "                           +
                         "          C.REG_DT        AS REG_DT, "                            +
                         "          C.LIKE_CNT      AS LIKE_CNT "                           +
                         "FROM      tb_comment_mst C JOIN tb_member_mst M ON C.id = M.id "  +
                         "                          LEFT JOIN tb_profile_mst P ON C.ID = P.ID " +
                         "WHERE     C.BOARD_ID = ? "                                        +
                         "ORDER BY  SEQ";
        return jt.queryForList(sqlStmt, boardId);
    }

    // 댓글 Insert
    public void insertComment(String id, String boardId, String comment)
    {
        String sqlStmt = "INSERT INTO tb_comment_mst (BOARD_ID, id, COMMENT) VALUES(?, ?, ?)";
        jt.update(sqlStmt, boardId, id, comment);
    }

    // 댓글 수정
    public void updateComment(String seq, String comment)
    {
        String sqlStmt = "UPDATE tb_comment_mst SET COMMENT = ? WHERE SEQ = ?";
        jt.update(sqlStmt, comment, seq);
    }

    // 댓글 삭제
    public void deleteComment(String seq)
    {
        String sqlStmt = "DELETE FROM tb_comment_mst WHERE SEQ = ?";
        jt.update(sqlStmt, seq);
    }

    // 글 좋아요
    public void boardLike(String boardId, String id)
    {
        String sqlStmt1 = "UPDATE tb_board_mst SET like_cnt = like_cnt + 1 WHERE seq = ?";
        String sqlStmt2 = "INSERT INTO tb_boardlike_mst(board_id, id, like_tmp) VALUES(?, ?, 1)";

        jt.update(sqlStmt1, boardId);
        jt.update(sqlStmt2, boardId, id);
    }

    // 글 좋아요 취소
    public void boardLikeUndo(String boardId, String id)
    {
        String sqlStmt1 = "UPDATE tb_board_mst SET like_cnt = like_cnt - 1 WHERE seq = ?";
        String sqlStmt2 = "DELETE FROM tb_boardlike_mst WHERE board_id = ? AND id = ?";

        jt.update(sqlStmt1, boardId);
        jt.update(sqlStmt2, boardId, id);
    }

    // 글 좋아요 했는지 확인
    public int boardLikeCheck(String boardId, String id)
    {
        String sqlStmt = "SELECT count(*) FROM tb_boardlike_mst WHERE id = ? AND board_id = ? AND like_tmp = 1";
        try {
            return jt.queryForObject(sqlStmt, Integer.class, id, boardId);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    // 글 싫어요
    public void boardDisLike(String boardId, String id)
    {
        String sqlStmt1 = "UPDATE tb_board_mst SET dlike_cnt = dlike_cnt + 1 WHERE seq = ?";
        String sqlStmt2 = "INSERT INTO tb_boardlike_mst(board_id, id, dlike_tmp) VALUES(?, ?, 1)";

        jt.update(sqlStmt1, boardId);
        jt.update(sqlStmt2, boardId, id);
    }

    // 글 싫어요 취소
    public void boardDisLikeUndo(String boardId, String id)
    {
        String sqlStmt1 = "UPDATE tb_board_mst SET dlike_cnt = dlike_cnt - 1 WHERE seq = ?";
        String sqlStmt2 = "DELETE FROM tb_boardlike_mst WHERE board_id = ? AND id = ?";

        jt.update(sqlStmt1, boardId);
        jt.update(sqlStmt2, boardId, id);
    }

    // 글 싫어요 했는지 확인
    public int boardDisLikeCheck(String boardId, String id)
    {
        String sqlStmt = "SELECT count(*) FROM tb_boardlike_mst WHERE id = ? AND board_id = ? AND dlike_tmp = 1";
        try {
            return jt.queryForObject(sqlStmt, Integer.class, id, boardId);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    // 댓글 수 확인
    public List<Map<String, Object>> commentSelect(String boardId)
    {
        String sqlStmt = "SELECT count(*) FROM tb_comment_mst WHERE board_id = ?";
        return jt.queryForList(sqlStmt, boardId);
    }

    // 글 총 개수 가져오기
    public int boardCnt()
    {
        String sqlStmt = "SELECT count(*) FROM tb_board_mst";
        return jt.queryForObject(sqlStmt, Integer.class);
    }

    // 보드 페이지 select
    public List<Map<String, Object>> boardSelectPage(int startRow, int pageSize) {
        String sqlStmt = "SELECT    B.SEQ           AS SEQ, "           +
                         "          B.SUBJECT       AS SUBJECT, "       +
                         "          B.TITLE         AS TITLE, "         +
                         "          B.NAME          AS NAME, "          +
                         "          P.IMGPATH       AS imgPath, "       +
                         "          P.IMGNAME       AS imgName, "       +
                         "          M.GRADE         AS GRADE, "         +
                         "          B.SEARCH_CNT    AS SEARCH_CNT, "    +
                         "          B.REG_DT        AS REG_DT, "        +
                         "          B.LIKE_CNT      AS LIKE_CNT, "      +
                         "          B.dLIKE_CNT     AS DLIKE_CNT, "     +
                         "          COALESCE(C.comment_count, 0) AS COMMENT_COUNT " +
                         "FROM      tb_board_mst B  JOIN TB_MEMBER_MST M ON B.NAME = M.NAME "   +
                         "                          LEFT JOIN tb_profile_mst P ON M.ID = P.ID " +
                         "                          LEFT JOIN (SELECT board_id, count(*) as comment_count FROM tb_comment_mst GROUP BY board_id) C ON B.seq = C.board_id " +
                         "ORDER BY  SEQ DESC " +
                         "LIMIT ?, ?";
        return jt.queryForList(sqlStmt, startRow, pageSize);
    }
}
