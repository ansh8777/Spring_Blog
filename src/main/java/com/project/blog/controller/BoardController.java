package com.project.blog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import com.project.blog.dao.BoardDao;
import com.project.blog.dao.MemberDao;

import jakarta.servlet.http.HttpSession;

@Controller
public class BoardController {
    @Autowired
    BoardDao bd;

    @Autowired
    MemberDao md;

    // 홈페이지
    @GetMapping("/")
    public String index(HttpSession session,
                        @RequestParam(name = "pageNum", defaultValue = "1") int pageNum,
                        Model model)
    {
        int pageSize = 20; // 한 페이지에 보여줄 게시글 수
        int startRow = (pageNum - 1) * pageSize; // 시작 로우

        int boardCnt = bd.boardCnt();

        // List<Map<String, Object>> resultSet = bd.boardSelect();
        // model.addAttribute("resultSet", resultSet);

        List<Map<String, Object>> resultSet = bd.boardSelectPage(startRow, pageSize);
        model.addAttribute("resultSet", resultSet);

        String id = (String) session.getAttribute("id");
        List<Map<String, Object>> profileSelect = md.selectProfile(id);
        model.addAttribute("imageSet", profileSelect);

        model.addAttribute("currentPage", pageNum);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("boardCnt", boardCnt);

        return "index";
    }

    // 글쓰기 페이지
    @GetMapping("board/insert")
    public String blogInsert(Model model,
                             HttpSession session)
    {
        if (session.getAttribute("id") == null)
        {
            return "redirect:/member/login";
        }
        if ((int)session.getAttribute("grade") == 0)
        {
            return "redirect:/board/fail";
        }
        
        List<Map<String, Object>> codeList = bd.codeSelect();
        
        model.addAttribute("codeList", codeList);
        model.addAttribute("userName", session.getAttribute("userName"));
        
        return "board/insert";
    }

    // 글쓰기 db입력
    @PostMapping("board/insert/action")
    public String blogInsertAction(@RequestParam String subject,
                                   @RequestParam String title,
                                   @RequestParam String content,
                                   HttpSession session)
    {
        if (session.getAttribute("id") == null)
        {
            return "redirect:/member/login";
        }
        if ((int)session.getAttribute("grade") == 0)
        {
            return "redirect:/board/fail";
        }

        String name = session.getAttribute("name").toString();
        bd.insert(subject, title, content, name);
        return "redirect:/";
    } 

    // 글쓰기 레벨 0이면 오류페이지
    @GetMapping("board/fail")
    public String failPage(Model model)
    {
        String message = "아이언 등급은 글쓰기가 불가능합니다. ㅋ";
        model.addAttribute("message", message);
        return "board/err";
    }

    // 글 페이지
    @GetMapping("board/detail")
    public String blogDetail(Model model,
                             @RequestParam String seq,
                             HttpSession session)
    {
        bd.viewCount(seq);  // 조회수 1 늘리기
        List<Map<String, Object>> resultSet = bd.getBlogDetail(seq);    // resultSet에 블로그 글 가져오기
        model.addAttribute("resultSet", resultSet);       //  resultSet을 html에 쓸 수 있게 (resultSet[0].??)
        model.addAttribute("currentUserName", session.getAttribute("name"));    // 세션의 name을 html으로 쓸 수 있게
        model.addAttribute("id", session.getAttribute("id"));               // 세션의 userId를 html으로  쓸 수 있게

        String id = (String) session.getAttribute("id");
        List<Map<String, Object>> profileSelect = md.selectProfile(id);
        model.addAttribute("imageSet", profileSelect);

        List<Map<String, Object>> comment = bd.getComment(seq);
        model.addAttribute("comment", comment);
    
        return "board/detail";
    }

    // 글 수정 페이지
    @GetMapping("board/update")
    public String boardUpdate(@RequestParam String seq_1,
                              HttpSession session,
                              Model model) {
        if (session.getAttribute("id") == null)
        {
            return "redirect:/member/login";
        }
        String name = (String) session.getAttribute("name");

        List<Map<String, Object>> codeList = bd.codeSelect();
        model.addAttribute("codeList", codeList);

        List<Map<String, Object>> resultSet = bd.boardSelect(seq_1, name);
        model.addAttribute("resultSet", resultSet);
        return "board/update";
    }

