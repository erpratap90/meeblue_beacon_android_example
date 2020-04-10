/*
 * Copyright (C) 2020 meeblue
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.meeblue.checkblue.utils;

import org.reactivestreams.Publisher;

import java.util.concurrent.Executor;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.CompletableTransformer;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.MaybeTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * author alvin
 * since 2020-03-17
 */

public class SchedulerTransformer<T> implements ObservableTransformer<T, T>, FlowableTransformer<T, T>, SingleTransformer<T, T>, MaybeTransformer<T, T>, CompletableTransformer {

    /**
     * 线程类型
     */
    private SchedulerType mSchedulerType;
    /**
     * io线程池
     */
    private Executor mIOExecutor;

    public SchedulerTransformer() {
        this(SchedulerType._io_main, RxSchedulerUtils.getIOExecutor());
    }

    /**
     * 构造方法
     *
     * @param schedulerType 线程类型
     */
    public SchedulerTransformer(SchedulerType schedulerType) {
        this(schedulerType, RxSchedulerUtils.getIOExecutor());
    }

    /**
     * 构造方法
     *
     * @param executor 线程池
     */
    public SchedulerTransformer(Executor executor) {
        this(SchedulerType._io_main, executor);
    }

    /**
     * 构造方法
     *
     * @param schedulerType 线程类型
     * @param executor 线程池
     */
    public SchedulerTransformer(SchedulerType schedulerType, Executor executor) {
        mSchedulerType = schedulerType;
        mIOExecutor = executor;
    }

    /**
     * 设置自定义IO线程池
     * @param executor
     * @return
     */
    public SchedulerTransformer<T> setIOExecutor(Executor executor) {
        mIOExecutor = executor;
        return this;
    }

    /**
     * 设置线程类型
     * @param schedulerType
     * @return
     */
    public SchedulerTransformer<T> setSchedulerType(SchedulerType schedulerType) {
        mSchedulerType = schedulerType;
        return this;
    }

    @Override
    public ObservableSource<T> apply(Observable<T> upstream) {
        switch (mSchedulerType) {
            case _main:
                return upstream.observeOn(AndroidSchedulers.mainThread());
            case _io:
                return upstream.observeOn(RxSchedulerUtils.io(mIOExecutor));
            case _io_main:
                return upstream
                        .subscribeOn(RxSchedulerUtils.io(mIOExecutor))
                        .unsubscribeOn(RxSchedulerUtils.io(mIOExecutor))
                        .observeOn(AndroidSchedulers.mainThread());
            case _io_io:
                return upstream
                        .subscribeOn(RxSchedulerUtils.io(mIOExecutor))
                        .unsubscribeOn(RxSchedulerUtils.io(mIOExecutor))
                        .observeOn(RxSchedulerUtils.io(mIOExecutor));
            default:
                break;
        }
        return upstream;
    }

    @Override
    public Publisher<T> apply(Flowable<T> upstream) {
        switch (mSchedulerType) {
            case _main:
                return upstream.observeOn(AndroidSchedulers.mainThread());
            case _io:
                return upstream.observeOn(RxSchedulerUtils.io(mIOExecutor));
            case _io_main:
                return upstream
                        .subscribeOn(RxSchedulerUtils.io(mIOExecutor))
                        .unsubscribeOn(RxSchedulerUtils.io(mIOExecutor))
                        .observeOn(AndroidSchedulers.mainThread());
            case _io_io:
                return upstream
                        .subscribeOn(RxSchedulerUtils.io(mIOExecutor))
                        .unsubscribeOn(RxSchedulerUtils.io(mIOExecutor))
                        .observeOn(RxSchedulerUtils.io(mIOExecutor));
            default:
                break;
        }
        return upstream;
    }

    @Override
    public MaybeSource<T> apply(Maybe<T> upstream) {
        switch (mSchedulerType) {
            case _main:
                return upstream.observeOn(AndroidSchedulers.mainThread());
            case _io:
                return upstream.observeOn(RxSchedulerUtils.io(mIOExecutor));
            case _io_main:
                return upstream
                        .subscribeOn(RxSchedulerUtils.io(mIOExecutor))
                        .unsubscribeOn(RxSchedulerUtils.io(mIOExecutor))
                        .observeOn(AndroidSchedulers.mainThread());
            case _io_io:
                return upstream
                        .subscribeOn(RxSchedulerUtils.io(mIOExecutor))
                        .unsubscribeOn(RxSchedulerUtils.io(mIOExecutor))
                        .observeOn(RxSchedulerUtils.io(mIOExecutor));
            default:
                break;
        }
        return upstream;
    }

    @Override
    public SingleSource<T> apply(Single<T> upstream) {
        switch (mSchedulerType) {
            case _main:
                return upstream.observeOn(AndroidSchedulers.mainThread());
            case _io:
                return upstream.observeOn(RxSchedulerUtils.io(mIOExecutor));
            case _io_main:
                return upstream
                        .subscribeOn(RxSchedulerUtils.io(mIOExecutor))
                        .unsubscribeOn(RxSchedulerUtils.io(mIOExecutor))
                        .observeOn(AndroidSchedulers.mainThread());
            case _io_io:
                return upstream
                        .subscribeOn(RxSchedulerUtils.io(mIOExecutor))
                        .unsubscribeOn(RxSchedulerUtils.io(mIOExecutor))
                        .observeOn(RxSchedulerUtils.io(mIOExecutor));
            default:
                break;
        }
        return upstream;
    }

    @Override
    public CompletableSource apply(Completable upstream) {
        switch (mSchedulerType) {
            case _main:
                return upstream.observeOn(AndroidSchedulers.mainThread());
            case _io:
                return upstream.observeOn(RxSchedulerUtils.io(mIOExecutor));
            case _io_main:
                return upstream
                        .subscribeOn(RxSchedulerUtils.io(mIOExecutor))
                        .unsubscribeOn(RxSchedulerUtils.io(mIOExecutor))
                        .observeOn(AndroidSchedulers.mainThread());
            case _io_io:
                return upstream
                        .subscribeOn(RxSchedulerUtils.io(mIOExecutor))
                        .unsubscribeOn(RxSchedulerUtils.io(mIOExecutor))
                        .observeOn(RxSchedulerUtils.io(mIOExecutor));
            default:
                break;
        }
        return upstream;
    }
}
