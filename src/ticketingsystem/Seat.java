package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;

public class Seat {
    // 座位序号
    private final int ID;
    // 根据一个 AtomicLong 型 64 位的 availableSeat判断是否空闲
    // availableSeat 的每一位表示座位对应的每一站， 0 表示未售出， 1 表示售出。
    private final AtomicLong availableSeat;


    public Seat(final int ID) {
        this.ID = ID;
        this.availableSeat = new AtomicLong(0);
    }

    private static long getTemp(int departure, int arrival) {
        long temp = 0;
        for (int i = departure - 1; i < arrival - 1; i++) {
            long pow = 1;
            pow = pow << i;
            temp |= pow;
        }
        return temp;
    }

    public int buySeat(final int departure, final int arrival) {
        long oldValue;
        long newValue;
        long temp = getTemp(departure, arrival);

        do {
            oldValue = this.availableSeat.get();
            long result = temp & oldValue;
            if (result == 0) {
                // 有空闲，把所要的区间置1
                newValue = temp | oldValue;
            } else return -1;

        } while (!this.availableSeat.compareAndSet(oldValue, newValue));
        return this.ID;
    }

    public int querySeat(final int departure, final int arrival) {
        long oldValue = this.availableSeat.get();
        long temp = getTemp(departure, arrival);
        long result = temp & oldValue;
        return (result == 0) ? 1 : 0;
    }

    public boolean refundSeat(final int departure, final int arrival) {

        long oldValue;
        long newValue;
        long temp = getTemp(departure, arrival);

        temp = ~temp;

        do {
            oldValue = this.availableSeat.get();
            newValue = temp & oldValue;
        } while (!this.availableSeat.compareAndSet(oldValue, newValue));
        return true;
    }
}
