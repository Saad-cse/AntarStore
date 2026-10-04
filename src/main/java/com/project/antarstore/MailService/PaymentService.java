package com.project.antarstore.MailService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.project.antarstore.Model.Cart;
import com.project.antarstore.Model.Orders;
import com.project.antarstore.Model.Products;
import com.project.antarstore.Model.SavedAddress;
import com.project.antarstore.Model.Users;
import com.project.antarstore.Repository.CartRepo;
import com.project.antarstore.Repository.OrderRepo;
import com.project.antarstore.Repository.ProductRepo;
import com.project.antarstore.Repository.SavedAddressRepo;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Service
public class PaymentService {

	@Autowired
	private CartRepo cartRepo;

	@Autowired
	private ProductRepo productRepo;

	@Autowired
	private OrderRepo ordersRepo;

	@Autowired
	private SavedAddressRepo savedAddressRepo;

	@Autowired
	private CartService cartService;

	// Credentials are never hardcoded here. They come from application.properties,
	// which in turn reads them from environment variables (RAZORPAY_KEY_ID /
	// RAZORPAY_KEY_SECRET). Use TEST-mode ("rzp_test_...") credentials while
	// developing; never commit real/live keys to source control.
	@Value("${razorpay.key.id}")
	private String razorpayKeyId;

	@Value("${razorpay.key.secret}")
	private String razorpayKeySecret;

	private RazorpayClient client() throws Exception {
		return new RazorpayClient(razorpayKeyId, razorpayKeySecret);
	}

	public Map<String, Object> createPayment(HttpSession session) throws Exception {

		Map<String, Object> map = new HashMap<>();

		Users user = (Users) session.getAttribute("loggedInUser");
		if (user == null) {
			map.put("error", "NOT_LOGGED_IN");
			map.put("message", "Please log in to continue.");
			return map;
		}

		List<Cart> carts = cartRepo.findAllByUser(user);
		if (carts.isEmpty()) {
			map.put("error", "EMPTY_CART");
			map.put("message", "Your cart is empty.");
			return map;
		}

		SavedAddress address = savedAddressRepo.findByUserAndActiveTrue(user);
		if (address == null) {
			map.put("error", "NO_ADDRESS");
			map.put("message", "Please add a delivery address before checkout.");
			return map;
		}

		// Re-validate stock server-side; never trust anything the browser sends.
		for (Cart cart : carts) {
			if (cart.getProduct().getQuantity() < cart.getQuantity()) {
				map.put("error", "OUT_OF_STOCK");
				map.put("message", "\"" + cart.getProduct().getProductName() + "\" no longer has enough stock.");
				return map;
			}
		}

		CartService.CartSummary summary = cartService.computeSummary(user);

		JSONObject orderRequest = new JSONObject();
		orderRequest.put("amount", Math.round(summary.grandTotal * 100));
		orderRequest.put("currency", "INR");
		orderRequest.put("receipt", "txn_" + System.currentTimeMillis());

		Order order = client().orders.create(orderRequest);

		map.put("key", razorpayKeyId);
		map.put("orderId", order.get("id"));
		map.put("amount", order.get("amount"));
		map.put("name", user.getName());
		map.put("email", user.getEmail());
		map.put("contact", user.getContactNo());

		return map;
	}

	@Transactional
	public String verifyPayment(String paymentId, String razorOrderId, String signature, HttpSession session) {

		try {
			// Idempotency guard: if a retry/duplicate callback arrives for an order we
			// already recorded, don't create the order rows a second time.
			if (ordersRepo.existsByOrderId(razorOrderId)) {
				return "success";
			}

			JSONObject json = new JSONObject();
			json.put("razorpay_order_id", razorOrderId);
			json.put("razorpay_payment_id", paymentId);
			json.put("razorpay_signature", signature);

			boolean valid = Utils.verifyPaymentSignature(json, razorpayKeySecret);

			if (!valid) {
				return "failed";
			}

			// FIX: this previously read "LoggedInUser" (capital L) while login stores
			// "loggedInUser", so `user` was always null here and the flow failed
			// silently past this point. See recovery audit F-02.
			Users user = (Users) session.getAttribute("loggedInUser");
			if (user == null) {
				return "failed";
			}

			SavedAddress address = savedAddressRepo.findByUserAndActiveTrue(user);
			if (address == null) {
				return "failed";
			}

			List<Cart> carts = cartRepo.findAllByUser(user);
			if (carts.isEmpty()) {
				return "failed";
			}

			// Validate stock for every line BEFORE writing anything. The previous
			// version checked stock inside the same loop that saved orders, so a
			// failure partway through left already-created order rows committed
			// even though checkout was reported as failed. See recovery audit F-04.
			for (Cart cart : carts) {
				if (cart.getProduct().getQuantity() < cart.getQuantity()) {
					return "Stock Not Available";
				}
			}

			for (Cart cart : carts) {

				Products product = cart.getProduct();

				Orders order = new Orders();
				order.setProducts(product);
				order.setUsers(user);
				order.setProductName(product.getProductName());
				order.setDescription(product.getProductDescription());
				order.setProductPrice(product.getSellingPrice());
				order.setDiscount(product.getDiscount());
				order.setFinalPrice(product.getFinalprice());
				order.setShippingCharge(product.getShippingCharge());
				order.setQuantity(cart.getQuantity());
				order.setColor(cart.getColor());
				order.setSize(cart.getSize());
				order.setTotalAmount(cart.getFinalPrice() + product.getShippingCharge());
				order.setCustomerName(address.getCustomerName());
				order.setContactNo(address.getContactNo());
				order.setShippingAddress(address.getAddress());
				order.setPincode(address.getPincode());
				order.setOrderId(razorOrderId);
				order.setPaymentId(paymentId);
				order.setPaymentSignature(signature);
				order.setOrderStatus(Orders.OrderStatus.Confirmed);
				order.setPaymentStatus(Orders.PaymentStatus.Success);
				order.setOrderedAt(LocalDateTime.now());

				ordersRepo.save(order);

				product.setQuantity(product.getQuantity() - cart.getQuantity());
				productRepo.save(product);
			}

			cartRepo.deleteAll(carts);

			return "success";

		} catch (Exception e) {
			// Log a safe, generic diagnostic only. Never log payment signatures,
			// full payloads, or other sensitive payment data (see recovery audit F-18).
			System.err.println("Payment verification failed: " + e.getClass().getSimpleName());
			return "failed";
		}
	}
}
