package com.example.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Message;
@Repository
public interface MessageDAO_jpa extends JpaRepository<Message, Integer> {

}
