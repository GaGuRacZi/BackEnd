package com.gaguraczi.paw.global.time;

import java.time.ZoneId;

/** Wall-clock times in this app are KST. */
public final class AppTime {

    public static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private AppTime() {
    }
}
