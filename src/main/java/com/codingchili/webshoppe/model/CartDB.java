package com.codingchili.webshoppe.model;

import com.codingchili.webshoppe.model.exception.CartStoreException;
import com.codingchili.webshoppe.model.exception.ProductStoreException;
import com.codingchili.webshoppe.model.exception.StoreException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Robin on 2015-09-30.
 * <p>
 * Implementation of CartStore using MySQL Database.
 */

class CartDB implements CartStore {

    @Override
    public void setCartItems(List<Product> products, Account account) throws CartStoreException {
        clearCart(account);
        try {
            Database.prepared(CartTable.AddToCart.QUERY, (connection, statement) -> {
                connection.setAutoCommit(false);

                for (Product product : products) {
                    statement.setInt(CartTable.AddToCart.IN.COUNT, product.getCount());
                    statement.setInt(CartTable.AddToCart.IN.OWNER, account.getId());
                    statement.setInt(CartTable.AddToCart.IN.PRODUCT, product.getId());
                    statement.execute();
                }
                connection.commit();
                return null;
            });
        } catch (SQLException e) {
            throw new CartStoreException(e);
        }
    }

    @Override
    public void setCartItems(Product product, Account account) throws CartStoreException {
        int productsInCart = getProductCountInCart(product, account);

        if (product.getCount() + productsInCart <= 0) {
            removeFromCart(product, account);
        } else if (productsInCart > 0) {
            updateCartCount(product, product.getCount() + productsInCart, account);
        } else {
            try {
                Database.prepared(CartTable.AddToCart.QUERY, (connection, statement) -> {
                    statement.setInt(CartTable.AddToCart.IN.COUNT, product.getCount() + productsInCart);

                    statement.setInt(CartTable.AddToCart.IN.OWNER, account.getId());
                    statement.setInt(CartTable.AddToCart.IN.PRODUCT, product.getId());
                    statement.execute();
                    return null;
                });
            } catch (SQLException e) {
                throw new CartStoreException(e);
            }
        }
    }

    private void updateCartCount(Product product, int count, Account account) throws CartStoreException {
        try {
            Database.prepared(CartTable.UpdateCartCount.QUERY, (connection, statement) -> {
                statement.setInt(CartTable.UpdateCartCount.IN.COUNT, count);
                statement.setInt(CartTable.UpdateCartCount.IN.PRODUCT, product.getId());
                statement.setInt(CartTable.UpdateCartCount.IN.OWNER, account.getId());
                statement.execute();
                return null;
            });
        } catch (SQLException e) {
            throw new CartStoreException(e);
        }
    }

    private int getProductCountInCart(Product product, Account account) throws CartStoreException {
        try {
            return Database.prepared(CartTable.ProductIdCount.QUERY, (connection, statement) -> {
                statement.setInt(CartTable.ProductIdCount.IN.OWNER, account.getId());
                statement.setInt(CartTable.ProductIdCount.IN.PRODUCT, product.getId());

                ResultSet result = statement.executeQuery();

                if (result.next()) {
                    return result.getInt(CartTable.ProductIdCount.OUT.COUNT);
                } else
                    return 0;
            });
        } catch (SQLException e) {
            throw new CartStoreException(e);
        }
    }

    @Override
    public void removeFromCart(Product product, Account account) throws CartStoreException {
        try {
            Database.prepared(CartTable.RemoveFromCart.QUERY, (connection, statement) -> {
                statement.setInt(CartTable.RemoveFromCart.IN.OWNER, account.getId());
                statement.setInt(CartTable.RemoveFromCart.IN.PRODUCT, product.getId());
                statement.execute();
                return null;
            });
        } catch (SQLException e) {
            throw new CartStoreException(e);
        }
    }

    @Override
    public void clearCart(Account account) throws CartStoreException {
        try {
            Database.prepared(CartTable.ClearCart.QUERY, (connection, statement) -> {
                statement.setInt(CartTable.ClearCart.IN.OWNER, account.getId());
                statement.execute();
                return null;
            });
        } catch (SQLException e) {
            throw new CartStoreException(e);
        }
    }

    @Override
    public int productCount(Account account) throws CartStoreException {
        try {
            return Database.prepared(CartTable.ProductCount.QUERY, (connection, statement) -> {
                statement.setInt(CartTable.ProductCount.IN.OWNER, account.getId());
                ResultSet result = statement.executeQuery();

                if (result.next()) {
                    return result.getInt(CartTable.ProductCount.OUT.COUNT);
                } else
                    return 0;
            });
        } catch (SQLException e) {
            throw new CartStoreException(e);
        }
    }

    @Override
    public Cart getCart(Account account) throws StoreException {
        try {
            return Database.prepared(CartTable.GetCart.QUERY, (connection, statement) -> {
                statement.setInt(CartTable.GetCart.IN.OWNER, account.getId());
                ResultSet result = statement.executeQuery();
                Cart cart =cartFromResult(result);
                cart.setOwner(account);
                return cart;
            });
        } catch (SQLException e) {
            throw new CartStoreException(e);
        }
    }

    private Cart cartFromResult(ResultSet result) throws SQLException, ProductStoreException {
        Cart cart = new Cart();

        while (result.next()) {
            Product product = new Product();

            product.setId(result.getInt(CartTable.GetCart.OUT.PRODUCT_ID));
            product.setName(result.getString(CartTable.GetCart.OUT.NAME));
            product.setCount(result.getInt(CartTable.GetCart.OUT.COUNT));
            product.setCost(result.getInt(CartTable.GetCart.OUT.COST));
            product.setImageId(result.getInt(CartTable.GetCart.OUT.IMAGE_ID));
            cart.addProduct(product);
        }

        return cart;
    }
}
