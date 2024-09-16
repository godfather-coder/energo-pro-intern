package com.example.mssqll.models;


import jakarta.persistence.*;

@Entity
@Table(name = "business_units")
public class BusinessUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "unit_number")
    private Integer unitNumber;

    @Column(name = "name")
    private String name;

    @Column(name = "unit_type_key")
    private Integer unitTypeKey;

    @Column(name = "parent_id")
    private Long parentId;
}