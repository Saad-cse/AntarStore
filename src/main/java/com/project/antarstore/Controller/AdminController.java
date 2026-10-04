package com.project.antarstore.Controller;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.antarstore.Dto.CategoryDto;
import com.project.antarstore.Dto.ProductDto;
import com.project.antarstore.Model.Category;
import com.project.antarstore.Model.Enquiry;
import com.project.antarstore.Model.Orders;
import com.project.antarstore.Model.Products;
import com.project.antarstore.Model.Products.ProductStatus;
import com.project.antarstore.Model.Users;
import com.project.antarstore.Model.Users.UserRole;
import com.project.antarstore.Model.Users.UserStatus;
import com.project.antarstore.Repository.CategoryRepo;
import com.project.antarstore.Repository.EnquiryRepo;
import com.project.antarstore.Repository.OrderRepo;
import com.project.antarstore.Repository.ProductRepo;
import com.project.antarstore.Repository.UserRepo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

// NOTE: Every request under /Admin/** already passes through AdminAuthInterceptor
// (see Config/AdminAuthInterceptor.java), which redirects to /Login if there is
// no valid admin session. That replaces the old per-method
// "if (session.getAttribute(\"loggedInAdmin\") == null)" checks, one of which
// (UpdateUserStatus) was missing entirely - recovery audit F-11.
@Controller
@RequestMapping("/Admin")
public class AdminController {

	@Autowired
	private HttpSession session;

	@Autowired
	private EnquiryRepo enquiryRepo;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private CategoryRepo categoryRepo;

	@Autowired
	private ProductRepo productRepo;

	@Autowired
	private OrderRepo orderRepo;

	@GetMapping("/Dashboard")
	public String showDashboard(Model model) {

		List<Orders> allOrders = orderRepo.findAll();

		model.addAttribute("userCount", userRepo.findAllByRole(UserRole.User).size());
		model.addAttribute("productCount", productRepo.findAll().size());
		model.addAttribute("categoryCount", categoryRepo.findAllByIsVisible(true).size());
		model.addAttribute("orderCount", allOrders.size());
		model.addAttribute("enquiryCount", enquiryRepo.findAll().size());

		long cancelled = allOrders.stream().filter(o -> o.getOrderStatus() == Orders.OrderStatus.Cancelled).count();
		long delivered = allOrders.stream().filter(o -> o.getOrderStatus() == Orders.OrderStatus.Delivered).count();
		long confirmed = allOrders.stream().filter(o -> o.getOrderStatus() == Orders.OrderStatus.Confirmed).count();
		model.addAttribute("cancelledOrders", cancelled);
		model.addAttribute("deliveredOrders", delivered);
		model.addAttribute("confirmedOrders", confirmed);

		List<Enquiry> recentEnquiries = enquiryRepo.findAll().stream()
				.sorted((a, b) -> b.getEnquiryAt().compareTo(a.getEnquiryAt()))
				.limit(5)
				.collect(java.util.stream.Collectors.toList());
		model.addAttribute("recentEnquiries", recentEnquiries);

		// Orders placed per month, for the last 6 months, oldest first.
		List<String> orderMonths = new ArrayList<>();
		List<Long> orderCounts = new ArrayList<>();
		java.time.YearMonth current = java.time.YearMonth.now();
		for (int i = 5; i >= 0; i--) {
			java.time.YearMonth month = current.minusMonths(i);
			orderMonths.add(month.getMonth().toString().substring(0, 3) + " " + month.getYear());
			long count = allOrders.stream()
					.filter(o -> o.getOrderedAt() != null
							&& java.time.YearMonth.from(o.getOrderedAt()).equals(month))
					.count();
			orderCounts.add(count);
		}
		model.addAttribute("orderMonths", orderMonths);
		model.addAttribute("orderCounts", orderCounts);

		return "Admin/Dashboard";
	}

	@GetMapping("/ManageUsers")
	public String ShowManageUsers(@RequestParam(value = "status", required = false) UserStatus status, Model model) {

		if (status == null) {
			List<Users> users = userRepo.findAllByRole(UserRole.User);
			model.addAttribute("users", users);
		} else {
			List<Users> users = userRepo.findAllByRoleAndStatus(UserRole.User, status);
			model.addAttribute("users", users);
		}
		return "Admin/ManageUsers";
	}

