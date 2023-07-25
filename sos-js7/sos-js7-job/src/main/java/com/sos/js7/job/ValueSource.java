package com.sos.js7.job;

public class ValueSource {

    public enum ValueSourceType {

        JAVA("Resulting Arguments", "Resulting Argument"), ORDER("Order Variables", "Order Variable"), ORDER_OR_NODE(
                "Default Order Variables or Node Arguments", "Default Order Variable or Node Argument"), JOB("Arguments", "Argument"), JOB_ARGUMENT(
                        "Job Arguments", "Job Argument"), JOB_RESOURCE("Job Resources", "Job Resource"), LAST_SUCCEEDED_OUTCOME(
                                "Last Succeeded Outcomes"), LAST_FAILED_OUTCOME("Last Failed Outcomes");

        private final String header;
        private final String title;

        private ValueSourceType(String header) {
            this(header, header);
        }

        private ValueSourceType(String header, String title) {
            this.header = header;
            this.title = title;
        }

        public String getHeader() {
            return header;
        }

        public String getTitle() {
            return title;
        }

    }

    private final ValueSourceType type;
    private String details;

    protected ValueSource(ValueSourceType type) {
        this.type = type;
    }

    protected ValueSourceType getType() {
        return type;
    }

    public boolean isTypeJAVA() {
        return type != null && type.equals(ValueSourceType.JAVA);
    }

    public boolean isTypeOrder() {
        return type != null && type.equals(ValueSourceType.ORDER);
    }

    public boolean isTypeOrderOrNode() {
        return type != null && type.equals(ValueSourceType.ORDER_OR_NODE);
    }

    public boolean isTypeJob() {
        return type != null && type.equals(ValueSourceType.JOB);
    }

    public boolean isTypeJobArgument() {
        return type != null && type.equals(ValueSourceType.JOB_ARGUMENT);
    }

    public boolean isTypeJobResource() {
        return type != null && type.equals(ValueSourceType.JOB_RESOURCE);
    }

    protected void setDetails(String val) {
        details = val;
    }

    protected String getDetails() {
        return details;
    }
}
