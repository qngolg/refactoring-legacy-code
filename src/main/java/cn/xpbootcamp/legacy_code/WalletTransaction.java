package cn.xpbootcamp.legacy_code;

import cn.xpbootcamp.legacy_code.enums.STATUS;
import cn.xpbootcamp.legacy_code.service.WalletService;
import cn.xpbootcamp.legacy_code.service.WalletServiceImpl;
import cn.xpbootcamp.legacy_code.utils.IdGenerator;
import cn.xpbootcamp.legacy_code.utils.RedisDistributedLock;

import javax.transaction.InvalidTransactionException;

public class WalletTransaction {
    private static final int THIRTY_DAYS = 1728000000;
    private String id;
    private Long buyerId;
    private Long sellerId;
    private Long createdTimestamp;
    private Double amount;
    private STATUS status;


    public WalletTransaction(String preAssignedId, Long buyerId, Long sellerId, Double amount) {
        generateId(preAssignedId);
        this.amount = amount;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.status = STATUS.TO_BE_EXECUTED;
        this.createdTimestamp = System.currentTimeMillis();
    }

    public boolean execute() throws InvalidTransactionException {
        if (hasInvalidParam()) {
            throw new InvalidTransactionException("This is an invalid transaction");
        }
        if (status == STATUS.EXECUTED) {
            return true;
        }
        boolean isLocked = false;
        try {
            isLocked = RedisDistributedLock.getSingletonInstance().lock(id);

            // 锁定未成功，返回false
            if (!isLocked) {
                return false;
            }
            if (status == STATUS.EXECUTED) {
                return true; // double check
            }
            // 交易超过20天
            if (isExpired()) {
                this.status = STATUS.EXPIRED;
                return false;
            }
            WalletService walletService = new WalletServiceImpl();
            String walletTransactionId = walletService.moveMoney(id, buyerId, sellerId, amount);
            if (walletTransactionId != null) {
                this.status = STATUS.EXECUTED;
                return true;
            } else {
                this.status = STATUS.FAILED;
                return false;
            }
        } finally {
            if (isLocked) {
                RedisDistributedLock.getSingletonInstance().unlock(id);
            }
        }
    }

    private boolean isExpired() {
        return System.currentTimeMillis() - createdTimestamp > THIRTY_DAYS;
    }

    private boolean hasInvalidParam() {
        return buyerId == null || (sellerId == null || amount < 0.0);
    }

    private void generateId(String preAssignedId) {
        if (preAssignedId != null && !preAssignedId.isEmpty()) {
            this.id = preAssignedId;
        } else {
            this.id = IdGenerator.generateTransactionId();
        }
        if (!this.id.startsWith("t_")) {
            this.id = "t_" + preAssignedId;
        }
    }

}