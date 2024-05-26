package com.dalcho.adme.config.reactive;

// 데이터를 생성하고 통지
public interface Publisher<T> {
    public void subscribe(Subscriber<? super T> s);
}
