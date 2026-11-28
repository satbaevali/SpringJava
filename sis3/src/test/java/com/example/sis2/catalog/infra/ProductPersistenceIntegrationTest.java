package com.example.sis2.catalog.infra;

import com.example.sis2.catalog.domain.Category;
import com.example.sis2.catalog.domain.Money;
import com.example.sis2.catalog.domain.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaProductRepositoryAdapter.class,
        JpaCategoryRepositoryAdapter.class,
        ProductEntityMapper.class,
        CategoryEntityMapper.class
})
class ProductPersistenceIntegrationTest {

    @Autowired
    private JpaProductRepositoryAdapter productAdapter;

    @Autowired
    private JpaCategoryRepositoryAdapter categoryAdapter;

    @Test
    void flywaySeedDataIsLoaded() {
        assertThat(categoryAdapter.findAll()).hasSizeGreaterThanOrEqualTo(2);
        Page<Product> page = productAdapter.findAll(PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(5);
    }

    @Test
    void findByCategoryIdWithPaginationAndEntityGraph() {
        Category electronics = categoryAdapter.findByName("Электроника").orElseThrow();

        Page<Product> page = productAdapter.findByCategoryId(
                electronics.getId(),
                PageRequest.of(0, 2, Sort.by("price").descending())
        );

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(3);
        assertThat(page.getContent().get(0).getCategoryName()).isEqualTo("Электроника");
    }

    @Test
    void findByIdWithCategoryJoinFetch() {
        Product first = productAdapter.findAll(PageRequest.of(0, 1)).getContent().get(0);

        Optional<Product> loaded = productAdapter.findById(first.getId());
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getCategoryName()).isNotBlank();
    }

    @Test
    void saveAndUpdateDomainProductViaAdapter() {
        Category books = categoryAdapter.findByName("Книги").orElseThrow();
        Product newBook = Product.createNew("Domain-Driven Design (Eric Evans)", Money.of("55.00"), 15, books);

        Product saved = productAdapter.save(newBook);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getVersion()).isEqualTo(0L);

        saved.reduceStock(5);
        Product updated = productAdapter.save(saved);
        assertThat(updated.getStock()).isEqualTo(10);
        assertThat(updated.getVersion()).isEqualTo(1L);
    }
}
