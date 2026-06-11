package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String text;
    private String author;

    @ManyToOne
    @JoinColumn(name = "post_id")
    @JsonIgnore // Prevents infinite loops when serializing to JSON
    private Post post;
}