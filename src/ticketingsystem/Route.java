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

    public Route(final int ID, final int coachNum, final int SEAT_NUM) {
        this.ID = ID;
        this.coachNum = coachNum;

        // 构造车厢动态数组
        this.coachList = new ArrayList<>(coachNum);
        for (int coach = 1; coach <= coachNum; coach++) {
            this.coachList.add(new Coach(coach, SEAT_NUM));
        }
    }

    public Ticket buyRoute(final int departure, final int arrival) {
        // ThreadLocalRandom是线程相关的，调用ThreadLocalRandom.current()会返回当前线程的ThreadLocalRandom对象。
        // ThreadLocalRandom优点在于高并发条件下，不同线程产生的随机数能不一致。
        int random = ThreadLocalRandom.current().nextInt(this.coachNum);
        for (int i = 0; i < this.coachNum; i++) {
            Ticket ticket = this.coachList.get(random).buyCoach(departure, arrival);
            if (ticket == null) {
                random = (random + 1) % this.coachNum;
            } else {
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
        return this.coachList.get(ticket.coach - 1).refundCoach(ticket.seat, ticket.departure, ticket.arrival);
    }
}