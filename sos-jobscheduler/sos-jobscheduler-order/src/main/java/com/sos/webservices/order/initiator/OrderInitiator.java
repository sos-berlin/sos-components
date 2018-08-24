package com.sos.webservices.order.initiator;

import java.util.Timer;

public class OrderInitiator {

    private Timer orderInitiateTimer;


    public void go() {
        orderInitiateTimer = new Timer();
        orderInitiateTimer.schedule(new OrderInitiatorRunner(), 1000, 30000);
    }

 
    public static void main(String[] args) {
        OrderInitiator orderInitiator = new OrderInitiator();      
        orderInitiator.go();     
    }

}
