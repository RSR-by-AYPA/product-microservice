package com.rsr.product_microservice.unitTests;

import com.rsr.product_microservice.ProductFactory;
import com.rsr.product_microservice.ProductHelper;
import com.rsr.product_microservice.core.domain.model.Product;
import com.rsr.product_microservice.core.domain.service.impl.ProductService;
import com.rsr.product_microservice.core.domain.service.interfaces.IProductRepository;
import com.rsr.product_microservice.core.domain.service.interfaces.IProductService;
import com.rsr.product_microservice.port.product.user.exceptions.UnknownProductIdException;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class ProductServiceTests {

    private IProductRepository productRepository;

    private IProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = mock(IProductRepository.class);
        productService = new ProductService(productRepository);
    }

    @Nested
    @DisplayName("Test cases for creating a product")
    class CreateProductTests {

        @Test
        @DisplayName("Check if valid Product is created - White Box Test")
        void createValidProductTest() {
            Product product = ProductFactory.getExampleValidProduct();
            productService.createProduct(product);
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("create Product with no name (bad case) - White Box Test")
        void createProductWithInvalidNameTest() {
            Product product = ProductFactory.getExampleValidProduct();
            product.setName("");
            Assertions.assertThrows(IllegalArgumentException.class, () -> productService.createProduct(product));
            verify(productRepository, never()).save(product);
        }

        @Test
        @DisplayName("create Product with negative price (bad case) - White Box Test")
        void createProductWithNegativePriceTest() {
            Product product = ProductFactory.getExampleValidProduct();
            product.setPriceInEuro(-2.5);
            Assertions.assertThrows(IllegalArgumentException.class, () -> productService.createProduct(product));
            verify(productRepository, never()).save(product);
        }

        @Test
        @DisplayName("create Product with more than 2 decimal places (bad case) - White Box Test")
        void createProductWithWrongPriceFormatTest() {
            Product product = ProductFactory.getExampleValidProduct();
            product.setPriceInEuro(2.333);
            Assertions.assertThrows(IllegalArgumentException.class, () -> productService.createProduct(product));
            verify(productRepository, never()).save(product);
        }

        @Test
        @DisplayName("create Product with negative amount (bad case) - White Box Test")
        void createProductWithNegativeAmountTest() {
            Product product = ProductFactory.getExampleValidProduct();
            product.setAmount(-3);
            Assertions.assertThrows(IllegalArgumentException.class, () -> productService.createProduct(product));
            verify(productRepository, never()).save(product);
        }

        @Test
        @DisplayName("create null-Product (bad case) - White Box Test")
        void createNullProductTest() {
            Product product = null;
            Assertions.assertThrows(IllegalArgumentException.class, () -> productService.createProduct(product));
            verify(productRepository, never()).save(product);
        }

    }

    @Nested
    @DisplayName("Test cases for getting a product or multiple")
    class GetProductTests {

        @Test
        @DisplayName("Get All Products - White Box")
        void getAllProductsTest() {
            Product productStone = ProductFactory.getExampleValidProduct();
            productStone.setName("Stone");

            Product productRock = ProductFactory.getExampleValidProduct();
            productRock.setName("Rock");

            Product productGemstone =  ProductFactory.getExampleValidProduct();
            productGemstone.setName("Gemstone");

            List<Product> products = Arrays.asList(
                    productRock, productStone, productGemstone
            );

            when(productRepository.findAll()).thenReturn(products);

            List<Product> returnedProducts = productService.getAllProducts();
            Assertions.assertTrue(ProductHelper.compareProductListsByName(products, returnedProducts));
        }

        @Test
        @DisplayName("Get one Product by Id - White Box")
        void getOneProductByIdTest() throws UnknownProductIdException {
            Product product = ProductFactory.getExampleValidProduct();

            //Mock Setup
            UUID productId = UUID.randomUUID();
            Product productWithId = ProductFactory.getExampleValidProduct();
            productWithId.setId(productId);
            when(productRepository.save(product)).thenReturn(productWithId);
            when(productRepository.findById(productId)).thenReturn(Optional.of(productWithId));

            //saving Product
            Product createdProduct = productService.createProduct(product);

            //getting Product by ID
            Product returnedProduct = productService.getProductById(createdProduct.getId());
            Assertions.assertEquals(returnedProduct.getId(), productId);
        }

        @Test
        @DisplayName("Get Product by id with wrong id")
        void getProductByWrongIdTest() {
            Product product = ProductFactory.getExampleValidProduct();

            //Mock Setup
            UUID productId = UUID.randomUUID();
            Product productWithId = ProductFactory.getExampleValidProduct();
            productWithId.setId(productId);
            when(productRepository.save(product)).thenReturn(productWithId);
            when(productRepository.findById(productId)).thenReturn(Optional.of(productWithId));

            //saving Product
            Product createdProduct = productService.createProduct(product);

            //getting Product by wrong ID
            Assertions.assertThrows(UnknownProductIdException.class, () -> productService.getProductById(UUID.randomUUID()));

        }
    }

    @Nested
    @DisplayName("Test Cases for deleting a product with the ProductService")
    class DeleteProductTests {

        @Test
        @DisplayName("Delete existing product properly")
        void deleteProductProperlyTest() {
            UUID productId = UUID.randomUUID();

            when(productRepository.findById(productId)).thenReturn(Optional.of(new Product()));

            productService.deleteProduct(productId);
            verify(productRepository, times(1)).deleteById(productId);
        }
    }

    @Nested
    @DisplayName("Test Cases for updating a Product with the ProductService")
    class UpdateProductTests {

        @Test
        @DisplayName("Update existing Product properly")
        void updateProductProperlyTest() {
            Product updatedProduct = ProductFactory.getExampleValidProduct();
            UUID productId = UUID.randomUUID();
            updatedProduct.setId(productId);

            when(productRepository.findById(productId)).thenReturn(Optional.of(updatedProduct));
            when(productRepository.save(updatedProduct)).thenReturn(updatedProduct);

            productService.updateProduct(updatedProduct);

            verify(productRepository, times(1)).save(updatedProduct);
        }
    }
}
