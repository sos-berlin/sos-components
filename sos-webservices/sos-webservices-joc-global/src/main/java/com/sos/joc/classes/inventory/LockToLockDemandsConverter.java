package com.sos.joc.classes.inventory;

import java.util.Collections;

import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.LockDemand;

public class LockToLockDemandsConverter {
    
    public static Lock lockToInventoryLockDemands(Lock lock) {
        if (lock.getDemands() == null || lock.getDemands().isEmpty()) {
            lock.setDemands(Collections.singletonList(new LockDemand(lock.getLockName(), lock.getCount())));
            lock.setCount(null);
            lock.setLockName(null);
        }
        return lock;
    }
    
    public static com.sos.sign.model.instruction.Lock lockToSignLockDemands(Lock lock, com.sos.sign.model.instruction.Lock sLock) {
        if (lock.getDemands() == null || lock.getDemands().isEmpty()) {
            sLock.setDemands(Collections.singletonList(new com.sos.sign.model.instruction.LockDemand(lock.getLockName(), lock
                    .getCount())));
        }
        return sLock;
    }

}
