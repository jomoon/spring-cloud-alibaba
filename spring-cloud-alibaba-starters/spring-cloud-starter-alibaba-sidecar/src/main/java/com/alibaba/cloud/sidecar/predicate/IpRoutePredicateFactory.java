/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.sidecar.predicate;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author zain
 * @date 2020/11/16 下午4:22
 */
@Component
public class IpRoutePredicateFactory
        extends AbstractRoutePredicateFactory<Object> {

    public IpRoutePredicateFactory() {
        super(Object.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Object config) {
        return exchange -> {
            InetSocketAddress inetSocketAddress = exchange.getRequest()
                    .getRemoteAddress();

            if (inetSocketAddress != null) {
                InetAddress address = inetSocketAddress.getAddress();
                if (address == null) {
                    return true;
                }
                String remoteAddress = address.getHostAddress();
                List<String> localIPList = getLocalIps();
                HashSet<String> ipSets = new HashSet<>(localIPList);
                ipSets.add("localhost");
                ipSets.add("0:0:0:0:0:0:0:1");
                if (ipSets.contains(remoteAddress)) {
                    // 这里表明其为本地来的，即代理应用打到sidecar 应该返回false 让其走默认的
                    return false;
                }
            }
            return true;
        };
    }

    public static List<String> getLocalIps() {
        List<String> ipList = new ArrayList<String>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            NetworkInterface networkInterface;
            Enumeration<InetAddress> inetAddresses;
            InetAddress inetAddress;
            String ip;
            while (networkInterfaces.hasMoreElements()) {
                networkInterface = networkInterfaces.nextElement();
                inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    inetAddress = inetAddresses.nextElement();
                    // IPV4
                    if (inetAddress != null && inetAddress instanceof Inet4Address) {
                        ip = inetAddress.getHostAddress();
                        ipList.add(ip);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ipList;
    }

}
