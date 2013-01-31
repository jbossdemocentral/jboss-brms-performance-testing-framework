package org.jboss.brms.test;

import org.jboss.brms.test.util.BusinessCentralUtil;
import org.jboss.brms.test.util.GuvnorUtil;

public class ProcessRunner {
    public static void main(final String[] args) {
        System.out.println("Getting info from Guvnor:");

        final String packagesJson = GuvnorUtil.getFromGuvnor("/packages");
        System.out.println(packagesJson);

        System.out.println("Getting info from Business Central:");

        final String definitionsJson = BusinessCentralUtil.getFromBusinessCentral("/process/definitions");
        System.out.println(definitionsJson);
    }
}
