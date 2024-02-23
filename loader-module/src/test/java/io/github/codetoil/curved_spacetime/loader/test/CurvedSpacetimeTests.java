package io.github.codetoil.curved_spacetime.loader.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CurvedSpacetimeTests {
    static CurvedSpacetimeLaunchSessionListener curvedSpacetimeLaunchSessionListener;

    @BeforeAll
    static void init()
    {
        curvedSpacetimeLaunchSessionListener = new CurvedSpacetimeLaunchSessionListener();
    }

    @Test
    void test1() {
    }
}
