package com.sos.joc.db;

public abstract class SOSFilter {

    private String sortMode = "asc";
    private String orderCriteria;

    public String getSortMode() {
        if (orderCriteria == null || "".equals(orderCriteria) || sortMode == null) {
            return "";
        } else {
            return " " + sortMode + " ";
        }
    }

    public void setSortMode(String sortMode) {
        this.sortMode = sortMode;
    }

    public String getOrderCriteria() {
        if (orderCriteria == null || "".equals(orderCriteria)) {
            return "";
        } else {
            return " order by " + orderCriteria + " ";
        }
    }

    public void setOrderCriteria(String orderCriteria) {
        this.orderCriteria = orderCriteria;
    }

}