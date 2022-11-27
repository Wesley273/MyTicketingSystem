package ticketingsystem;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {
    // 车次总数
    private final int routeNum;
    // 车站总数
    private final int stationNum;
    // 车次列表
    private final ArrayList<Route> routeList;
    // 车票唯一编号
    private AtomicLong ticketID;
    // 构造已售车票队列
    private Queue<Long> soldTicket;

    public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        this.routeNum = routeNum;
        this.stationNum = stationNum;
        this.ticketID = new AtomicLong(0);
        // ConcurrentLinkedQueue是一个基于链接节点的无界线程安全队列，是“wait－free”的
        this.soldTicket = new ConcurrentLinkedQueue<>();
        // 构建车次动态数组，ArrayList不是线程安全的
        this.routeList = new ArrayList<>(this.routeNum);
        for (int route = 1; route <= this.routeNum; route++) {
            this.routeList.add(new Route(route, coachNum, seatNum));
        }
    }

    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        // 判断购票合法性
        if (route <= 0 || route > this.routeNum || arrival > this.stationNum || departure >= arrival) {
            return null;
        }
        // 尝试购票，注意由于数组从0索引，需要get(route-1)
        Ticket ticket = this.routeList.get(route - 1).buyRoute(departure, arrival);
        if (ticket == null) {
            return null;
        }
        ticket.tid = this.ticketID.getAndIncrement();
        ticket.passenger = passenger;
        ticket.route = route;
        ticket.departure = departure;
        ticket.arrival = arrival;
        this.soldTicket.add(ticket.tid);
        return ticket;
    }

    public int inquiry(int route, int departure, int arrival) {
        if (route <= 0 || route > this.routeNum || arrival > this.stationNum || departure >= arrival)
            return -1;
        return this.routeList.get(route - 1).queryRoute(departure, arrival);

    }

    public boolean refundTicket(Ticket ticket) {
        if (ticket.route <= 0 || ticket.route > this.routeNum || !this.soldTicket.contains(ticket.tid)) {
            return false;
        }
        this.soldTicket.remove(ticket.tid);
        return this.routeList.get(ticket.route - 1).refundRoute(ticket);
    }
}
