package com.lambarki.productservice.query;

import com.lambarki.productservice.core.data.ProductEntity;
import com.lambarki.productservice.core.data.ProductsRepository;
import com.lambarki.productservice.query.rest.ProductRestModel;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductsQueryHandler {

    private ProductsRepository productsRepository;
    public ProductsQueryHandler(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @QueryHandler
    public List<ProductRestModel> findProducts(FindProductsQuery query) {
        List<ProductRestModel> productsRest = new ArrayList<>();

        List<ProductEntity> storedProducts = productsRepository.findAll();

        for(ProductEntity productEntity: storedProducts) {
            ProductRestModel productRestModel = new ProductRestModel();
            BeanUtils.copyProperties(productEntity, productRestModel);
            productsRest.add(productRestModel);
        }

        return productsRest;
    }
}
