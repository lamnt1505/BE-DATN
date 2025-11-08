package com.vnpt.mini_project_java.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trademark", schema = "public")
@Getter
@Setter
public class Trademark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_id")
    private Long tradeID;
    @Column(name = "trade_Name")
    private String tradeName;

    @JsonManagedReference
    @JsonIgnore
    @OneToMany(mappedBy ="trademark", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Product> products = new HashSet<>();
}
