package com.ninja_squad.console.jmx;

import com.ninjasquad.console.Instance;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        CamelJmxConnector connector = new CamelJmxConnector(new Instance());
        connector.listen();
        Thread.sleep(100000);
    }
}
