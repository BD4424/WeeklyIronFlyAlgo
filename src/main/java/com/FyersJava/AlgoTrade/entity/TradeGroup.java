package com.FyersJava.AlgoTrade.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "trade_groups", schema = "algo_trades")
@Getter
@Setter
public class TradeGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String strategy; // e.g. IRON_FLY
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Double netPnl;
    private Boolean isActive;
    private String exitType;
    private Double stopLoss;
    private Double tradeEntryIndex;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<TradeLog> legs;
}
