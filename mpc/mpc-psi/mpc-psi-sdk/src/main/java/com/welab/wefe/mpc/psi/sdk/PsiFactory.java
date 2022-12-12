package com.welab.wefe.mpc.psi.sdk;

public class PsiFactory {

    public static final Psi generatePsi() {
        return new EcdhPsi();
    }

    public static final Psi generatePsi(String type) {
        if (type == null || type.trim().equalsIgnoreCase("") || type.equalsIgnoreCase("ecdh")) {
            return new EcdhPsi();
        } else {
            return new PrivateSetIntersection();
        }
    }
}
