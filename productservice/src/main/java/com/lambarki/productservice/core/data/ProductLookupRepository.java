package com.lambarki.productservice.core.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductLookupRepository extends JpaRepository<ProductLookUpEntity, String> {
    ProductLookUpEntity findByProductIdOrTitle(String productId, String title);
}
