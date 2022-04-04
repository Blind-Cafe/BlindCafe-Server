package me.blindcafe.blindcafe.controller;

import me.blindcafe.blindcafe.dto.request.AdminLoginRequest;
import me.blindcafe.blindcafe.dto.response.MemberResponse;
import me.blindcafe.blindcafe.dto.response.WeeklyMemberStateResponse;
import me.blindcafe.blindcafe.service.AdminService;
import me.blindcafe.blindcafe.service.PresenceService;
import me.blindcafe.blindcafe.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final String SESSION = "token";

    private final AdminService adminService;
    private final UserService userService;
    private final PresenceService presenceService;

    /**
     * 페이지
     */
    // 관리자 로그인 페이지
    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    // 관리자 메인 페이지
    @GetMapping()
    public String mainPage(HttpSession session, Model model) {
        if (!isValid(session))
            return "redirect:/admin/login";

        // 실시간 사용자 수
        Long realtimeMemberCount = presenceService.getConnectedMemberCount();
        model.addAttribute("realtimeMemberCount", realtimeMemberCount);
        // 전체 사용자 수
        Long entireMemberCount = userService.getEntireMemberCount();
        model.addAttribute("entireMemberCount", entireMemberCount);
        // 처리 안한 신고 내역 수
        Long uncheckedReportCount = adminService.getUncheckedReportCount();
        model.addAttribute("uncheckedReportCount", uncheckedReportCount);
        // 처리 안한 건의 사항 수
        Long uncheckedSuggestionCount = adminService.getUncheckedSuggestionCount();
        model.addAttribute("uncheckedSuggestionCount", uncheckedSuggestionCount);
        // 주간 접속자, 주간 접속자 비율
        WeeklyMemberStateResponse weeklyMemberState = adminService.getWeeklyMemberState();
        model.addAttribute("weekly", weeklyMemberState.getWeekly());
        model.addAttribute("male", weeklyMemberState.getMale());
        model.addAttribute("female", weeklyMemberState.getFemale());

        return "admin/main";
    }

    // 회원 목록 페이지
    @GetMapping("/member")
    public String memberPage(
            HttpSession session,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model
    ) {
        if (!isValid(session))
            return "redirect:/admin/login";

        List<MemberResponse> members = userService.getMembers(page);

        model.addAttribute("members", members);

        return "admin/member";
    }

    // 신고 내역 페이지
    @GetMapping("/report")
    public String reportPage(HttpSession session) {
        if (!isValid(session))
            return "redirect:/admin/login";

        return "admin/report";
    }

    // 건의 사항 페이지
    @GetMapping("/suggestion")
    public String suggestionPage(HttpSession session) {
        if (!isValid(session))
            return "redirect:/admin/login";

        return "admin/suggestion";
    }

    // 건의사항 상세 페이지
    @GetMapping("/suggestion/{id}}")
    public String suggestionDetailPage(
            HttpSession session,
            @PathVariable(value = "id") Long sid
    ) {
        if (!isValid(session))
            return "redirect:/admin/login";

        return "admin/suggestion-detail";
    }

    // 탈퇴 & 방 나가기 사유
    @GetMapping("/reason")
    public String reasonPage(HttpSession session) {
        if (!isValid(session))
            return "redirect:/admin/login";
        return "admin/reason";
    }

    /**
     * 기능
     */
    // 로그인
    @PostMapping("/login")
    public String login(HttpSession session, @ModelAttribute AdminLoginRequest request) {
        // 세션이 이미 있는 경우 제거
        if (session.getAttribute(SESSION) != null)
            session.removeAttribute(SESSION);

        // 로그인 확인
        String token = adminService.login(request);

        // 관리자인 경우
        if (token != null) {
            session.setAttribute(SESSION, token);
            return "redirect:/admin";
        }
        return "redirect:/admin/login";
    }

    // 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session.getAttribute(SESSION) != null) {
            session.removeAttribute(SESSION);
        }
        return "redirect:/admin/login";
    }

    // 세션 검사
    private boolean isValid(HttpSession session) {
        if (session.getAttribute(SESSION) == null)
            return false;
        return adminService.isValidAdmin(session.getAttribute(SESSION).toString());
    }
}
