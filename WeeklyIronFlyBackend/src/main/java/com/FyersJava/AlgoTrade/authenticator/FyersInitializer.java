package com.FyersJava.AlgoTrade.authenticator;

import com.FyersJava.AlgoTrade.App;
import com.FyersJava.AlgoTrade.PythonCaller;
import com.FyersJava.AlgoTrade.utility.AccessToken;

import com.tts.in.model.FyersClass;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class FyersInitializer {

    @PostConstruct
    public void init() {
        FyersClass fyersClass = FyersClass.getInstance();
        fyersClass.clientId = "CDHLDJLQHS-100";  // or read from properties

        fyersClass.accessToken = AccessToken.readAccessToken();

        App app = new App();
        Boolean isAccessTokenValid = app.GetProfile(fyersClass);

        if (!isAccessTokenValid) {
            fyersClass.accessToken = PythonCaller.accessToken();

            if (!app.GetProfile(fyersClass)) {
                throw new RuntimeException("Unable to validate access token");
            }
        }

        System.out.println("✅ Fyers initialized successfully");
    }
}
