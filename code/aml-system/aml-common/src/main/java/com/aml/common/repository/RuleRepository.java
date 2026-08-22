package com.aml.common.repository;
import com.aml.common.entity.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RuleRepository extends JpaRepository<Rule, String> {
}
