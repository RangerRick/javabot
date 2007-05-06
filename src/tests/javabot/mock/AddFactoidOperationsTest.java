package javabot.mock;

import javabot.BotEvent;
import javabot.Message;
import javabot.dao.ChangesDao;
import javabot.dao.FactoidDao;
import javabot.dao.model.Factoid;
import javabot.operations.AddFactoidOperation2;
import static org.easymock.EasyMock.*;
import org.testng.Assert;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author joed
 */

@Test(groups = {"operations"})
public class AddFactoidOperationsTest extends UnitilsTestNG {

    private static String CHANNEL = "#TEST";
    private static String SENDER = "joed";
    private static String LOGIN = "joed";
    private static String HOSTNAME = "localhost";

    private FactoidDao f_dao;
    private ChangesDao c_dao;
    private AddFactoidOperation2 addOperation;

    @Configuration(beforeTestMethod = true)
    public void setUp() {
        f_dao = createMock(FactoidDao.class);
        c_dao = createMock(ChangesDao.class);
        addOperation = new AddFactoidOperation2(f_dao, c_dao, "test");
    }

    public void testDaoCount() {
        reset(f_dao);
        Integer expectedCount = 1;
        expect(f_dao.getNumberOfFactoids()).andReturn(expectedCount.longValue());
        replay(f_dao);
        Assert.assertEquals(1, f_dao.getNumberOfFactoids().intValue());
    }

    public void testAlreadyHaveFactoid() {
        reset(f_dao);

        List<Factoid> listOfFactoids = new LinkedList<Factoid>();
        Integer id = 100;

        Factoid factoid = new Factoid();
        factoid.setId(id.longValue());
        factoid.setName("magnificent");
        factoid.setValue("MAGNIFICENT");
        factoid.setUpdated(new Date());
        factoid.setUserName(SENDER);

        listOfFactoids.add(factoid);

        BotEvent event = new BotEvent(CHANNEL, SENDER, LOGIN, HOSTNAME, "magnificent is MAGNIFICENT");
        expect(f_dao.hasFactoid("magnificent")).andReturn(true);
        expect(f_dao.getFactoid("magnificent")).andReturn(factoid);
        expect(f_dao.getFactoid("magnificent")).andReturn(factoid);

        replay(f_dao);

        List<Message> results = addOperation.handleMessage(event);
        Assert.assertEquals(results.get(0).getMessage(), "I already have a factoid with that name, joed");
    }


    public void testAddFactoid() {
        reset(f_dao);

        List<Factoid> listOfFactoids = new LinkedList<Factoid>();
        Integer id = 100;

        Factoid factoid = new Factoid();


        listOfFactoids.add(factoid);

        BotEvent event = new BotEvent(CHANNEL, SENDER, LOGIN, HOSTNAME, "magnificent is MAGNIFICENT");

        expect(f_dao.hasFactoid("magnificent")).andReturn(false);
        f_dao.addFactoid("joed", "magnificent", "MAGNIFICENT", c_dao, "test");

        replay(f_dao);

        List<Message> results = addOperation.handleMessage(event);
        Assert.assertEquals(results.get(0).getMessage(), "Okay, joed.");

    }

    public void testAddLongFactoid() {
        reset(f_dao);

        List<Factoid> listOfFactoids = new LinkedList<Factoid>();
        Integer id = 100;

        Factoid factoid = new Factoid();
        listOfFactoids.add(factoid);

        BotEvent event = new BotEvent(CHANNEL, SENDER, LOGIN, HOSTNAME, "magnificent is bla bla bla bla bla");

        expect(f_dao.hasFactoid("magnificent")).andReturn(false);
        f_dao.addFactoid("joed", "magnificent", "bla bla bla bla bla", c_dao, "test");

        replay(f_dao);

        List<Message> results = addOperation.handleMessage(event);
        Assert.assertEquals(results.get(0).getMessage(), "Okay, joed.");

    }

     public void testBlankFactoid(){
            BotEvent event = new BotEvent(CHANNEL, SENDER, LOGIN, HOSTNAME, "is SLASK");
            List<Message> results = addOperation.handleMessage(event);

              Assert.assertEquals(results.get(0).getMessage(), "Invalind");
     }


}