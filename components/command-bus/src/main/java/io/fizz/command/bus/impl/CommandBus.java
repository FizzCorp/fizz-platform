package io.fizz.command.bus.impl;

import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.fizz.command.bus.AbstractCommand;
import io.fizz.command.bus.AbstractCommandBus;
import io.fizz.command.bus.CommandHandler;
import io.vertx.core.Vertx;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class CommandBus implements AbstractCommandBus {
    private static class Handler {
        Method method;
        Object instance;

        public Handler(Method aMethod, Object aInstance) {
            this.method = aMethod;
            this.instance = aInstance;
        }
    }

    private static final LoggingService.Log log = LoggingService.getLogger(CommandBus.class);

    private final Map<String, Handler> handlers = new HashMap<>();
    protected final Vertx vertx;

    public CommandBus(Vertx aVertx) {
        Utils.assertRequiredArgument(aVertx, "invalid vertx instance");

        vertx = aVertx;
    }

    public void register(Object aHandler) {
        Utils.assertRequiredArgument(aHandler, "invalid handler");

        final Class<?> clazz = aHandler.getClass();
        if ((clazz.getModifiers()&Modifier.PUBLIC) == 0) {
            throw new IllegalArgumentException("command handler container classes must be public");
        }

        for (final Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(CommandHandler.class)) {
                continue;
            }

            if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
                throw new IllegalArgumentException("command handlers must be public");
            }

            if (method.getParameterCount() != 1) {
                throw new IllegalArgumentException("command handlers must define two parameters");
            }

            Parameter commandParam = method.getParameters()[0];
            if (!AbstractCommand.class.isAssignableFrom(commandParam.getType())) {
                throw new IllegalArgumentException("command handler's first parameter should be a command");
            }

            Class<?> returnType = method.getReturnType();
            if (!CompletableFuture.class.isAssignableFrom(returnType)) {
                throw new IllegalArgumentException("command handler's return type should be a future");
            }

            handlers.put(commandParam.getType().getName(), new Handler(method, aHandler));
        }
    }

    @Override
    public <T> CompletableFuture<T> execute(AbstractCommand aCommand) {
        return handle(aCommand);
    }

    @SuppressWarnings("unchecked")
    protected <T> CompletableFuture<T> handle(AbstractCommand aCommand) {
        if (Objects.isNull(aCommand)) {
            return Utils.failedFuture(new IllegalArgumentException("invalid_command"));
        }

        final String className = aCommand.getClass().getName();
        final Handler handler = handlers.get(className);

        if (Objects.isNull(handler)) {
            log.warn("no handler found for command: " + aCommand.getClass().toString());
            return CompletableFuture.completedFuture(null);
        }

        try {
            return (CompletableFuture<T>) handler.method.invoke(handler.instance, aCommand);
        }
        catch (InvocationTargetException | IllegalAccessException ex) {
            return Utils.failedFuture(ex);
        }
    }
}
