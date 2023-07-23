package com.lambarki.productservice.core.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serial;
import java.io.Serializable;

@Data @AllArgsConstructor @NoArgsConstructor
@Entity
@Table(name = "productlookup")
public class ProductLookUpEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = -6468035270160012627L;

    @Id
    private String productId;
    @Column(unique = true)
    private String title;
}
