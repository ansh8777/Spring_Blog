package com.project.blog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.project.blog.dao.MemberDao;
import com.project.blog.dao.BoardDao;

import jakarta.servlet.http.HttpSession;

@Controller
public class MemberController {
    @Autowired
    MemberDao memberDao;
    
    @Autowired
    BoardDao bd;

    // 어드민: 멤버관리
    @GetMapping("admin/list")
    public String memberList(Model model,
                             HttpSession session) {
        if (session.getAttribute("grade") == null || (int) session.getAttribute("grade") != 99) {
            // 권한이 없는 경우 로그인 페이지로 리다이렉트
            return "redirect:/member/login";
        }
        List<Map<String, Object>> resultSet = memberDao.selectMemberList();
        model.addAttribute("resultSet", resultSet);
        return "member/list";
    }

    // 어드민: 멤버관리 - 추가
    @PostMapping("admin/memberRegister")
    public String memberRegister( @RequestParam String id_,
                                  @RequestParam String pw_,
                                  @RequestParam String name_,
                                  @RequestParam String birthDate_,
                                  @RequestParam String email_,
                                  @RequestParam String phone_,
                                  @RequestParam String address_,
                                  @RequestParam String addressDetail_,
                                  @RequestParam String grade_,
                                  @RequestParam int delFg_,
                                  HttpSession session)
    {
        if (session.getAttribute("grade") == null || (int) session.getAttribute("grade") != 99) {
            // 권한이 없는 경우 로그인 페이지로 리다이렉트
            return "redirect:/member/login";
        }
        memberDao.memberRegister(id_, pw_, name_, birthDate_, email_, phone_, address_, addressDetail_, grade_, delFg_);
        return "redirect:/admin/list";
    }

    // 어드민: 멤버관리 - 수정
    @PostMapping("admin/memberUpdate")
    public String memberUpdate( @RequestParam int seq,
                                @RequestParam String id,
                                @RequestParam String pw,
                                @RequestParam String name,
                                @RequestParam String birthDate,
                                @RequestParam String email,
                                @RequestParam String phone,
                                @RequestParam String address,
                                @RequestParam String addressDetail,
                                @RequestParam String grade,
                                @RequestParam int delFg,
                                HttpSession session)
    {
        if (session.getAttribute("grade") == null || (int) session.getAttribute("grade") != 99) {
            // 권한이 없는 경우 로그인 페이지로 리다이렉트
            return "redirect:/member/login";
        }
        memberDao.memberUpdate(seq, id, pw, name, birthDate, email, phone, address, addressDetail, grade, delFg);
        return "redirect:/admin/list";
    }

    // 어드민: 멤버관리 - 완전 삭제
    @PostMapping("admin/memberDelete")
    public String memberDelete(@RequestParam int seq,
                               HttpSession session)
    {
        if (session.getAttribute("grade") == null || (int) session.getAttribute("grade") != 99) {
            // 권한이 없는 경우 로그인 페이지로 리다이렉트
            return "redirect:/member/login";
        }
        memberDao.memberDelete(seq);
        return "redirect:/admin/list";
    }

    // 회원가입
    @GetMapping("member/register")
    public String memberRegisterForm() {
        return "member/register";
    }

    // 회원가입 sql에 넣기
    @PostMapping("member/register/action")
    public String memberRegisterAction(@RequestParam String id,
                                       @RequestParam String pw,
                                       @RequestParam String name,
                                       @RequestParam String year,
                                       @RequestParam String month,
                                       @RequestParam String day,
                                       @RequestParam String email,
                                       @RequestParam String phone,
                                       @RequestParam String address,
                                       @RequestParam String addressDetail,
                                       HttpSession session,
                                       Model model)
    {
        String birthDate = year + String.format("%02d", Integer.parseInt(month)) + String.format("%02d", Integer.parseInt(day));
        memberDao.insertMember(id, pw, name, birthDate, email, phone, address, addressDetail);
        return "redirect:/";
    }

    // 중복확인
    @GetMapping("member/dupcheck")
    @ResponseBody
    public Map<String, Object> dupCheck(@RequestParam String id) {
        int dup = memberDao.dupCheck(id);
        Map<String, Object> response = new HashMap<>();
        response.put("isDuplicate", dup > 0);
        return response;
    }

    // 로그인
    @GetMapping("member/login")
    public String login(HttpSession session) {
        if (session.getAttribute("id") != null)
        {
            return "redirect:/";
        }
        return "member/login";
    }

    // 로그인 되면 세션 만들기
    @PostMapping("member/login/action")
    public String loginAction(@RequestParam String id,
                              @RequestParam String pw,
                              HttpSession session) {
        List<Map<String, Object>> loginResult = memberDao.loginCheck(id, pw);
        Integer delFlag = memberDao.getDelFlag(id);

        if (loginResult.size() == 1 && delFlag == 0) {
            session.setAttribute("id", loginResult.get(0).get("id"));
            session.setAttribute("pw", loginResult.get(0).get("pw"));
            session.setAttribute("name", loginResult.get(0).get("name"));
            session.setAttribute("grade", loginResult.get(0).get("grade"));
            return "redirect:/";
        }
        else
        {
            return "redirect:/member/login";
        }
    }

