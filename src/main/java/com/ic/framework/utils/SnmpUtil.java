package com.ic.framework.utils;

import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SnmpUtil {
    public static void main(String[] args) throws Exception {
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(GenericAddress.parse("udp:x.x.x.x/161")); // supply your own IP and port
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);

        Map<String, String> result = doWalk(".1.3.6.1.2.1.2.2", target); // ifTable, mib-2 interfaces

        for (Map.Entry<String, String> entry : result.entrySet()) {
            if (entry.getKey().startsWith(".1.3.6.1.2.1.2.2.1.2.")) {
                System.out.println("ifDescr" + entry.getKey().replace(".1.3.6.1.2.1.2.2.1.2", "") + ": " + entry.getValue());
            }
            if (entry.getKey().startsWith(".1.3.6.1.2.1.2.2.1.3.")) {
                System.out.println("ifType" + entry.getKey().replace(".1.3.6.1.2.1.2.2.1.3", "") + ": " + entry.getValue());
            }
        }
    }

    public static Map<String, String> doWalk(String tableOid, Target target) throws IOException {
        Map<String, String> result = new TreeMap<>();
        TransportMapping<? extends Address> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
        List<TreeEvent> events = treeUtils.getSubtree(target, new OID(tableOid));
        if (events == null || events.size() == 0) {
            System.out.println("Error: Unable to read table...");
            return result;
        }

        for (TreeEvent event : events) {
            if (event == null) {
                continue;
            }
            if (event.isError()) {
                System.out.println("Error: table OID [" + tableOid + "] " + event.getErrorMessage());
                continue;
            }

            VariableBinding[] varBindings = event.getVariableBindings();
            if (varBindings == null || varBindings.length == 0) {
                continue;
            }
            for (VariableBinding varBinding : varBindings) {
                if (varBinding == null) {
                    continue;
                }

                result.put("." + varBinding.getOid().toString(), varBinding.getVariable().toString());
            }

        }
        snmp.close();

        return result;
    }

    /**
     * walk方式获取指定的oid value
     *
     * @param snmp
     * @param target
     * @param oid
     * @return
     * @throws IOException
     */
    public static List<PDU> snmpWalk(Snmp snmp, Target target, String oid) throws IOException {
        List<PDU> pduList = new ArrayList<>();

        ScopedPDU pdu = new ScopedPDU();
        OID targetOID = new OID(oid);
        pdu.add(new VariableBinding(targetOID));

        boolean finished = false;
        while (!finished) {
            VariableBinding vb = null;
            ResponseEvent respEvent = snmp.getNext(pdu, target);

            PDU response = respEvent.getResponse();

            if (null == response) {
                break;
            } else {
                vb = response.get(0);
            }
            // check finish
            finished = checkWalkFinished(targetOID, pdu, vb);
            if (!finished) {
                pduList.add(response);

                // Set up the variable binding for the next entry.
                pdu.setRequestID(new Integer32(0));
                pdu.set(0, vb);
            }
        }

        return pduList;
    }

    private static boolean checkWalkFinished(OID targetOID, PDU pdu, VariableBinding vb) {
        boolean finished = false;
        if (pdu.getErrorStatus() != 0) {
            finished = true;
        } else if (vb.getOid() == null) {
            finished = true;
        } else if (vb.getOid().size() < targetOID.size()) {
            finished = true;
        } else if (targetOID.leftMostCompare(targetOID.size(), vb.getOid()) != 0) {
            finished = true;
        } else if (Null.isExceptionSyntax(vb.getVariable().getSyntax())) {
            finished = true;
        } else if (vb.getOid().compareTo(targetOID) <= 0) {
            finished = true;
        }
        return finished;

    }
}
