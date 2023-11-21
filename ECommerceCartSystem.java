package eistudy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

interface DiscountStrategy {
    double applyDiscount(double price, int quantity);
}

class PercentageDiscount implements DiscountStrategy {
    private double percentage;

    PercentageDiscount(double percentage) {
        this.percentage = percentage;
    }

    @Override
    public double applyDiscount(double price, int quantity) {
        return price * quantity * (1 - percentage / 100);
    }
}

class BuyOneGetOneFreeDiscount implements DiscountStrategy {
    @Override
    public double applyDiscount(double price, int quantity) {
        int numberOfDiscountedItems = quantity / 2;
        int remainingItems = quantity % 2;
        return price * (numberOfDiscountedItems + remainingItems);
    }
}

class Product implements Cloneable {
    private String name;
    private double price;
    private boolean available;
    private int availableCount;

    Product() {
        this.name = "";
        this.price = 0;
        this.available = false;
        this.availableCount = 0;
    }

    Product(String name, double price, int availableCount) {
        this.name = name;
        this.price = price;
        this.available = (availableCount > 0);
        this.availableCount = availableCount;
    }

    @Override
    public String toString() {
        return "{name: \"" + name + "\", price: " + price + ", available " + available+ "}";
    }

    @Override
    protected Product clone() {
        try {
            return (Product) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    void display() {
        System.out.println(this);
    }

    boolean isAvailable() {
        return available;
    }

    int getAvailableCount() {
        return availableCount;
    }

    void setAvailableCount(int availableCount) {
        this.availableCount = availableCount;
        this.available = (availableCount > 0);
    }

    String getName() {
        return name;
    }

    double getPrice() {
        return price;
    }

    void decreaseAvailableCount(int quantity) {
        if (availableCount >= quantity) {
            availableCount -= quantity;
            available = (availableCount > 0);
        }
    }
}

class Laptop extends Product {
    Laptop(int availableCount) {
        super("Laptop", 1000, availableCount);
    }
}

class Headphones extends Product {
    Headphones(int availableCount) {
        super("Headphones", 50, availableCount);
    }
}

class BuyOneGetOne {
    private List<Product> eligibleProducts;

    BuyOneGetOne() {
        this.eligibleProducts = new ArrayList<>();
    }

    void addEligibleProduct(Product product) {
        eligibleProducts.add(product);
    }

    List<Product> getEligibleProducts() {
        return eligibleProducts;
    }

    boolean isProductEligible(Product product) {
        for (Product p : eligibleProducts) {
            if (p.getName().compareTo(product.getName()) == 0) {
                return true;
            }
        }
        return false;
    }
}

class Cart {
    private Map<String, CartItem> items = new HashMap<>();
    private DiscountStrategy discountStrategy;
    private BuyOneGetOne buyOneGetOne = new BuyOneGetOne();

    void addEligibleProductForBuyOneGetOne(Product product) {
        buyOneGetOne.addEligibleProduct(product);
    }

    void setDiscountStrategy(DiscountStrategy discountStrategy) {

        if (this.discountStrategy == null) {
            this.discountStrategy = discountStrategy;
            System.out.println("Discount applied successfully.");
        } else {
            System.out.println("Discount is already applied. To change the discount, use option 6.");
        }
    }

    void changeDiscountStrategy(DiscountStrategy discountStrategy) {
        if (this.discountStrategy instanceof BuyOneGetOneFreeDiscount) {
            System.out.println("Cannot change discount type. Buy One Get One Free discount is already applied.");
            return;
        }

        this.discountStrategy = discountStrategy;
        System.out.println("Discount type changed successfully.");
    }

    void displayEligibleProductsForBuyOneGetOne() {
        System.out.println("Eligible Products for Buy One Get One Free Discount:");
        for (Product product : buyOneGetOne.getEligibleProducts()) {
            product.display();
        }
    }

    boolean hasBuyOneGetOneItems() {
        return !buyOneGetOne.getEligibleProducts().isEmpty();
    }

    DiscountStrategy getDiscountStrategy() {
        return discountStrategy;
    }

    void addItem(Product product, int quantity) {
        if (product.isAvailable()) {
            try {
                Product clonedProduct = product.clone();
                clonedProduct.decreaseAvailableCount(quantity);
                items.compute(product.getName(), (key, existingItem) -> {
                    if (existingItem == null) {
                        return new CartItem(clonedProduct, quantity);
                    } else {
                        existingItem.setQuantity(existingItem.getQuantity() + quantity);
                        return existingItem;
                    }
                });
            } catch (Exception e) {
                System.out.println("Error cloning product: " + e.getMessage());
            }
        } else {
            System.out.println("Not enough quantity available for " + product.getName() +
                    ". Available quantity: " + product.getAvailableCount());
        }
    }

    void updateQuantity(String productName, int newQuantity) {
        CartItem item = items.get(productName);
        if (item != null) {
            Product product = item.getProduct();
            int availableCount = product.getAvailableCount() + item.getQuantity();
            if (newQuantity > availableCount) {
                System.out.println("Not enough quantity available for " + product.getName() +
                        ". Available quantity: " + availableCount);
            } else {
                item.setQuantity(newQuantity);
                product.decreaseAvailableCount(newQuantity - availableCount);
            }
        } else {
            System.out.println("The item is not present in the cart or available items\n");
        }
    }

    void removeItem(String productName) {
        items.remove(productName);
    }

    
    double calculateTotalBill() {
        double totalBill = 0;

        for (CartItem item : items.values()) {
            double price = item.getProduct().getPrice();
            int quantity = item.getQuantity();
            if (discountStrategy instanceof BuyOneGetOneFreeDiscount && buyOneGetOne.isProductEligible(item.getProduct())) {
                totalBill += ((price * quantity) / 2);
            } else if (discountStrategy instanceof PercentageDiscount) {
                totalBill += discountStrategy.applyDiscount(price, quantity);
            } else {
                totalBill += price * quantity;
            }
        }
        return totalBill;
    }

    void displayCart() {
        System.out.print("Cart Items: You have ");

        for (CartItem item : items.values()) {
            System.out.print(item.getQuantity() + " " + item.getProduct().getName() + ", ");
        }
        System.out.println("in your cart.");
        System.out.println("Total Bill: Your total bill is $" + calculateTotalBill() + ".");
    }
}

class CartItem {
    private Product product;
    private int quantity;

    CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    Product getProduct() {
        return product;
    }

    int getQuantity() {
        return quantity;
    }

    void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

public class ECommerceCartSystem {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Laptop laptop = new Laptop(10);
        Headphones headphones = new Headphones(10);

        Cart cart = new Cart();
        cart.addEligibleProductForBuyOneGetOne(laptop);

        while (true) {
            System.out.println("\n----- Menu -----");
            System.out.println("1. Display Products");
            System.out.println("2. Add to Cart");
            System.out.println("3. Update Quantity in Cart");
            System.out.println("4. Remove from Cart");
            System.out.println("5. Display Cart");
            System.out.println("6. Apply Discount");
            System.out.println("7. Change Discount Strategy");
            System.out.println("8. Exit");
            System.out.print("Enter your choice: ");

            try {
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        System.out.println("Available Products:");
                        laptop.display();
                        headphones.display();
                        break;

                    case 2:
                        System.out.println("Available Products:");
                        laptop.display();
                        headphones.display();

                        System.out.print("Enter the product name to add to the cart: ");
                        String productName = scanner.nextLine();

                        System.out.print("Enter the quantity: ");
                        int quantity = scanner.nextInt();

                        if ("Laptop".equalsIgnoreCase(productName)) {
                            cart.addItem(laptop, quantity);
                        } else if ("Headphones".equalsIgnoreCase(productName)) {
                            cart.addItem(headphones, quantity);
                        } else {
                            System.out.println("Invalid product name.");
                        }
                        break;

                    case 3:
                        System.out.print("Enter the product name to update quantity: ");
                        String updateProductName = scanner.nextLine();

                        System.out.print("Enter the new quantity: ");
                        int newQuantity = scanner.nextInt();

                        cart.updateQuantity(updateProductName, newQuantity);
                        break;

                    case 4:
                        System.out.print("Enter the product name to remove from the cart: ");
                        String removeProductName = scanner.nextLine();

                        cart.removeItem(removeProductName);
                        break;

                    case 5:
                        cart.displayCart();
                        break;

                    case 6:
                        if (cart.getDiscountStrategy() == null) {
                            System.out.println("No discount applied. Select a discount type:");
                            System.out.println("1. Percentage Discount");
                            System.out.println("2. Buy One Get One Free Discount");
                            System.out.print("Enter your choice: ");

                            int discountTypeChoice = scanner.nextInt();
                            scanner.nextLine();

                            switch (discountTypeChoice) {
                                case 1:
                                    cart.setDiscountStrategy(new PercentageDiscount(5));
                                    break;

                                case 2:
                                    if (cart.hasBuyOneGetOneItems()) {
                                        cart.setDiscountStrategy(new BuyOneGetOneFreeDiscount());
                                    } else {
                                        System.out.println("No products eligible for Buy One Get One Free Discount.");
                                      
                                    }
                                    break;

                                default:
                                    System.out.println("Invalid discount type choice.");
                                    break;
                            }
                        } else {
                            System.out.println("Discount is already applied. To change the discount, use option 7.");
                        }
                        break;

                    case 7:
                        System.out.println("Select Discount Strategy:");
                        System.out.println("1. Percentage Discount");
                        System.out.println("2. Buy One Get One Free Discount");
                        System.out.print("Enter your choice: ");

                        int discountChoice = scanner.nextInt();
                        scanner.nextLine();

                        switch (discountChoice) {
                            case 1:
                                cart.changeDiscountStrategy(new PercentageDiscount(5));
                                break;

                            case 2:
                                if (cart.hasBuyOneGetOneItems()) {
                                    cart.changeDiscountStrategy(new BuyOneGetOneFreeDiscount());
                                } else {
                                    System.out.println("No products eligible for Buy One Get One Free Discount.");
                                    // Switch to Percentage Discount
                                    cart.setDiscountStrategy(new PercentageDiscount(5));
                                }
                                break;

                            default:
                                System.out.println("Invalid discount choice.");
                                break;
                        }
                        break;

                    case 8:
                        System.out.println("Exiting the program. Thank you!");
                        System.exit(0);
                        break;

                    default:
                        System.out.println("Invalid choice. Please enter a valid option.");
                        break;
                }
            } catch (java.util.InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.nextLine(); 
            }
        }
    }
}
