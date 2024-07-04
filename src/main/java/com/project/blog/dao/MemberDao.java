package com.project.blog.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class MemberDao {
    @Autowired
    JdbcTemplate jt;

    // 멤버 리스트 전부 가져오기
    public List<Map<String, Object>> selectMemberList() {
        String sqlStmt =    "SELECT seq, " +
                            "       id, " +
                            "       pw, " +
                            "       name, " +
                            "       birth_date, " +
                            "       email, " +
                            "       phone, " +
                            "       address, " +
                            "       address_detail, " +
                            "       grade, " +
                            "       reg_dt, " +
                            "       del_fg " +
                            "FROM   tb_member_mst";
        return jt.queryForList(sqlStmt);
    }

    // 회원가입 Insert
    public void insertMember(   String id,
                                String pw,
                                String name,
                                String birthDate,
                                String email,
                                String phone,
                                String address,
                                String addressDetail)
    {
        String sqlStmt = "INSERT INTO TB_MEMBER_MST (id, pw, name, birth_date, email, phone, address, address_detail) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jt.update(sqlStmt, id, pw, name, birthDate, email, phone,address, addressDetail);
    }

    // 아이디 중복 체크
    public int dupCheck(String id) {
        String sqlStmt = "SELECT count(*) FROM TB_MEMBER_MST WHERE ID = ?";
        return jt.queryForObject(sqlStmt, Integer.class, id);
    }

    // 멤버 추가 (어드민)
    public void memberRegister(String id,
                               String pw,
                               String name,
                               String birthDate,
                               String email,
                               String phone,
                               String address,
                               String addressDetail,
                               String grade,
                               int delFg)
    {
        String sqlStmt = "INSERT INTO tb_member_mst (id, pw, name, birth_date, email, phone, address, address_detail, grade, del_fg) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jt.update(sqlStmt, id, pw, name, birthDate, email, phone, address, addressDetail, grade, delFg);
    }

    // 멤버 수정 (어드민)
    public void memberUpdate(int seq,
                             String id,
                             String pw,
                             String name,
                             String birthDate,
                             String email,
                             String phone,
                             String address,
                             String addressDetail,
                             String grade,
                             int delFg)
    {
        String sqlStmt = "UPDATE tb_member_mst set id = ?, pw = ?, name = ?, birth_date = ?, email = ?, phone = ?, address = ?, address_detail = ?, grade = ?, del_fg = ? WHERE seq = ?";
        jt.update(sqlStmt, id, pw, name, birthDate, email, phone, address, addressDetail, grade, delFg, seq);
    }

    // 멤버 삭제 (어드민)
    public void memberDelete(int seq)
    {
        String sqlStmt = "DELETE FROM tb_member_mst WHERE seq = ?";
        jt.update(sqlStmt, seq);
    }

    // 로그인
    public List<Map<String, Object>> loginCheck(String id, String pw)
    {
        String sqlStmt = "SELECT id, name, grade FROM tb_member_mst WHERE id = ? AND pw = ? AND del_fg = 0";
        return jt.queryForList(sqlStmt, id, pw);
    }

    // 삭제 유무 확인
    public int getDelFlag(String id)
    {
        String sqlStmt = "SELECT del_fg FROM tb_member_mst WHERE id = ?";
        return jt.queryForObject(sqlStmt, Integer.class, id);
    }

    // 아이디 찾기
    public String findid(String name, String birthDate)
    {
        String sqlStmt = "SELECT id FROM tb_member_mst WHERE name = ? AND birth_date = ?";
        try {
            return jt.queryForObject(sqlStmt, String.class, name, birthDate);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // 비밀번호 찾기
    public String findpw(String id, String email)
    {
        String sqlStmt = "SELECT pw FROM tb_member_mst WHERE id = ? AND email = ?";
        try {
            return jt.queryForObject(sqlStmt, String.class, id, email);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // 비밀번호 확인 (정보 변경을 위해서)
    public int verifyPassword(String memberId, String pw)
    {
        String sqlStmt = "SELECT COUNT(*) FROM tb_member_mst WHERE id = ? AND pw = ?";
        try {
            return jt.queryForObject(sqlStmt, Integer.class, memberId, pw);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    // 멤버 가져오기
    public List<Map<String, Object>> selectMember(String userId) {
        String sqlStmt =    "SELECT seq, " +
                            "       id, " +
                            "       pw, " +
                            "       name, " +
                            "       birth_date, " +
                            "       email, " +
                            "       phone, " +
                            "       address, " +
                            "       address_detail, " +
                            "       grade, " +
                            "       reg_dt " +
                            "FROM   tb_member_mst " +
                            "WHERE  id = ?";
        return jt.queryForList(sqlStmt, userId);
    }

    // 생년월일 가져오기
    public String selectBirth(String userId)
    {
        String sqlStmt = "SELECT birth_date FROM tb_member_mst WHERE id = ?";
        return jt.queryForObject(sqlStmt, String.class, userId);
    }

    // 멤버 정보 변경
    public void memberChangeAction(String pw, String name, String birth_date, String email, String phone, String address, String addressDetail, String id)
    {
        String sqlStmt = "UPDATE tb_member_mst SET pw = ?, name = ?, birth_date = ?, email = ?, phone = ?, address = ?, address_detail = ? WHERE id = ?";
        jt.update(sqlStmt, pw, name, birth_date, email, phone, address, addressDetail, id);
    }

    // 멤버 삭제 (del_fg == 1로 변경)
    public void memberDeleteAction(String id)
    {
        String sqlStmt = "UPDATE tb_member_mst SET del_fg = 1 WHERE id = ?";
        jt.update(sqlStmt, id);
    }

    // 프로필 가져오기
    public List<Map<String, Object>> selectProfile(String id)
    {
        String sqlStmt = "SELECT id, ogName, imgPath, imgName FROM tb_profile_mst WHERE id = ?";
        return jt.queryForList(sqlStmt, id);
    }

    // 프로필 다 가져오기
    public List<Map<String, Object>> selectProfileAll()
    {
        String sqlStmt = "SELECT id, ogName, imgPath, imgName FROM tb_profile_mst";
        return jt.queryForList(sqlStmt);
    }

    // 프로필 입력
    public void profileUpload(String id, String ogName, String imgPath, String imgName)
    {
        String sqlStmt = "INSERT INTO tb_profile_mst(id, ogName, imgPath, imgName) VALUES(?, ?, ?, ?)";
        jt.update(sqlStmt, id, ogName, imgPath, imgName);
    }

    // 프로필 수정
    public void profileChange(String id, String ogName, String imgPath, String imgName)
    {
        String sqlStmt = "UPDATE tb_profile_mst SET ogName = ?, imgPath = ?, imgName = ? WHERE id = ?";
        jt.update(sqlStmt, ogName, imgPath, imgName , id);
    }
}
