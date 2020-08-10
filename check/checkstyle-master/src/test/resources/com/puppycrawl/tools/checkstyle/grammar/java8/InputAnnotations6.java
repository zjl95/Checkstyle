package com.puppycrawl.tools.checkstyle.grammar.java8;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;


public class InputAnnotations6 {

    abstract class UnmodifiableList<T> implements @Readonly List<@Readonly T> {
    }

    @Target(ElementType.TYPE_USE)
    @interface Readonly {

    }
}
