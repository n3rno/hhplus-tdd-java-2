package io.hhplus.tdd;

import io.hhplus.tdd.point.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;

@SpringBootTest
public class CountDownLatchTest {

    private final long id = 136;

    @Autowired
    private PointService pointService;

    @DisplayName("비동기 테스트")
    @Test
    public void countDownLatchTest() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(2);

        new Thread(() -> {
            pointService.synchronizedCharge(id,8000);

            countDownLatch.countDown(); // 충전 완료 후 다음 단계 진행
        }).start();

        pointService.synchronizedCharge(id,2000);
        countDownLatch.countDown();

        // 스레드(충전)이 끝날 때까지 대기
        countDownLatch.await();

        pointService.synchronizedUse(id, 9000);

        pointService.synchronizedUse(id, 1000);

    }
}
