package com.dalcho.adme.config.reactive;

// Publisher와 Subscriber의 기능이 모두 존재하는 인터페이스
public interface Processor<T, R> extends Subscriber<T>, Publisher<R> {

}
