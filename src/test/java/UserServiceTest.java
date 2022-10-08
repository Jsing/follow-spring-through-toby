import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class) // 스프링의 테스트 컨텍스트 프레임워크의 JUnit 확장기능 지정
@ContextConfiguration(locations="/applicationContext.xml") // 테스트 컨텍스트가 자동으로 만들어줄 애플리케이션 컨텍스트의 위치 지정
public class UserServiceTest {
    @Autowired
    UserService userService;
    @Autowired
    UserDao userDao;

    List<User> users; // 테스트 픽스처

    @Before
    public void setUp() {
        userDao.deleteAll();

        users = Arrays.asList(
                new User("bumjini", "박범진", "p1", Level.BASIC, UserService.MIN_LOGIN_COUNT_FOR_SILVER - 1, 0),
                new User("joytouch", "강명성", "p2", Level.BASIC, UserService.MIN_LOGIN_COUNT_FOR_SILVER, 0),
                new User("erwins", "신승한", "p3", Level.SILVER, 60, UserService.MIN_RECOMMEND_COUNT_FOR_GOLD-1),
                new User("madnite1", "이상호", "p4", Level.SILVER, 60, UserService.MIN_RECOMMEND_COUNT_FOR_GOLD),
                new User("green", "오민규", "p5", Level.GOLD, 100, Integer.MAX_VALUE)
        );
    }

    @Test
    public void addWithLevel() {
        final User user = users.get(4);

        final Level levelBefore = user.getLevel();
        userService.add(user);
        Assert.assertEquals(levelBefore, user.getLevel());

        final User userRead = userDao.get(user.getId());
        Assert.assertEquals(levelBefore, userRead.getLevel());
    }

    @Test
    public void addWithoutLevel() {
        final User user = users.get(0);
        user.setLevel(null);

        userService.add(user);
        Assert.assertEquals(Level.BASIC, user.getLevel());

        final User userRead = userDao.get(user.getId());
        Assert.assertEquals(Level.BASIC, userRead.getLevel());
    }

    @Test
    public void upgradeLevels() {
        users.forEach(user -> userDao.add(user));

        userService.upgradeLevels();

        checkLevel(users.get(0), false);
        checkLevel(users.get(1), true);
        checkLevel(users.get(2), false);
        checkLevel(users.get(3), true);
        checkLevel(users.get(4), false);
    }

    private void checkLevel(User user, boolean upgraded) {
        final User userUpdated = userDao.get(user.getId());
        if (upgraded) {
            Assert.assertEquals(user.getLevel().nextLevel(), userUpdated.getLevel());
        } else {
            Assert.assertEquals(user.getLevel(), userUpdated.getLevel());
        }
    }
}
