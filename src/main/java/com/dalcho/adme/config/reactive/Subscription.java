package com.dalcho.adme.config.reactive;

// 데이터 개수를 요청하고 구독을 해지
public interface Subscription {
    public void request(long n);
    public void cancel();
}
