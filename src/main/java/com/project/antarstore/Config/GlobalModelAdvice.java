package com.project.antarstore.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.project.antarstore.Model.Users;
import com.project.antarstore.Repository.CartRepo;
import com.project.antarstore.Repository.WishlistRepo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Makes the logged-in customer's cart/wishlist item counts available to every
 * page's navbar, not just the dedicated /cart page. Runs before every
 * MainController request.
 */
@ControllerAdvice(assignableTypes = com.project.antarstore.Controller.MainController.class)
public class GlobalModelAdvice {

	@Autowired
	private HttpSession session;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private CartRepo cartRepo;

	@Autowired
	private WishlistRepo wishlistRepo;

	// FIX: base.html previously used ${#request.requestURI} directly inside a
	// Thymeleaf expression to decide whether to show the pill nav row. This
	// Thymeleaf version (3.1.5) deliberately blocks templates from reaching
	// into #request/#session/#response directly - it throws
	// IllegalArgumentException at render time instead ("expression utility
	// objects are no longer available by default"), which took down every
	// single page using base.html, not just Login/Register. The supported
	// fix is to hand the current path to the template as an ordinary model
	// attribute from the controller layer instead.
	@ModelAttribute("currentPath")
	public String currentPath() {
		return request.getRequestURI();
	}

	@ModelAttribute("navCartCount")
	public long navCartCount() {
		Users user = (Users) session.getAttribute("loggedInUser");
		if (user == null) {
			return 0;
		}
		return cartRepo.findAllByUser(user).size();
	}

	@ModelAttribute("navWishlistCount")
	public long navWishlistCount() {
		Users user = (Users) session.getAttribute("loggedInUser");
		if (user == null) {
			return 0;
		}
		return wishlistRepo.countByUser(user);
	}
}
