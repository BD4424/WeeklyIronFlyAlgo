package com.FyersJava.AlgoTrade.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "trade_logs_fyers", schema = "algo_trades")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class TradeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "entry_time")
    private LocalDateTime entryTime;

    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    @Column(name = "buy_index")
    private Double buyIndex;

    @Column(name = "target_index")
    private Double targetIndex;

    @Column(name = "sell_index")
    private Double sellIndex;

    @Column(name = "stop_loss")
    private Double stopLoss;

    @Column(name = "entry_premium")
    private Double entryPremium;

    @Column(name = "exit_premium")
    private Double exitPremium;

    @Column(name = "strike_price")
    private String strikePrice;

    @Column(name = "net_pnl")
    private Double netPnl;

    @Column(name = "strategy", length = 100)
    private String strategy;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "exit_type")
    private String exitType;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private TradeGroup group;

}