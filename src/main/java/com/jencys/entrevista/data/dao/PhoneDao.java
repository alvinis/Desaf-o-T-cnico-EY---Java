package com.jencys.entrevista.data.dao;

import com.jencys.entrevista.data.entity.Phone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneDao extends JpaRepository<Phone, Long> {
}