    // 글 수정하기
    @PostMapping("board/update/action")
    public String boardUpdateAction(@RequestParam String seq,
                                    @RequestParam String title,
                                    @RequestParam String subject,
                                    @RequestParam String content,
                                    HttpSession session)
    {
        if (session.getAttribute("id") == null)
        {
            return "redirect:/member/login";
        }
        String name = (String) session.getAttribute("name");
        bd.boardUpdate(seq, title, content, subject, name);

        return "redirect:/board/detail?seq=" + seq;
    }

    // 글 삭제
    @GetMapping("board/delete")
    public String boardDelete(@RequestParam String seq,
                              HttpSession session,
                              Model model)
    {
        if (session.getAttribute("id") == null)
        {
            return "redirect:/member/login";
        }
        model.addAttribute("link", seq);
        model.addAttribute("msg", "정말로 삭제하시겠습니까?");

        return "board/delete";
    }

    // 글 삭제 액션
    @GetMapping("board/delete/action")
    public String boardDeleteAction(@RequestParam String seq,
                                    HttpSession session)
    {
        if (session.getAttribute("id") == null)
        {
            return "redirect:/member/login";
        }
        String name = (String) session.getAttribute("name");
        bd.boardDelete(seq, name);

        return "redirect:/";
    }

    // 댓글 db에 입력
    @PostMapping("board/comment/insert")
    public String commentInsertAction(@RequestParam String id,
                                      @RequestParam String boardId,
                                      @RequestParam String comment,
                                      HttpSession session)
    {
        if (session.getAttribute("id") == null)
        {
            return "redirect:/member/login";
        }
        bd.insertComment(id, boardId, comment);
        String redirectURI = "/board/detail?seq=" + boardId;
        return "redirect:" + redirectURI;
    }

    // 댓글 수정
    @PostMapping("board/comment/update")
    public String updateComment(@RequestParam String seq_a,
                                @RequestParam String comment_a,
                                @RequestParam String boardId_a,
                                HttpSession session)
    {
        if (session.getAttribute("id") == null)
        {
            return "redirect:/member/login";
        }
        bd.updateComment(seq_a, comment_a);
        return "redirect:/board/detail?seq=" + boardId_a;
    }

    // 댓글 삭제
    @PostMapping("board/comment/delete")
    public String updateComment(@RequestParam String seq_b,
                                @RequestParam String boardId_b,
                                HttpSession session)
    {
        if (session.getAttribute("id") == null)
        {
            return "redirect:/member/login";
        }
        bd.deleteComment(seq_b);
        return "redirect:/board/detail?seq=" + boardId_b;
    }

    // 좋아요
    @PostMapping("board/like")
    public String boardLike(@RequestParam String boardId_3,
                            HttpSession session) {
        if (session.getAttribute("id") == null) {
            return "redirect:/member/login";
        }
        String id = (String) session.getAttribute("id");
        
        // 좋아요 상태 확인
        int likecheck = bd.boardLikeCheck(boardId_3, id);
        if (likecheck > 0) {
            // 이미 좋아요를 누른 경우, 좋아요 취소
            bd.boardLikeUndo(boardId_3, id);
        } else {
            // 싫어요를 누른 경우, 리다이렉트
            int dlikecheck = bd.boardDisLikeCheck(boardId_3, id);
            if (dlikecheck > 0) {
                return "redirect:/board/detail?seq=" + boardId_3;
            }
            // 좋아요 등록
            bd.boardLike(boardId_3, id);
        }
        
        return "redirect:/board/detail?seq=" + boardId_3;
    }

    // 싫어요
    @PostMapping("board/dlike")
    public String boardDisLike(@RequestParam String boardId_4,
                            HttpSession session) {
        if (session.getAttribute("id") == null) {
            return "redirect:/member/login";
        }
        String id = (String) session.getAttribute("id");

        // 싫어요 상태 확인
        int dlikecheck = bd.boardDisLikeCheck(boardId_4, id);
        if (dlikecheck > 0) {
            // 이미 싫어요를 누른 경우, 싫어요 취소
            bd.boardDisLikeUndo(boardId_4, id);
        } else {
            // 좋아요를 누른 경우, 리다이렉트
            int likecheck = bd.boardLikeCheck(boardId_4, id);
            if (likecheck > 0) {
                return "redirect:/board/detail?seq=" + boardId_4;
            }
            // 싫어요 등록
            bd.boardDisLike(boardId_4, id);
        }
        
        return "redirect:/board/detail?seq=" + boardId_4;
    }
}
