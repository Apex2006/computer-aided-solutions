package com.mycompany.mavenproject2.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Checks the stratified sampling math without a database.
 * Run with Test Project in NetBeans (or mvn test).
 *
 * @author Luis Gabriel Santiago
 */
public class ExpertMathTest {

    @Test
    public void slovinVotesNeeded() {
        assertEquals(0, User.requiredVotes(0));
        assertEquals(5, User.requiredVotes(5));
        assertEquals(45, User.requiredVotes(50));
        assertEquals(80, User.requiredVotes(100));
        assertEquals(223, User.requiredVotes(500));
        assertEquals(286, User.requiredVotes(1000));
        assertEquals(371, User.requiredVotes(5000));
        assertEquals(385, User.requiredVotes(10000));
    }

    @Test
    public void votesNeededNeverPass400() {
        assertEquals(400, User.requiredVotes(10_000_000));
    }

    @Test
    public void groupQuotasAreProportional() {
        int n = User.requiredVotes(1000);
        assertEquals(115, User.groupQuota(n, 400, 1000));
        assertEquals(86, User.groupQuota(n, 300, 1000));
        assertEquals(58, User.groupQuota(n, 200, 1000));
        assertEquals(29, User.groupQuota(n, 100, 1000));
        assertEquals(0, User.groupQuota(n, 0, 1000));
    }
}
