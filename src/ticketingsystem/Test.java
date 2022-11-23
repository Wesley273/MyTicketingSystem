package ticketingsystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {
    // 列车车次
    private final static int routeNum = 5;
    // 车箱数
    private final static int coachNum = 8;
    // 每个车厢的座位数
    private final static int seatNum = 100;
    // 总站数
    private final static int stationNum = 10;

    // 每个线程里调用的方法数是10000次
    private final static int testNum = 10000;
    // 退票数目
    private final static int refundNum = 10;
    // 买票数目
    private final static int buyNum = 40;
    // 查询票数目
    private final static int inquiryNum = 100;
    // 线程数目
    private final static int threadNum = 64;

    private final static long[] buyTime = new long[threadNum];
    private final static long[] refundTime = new long[threadNum];
    private final static long[] inquiryTime = new long[threadNum];

    private final static long[] buyTotal = new long[threadNum];
    private final static long[] refundTotal = new long[threadNum];
    private final static long[] inquiryTotal = new long[threadNum];

    private final static AtomicInteger threadID = new AtomicInteger(0);

    // 获取乘客信息
    static String passengerName() {
        Random rand = new Random();
        long uid = rand.nextInt(testNum);
        return "passenger" + uid;
    }

    private static long calculateTotal(long[] array, int threadNums) {
        long result = 0;
        for (int i = 0; i < threadNums; ++i)
            result += array[i];
        return result;
    }

    private static void clearRecord() {
        threadID.set(0);
        long[][] arrays = {buyTime, refundTime, inquiryTime, buyTotal, refundTotal, inquiryTotal};
        for (long[] array : arrays) Arrays.fill(array, 0);
    }

    private static void showResult(double time, int threadNum) {
        long buyTotalTime = calculateTotal(buyTime, threadNum);
        long refundTotalTime = calculateTotal(refundTime, threadNum);
        long inquiryTotalTime = calculateTotal(inquiryTime, threadNum);

        double bTotal = (double) calculateTotal(buyTotal, threadNum);
        double rTotal = (double) calculateTotal(refundTotal, threadNum);
        double iTotal = (double) calculateTotal(inquiryTotal, threadNum);

        long buyAvgTime = (long) (buyTotalTime / bTotal);
        long refundAvgTime = (long) (refundTotalTime / rTotal);
        long inquiryAvgTime = (long) (inquiryTotalTime / iTotal);


        long throughOutput = (long) ((long) threadNum * testNum / time) * 1000;
        System.out
                .printf("ThreadNum: %d BuyAvgTime(ns): %d RefundAvgTime(ns): %d InquiryAvgTime(ns): %d ThroughOut(t/s): %d%n",
                        threadNum, buyAvgTime, refundAvgTime,
                        inquiryAvgTime, throughOutput);
        clearRecord();
    }


    private static void createThreads(TicketingDS tds, Thread[] threads, int threadNum) {
        for (int i = 0; i < threadNum; i++) {
            // 在Thread构造函数中传入Runnable实现对象，在Thread源码中将Runnable对象传递给init方法。
            // 创建线程
            threads[i] = new Thread(() -> {
                Random rand = new Random();
                Ticket ticket = new Ticket();
                // getAndIncrement方法将原值+1，并且返回+1前的原值。获取id
                int id = threadID.getAndIncrement();

                ArrayList<Ticket> soldTicket = new ArrayList<>();
                for (int i1 = 0; i1 < testNum; i1++) {
                    int sel = rand.nextInt(inquiryNum);
                    if (0 <= sel && sel < refundNum
                            && soldTicket.size() > 0) {
                        // refund ticket 0-10，退票/购票/查询余票=1/3/6
                        int select = rand.nextInt(soldTicket.size());
                        if ((ticket = soldTicket.remove(select)) != null) {
                            long s = System.nanoTime();
                            tds.refundTicket(ticket);
                            long e = System.nanoTime();
                            refundTime[id] += e - s;
                            refundTotal[id] += 1;
                        } else {
                            System.out.println("ErrOfRefund2");
                        }
                    } else if (refundNum <= sel && sel < buyNum) { // buy
                        // ticket
                        // 10-40
                        String passenger = passengerName();
                        int route = rand.nextInt(routeNum) + 1;
                        int departure = rand.nextInt(stationNum - 1) + 1;
                        int arrival = departure
                                + rand.nextInt(stationNum - departure)
                                + 1;
                        long s = System.nanoTime();
                        ticket = tds.buyTicket(passenger, route,
                                departure, arrival);
                        long e = System.nanoTime();
                        buyTime[id] += e - s;
                        buyTotal[id] += 1;
                        if (ticket != null) {
                            soldTicket.add(ticket);
                        }
                    } else if (buyNum <= sel && sel < inquiryNum) { // inquiry
                        // ticket
                        // 40-100
                        int route = rand.nextInt(routeNum) + 1;
                        int departure = rand.nextInt(stationNum - 1) + 1;
                        int arrival = departure
                                + rand.nextInt(stationNum - departure)
                                + 1;
                        long start = System.nanoTime();
                        tds.inquiry(route, departure, arrival);
                        long end = System.nanoTime();
                        inquiryTime[id] += end - start;
                        inquiryTotal[id] += 1;
                    }
                }
            });
        }
    }

    private static long runThreads(Thread[] threads, int threadNum) throws InterruptedException {
        long start = System.currentTimeMillis();
        for (int i = 0; i < threadNum; ++i)
            threads[i].start();

        for (int i = 0; i < threadNum; i++) {
            threads[i].join();
        }
        long end = System.currentTimeMillis();
        return end - start;
    }

    public static void main(String[] args) throws InterruptedException {

        // 设置不同的线程总数
        final int[] threadNum = {4, 8, 16, 32, 64};
        int p;

        // 对不同threadNums数目的线程进行处理
        for (p = 0; p < threadNum.length; ++p) {
            final TicketingDS tds = new TicketingDS(routeNum, coachNum, seatNum, stationNum, threadNum[p]);
            Thread[] threads = new Thread[threadNum[p]];
            // 创建并运行threadNum个线程
            createThreads(tds, threads, threadNum[p]);
            long time = runThreads(threads, threadNum[p]);
            showResult((double) time, threadNum[p]);
        }
    }
}
