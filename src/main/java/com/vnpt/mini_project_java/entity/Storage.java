package com.vnpt.mini_project_java.entity;

import java.time.LocalDate;
import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@DynamicUpdate
@Table(name = "storage", schema = "public")
@Getter
@Setter
public class Storage {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "import_id")
    private Long idImport;
	
	@Column(columnDefinition = "nvarchar(150)")
	private String users;

	private int quantity;

	@Column(name = "createDate")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate createDate;

	@Column(name = "updateDate")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate updateDate;
	
	@JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
	@JsonIgnore
	@JoinColumn(name = "product_id")
	private Product product;
	
	public Storage() {
		
	}
	
	public Storage(Long idImport, String users, int quantity, LocalDate createDate, LocalDate updateDate, Product product) {
		this.idImport = idImport;
		this.users = users;
		this.quantity = quantity;
		this.createDate = createDate;
		this.updateDate = updateDate;
		this.product = product;
	}
}
