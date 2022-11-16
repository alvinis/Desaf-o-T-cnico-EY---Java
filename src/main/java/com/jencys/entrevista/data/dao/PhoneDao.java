package com.leonel.entrevista.data.dao;

import com.leonel.entrevista.data.entity.Phone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneDao extends JpaRepository<Phone, Long> {
}
