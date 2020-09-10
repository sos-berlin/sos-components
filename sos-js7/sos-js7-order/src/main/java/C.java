import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.sos.jobscheduler.model.order.FreshOrder;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.model.order.StartOrder;
import com.sos.joc.model.order.StartOrders;
import com.sos.js7.order.initiator.classes.PlannedOrder;

public class C {

   

    private boolean dayIsInPlan(Date start, String dailyPlanDate) throws ParseException {
        String timeZone = "Japan";
        String periodBegin = "21:06:00";
        String dateInString = String.format("%s %s", dailyPlanDate, periodBegin);

        Instant instant = JobSchedulerDate.getScheduledForInUTC(dateInString, timeZone).get();
        Date dailyPlanStartPeriod = Date.from(instant);

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(dailyPlanStartPeriod);
        calendar.add(java.util.Calendar.HOUR, 24);
        Date dailyPlanEndPeriod = calendar.getTime();

        return (start.after(dailyPlanStartPeriod) || start.equals(dailyPlanStartPeriod)) && (start.before(dailyPlanEndPeriod));

    }
    
    private void test() {
        StartOrders startOrders = new StartOrders();
        List<StartOrder> orders = new ArrayList<StartOrder>();
        for (int i=0;i<10000;i++){
            StartOrder o = new StartOrder();
            o.setOrderId(String.valueOf(i));
            orders.add(o);
        }
        startOrders.setOrders(orders);
        
        long timeStart = System.currentTimeMillis();
        Set<PlannedOrder> plannedOrders = new HashSet<PlannedOrder>();
        for (StartOrder startOrder : startOrders.getOrders()) {
            PlannedOrder plannedOrder = new PlannedOrder();
            FreshOrder freshOrder = new FreshOrder();
            freshOrder.setId(startOrder.getOrderId());
            freshOrder.setWorkflowPath(startOrder.getWorkflowPath());
            freshOrder.setArguments(startOrder.getArguments());

            plannedOrder.setFreshOrder(freshOrder);
            plannedOrders.add(plannedOrder);
        }
        long timeEnd = System.currentTimeMillis();
        Long duration = (timeEnd - timeStart);
        System.out.println(duration);
        
        timeStart = System.currentTimeMillis();

        Set<PlannedOrder> result = startOrders.getOrders().stream().map(mapper).collect(java.util.stream.Collectors.toSet());
        timeEnd = System.currentTimeMillis();
        duration = (timeEnd - timeStart);
        System.out.println(duration);
   
    }
    
    static Function<StartOrder, PlannedOrder> mapper = order -> {
        PlannedOrder plannedOrder = null;
        return plannedOrder = mapToPlannedOrder(order);
    };
   
   
    private static PlannedOrder mapToPlannedOrder(StartOrder startOrder) {
        PlannedOrder plannedOrder = new PlannedOrder();
        FreshOrder freshOrder = new FreshOrder();
        freshOrder.setId(startOrder.getOrderId());
      
        freshOrder.setWorkflowPath(startOrder.getWorkflowPath());
        freshOrder.setArguments(startOrder.getArguments());

        plannedOrder.setFreshOrder(freshOrder);
        return plannedOrder;
    }
    public static void main(String[] args) throws ParseException {
        C c = new C();
        Date start = new Date();
        System.out.println( c.dayIsInPlan(start, "2020-09-03"));

        c.test();
    }

}
