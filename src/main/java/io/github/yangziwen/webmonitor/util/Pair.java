package io.github.yangziwen.webmonitor.util;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Pair<K, V> implements Serializable {

    private static final long serialVersionUID = -4412445456920411280L;

    private K key;

    private V value;

}
