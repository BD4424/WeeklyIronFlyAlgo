package com.FyersJava.AlgoTrade.repo;


import com.FyersJava.AlgoTrade.entity.TradeGroup;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeGroupRepo extends JpaRepository<TradeGroup, Integer> {

    @Query("SELECT tg FROM TradeGroup tg WHERE tg.isActive = true")
    @EntityGraph(attributePaths = "legs")
    List<TradeGroup> findActiveTradeGroups();

}
