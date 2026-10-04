package com.project.antarstore.Config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.project.antarstore.Model.Users;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Central admin authorization check. Previously several /Admin/** endpoints
 * (e.g. UpdateUserStatus) had no session check at all, so anyone who guessed
 * the URL could call them directly (recovery audit F-11). Rather than
 * re-adding an ad-hoc check to every admin method, every request under
 * /Admin/** is now required to carry a valid "loggedInAdmin" session
 * attribute with an Admin role.
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		HttpSession session = request.getSession(false);
		Object admin = session == null ? null : session.getAttribute("loggedInAdmin");

		if (!(admin instanceof Users) || !((Users) admin).getRole().equals(Users.UserRole.Admin)) {
			response.sendRedirect(request.getContextPath() + "/Login");
			return false;
		}

		return true;
	}
}
