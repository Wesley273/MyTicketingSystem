package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;


public class SeatSection {

    private final int seatID;//座位序号
    private final AtomicLong availableSeat;//根据一个 AtomicLong 型 64 位的 availableSeat判断是否空闲

    //availableSeat 的每一位表示座位对应的每一站， 0 表示未售出， 1 表示售出。
    //购票查询退票时均采用从 route-\>coach-\>seat 的方式调用方法，在 seatNode 操作时，
    //用原语 compareAndSet 构造非阻塞式的自旋锁来保证并发操作的原子性。

    public SeatSection(final int seatID) {
        this.seatID = seatID;
        this.availableSeat = new AtomicLong(0);
    }

    private static long getTemp(int departure, int arrival) {
        long temp = 0;
        int i = departure - 1;
        while (i < arrival - 1) {
            long pow = 1;
            pow = pow << i;
            temp |= pow;
            i++;
        }
        return temp;
    }

    //尝试买票
    public int initSeal(final int departure, final int arrival) {
        long oldAvailSeat;
        long newAvailSeat;
        long temp = getTemp(departure, arrival);

        do {
            oldAvailSeat = this.availableSeat.get();
            long result = temp & oldAvailSeat;
            if (result != 0) {
                return -1;
            } else {
                newAvailSeat = temp | oldAvailSeat;
            }
        } while (!this.availableSeat.compareAndSet(oldAvailSeat, newAvailSeat));
        return this.seatID;
    }

    //查询余票
    public int initInquiry(final int departure, final int arrival) {
        long oldAvailSeat = this.availableSeat.get();
        long temp = getTemp(departure, arrival);
        long result = temp & oldAvailSeat;
        return (result == 0) ? 1 : 0;
    }


    //尝试退票
    public boolean initRefund(final int departure, final int arrival) {

        long oldAvailSeat;
        long newAvailSeat;
        long temp = getTemp(departure, arrival);

        temp = ~temp;

        do {
            oldAvailSeat = this.availableSeat.get();
            newAvailSeat = temp & oldAvailSeat;
        } while (!this.availableSeat.compareAndSet(oldAvailSeat, newAvailSeat));
        return true;
    }
}
