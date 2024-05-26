package com.dalcho.adme.config.reactive;


// 통지된 데이터를 전달받아 처리
public interface Subscriber<T> {
    public void onSubscribe(Subscription s);
    public void onNext(T t);
    public void onError(Throwable t);
    public void onComplete();
}
