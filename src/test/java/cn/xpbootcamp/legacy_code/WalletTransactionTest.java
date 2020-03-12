package cn.xpbootcamp.legacy_code;

import org.junit.jupiter.api.Test;

import javax.transaction.InvalidTransactionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WalletTransactionTest {

    @Test
    public void should_throw_exception_when_buyer_id_is_null() {
        WalletTransaction walletTransaction = new WalletTransaction("1", null, 1L);
        Exception exception = assertThrows(InvalidTransactionException.class, walletTransaction::execute);
        assertEquals("This is an invalid transaction",exception.getMessage());
    }

    @Test
    public void should_throw_a_exception_when_sell_id_is_null() {
        WalletTransaction walletTransaction = new WalletTransaction("1", 1L, null);
        Exception exception = assertThrows(InvalidTransactionException.class, walletTransaction::execute);
        assertEquals("This is an invalid transaction",exception.getMessage());
    }
}