	@GetMapping("/UpdateUserStatus/{id}")
	public String UpdateUserStatus(@PathVariable("id") long id, RedirectAttributes attributes, HttpServletRequest request) {

		Users user = userRepo.findById(id)
				.orElseThrow(() -> new RuntimeException("User not found"));

		if (user.getStatus().equals(UserStatus.Verified)) {
			user.setStatus(UserStatus.Disabled);
		} else if (user.getStatus().equals(UserStatus.Disabled)) {
			user.setStatus(UserStatus.Verified);
		}
		userRepo.save(user);
		attributes.addFlashAttribute("msg", "User status successfully updated");

		String referer = request.getHeader("referer");
		return "redirect:" + (referer != null ? referer : "/Admin/ManageUsers");
	}

	@GetMapping("/ManageOrders")
	public String ShowManageOrders(Model model) {
		List<Orders> orders = orderRepo.findAllByOrderByOrderedAtDesc();
		model.addAttribute("orders", orders);
		return "Admin/ManageOrders";
	}

	@PostMapping("/UpdateOrderStatus/{id}")
	public String UpdateOrderStatus(@PathVariable("id") long id,
			@RequestParam("orderStatus") Orders.OrderStatus status,
			RedirectAttributes attributes) {

		Orders order = orderRepo.findById(id)
				.orElseThrow(() -> new RuntimeException("Order not found"));

		order.setOrderStatus(status);
		if (status == Orders.OrderStatus.Delivered) {
			order.setDeliveredAt(LocalDateTime.now());
		} else if (status == Orders.OrderStatus.Cancelled) {
			order.setCancelledAt(LocalDateTime.now());
		}
		orderRepo.save(order);
		attributes.addFlashAttribute("msg", "Order status updated");
		return "redirect:/Admin/ManageOrders";
	}

	@GetMapping("/AddCategory")
	public String ShowAddCategory(Model model) {

		CategoryDto dto = new CategoryDto();
		model.addAttribute("dto", dto);

		List<Category> categories = categoryRepo.findAll();
		model.addAttribute("categories", categories);
		return "Admin/AddCategory";
	}

	@PostMapping("/AddCategory")
	public String AddCategory(@ModelAttribute CategoryDto dto, RedirectAttributes attributes) {

		if (!StringUtils.hasText(dto.getCategoryName())) {
			attributes.addFlashAttribute("msg", "Category name is required");
			return "redirect:/Admin/AddCategory";
		}

		if (categoryRepo.existsByCategoryNameIgnoreCase(dto.getCategoryName().trim())) {
			attributes.addFlashAttribute("msg", "Category already exists");
			return "redirect:/Admin/AddCategory";
		}

		Category category = new Category();
		category.setCategoryName(dto.getCategoryName().trim());
		category.setCategoryIcon(dto.getCategoryIcon());
		category.setVisible(true);

		categoryRepo.save(category);
		attributes.addFlashAttribute("msg", "Category successfully added");
		return "redirect:/Admin/AddCategory";
	}

	@GetMapping("/AddProduct")
	public String ShowAddProduct(Model model) {

		ProductDto dto = new ProductDto();
		model.addAttribute("dto", dto);

		List<Category> categories = categoryRepo.findAllByIsVisible(true);
		model.addAttribute("categories", categories);
		return "Admin/AddProduct";
	}

	private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024; // 5MB per image

