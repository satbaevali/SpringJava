package com.example.sis2.catalog.infra;

import com.example.sis2.catalog.domain.Product;
import com.example.sis2.catalog.domain.ProductRepository;
import com.example.sis2.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaProductRepositoryAdapter implements ProductRepository {

    private final SpringDataProductRepository jpaProductRepository;
    private final SpringDataCategoryRepository jpaCategoryRepository;
    private final ProductEntityMapper mapper;

    public JpaProductRepositoryAdapter(
            SpringDataProductRepository jpaProductRepository,
            SpringDataCategoryRepository jpaCategoryRepository,
            ProductEntityMapper mapper) {
        this.jpaProductRepository = jpaProductRepository;
        this.jpaCategoryRepository = jpaCategoryRepository;
        this.mapper = mapper;
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return jpaProductRepository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Product> findByCategoryId(Long categoryId, Pageable pageable) {
        return jpaProductRepository.findByCategoryId(categoryId, pageable).map(mapper::toDomain);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return jpaProductRepository.findByIdWithCategory(id).map(mapper::toDomain);
    }

    @Override
    public Product save(Product product) {
        CategoryEntity categoryEntity = jpaCategoryRepository.findById(product.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Категория с ID " + product.getCategoryId() + " не найдена"));

        ProductEntity entity;
        if (product.getId() != null) {
            entity = jpaProductRepository.findByIdWithCategory(product.getId())
                    .orElseGet(() -> mapper.toEntity(product, categoryEntity));
            entity.setName(product.getName());
            entity.setPrice(product.getPrice().amount());
            entity.setStock(product.getStock());
            entity.setCategory(categoryEntity);
        } else {
            entity = new ProductEntity(
                    null,
                    product.getName(),
                    product.getPrice().amount(),
                    product.getStock(),
                    categoryEntity,
                    null
            );
        }
        ProductEntity saved = jpaProductRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaProductRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaProductRepository.existsById(id);
    }
}
