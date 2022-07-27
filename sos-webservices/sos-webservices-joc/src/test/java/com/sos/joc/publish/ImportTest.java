package com.sos.joc.publish;

import static org.junit.Assert.assertTrue;

import java.util.EnumSet;

import org.junit.Test;

import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.publish.util.ImportUtils;

public class ImportTest {

    @Test
    public void testImportOrderLength() {
        // -2 : because JOBCLASS and FOLDER are not im-/export objects
        assertTrue(ImportUtils.getImportOrder().size() == EnumSet.allOf(ConfigurationType.class).size() - 2);
    }
}
