package com.denizyamac.synctemplates.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServiceHolder {
    public static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
}