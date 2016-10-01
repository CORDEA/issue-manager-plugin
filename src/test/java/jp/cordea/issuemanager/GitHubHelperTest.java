package jp.cordea.issuemanager;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Yoshihiro Tanaka on 2016/10/01.
 */
public class GitHubHelperTest {

    @Test
    public void validUser() throws Exception {
        assertTrue(GitHubHelper.validUser("u"));
        assertTrue(GitHubHelper.validUser("0"));
        assertTrue(GitHubHelper.validUser("user"));
        assertTrue(GitHubHelper.validUser("User"));
        assertTrue(GitHubHelper.validUser("USER"));
        assertTrue(GitHubHelper.validUser("user-user"));

        assertFalse(GitHubHelper.validUser("-user"));
        assertFalse(GitHubHelper.validUser("User-"));
        assertFalse(GitHubHelper.validUser("---user-"));
        assertFalse(GitHubHelper.validUser("user user"));
        assertFalse(GitHubHelper.validUser("user/user"));
        assertFalse(GitHubHelper.validUser("user\nuser"));
        assertFalse(GitHubHelper.validUser("user_user"));
        assertFalse(GitHubHelper.validUser(".user"));
    }

    @Test
    public void parseExpandableTextBoxString() throws Exception {
        assertArrayEquals(new String[]{"user"}, GitHubHelper.parseExpandableTextBoxString("user"));
        assertArrayEquals(new String[]{"user", "user", "user"}, GitHubHelper.parseExpandableTextBoxString("user user user"));
        assertArrayEquals(new String[]{"user", "user", "user"}, GitHubHelper.parseExpandableTextBoxString("user   user   user"));
        assertArrayEquals(new String[]{"user", "user"}, GitHubHelper.parseExpandableTextBoxString("user\nuser"));
        assertArrayEquals(new String[]{"user", "user"}, GitHubHelper.parseExpandableTextBoxString("user\n\n\nuser"));
        assertArrayEquals(new String[]{"user", "user user user"}, GitHubHelper.parseExpandableTextBoxString("user\nuser user user"));

        assertArrayEquals(new String[]{}, GitHubHelper.parseExpandableTextBoxString(""));
        assertArrayEquals(new String[]{}, GitHubHelper.parseExpandableTextBoxString(null));
    }

}