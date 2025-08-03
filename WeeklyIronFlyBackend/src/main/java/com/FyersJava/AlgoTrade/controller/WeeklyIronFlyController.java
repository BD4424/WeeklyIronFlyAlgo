package com.FyersJava.AlgoTrade.controller;

import com.FyersJava.AlgoTrade.strategy.LiveData;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weeklyIronFly")
public class WeeklyIronFlyController {

    public ResponseEntity<String> startWeeklyIronFly(){

        LiveData liveData = new LiveData();
        liveData.start();

        return new ResponseEntity<>("Strategy execution started", HttpStatus.CREATED);
    }
}
