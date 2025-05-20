package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointService {

    private static final Logger log = LoggerFactory.getLogger(PointService.class);

    @Autowired
    private UserPointTable userPointTable;
    @Autowired
    private PointHistoryTable pointHistoryTable;

    /**
     * 특정 유저의 포인트 조회
     */
    public UserPoint getPointById(long id) {
        return userPointTable.selectById(id);
    }

    /**
     * 특정 유저의 포인트 충전/이용 내역 조회
     */
    public List<PointHistory> getHistoryById(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }



    /**
     * 특정 유저의 포인트를 충전
     */
    public UserPoint charge(long id, long amount) {
        // 정책1) 충전은 1,000원 이상 922337203원 미만 가능하다.
        if (amount < 1000 || amount > 922337203) {
            throw new RuntimeException("충전할 수 없는 금액입니다. ");
        }

        // 현재 포인트 조회
        UserPoint userPoint = getPointById(id);

        log.info("[충전 시도 - {}] - 잔액: {}원 / 충전 {}원", id, userPoint.getPoint(), amount);

        // 포인트 충전
        UserPoint point = userPointTable.insertOrUpdate(id, userPoint.getPoint() + amount);

        log.info("[충전 완료 - {}] - 잔액: {}원", id, point.getPoint());

        // 충전 이력 저장
        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());


        return point;
    }

    /**
     * 특정 유저의 포인트를 사용
     */
    public UserPoint use(long id, long amount) throws RuntimeException {
        // 현재 포인트 조회
        UserPoint userPoint = getPointById(id);

        if (amount < 0) {
            throw new RuntimeException("차감할 수 없는 금액입니다. ");
        }

        // 정책2) 잔고가 부족할 경우, 포인트 사용은 실패한다.
        if (userPoint.getPoint() < amount) {
            throw new RuntimeException("잔고가 부족합니다.");
        }

        log.info("[차감 시도 - {}] - 잔액: {}원 / 차감 {}원", id, userPoint.getPoint(), amount);

        // 포인트 차감
        UserPoint point = userPointTable.insertOrUpdate(id, userPoint.getPoint() - amount);

        log.info("[차감 완료 - {}] - 잔액: {}원", id, point.getPoint());

        // 차감 이력
        pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());

        return point;
    }
}