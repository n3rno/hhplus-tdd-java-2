package io.hhplus.tdd;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.UserPoint;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class PointTest {
    private final long id = 135;

    @Autowired
    private PointService pointService;

    // 0원으로 초기화
    private void clear() {
        UserPoint p = pointService.getPointById(id);
        if (p.getPoint() > 0) pointService.use(id, p.getPoint());
    }


    @DisplayName("유저의 포인트를 조회합니다")
    @Test
    public void selectOne() {
        UserPoint p = pointService.getPointById(id);

        // 최초 id로 포인트 첫 조회 시 잔액은 0원이다.
        Assertions.assertThat(p.getPoint()).isEqualTo(0);
    }

    @DisplayName("잔액이 차감 금액보다 적을 때, 유저의 포인트는 차감할 수 없다.")
    @Test
    public void useEmptyPoint() {
        clear();
        // 0원일 때 10,000원 차감 시도 -> 불가능
        Assertions.assertThatThrownBy(() -> pointService.use(id, 10000));
    }

    @DisplayName("유저 포인트를 충전한다.")
    @Test
    public void charge () {
        // 20,000원 충전 시도 -> 가능
        Assertions.assertThatCode(() -> pointService.charge(id, 20000)).doesNotThrowAnyException();
    }

    @DisplayName("음수 금액은 충전할 수 없다.")
    @Test
    public void chargeFail () {
        Assertions.assertThatThrownBy(() -> pointService.charge(id, -1000));
    }

    @DisplayName("음수 금액을 차감할 수 없다.")
    @Test
    public void useFail () {
        Assertions.assertThatThrownBy(() -> pointService.use(id, -3000));
    }

    @DisplayName("유저 포인트를 사용한다.")
    @Test
    public void use() {
        pointService.charge(id, 20000);
        // 10,000원 차감
        Assertions.assertThatCode(() -> pointService.use(id, 10000)).doesNotThrowAnyException();
    }

    @DisplayName("포인트 이력이 모두 유저의 것인지 확인한다.")
    @Test
    public void checkHistoryUser() {
        List<PointHistory> history = pointService.getHistoryById(id);
        Assertions.assertThat(history.stream().filter(hist -> hist.id() == id));
    }
}
