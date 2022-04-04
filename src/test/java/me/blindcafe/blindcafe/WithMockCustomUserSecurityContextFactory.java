package me.blindcafe.blindcafe;

import me.blindcafe.blindcafe.domain.MatchingHistory;
import me.blindcafe.blindcafe.domain.NotificationSetting;
import me.blindcafe.blindcafe.domain.Ticket;
import me.blindcafe.blindcafe.domain.User;
import me.blindcafe.blindcafe.domain.type.Platform;
import me.blindcafe.blindcafe.domain.type.Social;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        User principal = User.create(Social.KAKAO, "socialId", Platform.IOS, "deviceToken", Ticket.create(), MatchingHistory.create(), NotificationSetting.create());
        principal.setId(1L);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null);
        context.setAuthentication(auth);
        return context;
    }
}
