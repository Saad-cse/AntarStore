package com.project.antarstore.MailService;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.antarstore.Model.Cart;
import com.project.antarstore.Model.Products;
import com.project.antarstore.Model.Users;
import com.project.antarstore.Repository.CartRepo;
import com.project.antarstore.Repository.ProductRepo;

@Service
public class CartService {

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private ProductRepo productRepo;

    /** Simple, single-source-of-truth cart total used by both the cart page and payment creation. */
    public static class CartSummary {
        public final double subtotal;
        public final double shippingTotal;
        public final double discountTotal;
        public final double grandTotal;

        public CartSummary(double subtotal, double shippingTotal, double discountTotal) {
            this.subtotal = subtotal;
            this.shippingTotal = shippingTotal;
            this.discountTotal = discountTotal;
            this.grandTotal = subtotal + shippingTotal;
        }
    }

    public CartSummary computeSummary(Users user) {
        List<Cart> cartItems = cartRepo.findAllByUser(user);
        double subtotal = 0;
        double shipping = 0;
        double discount = 0;
        for (Cart cart : cartItems) {
            subtotal += cart.getFinalPrice();
            shipping += cart.getProduct().getShippingCharge();
            double sellingPrice = cart.getProduct().getSellingPrice();
            discount += (sellingPrice - cart.getProductPrice()) * cart.getQuantity();
        }
        return new CartSummary(subtotal, shipping, discount);
    }

    public void addToCart(String color, String size, int qty, Users user, long productId) {

        Products product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product Not Found"));

        if (product.getQuantity() < qty) {
            throw new RuntimeException("Insufficient Stock");
        }

        Optional<Cart> existing = cartRepo.findByUserAndProductAndColorAndSize(user, product, color, size);

        if (existing.isPresent()) {

            Cart cart = existing.get();

            int newQty = cart.getQuantity() + qty;

            if (newQty > product.getQuantity()) {
                throw new RuntimeException("Stock Limit Exceeded");
            }

            cart.setQuantity(newQty);

            cart.setFinalPrice(newQty * product.getFinalprice());

            cartRepo.save(cart);

        } else {

            Cart cart = new Cart();

            cart.setUser(user);

            cart.setProduct(product);

            cart.setColor(color);

            cart.setSize(size);

            cart.setQuantity(qty);

            cart.setProductPrice(product.getFinalprice());

            cart.setFinalPrice(qty * product.getFinalprice());

            cartRepo.save(cart);

        }

    }

    private Cart loadOwnedCartItem(Long cartId, Users user) {
        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart Item Not Found"));

        if (user == null || cart.getUser() == null || cart.getUser().getId() != user.getId()) {
            // Ownership check: prevents one customer from mutating another
            // customer's cart line by guessing/incrementing the cart id (IDOR).
            throw new SecurityException("You do not have permission to modify this cart item");
        }
        return cart;
    }

    public void increaseQuantity(Long cartId, Users user) {

        Cart cart = loadOwnedCartItem(cartId, user);

        Products product = cart.getProduct();

        if (cart.getQuantity() >= product.getQuantity()) {
            throw new RuntimeException("No more stock available for this item");
        }

        cart.setQuantity(cart.getQuantity() + 1);

        cart.setFinalPrice(cart.getQuantity() * cart.getProductPrice());

        cartRepo.save(cart);
    }

    public void decreaseQuantity(Long cartId, Users user) {

        Cart cart = loadOwnedCartItem(cartId, user);

        if (cart.getQuantity() > 1) {

            cart.setQuantity(cart.getQuantity() - 1);

            cart.setFinalPrice(cart.getQuantity() * cart.getProductPrice());

            cartRepo.save(cart);

        }

    }

    public void removeItem(Long cartId, Users user) {
        Cart cart = loadOwnedCartItem(cartId, user);
        cartRepo.delete(cart);
    }

}
