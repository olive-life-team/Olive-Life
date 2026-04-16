package com.ecommerce.chatdemo.domain.cartitem.repository;

import com.ecommerce.chatdemo.domain.cart.entity.Cart;
import com.ecommerce.chatdemo.domain.cartitem.entity.CartItem;
import com.ecommerce.chatdemo.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    List<CartItem> findByCart(Cart cart);

    // N+1 방지
    // cart로 cartItem 목록 조회
    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.product WHERE ci.cart = :cart")
    List<CartItem> findByCartWithProduct(Cart cart);

    // N+1 방지
    // cartItemId로 해당 장바구니 + 사용자 + 상품 한 번에 조회 (Cart + member + product)
    @Query("SELECT ci FROM CartItem ci " +
            "JOIN FETCH ci.cart c " +
            "JOIN FETCH c.member " +
            "JOIN FETCH ci.product " +
            "WHERE ci.id = :cartItemId")
    Optional<CartItem> findByCartItemIdWithProductAndMember(@Param("cartItemId") Long cartItemId);

    // N+1 방지
    // CartItemId로 장바구니 + 사용자 조회 (Cart + member)
    @Query("SELECT ci FROM CartItem ci " +
            "JOIN FETCH ci.cart c " +
            "JOIN FETCH c.member " +
            "WHERE ci.id = :cartItemId")
    Optional<CartItem> findByCartItemWithCartAndMember(@Param("cartItemId") Long cartItemId);
}
