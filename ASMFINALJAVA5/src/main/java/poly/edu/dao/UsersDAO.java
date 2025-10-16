package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.*;

public interface UsersDAO extends JpaRepository<Users, Integer> {
	Users findByMail(String mail);
}

