package com.wipro.product_service.service.impl;

import com.wipro.product_service.exception.PermissionDeniedException;
import com.wipro.product_service.exception.ResourceNotFoundException;
import com.wipro.product_service.exception.ResourceServiceException;
import com.wipro.product_service.model.Category;
import com.wipro.product_service.model.CategoryAction;
import com.wipro.product_service.repository.CategoryRepository;
import com.wipro.product_service.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    PermissionService permissionService;

    @Override
    public Category createCategory(Category category, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        if (!permissionService.hasPermission(request, String.valueOf(CategoryAction.CREATE_CATEGORY))) {
            throw new PermissionDeniedException();
        }

        category.setId(UUID.randomUUID().toString());
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        category.setCreatedBy(userId);

        try {
            return this.categoryRepository.save(category);
        } catch (Exception e) {
            throw new ResourceServiceException("Failed to create category.");
        }
    }

    @Override
    public Category getCategoryById(String id) throws ResourceNotFoundException {
        return this.categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found with id :" + id));
    }

    @Override
    public List<Category> getAllCategories() {
        return this.categoryRepository.findAll();
    }

    @Override
    public Category updateCategory(Category category, String id) {

        Optional<Category> optionalCategory = categoryRepository.findById(id);
        if(optionalCategory.isEmpty()){
            throw new ResourceServiceException("Category with id: "+id+" not found");
        }
        Category existingCategory = optionalCategory.get();
        if (category.getImageUrl() != null) {
            existingCategory.setImageUrl(category.getImageUrl());
        }
        if(category.getTitle()!=null){
            existingCategory.setTitle(category.getTitle());
        }
        existingCategory.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(existingCategory);
    }

    @Override
    public void deleteCategory(String id) {
        Optional<Category> optionalCategory = categoryRepository.findById(id);
        if(optionalCategory.isEmpty()){
            throw new ResourceServiceException("Category with id: "+id+" not found");
        }
        this.categoryRepository.delete(optionalCategory.get());
    }
}