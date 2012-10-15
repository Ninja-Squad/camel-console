package com.ninja_squad.console.jmx;

import com.ninja_squad.console.Instance;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        new CamelJmxConnector(new Instance());
        Thread.sleep(1000000);
    }
}
