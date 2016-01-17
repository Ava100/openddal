package com.openddal.server.mysql.proto.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.openddal.server.mysql.proto.Flags;
import com.openddal.server.mysql.proto.OK;
import com.openddal.server.mysql.proto.Proto;

public class OKTest {
    @Test
    public void test1() {
        byte[] packet = Proto.packet_string_to_bytes(
            "07 00 00 02 00 00 00 02    00 00 00"
        );

        OK ok = OK.loadFromPacket(packet);
        assertArrayEquals(packet, ok.toPacket());
        assertEquals(ok.affectedRows, 0);
        assertEquals(ok.lastInsertId, 0);
        assertEquals(ok.hasStatusFlag(Flags.SERVER_STATUS_AUTOCOMMIT), true);
        assertEquals(ok.hasStatusFlag(Flags.SERVER_STATUS_IN_TRANS_READONLY), false);
        assertEquals(ok.warnings, 0);
    }
}
