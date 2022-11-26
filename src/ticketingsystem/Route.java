package ticketingsystem;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

class Route {
    // 车次序号
    private final int ID;
    // 车厢数目
    private final int coachNum;
    // 车厢列表
    private final ArrayList<Coach> coachList;
    // 车票的票号，每个车票有唯一的票号
    private final AtomicLong ticket;
    // 构造已售车票队列
    private final Queue<Long> soldTicket;

    public Route(final int ID, final int coachNum, final int SEAT_NUM) {
        this.ID = ID;
        this.coachNum = coachNum;
        this.ticket = new AtomicLong(0);

        // 构造车厢动态数组
        this.coachList = new ArrayList<>(coachNum);
        for (int coach = 1; coach <= coachNum; coach++) {
            this.coachList.add(new Coach(coach, SEAT_NUM));
        }

        // ConcurrentLinkedQueue是一个基于链接节点的无界线程安全队列，是“wait－free”的
        this.soldTicket = new ConcurrentLinkedQueue<>();
    }

    private static long hashTicket(Ticket ticket) {
        // 这里其实就是把各个数拼在一起，生成定长的hashcode
        long hashedTicket = ticket.tid << 32;
        hashedTicket |= (long) ticket.coach << 24;
        hashedTicket |= (long) ticket.seat << 12;
        hashedTicket |= (long) ticket.departure << 6;
        hashedTicket |= ticket.arrival;
        return hashedTicket;
    }

    public Ticket buyRoute(final String passenger, final int departure, final int arrival) {
        // ThreadLocalRandom是线程相关的，调用ThreadLocalRandom.current()会返回当前线程的ThreadLocalRandom对象。
        // ThreadLocalRandom优点在于高并发条件下，不同线程产生的随机数能不一致。
        int random = ThreadLocalRandom.current().nextInt(this.coachNum);

        for (int i = 0; i < this.coachNum; i++) {
            Ticket ticket = this.coachList.get(random).buyCoach(departure, arrival);

            if (ticket == null) {
                random = (random + 1) % this.coachNum;
            } else {
                ticket.tid = this.ID * 12345678L + this.ticket.getAndIncrement();
                // 使用getAndIncrement，以原子方式将当前值加 1，返回旧值（即加1前的原始值）
                ticket.passenger = passenger;
                ticket.route = this.ID;
                ticket.departure = departure;
                ticket.arrival = arrival;
                long hashedTicket = hashTicket(ticket);
                this.soldTicket.add(hashedTicket);
                return ticket;
            }
        }
        return null;
    }

    public int queryRoute(final int departure, final int arrival) {
        int ticketSum = 0;
        for (int i = 0; i < this.coachNum; i++) {
            ticketSum += this.coachList.get(i).queryCoach(departure, arrival);
        }
        return ticketSum;
    }

    public boolean refundRoute(final Ticket ticket) {

        // 求票对应的哈希值
        long hashedTicket = hashTicket(ticket);

        // 判断售出队列里是否包含这张票
        if (this.soldTicket.contains(hashedTicket)) {
            // 删除这张票
            this.soldTicket.remove(hashedTicket);
            // 重新返回
            return this.coachList.get(ticket.coach - 1).refundCoach(ticket.seat, ticket.departure, ticket.arrival);
        } else {
            return false;
        }
    }
}