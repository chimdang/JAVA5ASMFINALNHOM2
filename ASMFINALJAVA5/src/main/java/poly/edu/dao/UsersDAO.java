package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.*;
import java.util.*;

public interface UsersDAO extends JpaRepository<Users, Integer> {
	List<Users> findByMailContaining(String keyword);
}

