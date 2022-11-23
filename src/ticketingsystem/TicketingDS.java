package ticketingsystem;

//本并发购票数据结构将之拆分成四个类。
//其中 TicketingDS 类作为与外接的接口，包含三个方法
//其余三个分别为 RouteSection-车次, CoachSection-车厢, SeatSection-座位。

import java.util.*;

public class TicketingDS implements TicketingSystem {
    // 车次总数
    private final int routeNum;
    // 车站总数
    private final int stationNum;
    // 车次列表
    private final ArrayList<RouteSection> routeArray;
    // 线程数
    private final int threadNum;

    // 使用构造方法，当java类实例化时，输入参数值，将属性初始化
    public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum,
                       int threadNum) {

        this.routeNum = routeNum;
        this.stationNum = stationNum;
        this.threadNum = threadNum;

        // ArrayList是Java集合框架中的一个重要的类，它继承于AbstractList，实现了List接口，
        // 它是一个长度可变的集合，提供了增删改查的功能。集合中允许null的存在。
        // ArrayList类还是实现了RandomAccess接口，可以对元素进行快速访问。
        // 实现了Serializable接口，说明ArrayList可以被序列化，还有Cloneable接口，可以被复制。
        // 和Vector不同的是，ArrayList不是线程安全的。
        this.routeArray = new ArrayList<>(routeNum);

        // 依次遍历每个车次
        for (int routeID = 1; routeID <= routeNum; routeID++) {
            this.routeArray.add(new RouteSection(routeID, coachNum, seatNum));
        }
    }

    // 买票
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        // 先判断车次和车站是否在范围内
        if (route <= 0 || route > this.routeNum || arrival > this.stationNum
                || departure >= arrival)
            return null;
        // 尝试购票，并返回(route - 1)
        return this.routeArray.get(route - 1).initBuy(passenger, departure,
                arrival);
    }

    // 查询
    public int inquiry(int route, int departure, int arrival) {

        // 先判断车次和车站是否在范围内
        if (route <= 0 || route > this.routeNum || arrival > this.stationNum
                || departure >= arrival)

            return -1;

        // 尝试查询，并返回(route - 1)
        return this.routeArray.get(route - 1).initInquiry(departure, arrival);

    }

    // 退票
    public boolean refundTicket(Ticket ticket) {

        // 获取车票的车次
        final int routeID = ticket.route;
        // 先判断车票和车次是否在范围内
        if (ticket == null || routeID <= 0 || routeID > this.routeNum)
            return false;
        // 尝试退票，并返回(route - 1)
        return this.routeArray.get(routeID - 1).initRefund(ticket);
    }
}
