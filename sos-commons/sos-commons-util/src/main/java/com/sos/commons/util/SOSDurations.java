package com.sos.commons.util;

import java.util.ArrayList;
import java.util.Collections;


public class SOSDurations {
    private ArrayList<SOSDuration> listOfDurations;
    private Integer minimumDuration=0;
    private Integer confidence=10;
    
    
    public SOSDurations() {
        super();
    }    
    
    public SOSDurations(Integer mininmumDuration) {
        super();
        this.minimumDuration = mininmumDuration;
    }

    public void add(SOSDuration duration){
        if (listOfDurations == null){
            listOfDurations = new ArrayList<SOSDuration>();
        }
        if (duration.getDurationInMillis() >= minimumDuration){
            listOfDurations.add(duration);
        }
    }
    
    public void setConfidence(Integer confidence) {
        this.confidence = confidence;
    }

    public Long average(){
        if (listOfDurations == null){
            return 0L;
        }
        
        Collections.sort(listOfDurations);
        int from = (listOfDurations.size() * confidence / 100);
        int to = listOfDurations.size() - from;
        if (to < from){
            from = 0;
            to = listOfDurations.size();
        }
        long sum=0;
        long count=0;
        for (int i = from;i < to;i++){
            SOSDuration duration = listOfDurations.get(i);
            sum = sum + duration.getDurationInMillis();
            count = count + 1;
        }
        return (sum / count) ;
    }

    public int size(){
        if (listOfDurations == null){
            return 0;
        }
        return listOfDurations.size();
    }
}
