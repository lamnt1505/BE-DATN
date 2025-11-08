package com.vnpt.mini_project_java.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "category", schema = "public")
@Getter
@Setter
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryID;

    @Column(name = "categoryName")
    private String categoryName;

    @JsonManagedReference
    @JsonIgnore
    @JsonBackReference
    @OneToMany(mappedBy ="category", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Product> products = new HashSet<>();

    @Override
    public String toString() {
        return "Category{" +
                "categoryID=" + categoryID +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }
}