    // 로그아웃
    @GetMapping("member/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // 아이디 찾기 페이지 ()
    @GetMapping("member/findid")
    public String findId() {
        return "member/findid";
    }

    @PostMapping("member/findid/action")
    public String findIdAction(@RequestParam String name,
                               @RequestParam String birthDate,
                               Model model)
    {
        String id_ = memberDao.findid(name, birthDate);

        if (id_ == null || id_.equals("")) {
            model.addAttribute("link", "/member/findid");
            model.addAttribute("msg", "없는 회원정보입니다.");
            return "member/alert";
        } else {
            model.addAttribute("vari", id_);
            model.addAttribute("msg", "아이디는 " + id_ + " 입니다.");
            return "member/alert";
        }

    }

    // 비밀번호 찾기 페이지 (아이디와 이메일 입력시 비밀번호 찾기)
    @GetMapping("member/findpw")
    public String findPw() {
        return "member/findpw";
    }

    // 비밀번호 찾기 액션
    @PostMapping("/member/findpw/action")
    public String findPwAction(@RequestParam String id,
                               @RequestParam String email,
                               Model model) {
        String pass = memberDao.findpw(id, email);
        
        if (pass == null || pass.equals("")) {
            model.addAttribute("link", "/member/findpw");
            model.addAttribute("msg", "없는 회원정보입니다.");
            return "member/alert";
        } else {
            model.addAttribute("vari", pass);
            model.addAttribute("msg", "비밀번호는 " + pass + " 입니다.");
            return "member/alert";
        }
    }

    // 사용자 개인별로 정보수정 - 먼저 비밀번호 확인하기
    @GetMapping("member/change")
    public String memberChange() {
        return "member/change_1";
    }

    @PostMapping("member/change/verify")
    public String memberVerify(@RequestParam String id,
                               @RequestParam String pw,
                               HttpSession session,
                               Model model)
    {
        if (session.getAttribute("id") != null) {
            int cnt = memberDao.verifyPassword(id, pw);
            if (cnt == 1) {
                String userId = (String)session.getAttribute("id");
                List<Map<String, Object>> resultSet = memberDao.selectMember(userId);
                model.addAttribute("resultSet", resultSet);
                return "member/change_2";
            } else {
                model.addAttribute("link", "/member/change");
                model.addAttribute("msg", "틀린 비밀번호 입니다.");
                return "/member/alert";
            }
        } else {
            return "/";
        }
    }

    // 사용자 개개인 정보수정 액션
    @PostMapping("member/change/action")
    @ResponseBody
    public String memberChangeAction(@RequestParam String pw,
                                     @RequestParam String name,
                                     @RequestParam String year,
                                     @RequestParam String month,
                                     @RequestParam String day,
                                     @RequestParam String email,
                                     @RequestParam String phone,
                                     @RequestParam String address,
                                     @RequestParam String addressDetail,
                                     HttpSession session,
                                     Model model)
    {
        if (session.getAttribute("id") != null) {
            String userId = (String)session.getAttribute("id");

            String birthDate_after = year + String.format("%02d", Integer.parseInt(month)) + String.format("%02d", Integer.parseInt(day));
            memberDao.memberChangeAction(pw, name, birthDate_after, email, phone, address, addressDetail, userId);
        } else {
            return "<script>window.close();</script>";
        }
        return "<script>window.close(); alert('회원정보가 수정되었습니다.');</script>";
    }

    // 사용자 개개인 정보 삭제
    @GetMapping("member/delete/action")
    @ResponseBody
    public String memberDeleteAction(HttpSession session,
                                     Model model)
    {
        if (session.getAttribute("id") != null) {
            String userId = (String)session.getAttribute("id");
            memberDao.memberDeleteAction(userId);
            session.invalidate();
        }
        return "<script>window.close(); alert('회원정보가 삭제되었습니다.');</script>";
    }

    // 프로필 사진 변경 페이지
    @GetMapping("profile/upload")
    public String changeProfile(HttpSession session,
                                Model model) {

        if (session.getAttribute("id") == null) {
            return "redirect:/member/login";
        }
        String id = (String) session.getAttribute("id");
        model.addAttribute("id", id);

        return ("member/profileupload");
    }

    // 프로필 변경 액션
    @PostMapping("profile/upload/action")
    @ResponseBody
    public String changeProfileAction(HttpSession session,
                                      Model model,
                                      @RequestParam MultipartFile file) throws IOException
    {
        UUID uuid = UUID.randomUUID();
        String ogName = file.getOriginalFilename();
        String imgPath = "C:/project/blog/src/main/resources/static/images/";
        String imgName = uuid + "_" + ogName;
        String uploadImg = imgPath + imgName;

        File img = new File(uploadImg);
        file.transferTo(img);

        String id = (String) session.getAttribute("id");

        List<Map<String, Object>> profileSelect = memberDao.selectProfile(id);
        if (!profileSelect.isEmpty()) {
            memberDao.profileChange(id, ogName, imgPath, imgName);
        } else {
            memberDao.profileUpload(id, ogName, imgPath, imgName);
        }
        return "<script>window.close()</script>";
    }
}