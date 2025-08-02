package com.FyersJava.AlgoTrade.repo;


import com.FyersJava.AlgoTrade.entity.TradeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeLogRepo extends JpaRepository<TradeLog, Integer> {
    @Query("SELECT t FROM TradeLog t WHERE t.isActive = true AND t.strikePrice = :strikePrice")
    List<TradeLog> findActiveByStrikePrice(@Param("strikePrice") String strikePrice);

}

