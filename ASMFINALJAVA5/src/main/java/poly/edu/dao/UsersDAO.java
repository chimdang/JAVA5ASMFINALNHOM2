package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface UsersDAO extends JpaRepository<Users, Integer> {
	Users findByMail(String mail);
	List<Users> findByMailContaining(String keyword);
}

