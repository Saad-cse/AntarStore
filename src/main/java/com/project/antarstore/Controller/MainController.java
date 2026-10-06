package com.project.antarstore.Controller;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.antarstore.Dto.EnquiryDto;
import com.project.antarstore.Dto.SavedAddressDto;
import com.project.antarstore.Dto.UserDto;
import com.project.antarstore.MailService.CartService;
import com.project.antarstore.MailService.PaymentService;
import com.project.antarstore.MailService.SendMailService;
import com.project.antarstore.Model.Cart;
import com.project.antarstore.Model.Category;
import com.project.antarstore.Model.Enquiry;
import com.project.antarstore.Model.Orders;
import com.project.antarstore.Model.Products;
import com.project.antarstore.Model.SavedAddress;
import com.project.antarstore.Model.Users;
import com.project.antarstore.Model.Users.UserRole;
import com.project.antarstore.Model.Users.UserStatus;
import com.project.antarstore.Repository.CartRepo;
import com.project.antarstore.Repository.CategoryRepo;
import com.project.antarstore.Repository.EnquiryRepo;
import com.project.antarstore.Repository.OrderRepo;
import com.project.antarstore.Repository.ProductRepo;
import com.project.antarstore.Repository.SavedAddressRepo;
import com.project.antarstore.Repository.UserRepo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {

	@Autowired
	private EnquiryRepo enquiryRepo;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private SendMailService sendMailService;

	@Autowired
	private CategoryRepo categoryRepo;

	@Autowired
	private ProductRepo productRepo;

	@Autowired
	private HttpSession session;

	@Autowired
	private CartRepo cartRepo;

	@Autowired
	private CartService cartService;

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private SavedAddressRepo savedAddressRepo;

	@Autowired
	private OrderRepo orderRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private com.project.antarstore.Repository.WishlistRepo wishlistRepo;

	private Users currentUser() {
		return (Users) session.getAttribute("loggedInUser");
	}

	@GetMapping("/")
	public String showIndex(Model model) {

		// Homepage "Shop By Category": first 4 visible categories, with a live
		// count of visible products in each. Labels are built here (not in the
		// template) so the template stays simple.
		List<Category> homeCategories = categoryRepo.findAllByIsVisible(true).stream().limit(4).toList();
		Map<Long, String> homeCategoryLabels = new java.util.HashMap<>();
		for (Category c : homeCategories) {
			int count = productRepo.findAllByCategoryAndVisibilityTrue(c).size();
			homeCategoryLabels.put(c.getId(), count == 1 ? "1 PRODUCT" : count + " PRODUCTS");
		}

		// Homepage "Featured Selection": the 4 newest visible products that
		// have at least one image (the card needs one to display).
		List<Products> featuredProducts = productRepo.findAllByVisibilityTrue().stream()
				.filter(p -> p.getProductImages() != null && !p.getProductImages().isEmpty())
				.sorted(java.util.Comparator.comparingLong(Products::getId).reversed())
				.limit(4)
				.toList();

		model.addAttribute("homeCategories", homeCategories);
		model.addAttribute("homeCategoryLabels", homeCategoryLabels);
		model.addAttribute("featuredProducts", featuredProducts);
		return "index";
	}

	@GetMapping("/blog")
	public String showBlog() {
		return "blog";
	}

	@GetMapping("/about")
	public String showAbout() {
		return "about";
	}

	@GetMapping("/shop")
	public String showShop(@RequestParam(value = "id", required = false) Long id,
			@RequestParam(value = "q", required = false) String q, Model model) {

		List<Category> categories = categoryRepo.findAllByIsVisible(true);
		model.addAttribute("categories", categories);
		model.addAttribute("q", q);

		// FIX (recovery audit F-14): the shop listing previously returned every
		// product regardless of admin visibility. Only show products the admin
		// has marked visible.
		if (org.springframework.util.StringUtils.hasText(q)) {
			// Text search takes priority over a category filter.
			List<Products> products = productRepo.findAllByVisibilityTrueAndProductNameContainingIgnoreCase(q.trim());
			model.addAttribute("products", products);
		} else if (id == null) {
			List<Products> products = productRepo.findAllByVisibilityTrue();
			model.addAttribute("products", products);
		} else {
			Optional<Category> category = categoryRepo.findById(id);
			if (category.isEmpty()) {
				model.addAttribute("products", List.of());
			} else {
				List<Products> products = productRepo.findAllByCategoryAndVisibilityTrue(category.get());
				model.addAttribute("products", products);
			}
		}
		return "shop";
	}

	@GetMapping("/Product/{id}")
	public String showProduct(@PathVariable("id") long id, Model model, RedirectAttributes redirectAttributes) {

		Optional<Products> productOptional = productRepo.findById(id);

		if (productOptional.isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "Product not found");
			return "redirect:/shop";
		}

		Products product = productOptional.get();

		if (product.getCategory() == null) {
			Category defaultCategory = new Category();
			defaultCategory.setCategoryName("Uncategorized");
			product.setCategory(defaultCategory);
		}

		model.addAttribute("product", product);
		return "Product";
	}

	@PostMapping("/addToCart")
	public String addToCart(HttpServletRequest request,
			RedirectAttributes redirectAttributes) {

		String color = request.getParameter("color");
		String size = request.getParameter("size");
		long productId = Long.parseLong(request.getParameter("productId"));

		Users user = currentUser();

		if (user == null) {
			return "redirect:/Login";
		}

		try {
			cartService.addToCart(color, size, 1, user, productId);
			redirectAttributes.addFlashAttribute("success", "Product added to cart successfully.");
		} catch (RuntimeException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}

		return "redirect:/Product/" + productId;
	}

	@PostMapping("/cart/increase/{id}")
	public String increase(@PathVariable Long id, RedirectAttributes attributes) {
		try {
			cartService.increaseQuantity(id, currentUser());
		} catch (SecurityException e) {
			return "redirect:/Login";
		} catch (RuntimeException e) {
			attributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/cart";
	}

	@PostMapping("/cart/decrease/{id}")
	public String decrease(@PathVariable Long id, RedirectAttributes attributes) {
		try {
			cartService.decreaseQuantity(id, currentUser());
		} catch (SecurityException e) {
			return "redirect:/Login";
		} catch (RuntimeException e) {
			attributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/cart";
	}

	@PostMapping("/cart/remove/{id}")
	public String remove(@PathVariable Long id, RedirectAttributes attributes) {
		// FIX (recovery audit F-08): this endpoint used to delete the cart row by id
		// with no check that the row belonged to the caller (IDOR) - anyone logged in
		// could remove another customer's cart items by guessing ids. It's also now
		// POST-only instead of a GET link, since it mutates data.
		try {
			cartService.removeItem(id, currentUser());
		} catch (SecurityException e) {
			return "redirect:/Login";
		} catch (RuntimeException e) {
			attributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/cart";
	}

	// FIX (recovery audit F-06): there used to be two different cart routes,
	// GET /cart (broken, returned a view named "/cart") and GET /Cart (the real
	// one). There is now exactly one canonical cart route.
	@GetMapping("/cart")
	public String showCart(Model model) {

		Users user = currentUser();
		if (user == null) {
			return "redirect:/Login";
		}

		List<Cart> cartItems = cartRepo.findAllByUser(user);
		model.addAttribute("cartItems", cartItems);

		// FIX (recovery audit F-07): the cart template expects subtotal/shipping/
		// grandTotal, but only cartItems was ever supplied. Both the cart page and
		// payment creation now derive totals from the same CartService method so
		// the number shown to the customer always matches what they're charged.
		CartService.CartSummary summary = cartService.computeSummary(user);
		model.addAttribute("subtotal", summary.subtotal);
		model.addAttribute("shipping", summary.shippingTotal);
		model.addAttribute("discount", summary.discountTotal);
		model.addAttribute("grandTotal", summary.grandTotal);
		model.addAttribute("hasAddress", savedAddressRepo.findByUserAndActiveTrue(user) != null);

		return "cart";
	}

	@GetMapping("/payment/create")
	@ResponseBody
	public Map<String, Object> createPayment(HttpSession session) throws Exception {
		return paymentService.createPayment(session);
	}

	@PostMapping("/payment/verify")
	@ResponseBody
	public String verifyPayment(@RequestParam String paymentId, @RequestParam String orderId,
			@RequestParam String signature, HttpSession session) throws Exception {
		return paymentService.verifyPayment(paymentId, orderId, signature, session);
	}

	@GetMapping("/Register")
	public String showRegister(Model model) {
		UserDto dto = new UserDto();
		model.addAttribute("dto", dto);
		return "register";
	}

	@PostMapping("/Register")
	public String Register(@ModelAttribute UserDto dto, RedirectAttributes attributes, HttpSession session) {
		try {
			if (userRepo.existsByEmail(dto.getEmail())) {
				attributes.addFlashAttribute("msg", "User already exists!");
				return "redirect:/Register";
			}
			Users user = new Users();
			user.setName(dto.getName());
			user.setContactNo(dto.getContactNo());
			user.setEmail(dto.getEmail());
			user.setGender(dto.getGender());
			// FIX (recovery audit F-09): passwords were stored and compared in
			// plaintext. Every password is now hashed with BCrypt before it is
			// ever persisted.
			user.setPassword(passwordEncoder.encode(dto.getPassword()));

			user.setRole(UserRole.User);
			user.setStatus(UserStatus.Unverified);
			user.setRegisterAt(LocalDateTime.now());

			SecureRandom secureRandom = new SecureRandom();
			String otp = 100000 + secureRandom.nextInt(900000) + "";

			user.setOtp(otp);
			user.setGeneratedAt(LocalDateTime.now());
			user.setExpiryTime(LocalDateTime.now().plusMinutes(5));

			userRepo.save(user);

			// FIX (recovery audit F-10): the OTP email call was commented out, so a
			// new customer never actually received their verification code by mail
			// (only visible in the server console). It's now sent for real.
			//
			// IMPORTANT: the mail send is deliberately its own try/catch. The account
			// is already saved at this point - if the SMTP call fails (e.g. a bad
			// Gmail app password), the customer should still be able to continue to
			// the OTP page and use "Resend OTP" once mail is fixed, rather than being
			// told registration itself failed when it actually succeeded.
			try {
				sendMailService.sendOtpMail(user);
				attributes.addFlashAttribute("msg", "Registration successful, please verify your OTP!");
			} catch (Exception mailEx) {
				System.err.println("OTP email failed to send during registration:");
				mailEx.printStackTrace();
				attributes.addFlashAttribute("msg",
						"Account created, but we couldn't send the OTP email. Use \"Resend OTP\" once your email settings are fixed, or check the server console for the OTP.");
				// Print the OTP itself so registration is still usable while mail is broken.
				System.out.println("OTP for " + user.getEmail() + " is: " + otp);
			}

			session.setAttribute("email", user.getEmail());
			return "redirect:/verify-otp";

		} catch (Exception e) {
			// FIX: this used to swallow every exception silently with no logging at
			// all, which made real failures (e.g. a bad mail password) invisible in
			// the console and impossible to diagnose. Always log the real cause.
			System.err.println("Registration failed:");
			e.printStackTrace();
			attributes.addFlashAttribute("msg", "Something went wrong during registration. Check the server console for details.");
		}

		return "redirect:/Register";
	}

	@GetMapping("/verify-otp")
	public String showVerifyOtp(HttpSession session) {
		if (session.getAttribute("email") == null) {
			return "redirect:/Register";
		}
		return "verify-otp";
	}

	@PostMapping("/verify-otp")
	public String VerifyOtp(@RequestParam("otp") String otp, RedirectAttributes attributes, HttpSession session) {
		try {
			String email = (String) session.getAttribute("email");
			Users user = userRepo.findByEmail(email);

			if (user == null) {
				attributes.addFlashAttribute("msg", "Session expired, please register again");
				return "redirect:/Register";
			}

			if (!otp.equals(user.getOtp())) {
				attributes.addFlashAttribute("msg", "Invalid OTP");
				return "redirect:/verify-otp";
			}

			// FIX (recovery audit F-10): expiry now uses the stored expiryTime field
			// (kept in sync by both register and resend) instead of re-deriving "5
			// minutes since generatedAt" separately, which drifted out of sync with
			// resend-otp's own expiry logic.
			if (LocalDateTime.now().isAfter(user.getExpiryTime())) {
				attributes.addFlashAttribute("msg", "OTP expired, please request a new one");
				return "redirect:/verify-otp";
			}

			user.setStatus(UserStatus.Verified);
			userRepo.save(user);
			attributes.addFlashAttribute("msg", "Registration completed! Please log in.");
			return "redirect:/Login";

		} catch (Exception e) {
			System.err.println("OTP verification failed:");
			e.printStackTrace();
			attributes.addFlashAttribute("msg", "Something went wrong. Check the server console for details.");
		}
		return "redirect:/verify-otp";
	}

	@GetMapping("/resend-otp")
	public String resendOtp(RedirectAttributes attributes, HttpSession session) {
		try {
			String email = (String) session.getAttribute("email");
			Users user = userRepo.findByEmail(email);
			if (user == null) {
				return "redirect:/Register";
			}

			// Simple throttle: don't allow another resend within 30 seconds of the
			// last one, to prevent OTP-request spam.
			if (user.getGeneratedAt() != null
					&& ChronoUnit.SECONDS.between(user.getGeneratedAt(), LocalDateTime.now()) < 30) {
				attributes.addFlashAttribute("msg", "Please wait a moment before requesting another OTP");
				return "redirect:/verify-otp";
			}

			SecureRandom secureRandom = new SecureRandom();
			String otp = 100000 + secureRandom.nextInt(900000) + "";
			user.setOtp(otp);
			user.setGeneratedAt(LocalDateTime.now());
			user.setExpiryTime(LocalDateTime.now().plusMinutes(5));
			userRepo.save(user);

			sendMailService.sendOtpMail(user);
			attributes.addFlashAttribute("msg", "A new OTP has been sent to your email");
		} catch (Exception e) {
			System.err.println("Resend OTP failed:");
			e.printStackTrace();
			attributes.addFlashAttribute("msg", "Something went wrong sending the OTP. Check the server console for details.");
		}
		return "redirect:/verify-otp";
	}

	@GetMapping("/Login")
	public String showLogin() {
		return "Login";
	}

	@PostMapping("/Login")
	public String Login(HttpServletRequest request, RedirectAttributes attributes, HttpSession session) {
		try {
			String email = request.getParameter("email");
			String password = request.getParameter("password");

			Users user = userRepo.findByEmail(email);
			if (user == null) {
				attributes.addFlashAttribute("msg", "Invalid email or password");
				return "redirect:/Login";
			}

			// FIX (recovery audit F-09): passwords are hashed, so login must use the
			// encoder's matches() rather than a plaintext String.equals().
			if (!passwordEncoder.matches(password, user.getPassword())) {
				attributes.addFlashAttribute("msg", "Invalid email or password");
				return "redirect:/Login";
			}

			if (user.getRole().equals(UserRole.User)) {
				if (user.getStatus().equals(UserStatus.Unverified)) {
					sendMailService.sendOtpMail(user);
					session.setAttribute("email", user.getEmail());
					return "redirect:/verify-otp";
				} else if (user.getStatus().equals(UserStatus.Disabled)) {
					attributes.addFlashAttribute("msg", "Login disabled, please contact administrator!");
					return "redirect:/Login";
				} else {
					session.setAttribute("loggedInUser", user);
					return "redirect:/";
				}
			} else if (user.getRole().equals(UserRole.Admin)) {
				session.setAttribute("loggedInAdmin", user);
				return "redirect:/Admin/Dashboard";
			}

		} catch (Exception e) {
			System.err.println("Login failed:");
			e.printStackTrace();
			attributes.addFlashAttribute("msg", "Something went wrong. Check the server console for details.");
		}
		return "redirect:/Login";
	}

	@GetMapping("/logout")
	public String logout() {
		session.removeAttribute("loggedInUser");
		return "redirect:/";
	}

	@GetMapping("/ContactUs")
	public String showContactUs(Model model) {
		EnquiryDto dto = new EnquiryDto();
		model.addAttribute("dto", dto);
		return "ContactUs";
	}

	@PostMapping("/submitEnquiry")
	public String submitEnquiry(@ModelAttribute EnquiryDto dto, RedirectAttributes attributes) {
		try {
			Enquiry enquiry = new Enquiry();
			enquiry.setName(dto.getName());
			enquiry.setAddress(dto.getAddress());
			enquiry.setContactNo(dto.getContactNo());
			enquiry.setEmail(dto.getEmail());
			enquiry.setMessage(dto.getMessage());
			enquiry.setEnquiryType(dto.getEnquiryType());
			enquiry.setEnquiryAt(LocalDateTime.now());
			enquiryRepo.save(enquiry);
			attributes.addFlashAttribute("msg", "Enquiry successfully submitted, we will contact you soon!");

		} catch (Exception e) {
			System.err.println("Enquiry submission failed:");
			e.printStackTrace();
			attributes.addFlashAttribute("msg", "Something went wrong. Check the server console for details.");
		}
		return "redirect:/ContactUs";
	}

	// FIX (recovery audit F-05): previously GET /Address returned a view named
	// "Address" while the actual template file was SaveAddress.html, and the
	// POST target the form submitted to (/saveaddress) didn't match any mapping
	// at all (the real POST handler was hanging off /cart). The address flow now
	// lives at a single, consistent /address GET+POST pair, and saving actually
	// persists the address (previously it built a SavedAddress object and threw
	// it away without ever calling save()).
	@GetMapping("/address")
	public String showAddress(Model model) {

		Users user = currentUser();
		if (user == null) {
			return "redirect:/Login";
		}

		SavedAddress existing = savedAddressRepo.findByUserAndActiveTrue(user);
		SavedAddressDto dto = new SavedAddressDto();
		if (existing != null) {
			dto.setCustomerName(existing.getCustomerName());
			dto.setContactNo(existing.getContactNo());
			dto.setPincode(existing.getPincode());
			dto.setAddress(existing.getAddress());
		}
		model.addAttribute("dto", dto);
		return "address";
	}

	@PostMapping("/address")
	public String saveAddress(@ModelAttribute SavedAddressDto dto, RedirectAttributes attributes) {

		Users user = currentUser();
		if (user == null) {
			return "redirect:/Login";
		}

		if (!org.springframework.util.StringUtils.hasText(dto.getCustomerName())
				|| !org.springframework.util.StringUtils.hasText(dto.getContactNo())
				|| !org.springframework.util.StringUtils.hasText(dto.getPincode())
				|| !org.springframework.util.StringUtils.hasText(dto.getAddress())) {
			attributes.addFlashAttribute("msg", "Please fill in all address fields");
			return "redirect:/address";
		}

		SavedAddress address = savedAddressRepo.findByUserAndActiveTrue(user);
		if (address == null) {
			address = new SavedAddress();
			address.setUser(user);
			address.setActive(true);
		}
		address.setCustomerName(dto.getCustomerName());
		address.setContactNo(dto.getContactNo());
		address.setPincode(dto.getPincode());
		address.setAddress(dto.getAddress());

		savedAddressRepo.save(address);
		attributes.addFlashAttribute("msg", "Address saved");
		return "redirect:/cart";
	}

	// FIX (recovery audit F-17): OrderDto existed but there was no controller
	// for a customer to view their own past orders, despite the navigation
	// already linking to one.
	@GetMapping("/MyOrders")
	public String myOrders(Model model) {
		Users user = currentUser();
		if (user == null) {
			return "redirect:/Login";
		}
		List<Orders> orders = orderRepo.findAllByUsersOrderByOrderedAtDesc(user);
		model.addAttribute("orders", orders);
		return "MyOrders";
	}

	@GetMapping("/Wishlist")
	public String wishlist(Model model) {
		Users user = currentUser();
		if (user == null) {
			return "redirect:/Login";
		}
		model.addAttribute("items", wishlistRepo.findAllByUser(user));
		return "Wishlist";
	}

	@PostMapping("/wishlist/toggle/{productId}")
	public String toggleWishlist(@PathVariable("productId") long productId, HttpServletRequest request) {
		Users user = currentUser();
		if (user == null) {
			return "redirect:/Login";
		}

		Optional<Products> productOpt = productRepo.findById(productId);
		if (productOpt.isPresent()) {
			Products product = productOpt.get();
			var existing = wishlistRepo.findByUserAndProduct(user, product);
			if (existing.isPresent()) {
				wishlistRepo.delete(existing.get());
			} else {
				com.project.antarstore.Model.Wishlist w = new com.project.antarstore.Model.Wishlist();
				w.setUser(user);
				w.setProduct(product);
				wishlistRepo.save(w);
			}
		}

		String referer = request.getHeader("referer");
		return "redirect:" + (referer != null ? referer : "/Wishlist");
	}

	@GetMapping("/FAQ")
	public String faq() {
		return "FAQ";
	}

	@GetMapping("/Profile")
	public String profile(Model model) {
		Users user = currentUser();
		if (user == null) {
			return "redirect:/Login";
		}
		model.addAttribute("user", user);
		return "Profile";
	}
}