	@PostMapping("/AddProduct")
	public String AddProduct(@ModelAttribute("dto") ProductDto dto, @RequestParam("images") MultipartFile[] images,
			RedirectAttributes attributes) {

		// FIX (recovery audit F-12): previously these validations set a flash
		// message but execution continued regardless, so an invalid image count
		// never actually stopped the upload. Now we validate first and return
		// immediately on any failure.
		if (images == null || images.length < 2) {
			attributes.addFlashAttribute("msg", "Please upload at least 2 images");
			return "redirect:/Admin/AddProduct";
		}
		if (images.length > 5) {
			attributes.addFlashAttribute("msg", "You can upload a maximum of 5 images");
			return "redirect:/Admin/AddProduct";
		}
		for (MultipartFile image : images) {
			String contentType = image.getContentType();
			if (contentType == null || !contentType.startsWith("image/")) {
				attributes.addFlashAttribute("msg", "Only image files are allowed");
				return "redirect:/Admin/AddProduct";
			}
			if (image.getSize() > MAX_IMAGE_BYTES) {
				attributes.addFlashAttribute("msg", "Each image must be 5MB or smaller");
				return "redirect:/Admin/AddProduct";
			}
		}

		try {
			String uploadDir = "public/ProductImages/";
			File folder = new File(uploadDir);

			if (!folder.exists()) {
				folder.mkdirs();
			}

			List<String> productImages = new ArrayList<>();
			for (MultipartFile image : images) {
				// FIX (recovery audit F-13): image.getName() returns the multipart FORM
				// FIELD name ("images"), not the file's original name, so every uploaded
				// file was being saved with (effectively) the same base name. Use the
				// original filename's extension and a fresh UUID for the stored name.
				String original = StringUtils.cleanPath(
						image.getOriginalFilename() == null ? "" : image.getOriginalFilename());
				String extension = "";
				int dot = original.lastIndexOf('.');
				if (dot >= 0) {
					extension = original.substring(dot); // includes the dot
				}
				String storageFileName = UUID.randomUUID() + extension;
				Path uploadPath = Paths.get(uploadDir, storageFileName);
				try (InputStream inputStream = image.getInputStream()) {
					Files.copy(inputStream, uploadPath, StandardCopyOption.REPLACE_EXISTING);
				}

				productImages.add(storageFileName);
			}

			Products product = new Products();
			product.setProductName(dto.getProductName());
			product.setProductDescription(dto.getProductDescription());
			product.setCategory(dto.getCategory());
			product.setBrandName(dto.getBrandName());
			product.setGender(dto.getGender());

			product.setCostPrice(dto.getCostPrice());
			product.setDiscount(dto.getDiscount());
			product.setSellingPrice(dto.getSellingPrice());
			double finalprice = dto.getSellingPrice() - (dto.getSellingPrice() * dto.getDiscount()) / 100;
			product.setFinalprice(finalprice);

			product.setColors(dto.getColors());
			product.setSizes(dto.getSizes());

			product.setQuantity(dto.getQuantity());
			product.setShippingCharge(dto.getShippingCharge());
			product.setDeliveryTime(dto.getDeliveryTime());
			product.setReturnPolicy(dto.isReturnPolicy());
			product.setVisibility(true);
			product.setAddedAt(LocalDateTime.now());
			product.setStatus(ProductStatus.Available);
			product.setProductImages(productImages);
			productRepo.save(product);
			attributes.addFlashAttribute("msg", "Product successfully uploaded");

		} catch (Exception e) {
			System.err.println("Product upload failed:");
			e.printStackTrace();
			attributes.addFlashAttribute("msg", "Something went wrong while uploading the product");
		}
		return "redirect:/Admin/AddProduct";
	}

	@GetMapping("/ManageProduct")
	public String ShowManageProduct(Model model) {

		List<Products> products = productRepo.findAll();
		model.addAttribute("products", products);
		return "Admin/ManageProduct";
	}

	@PostMapping("/ToggleProductVisibility/{id}")
	public String toggleProductVisibility(@PathVariable("id") long id, RedirectAttributes attributes) {
		Products product = productRepo.findById(id)
				.orElseThrow(() -> new RuntimeException("Product not found"));
		product.setVisibility(!product.isVisibility());
		productRepo.save(product);
		attributes.addFlashAttribute("msg", "Product visibility updated");
		return "redirect:/Admin/ManageProduct";
	}

	@GetMapping("/ViewEnquiry")
	public String ShowViewEnquiry(Model model) {

		List<Enquiry> enquiries = enquiryRepo.findAll();
		model.addAttribute("enquiries", enquiries);
		return "Admin/ViewEnquiry";
	}

	@GetMapping("/logout")
	public String Logout() {
		session.removeAttribute("loggedInAdmin");
		return "redirect:/Login";
	}
}
