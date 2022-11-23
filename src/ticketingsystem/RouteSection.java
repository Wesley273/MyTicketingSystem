package ticketingsystem;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

//具体实现TicketingDS类里的三个方法

class RouteSection {

    private final int routeID;// 车次序号
    private final int coachNum;// 车厢数目
    private final ArrayList<CoachSection> coachList;// 车厢列表
    private final AtomicLong ticketID;// 车票的票号，每个车票有唯一的票号
    private final Queue<Long> ticketSold;// 构造队列

    // 使用构造方法，当java类实例化时，输入参数值，将属性初始化
    public RouteSection(final int routeId, final int coachNum, final int seatNum) {

        this.routeID = routeId;
        this.coachNum = coachNum;
        this.coachList = new ArrayList<CoachSection>(coachNum);
        this.ticketID = new AtomicLong(0);

        this.ticketSold = new ConcurrentLinkedQueue<Long>();
        // ConcurrentLinkedQueue是一个基于链接节点的无界线程安全队列，
        // 它采用先进先出的规则对节点进行排序，当我们添加一个元素的时候，它会添加到队列的尾部，当我们获取一个元素时，它会返回队列头部的元素。
        // 它采用了“wait－free”算法来实现

        // 遍历车厢
        int coachID = 1;
        while (coachID <= coachNum) {
            this.coachList.add(new CoachSection(coachID, seatNum));
            coachID++;
        }
    }

    private static long getTicketHashCode(Ticket ticket, long ticketHashCode) {
        ticketHashCode |= ticket.tid << 32;
        ticketHashCode |= (long) ticket.coach << 24;
        ticketHashCode |= (long) ticket.seat << 12;
        ticketHashCode |= (long) ticket.departure << 6;
        ticketHashCode |= ticket.arrival;
        return ticketHashCode;
    }

    // 尝试购票
    public Ticket initBuy(final String passenger, final int departure,
                          final int arrival) {

        int randCoach = ThreadLocalRandom.current().nextInt(this.coachNum);
        // 建立车厢的数据结构
        // ThreadLocalRandom是线程相关的，调用ThreadLocalRandom.current()会返回当前线程的ThreadLocalRandom对象。
        // ThreadLocalRandom优点在于高并发条件下，不同线程产生的随机数能不一致。同样的，调用nextInt方法也会返回一个伪随机整数值。

        int i = 0;
        while (i < this.coachNum) {
            Ticket ticket = this.coachList.get(randCoach).initSeal(departure,
                    arrival);

            if (ticket != null) {
                ticket.tid = this.routeID * 12345678L
                        + this.ticketID.getAndIncrement();
                // 使用getAndIncrement，以原子方式将当前值加 1，返回旧值（即加1前的原始值）
                ticket.passenger = passenger;
                ticket.route = this.routeID;
                ticket.departure = departure;
                ticket.arrival = arrival;
                long tic_hashCode = 0;

                tic_hashCode = getTicketHashCode(ticket, tic_hashCode);
                this.ticketSold.add(new Long(tic_hashCode));
                return ticket;

            }
            randCoach = (randCoach + 1) % this.coachNum;
            i++;
        }
        return null;
    }

    // 查询，查询余票情况
    public int initInquiry(final int departure, final int arrival) {
        int ticketSum = 0;
        int i = 0;
        while (i < this.coachNum) {
            ticketSum += this.coachList.get(i).initInquiry(departure, arrival);
            i++;
        }
        return ticketSum;
    }

    // 退票
    public boolean initRefund(final Ticket ticket) {

        // 建立哈希表，根据对象的地址或者字符串或者数字算出对应的int类型的数值
        long ticketHashCode = 0;
        ticketHashCode = getTicketHashCode(ticket, ticketHashCode);

        // contains判断是否包含这张票
        if (!this.ticketSold.contains(ticketHashCode))
            return false;
        else {
            // 删除这张票
            this.ticketSold.remove(ticketHashCode);
            // 重新返回
            return this.coachList.get(ticket.coach - 1).initRefund(
                    ticket.seat, ticket.departure, ticket.arrival);
        }
    }
}