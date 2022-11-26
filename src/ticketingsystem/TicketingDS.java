package ticketingsystem;

import java.util.*;

public class TicketingDS implements TicketingSystem {
    // 车次总数
    private final int routeNum;
    // 车站总数
    private final int stationNum;
    // 车次列表
    private final ArrayList<Route> routeList;

    public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        this.routeNum = routeNum;
        this.stationNum = stationNum;

        // 构建车次动态数组，ArrayList不是线程安全的
        this.routeList = new ArrayList<>(this.routeNum);
        for (int route = 1; route <= this.routeNum; route++) {
            this.routeList.add(new Route(route, coachNum, seatNum));
        }
    }

    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        // 判断购票合法性
        if (route <= 0 || route > this.routeNum || arrival > this.stationNum || departure >= arrival)
            return null;
        // 尝试购票，注意由于数组从0索引，需要get(route-1)
        return this.routeList.get(route - 1).buyRoute(passenger, departure, arrival);
    }

    public int inquiry(int route, int departure, int arrival) {

        if (route <= 0 || route > this.routeNum || arrival > this.stationNum || departure >= arrival)
            return -1;

        // 尝试查询
        return this.routeList.get(route - 1).queryRoute(departure, arrival);

    }

    public boolean refundTicket(Ticket ticket) {
        // 获取车票的车次
        final int route = ticket.route;

        if (route <= 0 || route > this.routeNum)
            return false;
        // 尝试退票
        return this.routeList.get(route - 1).refundRoute(ticket);
    }
}
