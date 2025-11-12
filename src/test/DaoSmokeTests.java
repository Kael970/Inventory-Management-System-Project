package test;

import dao.ProductDAO;
import dao.UserDAO;
import dao.SaleDAO;
import dao.RequestDAO;
import models.Product;
import models.User;
import models.Sale;
import models.Request;
import utils.Logger;

import java.sql.Date;
import java.util.List;

/**
 * Very lightweight smoke tests for DAO operations.
 * Run manually to verify core CRUD flows. Not a full JUnit test (no external deps).
 */
public class DaoSmokeTests {
    public static void main(String[] args) {
        Logger.info("Starting DAO smoke tests...");
        testUserDAO();
        testProductDAO();
        testSaleDAO();
        testRequestDAO();
        Logger.info("DAO smoke tests completed.");
    }

    private static void testUserDAO() {
        UserDAO userDAO = new UserDAO();
        User u = new User("smoke_user","pass123","Smoke Test User","Staff");
        boolean created = userDAO.createUser(u);
        Logger.info("User create result: " + created);
        List<User> users = userDAO.getAllUsers();
        Logger.info("Total users fetched: " + users.size());
    }

    private static void testProductDAO() {
        ProductDAO productDAO = new ProductDAO();
        Product p = new Product();
        p.setProductName("Smoke Product");
        p.setBuyingPrice(10.0);
        p.setSellingPrice(15.0);
        p.setStockQuantity(50);
        p.setThresholdValue(5);
        p.setExpiryDate(Date.valueOf(java.time.LocalDate.now().plusDays(30)));
        boolean created = productDAO.createProduct(p);
        Logger.info("Product create result: " + created);
        List<Product> list = productDAO.getAllProducts();
        Logger.info("Total products fetched: " + list.size());
    }

    private static void testSaleDAO() {
        ProductDAO productDAO = new ProductDAO();
        List<Product> products = productDAO.getAllProducts();
        if (products.isEmpty()) {
            Logger.warn("No products available to create sale.");
            return;
        }
        Product first = products.get(0);
        SaleDAO saleDAO = new SaleDAO();
        Sale sale = new Sale(first.getProductId(), first.getProductName(), 1, first.getSellingPrice(), 0);
        sale.setUnitPrice(first.getSellingPrice());
        sale.setTotalPrice(sale.getUnitPrice() * sale.getQuantity());
        int id = saleDAO.createSaleWithStockCheck(sale);
        Logger.info("Sale created id: " + id);
        Logger.info("Sales count (7d): " + saleDAO.getLast7DaysSalesCount());
    }

    private static void testRequestDAO() {
        ProductDAO productDAO = new ProductDAO();
        List<Product> products = productDAO.getAllProducts();
        if (products.isEmpty()) {
            Logger.warn("No products available to create request.");
            return;
        }
        Product first = products.get(0);
        Request r = new Request();
        r.setProductId(first.getProductId());
        r.setProductName(first.getProductName());
        r.setRequestedQuantity(10);
        r.setRequestedBy("Smoke Tester");
        r.setStatus("Pending");
        RequestDAO requestDAO = new RequestDAO();
        boolean created = requestDAO.createRequest(r);
        Logger.info("Request create result: " + created);
        Logger.info("Pending requests count: " + requestDAO.getRequestsCount());
    }
}

