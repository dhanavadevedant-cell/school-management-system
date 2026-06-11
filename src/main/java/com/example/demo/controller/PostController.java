package com.example.demo.controller;

import com.example.demo.model.Post;
import com.example.demo.model.Comment;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "http://localhost:3000")
public class PostController {

    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private CommentRepository commentRepository;

    @GetMapping("/all")
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    @PostMapping("/add")
    public Post createPost(@RequestBody Post post) {
        return postRepository.save(post);
    }

    @PutMapping("/{id}/like")
    public ResponseEntity<Post> likePost(@PathVariable Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        post.setLikes(post.getLikes() + 1);
        return ResponseEntity.ok(postRepository.save(post));
    }

    @PutMapping("/{id}/dislike")
    public ResponseEntity<Post> dislikePost(@PathVariable Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        post.setDislikes(post.getDislikes() + 1);
        return ResponseEntity.ok(postRepository.save(post));
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<Post> addComment(@PathVariable Long id, @RequestBody Comment comment) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        comment.setPost(post);
        commentRepository.save(comment);
        return ResponseEntity.ok(post);
    }

    // ==========================================
    // --- NEWLY ADDED ADMIN ACTIONS ---
    // ==========================================

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        
        // This will delete the post. 
        // Note: If you face a foreign key constraint error here, make sure your Post model 
        // has CascadeType.ALL or CascadeType.REMOVE defined on its comments relationship list.
        postRepository.delete(post);
        
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/reset-reactions")
    public ResponseEntity<Post> resetReactions(@PathVariable Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        post.setLikes(0);
        post.setDislikes(0);
        return ResponseEntity.ok(postRepository.save(post));
    }
}