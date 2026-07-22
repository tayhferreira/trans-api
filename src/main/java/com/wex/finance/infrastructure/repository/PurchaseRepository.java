package com.wex.finance.infrastructure.repository;

import com.wex.finance.domain.entity.Purchase;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseRepository extends CrudRepository<Purchase, String> {
}
