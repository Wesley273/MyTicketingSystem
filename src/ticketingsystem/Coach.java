package ticketingsystem;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

class Coach {
    // 车厢序号
    private final int ID;
    // 座位数目
    private final int seatNum;
    // 座位列表
    private final ArrayList<Seat> seatList;

    public Coach(final int ID, final int seatNum) {

        this.ID = ID;
        this.seatNum = seatNum;

        // 构造车座动态数组
        seatList = new ArrayList<>(seatNum);
        for (int seat = 1; seat <= seatNum; seat++) {
            this.seatList.add(new Seat(seat));
        }
    }

    public Ticket buyCoach(final int departure, final int arrival) {

        Ticket ticket = new Ticket();
        // nextInt生成[0,n)范围内的随机整数
        int random = ThreadLocalRandom.current().nextInt(this.seatNum);
        for (int i = 0; i < this.seatNum; i++) {
            int seat = this.seatList.get(random).buySeat(departure, arrival);

            if (seat == -1) {
                random = (random + 1) % this.seatNum;
            } else {
                ticket.coach = this.ID;
                ticket.seat = seat;
                return ticket;
            }
        }
        return null;
    }

    public int queryCoach(final int departure, final int arrival) {
        int seatSum = 0;
        for (int i = 0; i < this.seatNum; i++) {
            seatSum += this.seatList.get(i).querySeat(departure, arrival);
        }
        return seatSum;

    }

    public boolean refundCoach(final int seatID, final int departure,
            final int arrival) {
        return this.seatList.get(seatID - 1).refundSeat(departure, arrival);
    }

}