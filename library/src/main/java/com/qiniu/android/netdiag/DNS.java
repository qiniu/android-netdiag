package com.qiniu.android.netdiag;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by bailong on 16/2/24.
 */
public final class DNS {
    //provide by fastweb
    private static final String F_Start = "<iframe src=\"";
    private static final String F_End = ".php";

    private static final String C_Start = "<tr>";
    private static final String C_End = "</table>";

    private static String getDiagUrl() throws IOException {
        String fetch = Util.httpGetString("http://ns.pbt.cachecn.net/fast_tools/fetch_ldns_diag_client.php");
        if (fetch == null) {
            return null;
        }

        int x = fetch.indexOf(F_Start);
        if (x <= 0) {
            return null;
        }
        int y = fetch.indexOf(F_End, x + F_Start.length());
        if (y <= 0) {
            return null;
        }
        return fetch.substring(x + F_Start.length(), y + F_End.length());
    }

    public static String check() {
        String f = null;
        try {
            f = getDiagUrl();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (f == null) {
            return "get fetch url failed";
        }
        String result = null;
        try {
            result = Util.httpGetString(f);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (result == null) {
            return "check server error";
        }

        int start = result.indexOf(C_Start);
        int end = result.indexOf(C_End, start + C_Start.length());
        result = result.substring(start + C_Start.length(), end);
        result = result.replaceAll("</", "<");

        result = result.replaceAll("<tr>", "");
        result = result.replaceAll("<th>", "");
        result = result.replaceAll("<td>", "");
        result = result.replaceAll("<td>", "");
        result = result.replaceAll("<td width=\"128\" >", "");

        result = result.replaceAll("<table>", "");
        result = result.replaceAll("<p class=\"result\">", "");
        result = result.replaceAll("<pre>", "");
        result = result.replaceAll("<p>", "");

        result = result.replaceAll("\n\n", "\n");
        return result.trim();
    }


    //    15ms
    private static InetAddress[] getByCommand() {
        try {
            Process process = Runtime.getRuntime().exec("getprop");
            InputStream inputStream = process.getInputStream();
            LineNumberReader lnr = new LineNumberReader(
                    new InputStreamReader(inputStream));
            String line = null;
            ArrayList<InetAddress> servers = new ArrayList<InetAddress>(5);
            while ((line = lnr.readLine()) != null) {
                int split = line.indexOf("]: [");
                if (split == -1) {
                    continue;
                }
                String property = line.substring(1, split);
                String value = line.substring(split + 4, line.length() - 1);
                if (property.endsWith(".dns") || property.endsWith(".dns1") ||
                        property.endsWith(".dns2") || property.endsWith(".dns3") ||
                        property.endsWith(".dns4")) {

                    // normalize the address

                    InetAddress ip = InetAddress.getByName(value);

                    if (ip == null) continue;

                    value = ip.getHostAddress();

                    if (value == null) continue;
                    if (value.length() == 0) continue;

                    servers.add(ip);
                }
            }
            if (servers.size() > 0) {
                return servers.toArray(new InetAddress[servers.size()]);
            }
        } catch (IOException e) {
            Logger.getLogger("AndroidDnsServer").log(Level.WARNING, "Exception in findDNSByExec", e);
        }
        return null;
    }

    // 1ms
    private static InetAddress[] getByReflection() {
        try {
            Class<?> SystemProperties =
                    Class.forName("android.os.SystemProperties");
            Method method = SystemProperties.getMethod("get",
                    new Class<?>[]{String.class});

            ArrayList<InetAddress> servers = new ArrayList<InetAddress>(5);

            for (String propKey : new String[]{
                    "net.dns1", "net.dns2", "net.dns3", "net.dns4"}) {

                String value = (String) method.invoke(null, propKey);

                if (value == null) continue;
                if (value.length() == 0) continue;

                InetAddress ip = InetAddress.getByName(value);

                if (ip == null) continue;

                value = ip.getHostAddress();

                if (value == null) continue;
                if (value.length() == 0) continue;
                if (servers.contains(ip)) continue;

                servers.add(ip);
            }

            if (servers.size() > 0) {
                return servers.toArray(new InetAddress[servers.size()]);
            }
        } catch (Exception e) {
            // we might trigger some problems this way
            Logger.getLogger("AndroidDnsServer").log(Level.WARNING, "Exception in findDNSByReflection", e);
        }
        return null;
    }


    public static String[] local() {
        InetAddress[] addresses = getByReflection();
        if (addresses == null) {
            addresses = getByCommand();
            if (addresses == null) {
                return new String[0];
            }
        }
        String[] ret = new String[addresses.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = addresses[i].toString();
            if (ret[i].indexOf('/') == 0) {
                ret[i] = ret[i].substring(1);
            }
        }
        return ret;
    }
}
